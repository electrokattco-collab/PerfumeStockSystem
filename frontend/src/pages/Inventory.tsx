import { useEffect, useState, useCallback } from 'react';
import { productApi, unwrapList } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatCurrency } from '@/lib/utils';
import { Search, Plus, X, AlertTriangle } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type { Product } from '@/types';

const CATEGORIES = ['30mL', '50mL', 'EDT', 'Lotion', 'Roll-on', 'Combo', '3-in-1 Face Wash', 'Face Toner', 'Purelite Face Moisturizer'];

export default function Inventory() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const { toast } = useToast();
  const [form, setForm] = useState({ productCode: '', name: '', category: '50mL', buyPrice: '', sellPrice: '', lowStockThreshold: '5', combo: false });
  const [bundleItems, setBundleItems] = useState<{ productId: number; quantity: string }[]>([]);

  useEffect(() => { const t = setTimeout(() => { setDebouncedSearch(search); }, 300); return () => clearTimeout(t); }, [search]);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      const res = debouncedSearch
        ? await productApi.search(debouncedSearch, { page: 0, size: 100 })
        : await productApi.getAll({ page: 0, size: 100 });
      setProducts(unwrapList<Product>(res));
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally { setLoading(false); }
  }, [debouncedSearch, toast]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  const openForm = async () => {
    setShowForm(true);
    try {
      const res = await productApi.getNonCombo();
      setAllProducts(unwrapList<Product>(res));
    } catch {}
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload: any = {
        ...form,
        buyPrice: Number(form.buyPrice),
        sellPrice: Number(form.sellPrice),
        lowStockThreshold: Number(form.lowStockThreshold),
      };
      if (form.combo && bundleItems.length > 0) {
        payload.bundleItems = bundleItems.map(b => ({ componentProductId: b.productId, quantity: Number(b.quantity) }));
      }
      await productApi.create(payload);
      toast({ title: 'Product created' });
      setShowForm(false);
      setForm({ productCode: '', name: '', category: '50mL', buyPrice: '', sellPrice: '', lowStockThreshold: '5', combo: false });
      setBundleItems([]);
      fetchProducts();
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally { setSaving(false); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Inventory</h1>
        <Button onClick={openForm}><Plus className="mr-2 h-4 w-4" />Add Product</Button>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
        <Input className="pl-9" placeholder="Search products..." value={search} onChange={(e) => setSearch(e.target.value)} />
      </div>

      {showForm && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>{form.combo ? 'Add Combo Product' : 'Add Product'}</CardTitle>
            <Button variant="ghost" size="icon" onClick={() => setShowForm(false)}><X className="h-4 w-4" /></Button>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div><Label>Product Code</Label><Input value={form.productCode} onChange={(e) => setForm({...form, productCode: e.target.value})} required placeholder="e.g. Black #005" /></div>
                <div><Label>Name</Label><Input value={form.name} onChange={(e) => setForm({...form, name: e.target.value})} required placeholder="e.g. Perfume Black #005" /></div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <Label>Category</Label>
                  <select className="w-full rounded-md border bg-background px-3 py-2 text-sm" value={form.category} onChange={(e) => setForm({...form, category: e.target.value})}>
                    {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div><Label>Buy Price (R)</Label><Input type="number" step="0.01" min="0" value={form.buyPrice} onChange={(e) => setForm({...form, buyPrice: e.target.value})} required /></div>
                <div><Label>Sell Price (R)</Label><Input type="number" step="0.01" min="0" value={form.sellPrice} onChange={(e) => setForm({...form, sellPrice: e.target.value})} required /></div>
              </div>
              <div className="flex items-center gap-4">
                <label className="flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={form.combo} onChange={(e) => setForm({...form, combo: e.target.checked})} className="rounded" />
                  This is a Combo/Bundle
                </label>
                <div className="w-32"><Label className="text-xs">Low Stock At</Label><Input type="number" min="0" value={form.lowStockThreshold} onChange={(e) => setForm({...form, lowStockThreshold: e.target.value})} /></div>
              </div>

              {form.combo && (
                <div className="border rounded-lg p-4 space-y-3">
                  <Label className="font-medium">Bundle Components</Label>
                  {bundleItems.map((bi, i) => (
                    <div key={i} className="flex gap-2 items-end">
                      <div className="flex-1">
                        <select className="w-full rounded-md border bg-background px-3 py-2 text-sm" value={bi.productId} onChange={(e) => {
                          const next = [...bundleItems]; next[i].productId = Number(e.target.value); setBundleItems(next);
                        }}>
                          <option value={0}>Select component</option>
                          {allProducts.map(p => <option key={p.id} value={p.id}>{p.productCode} - {p.name}</option>)}
                        </select>
                      </div>
                      <div className="w-20"><Input type="number" min="1" value={bi.quantity} onChange={(e) => {
                        const next = [...bundleItems]; next[i].quantity = e.target.value; setBundleItems(next);
                      }} /></div>
                      <Button type="button" variant="ghost" size="icon" onClick={() => setBundleItems(bundleItems.filter((_, idx) => idx !== i))}>
                        <X className="h-4 w-4 text-red-500" />
                      </Button>
                    </div>
                  ))}
                  <Button type="button" variant="outline" size="sm" onClick={() => setBundleItems([...bundleItems, { productId: 0, quantity: '1' }])}>
                    <Plus className="mr-1 h-3 w-3" />Add Component
                  </Button>
                </div>
              )}

              <Button type="submit" disabled={saving}>{saving ? 'Creating...' : 'Create Product'}</Button>
            </form>
          </CardContent>
        </Card>
      )}

      {loading ? <div className="text-center py-8 text-muted-foreground">Loading...</div> : (
        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
          {products.map((p) => (
            <Card key={p.id} className={p.lowStock ? 'border-orange-400' : ''}>
              <CardContent className="pt-4">
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-xs text-muted-foreground font-mono">{p.productCode}</p>
                    <p className="font-medium">{p.name}</p>
                    <p className="text-xs text-muted-foreground">{p.category} {p.combo ? '(Combo)' : ''}</p>
                  </div>
                  {p.lowStock && <AlertTriangle className="h-4 w-4 text-orange-500" />}
                </div>
                <div className="mt-3 flex items-center justify-between text-sm">
                  <div>
                    <span className="text-muted-foreground">Stock: </span>
                    <span className={p.lowStock ? 'text-orange-600 font-bold' : 'font-medium'}>{p.stockQuantity}</span>
                  </div>
                  <div className="text-right">
                    <span className="text-muted-foreground">Buy: </span><span>{formatCurrency(p.buyPrice)}</span>
                    <span className="ml-2 text-muted-foreground">Sell: </span><span className="font-medium">{formatCurrency(p.sellPrice)}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
          {products.length === 0 && <p className="col-span-full text-center text-muted-foreground py-8">No products found</p>}
        </div>
      )}
    </div>
  );
}
