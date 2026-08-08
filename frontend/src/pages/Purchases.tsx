import { useEffect, useState } from 'react';
import { purchaseApi, productApi, unwrapList } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatCurrency } from '@/lib/utils';
import { Plus, X, Trash2 } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type { Purchase, Product } from '@/types';

export default function Purchases() {
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [items, setItems] = useState<{ productId: number; quantity: string; unitCost: string }[]>([]);
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const { toast } = useToast();

  useEffect(() => {
    Promise.all([
      purchaseApi.getAll({ page: 0, size: 50 }),
      productApi.getNonCombo(),
    ]).then(([pRes, prRes]) => {
      setPurchases(unwrapList<Purchase>(pRes));
      setProducts(unwrapList<Product>(prRes));
    }).finally(() => setLoading(false));
  }, []);

  const addItem = () => setItems([...items, { productId: 0, quantity: '1', unitCost: '' }]);
  const removeItem = (i: number) => setItems(items.filter((_, idx) => idx !== i));
  const updateItem = (i: number, field: string, val: string) => {
    const next = [...items];
    (next[i] as any)[field] = val;
    setItems(next);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (items.length === 0) { toast({ title: 'Add at least one item', variant: 'destructive' }); return; }
    setSaving(true);
    try {
      await purchaseApi.record({
        items: items.map(i => ({
          productId: Number(i.productId),
          quantity: Number(i.quantity),
          unitCost: Number(i.unitCost),
        })),
        notes,
      });
      toast({ title: 'Purchase recorded' });
      setShowForm(false);
      setItems([]);
      setNotes('');
      const res = await purchaseApi.getAll({ page: 0, size: 50 });
      setPurchases(unwrapList<Purchase>(res));
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally {
      setSaving(false);
    }
  };

  const confirmPurchase = async (id: number) => {
    try {
      await purchaseApi.confirm(id);
      const res = await purchaseApi.getAll({ page: 0, size: 50 });
      setPurchases(unwrapList<Purchase>(res));
      toast({ title: 'Purchase confirmed' });
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    }
  };

  if (loading) return <div className="flex items-center justify-center h-64 text-muted-foreground">Loading...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Purchases</h1>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? <X className="mr-2 h-4 w-4" /> : <Plus className="mr-2 h-4 w-4" />}
          {showForm ? 'Cancel' : 'Record Purchase'}
        </Button>
      </div>

      {showForm && (
        <Card>
          <CardHeader><CardTitle>Record Purchase</CardTitle></CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {items.map((item, i) => (
                <div key={i} className="flex gap-2 items-end">
                  <div className="flex-1">
                    <Label className="text-xs">Product</Label>
                    <select className="w-full rounded-md border bg-background px-3 py-2 text-sm"
                      value={item.productId} onChange={(e) => updateItem(i, 'productId', e.target.value)} required>
                      <option value={0}>Select product</option>
                      {products.map(p => <option key={p.id} value={p.id}>{p.productCode} - {p.name}</option>)}
                    </select>
                  </div>
                  <div className="w-24">
                    <Label className="text-xs">Qty</Label>
                    <Input type="number" min="1" value={item.quantity} onChange={(e) => updateItem(i, 'quantity', e.target.value)} required />
                  </div>
                  <div className="w-32">
                    <Label className="text-xs">Unit Cost (R)</Label>
                    <Input type="number" step="0.01" min="0" value={item.unitCost} onChange={(e) => updateItem(i, 'unitCost', e.target.value)} required />
                  </div>
                  <Button type="button" variant="ghost" size="icon" onClick={() => removeItem(i)}>
                    <Trash2 className="h-4 w-4 text-red-500" />
                  </Button>
                </div>
              ))}
              <Button type="button" variant="outline" onClick={addItem}>
                <Plus className="mr-2 h-4 w-4" />Add Item
              </Button>
              <div>
                <Label className="text-xs">Notes</Label>
                <Input value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Optional notes" />
              </div>
              <Button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save Purchase'}</Button>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="space-y-3">
        {purchases.map((p) => (
          <Card key={p.id}>
            <CardContent className="pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">Purchase #{p.id}</p>
                  <p className="text-sm text-muted-foreground">
                    {new Date(p.purchaseDate).toLocaleDateString()} - {p.items?.length || 0} items - {p.status.replace('_', ' ')}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-bold">{formatCurrency(p.totalAmount)}</p>
                  {p.status === 'PENDING_REVIEW' && (
                    <Button className="mt-2" size="sm" onClick={() => confirmPurchase(p.id)}>
                      Confirm
                    </Button>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
        {purchases.length === 0 && <p className="text-center text-muted-foreground py-8">No purchases recorded yet</p>}
      </div>
    </div>
  );
}
