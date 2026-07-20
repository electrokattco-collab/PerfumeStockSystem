import { useEffect, useState } from 'react';
import { productApi, saleApi } from '@/services/api';
import { Product } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { AlertCircle, Plus, Trash2 } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface SaleLine { productName: string; productId: string; quantity: string; unitPrice: string; }
const emptyLine = (): SaleLine => ({ productName: '', productId: '', quantity: '1', unitPrice: '' });

export default function RecordSale() {
  const [products, setProducts] = useState<Product[]>([]);
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [lines, setLines] = useState<SaleLine[]>([emptyLine()]);
  const [paid, setPaid] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { toast } = useToast();

  useEffect(() => { productApi.getAll({ size: 200 }).then(r => setProducts(r.data.content)).catch(() => {}); }, []);

  const updateLine = (i: number, field: keyof SaleLine, value: string) => {
    setLines(prev => prev.map((l, idx) => {
      if (idx !== i) return l;
      const u = { ...l, [field]: value };
      if (field === 'productId' && value) {
        const p = products.find(p => p.id === Number(value));
        if (p) { u.productName = p.name; u.unitPrice = p.sellPrice.toString(); }
      }
      return u;
    }));
    setError('');
  };

  const grandTotal = lines.reduce((s, l) => s + (parseInt(l.quantity) || 0) * (parseFloat(l.unitPrice) || 0), 0);
  const canSubmit = lines.some(l => l.productName.trim() && parseFloat(l.unitPrice) > 0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const valid = lines.filter(l => l.productName.trim() && parseFloat(l.unitPrice) > 0);
    if (!valid.length) { setError('Add at least one product'); return; }
    setLoading(true);
    try {
      await saleApi.create({
        productName: valid.length === 1 ? valid[0].productName : `Multiple items (${valid.length} products)`,
        productId: valid[0].productId || undefined,
        quantity: valid.reduce((s, l) => s + (parseInt(l.quantity) || 1), 0),
        unitPrice: valid.length === 1 ? parseFloat(valid[0].unitPrice) : grandTotal / valid.reduce((s, l) => s + (parseInt(l.quantity) || 1), 0),
        customerName: customerName.trim() || undefined,
        customerPhone: customerPhone.trim() || undefined,
        paid,
        items: valid.map(l => ({ productName: l.productName.trim(), productId: l.productId || undefined, quantity: parseInt(l.quantity) || 1, unitPrice: parseFloat(l.unitPrice) })),
      });
      toast({ title: 'Sale recorded', description: `${valid.length} product(s) for R${grandTotal.toFixed(2)}` });
      setLines([emptyLine()]); setCustomerName(''); setCustomerPhone(''); setPaid(false);
    } catch (err: any) { setError(err.message); toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setLoading(false); }
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Record Sale</h1><p className="text-sm text-muted-foreground md:text-base">Add one or more products to a single sale</p></div>
      <Card className="w-full md:max-w-lg">
        <CardHeader className="pb-3 md:pb-4"><CardTitle className="text-lg md:text-xl">New Sale</CardTitle><CardDescription className="text-xs md:text-sm">Add products, set quantities and prices</CardDescription></CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && <div className="flex items-center gap-2 rounded-lg border border-destructive/50 bg-destructive/10 p-3 text-sm text-destructive"><AlertCircle className="h-4 w-4 shrink-0" />{error}</div>}
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-2"><Label className="text-sm">Customer Name</Label><Input value={customerName} onChange={e => setCustomerName(e.target.value)} placeholder="Name (optional)" /></div>
              <div className="space-y-2"><Label className="text-sm">Phone</Label><Input value={customerPhone} onChange={e => setCustomerPhone(e.target.value)} placeholder="Phone (optional)" /></div>
            </div>
            <div className="space-y-3">
              <Label className="text-sm font-medium">Products</Label>
              {lines.map((line, i) => (
                <div key={i} className="rounded-lg border p-3 space-y-2">
                  <div className="flex items-center justify-between"><span className="text-xs font-medium text-muted-foreground">Item {i+1}</span>{lines.length > 1 && <Button type="button" variant="ghost" size="icon" className="h-6 w-6" onClick={() => setLines(p => p.filter((_, idx) => idx !== i))}><Trash2 className="h-3 w-3 text-destructive" /></Button>}</div>
                  {products.length > 0 && <select className="flex h-8 w-full rounded-md border border-input bg-background px-2 text-xs" value={line.productId} onChange={e => updateLine(i, 'productId', e.target.value)}><option value="">— inventory (optional) —</option>{products.map(p => <option key={p.id} value={p.id}>{p.name} [{p.stockQuantity}]</option>)}</select>}
                  <Input value={line.productName} onChange={e => updateLine(i, 'productName', e.target.value)} placeholder="Product name" className="h-8 text-sm" />
                  <div className="grid grid-cols-2 gap-2">
                    <div><Label className="text-[10px] text-muted-foreground">Qty</Label><Input type="number" min={1} value={line.quantity} onChange={e => updateLine(i, 'quantity', e.target.value)} className="h-8 text-sm" /></div>
                    <div><Label className="text-[10px] text-muted-foreground">Price (R)</Label><Input type="number" step="0.01" min="0" value={line.unitPrice} onChange={e => updateLine(i, 'unitPrice', e.target.value)} placeholder="0.00" className="h-8 text-sm" /></div>
                  </div>
                </div>
              ))}
              <Button type="button" variant="outline" size="sm" onClick={() => setLines(p => [...p, emptyLine()])} className="w-full"><Plus className="mr-1 h-3.5 w-3.5" />Add Product</Button>
            </div>
            <div className="flex items-center gap-3 rounded-lg border p-3"><input type="checkbox" id="paid" checked={paid} onChange={e => setPaid(e.target.checked)} className="h-4 w-4 rounded" /><Label htmlFor="paid" className="text-sm cursor-pointer">Customer has paid</Label></div>
            {grandTotal > 0 && <div className="rounded-lg bg-muted p-3 space-y-1">
              {lines.filter(l => l.productName.trim() && parseFloat(l.unitPrice) > 0).map((l, i) => <div key={i} className="flex justify-between text-xs text-muted-foreground"><span>{l.productName} {parseInt(l.quantity)>1?`x${l.quantity}`:''}</span><span>R{((parseInt(l.quantity)||1)*(parseFloat(l.unitPrice)||0)).toFixed(2)}</span></div>)}
              <div className="flex justify-between text-lg font-bold border-t pt-1 mt-1"><span>Total:</span><span>R{grandTotal.toFixed(2)}</span></div>
              {!paid && <div className="flex justify-between text-sm text-amber-600 font-medium"><span>Owing:</span><span>R{grandTotal.toFixed(2)}</span></div>}
            </div>}
            <Button type="submit" className="w-full" disabled={loading || !canSubmit}>{loading ? 'Recording...' : 'Record Sale'}</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
