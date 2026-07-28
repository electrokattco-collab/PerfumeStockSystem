import { useEffect, useState } from 'react';
import { saleApi, productApi, customerApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatCurrency } from '@/lib/utils';
import { Plus, X, Trash2, ShoppingCart } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type { Sale, Product, Customer, SalePaymentType } from '@/types';

export default function Sales() {
  const [sales, setSales] = useState<Sale[]>([]);
  const [loading, setLoading] = useState(true);
  const [products, setProducts] = useState<Product[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [items, setItems] = useState<{ productId: number; quantity: string; unitPrice: string }[]>([]);
  const [paymentType, setPaymentType] = useState<SalePaymentType>('PAID');
  const [amountPaid, setAmountPaid] = useState('');
  const [customerId, setCustomerId] = useState<number | ''>('');
  const { toast } = useToast();

  useEffect(() => {
    Promise.all([
      saleApi.getAll({ page: 0, size: 50 }),
      productApi.getNonCombo(),
      customerApi.getAll({ page: 0, size: 200 }),
    ]).then(([sRes, pRes, cRes]) => {
      setSales(sRes.data.content);
      setProducts(pRes.data);
      setCustomers(cRes.data.content);
    }).finally(() => setLoading(false));
  }, []);

  const addItem = () => setItems([...items, { productId: 0, quantity: '1', unitPrice: '' }]);
  const removeItem = (i: number) => setItems(items.filter((_, idx) => idx !== i));
  const updateItem = (i: number, field: string, val: string) => {
    const next = [...items];
    (next[i] as any)[field] = val;
    setItems(next);
  };

  const autoFillPrice = (i: number, productId: number) => {
    const p = products.find(pr => pr.id === productId);
    if (p) {
      const next = [...items];
      next[i].unitPrice = String(p.sellPrice);
      setItems(next);
    }
  };

  const total = items.reduce((sum, item) => sum + (Number(item.quantity) * Number(item.unitPrice || 0)), 0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (items.length === 0) return;
    setSaving(true);
    try {
      const payload: any = {
        items: items.map(i => ({ productId: Number(i.productId), quantity: Number(i.quantity), unitPrice: Number(i.unitPrice) })),
        paymentType,
        amountPaid: paymentType === 'PAID' ? total : paymentType === 'PARTIAL' ? Number(amountPaid) : 0,
        customerId: customerId || null,
      };
      await saleApi.record(payload);
      toast({ title: 'Sale recorded!' });
      setShowForm(false);
      setItems([]);
      const res = await saleApi.getAll({ page: 0, size: 50 });
      setSales(res.data.content);
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally { setSaving(false); }
  };

  if (loading) return <div className="flex items-center justify-center h-64 text-muted-foreground">Loading...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Sales</h1>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? <X className="mr-2 h-4 w-4" /> : <ShoppingCart className="mr-2 h-4 w-4" />}
          {showForm ? 'Cancel' : 'Record Sale'}
        </Button>
      </div>

      {showForm && (
        <Card>
          <CardHeader><CardTitle>Record Sale</CardTitle></CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-xs">Customer (optional)</Label>
                  <select className="w-full rounded-md border bg-background px-3 py-2 text-sm" value={customerId} onChange={(e) => setCustomerId(e.target.value ? Number(e.target.value) : '')}>
                    <option value="">Walk-in customer</option>
                    {customers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div>
                  <Label className="text-xs">Payment Type</Label>
                  <select
                    className="w-full rounded-md border bg-background px-3 py-2 text-sm"
                    value={paymentType}
                    onChange={(e) => setPaymentType(e.target.value as SalePaymentType)}
                  >
                    <option value="PAID">Paid</option>
                    <option value="PARTIAL">Partially Paid</option>
                    <option value="CREDIT">Credit</option>
                  </select>
                </div>
              </div>

              {items.map((item, i) => (
                <div key={i} className="flex gap-2 items-end">
                  <div className="flex-1">
                    <Label className="text-xs">Product</Label>
                    <select className="w-full rounded-md border bg-background px-3 py-2 text-sm" value={item.productId}
                      onChange={(e) => { updateItem(i, 'productId', e.target.value); autoFillPrice(i, Number(e.target.value)); }} required>
                      <option value={0}>Select</option>
                      {products.filter(p => p.stockQuantity > 0).map(p => <option key={p.id} value={p.id}>{p.productCode} - {p.name} (Stock: {p.stockQuantity})</option>)}
                    </select>
                  </div>
                  <div className="w-20"><Label className="text-xs">Qty</Label><Input type="number" min="1" value={item.quantity} onChange={(e) => updateItem(i, 'quantity', e.target.value)} required /></div>
                  <div className="w-28"><Label className="text-xs">Price (R)</Label><Input type="number" step="0.01" min="0" value={item.unitPrice} onChange={(e) => updateItem(i, 'unitPrice', e.target.value)} required /></div>
                  <Button type="button" variant="ghost" size="icon" onClick={() => removeItem(i)}><Trash2 className="h-4 w-4 text-red-500" /></Button>
                </div>
              ))}

              <Button type="button" variant="outline" onClick={addItem}><Plus className="mr-1 h-4 w-4" />Add Item</Button>

              {paymentType === 'PARTIAL' && (
                <div className="w-48"><Label className="text-xs">Amount Paid (R)</Label><Input type="number" step="0.01" min="0" value={amountPaid} onChange={(e) => setAmountPaid(e.target.value)} required /></div>
              )}

              <div className="flex items-center justify-between border-t pt-4">
                <span className="text-lg font-bold">Total: {formatCurrency(total)}</span>
                <Button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Complete Sale'}</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="space-y-3">
        {sales.map((s) => (
          <Card key={s.id}>
            <CardContent className="pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium">{s.customerName || 'Walk-in'}</p>
                  <p className="text-sm text-muted-foreground">{s.paymentType} - {new Date(s.saleDate).toLocaleDateString()}</p>
                </div>
                <div className="text-right">
                  <p className="font-bold">{formatCurrency(s.totalAmount)}</p>
                  {s.amountOwing > 0 && <p className="text-xs text-red-500">Owing: {formatCurrency(s.amountOwing)}</p>}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
        {sales.length === 0 && <p className="text-center text-muted-foreground py-8">No sales yet</p>}
      </div>
    </div>
  );
}
