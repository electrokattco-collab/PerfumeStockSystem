import { useEffect, useState, useCallback } from 'react';
import { saleApi } from '@/services/api';
import { Sale, PaginatedResponse } from '@/types';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Search, X, Pencil, ShoppingCart } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';
import { SkeletonTable } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import { Pagination } from '@/components/Pagination';

export default function SalesHistory() {
  const [sales, setSales] = useState<Sale[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [editingSale, setEditingSale] = useState<Sale | null>(null);
  const [expandedSale, setExpandedSale] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const { toast } = useToast();

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [editForm, setEditForm] = useState({ productName: '', customerName: '', quantity: 1, unitPrice: 0, paid: false });

  useEffect(() => {
    const timer = setTimeout(() => { setDebouncedSearch(search); setPage(0); }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  const fetchSales = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20, sort: 'createdAt', direction: 'desc' as const };
      const response = debouncedSearch
        ? await saleApi.getAll({ ...params, sort: 'createdAt' })
        : await saleApi.getAll(params);
      const data: PaginatedResponse<Sale> = response.data;
      setSales(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearch, toast]);

  useEffect(() => { fetchSales(); }, [fetchSales]);

  const handleMarkPaid = async (id: number) => {
    try { await saleApi.markPaid(id); toast({ title: 'Sale marked as paid' }); fetchSales(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
  };

  const handleEditClick = (sale: Sale) => {
    setEditingSale(sale);
    setEditForm({ productName: sale.productName, customerName: sale.customerName || '', quantity: sale.quantity, unitPrice: sale.unitPrice, paid: sale.paid });
  };

  const handleSaveEdit = async () => {
    if (!editingSale) return;
    setSaving(true);
    try {
      await saleApi.update(editingSale.id, { productName: editForm.productName, customerName: editForm.customerName || undefined, quantity: editForm.quantity, unitPrice: editForm.unitPrice, paid: editForm.paid });
      toast({ title: 'Sale updated' }); setEditingSale(null); fetchSales();
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setSaving(false); }
  };

  if (loading) {
    return (
      <div className="space-y-4 md:space-y-6">
        <div><div className="h-8 w-36 animate-pulse rounded bg-muted" /><div className="mt-1 h-4 w-48 animate-pulse rounded bg-muted" /></div>
        <SkeletonTable rows={5} />
      </div>
    );
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight md:text-3xl">Sales History</h1>
        <p className="text-sm text-muted-foreground md:text-base">{totalElements} sales</p>
      </div>
      <Card>
        <CardHeader className="pb-3">
          <div className="relative max-w-sm">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input placeholder="Search sales..." value={search} onChange={e => setSearch(e.target.value)} className="pl-9" />
          </div>
        </CardHeader>
        <CardContent>
          {sales.length === 0 ? (
            <EmptyState icon={<ShoppingCart className="h-8 w-8 text-muted-foreground" />} title={search ? 'No sales found' : 'No sales yet'} description={search ? 'Try a different search term' : 'Sales will appear here after you record them'} />
          ) : (
            <div className="space-y-3">
              {sales.map((sale) => {
                const isMulti = sale.items && sale.items.length > 0;
                const total = sale.totalAmount || (sale.quantity * sale.unitPrice);
                return (
                  <div key={sale.id} className="rounded-lg border p-3">
                    <div className="flex items-start justify-between">
                      <div>
                        <div className="text-sm font-medium">{sale.productName}</div>
                        <div className="text-xs text-muted-foreground">{sale.saleId} · {new Date(sale.createdAt).toLocaleDateString('en-ZA')}{sale.customerName && ` · ${sale.customerName}`}</div>
                      </div>
                      <div className="flex items-center gap-1">
                        <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-medium ${sale.paid ? 'bg-green-100 text-green-800' : 'bg-amber-100 text-amber-800'}`}>{sale.paid ? 'Paid' : 'Owing'}</span>
                        <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => handleEditClick(sale)}><Pencil className="h-3 w-3" /></Button>
                      </div>
                    </div>
                    {isMulti && (
                      <div className="mt-1">
                        <button className="text-xs text-muted-foreground hover:text-foreground" onClick={() => setExpandedSale(expandedSale === sale.id ? null : sale.id)}>
                          {expandedSale === sale.id ? 'Hide items' : `Show ${sale.items.length} product type(s)`}
                        </button>
                        {expandedSale === sale.id && (
                          <div className="mt-1 space-y-0.5 rounded bg-muted p-2 text-xs">
                            {sale.items.map((item) => (<div key={item.id} className="flex justify-between"><span>{item.productName} x{item.quantity}</span><span>{formatCurrency(item.lineTotal || item.unitPrice * item.quantity)}</span></div>))}
                          </div>
                        )}
                      </div>
                    )}
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-muted-foreground">{isMulti ? `Total: ${formatCurrency(total)}` : `Qty: ${sale.quantity} x R${sale.unitPrice.toFixed(2)}`}</span>
                      <span className="font-bold">{formatCurrency(total)}</span>
                    </div>
                    {!sale.paid && (
                      <div className="mt-2 flex items-center justify-between">
                        <span className="text-xs font-medium text-amber-600">Owing: R{(sale.amountOwing || 0).toFixed(2)}</span>
                        <Button variant="outline" size="sm" className="h-6 text-xs" onClick={() => handleMarkPaid(sale.id)}>Mark Paid</Button>
                      </div>
                    )}
                  </div>
                );
              })}
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
            </div>
          )}
        </CardContent>
      </Card>
      {editingSale && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold">Edit Sale {editingSale.saleId}</h3>
              <Button variant="ghost" size="icon" onClick={() => setEditingSale(null)}><X className="h-4 w-4" /></Button>
            </div>
            <div className="space-y-3">
              <div className="space-y-1"><Label className="text-xs">Product Name</Label><Input value={editForm.productName} onChange={(e) => setEditForm({ ...editForm, productName: e.target.value })} /></div>
              <div className="space-y-1"><Label className="text-xs">Customer Name</Label><Input value={editForm.customerName} onChange={(e) => setEditForm({ ...editForm, customerName: e.target.value })} /></div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><Label className="text-xs">Quantity</Label><Input type="number" min={1} value={editForm.quantity} onChange={(e) => setEditForm({ ...editForm, quantity: Number(e.target.value) })} /></div>
                <div className="space-y-1"><Label className="text-xs">Unit Price (R)</Label><Input type="number" step="0.01" min="0" value={editForm.unitPrice} onChange={(e) => setEditForm({ ...editForm, unitPrice: parseFloat(e.target.value) || 0 })} /></div>
              </div>
              <div className="flex items-center gap-3 rounded-lg border p-3"><input type="checkbox" id="editPaid" checked={editForm.paid} onChange={(e) => setEditForm({ ...editForm, paid: e.target.checked })} className="h-4 w-4 rounded" /><Label htmlFor="editPaid" className="text-sm cursor-pointer">Customer has paid</Label></div>
              <div className="flex justify-between rounded-lg bg-muted p-3 text-sm"><span>Total:</span><span className="font-bold">R{(editForm.unitPrice * editForm.quantity).toFixed(2)}</span></div>
              <div className="flex gap-2">
                <Button variant="outline" className="flex-1" onClick={() => setEditingSale(null)}>Cancel</Button>
                <Button className="flex-1" onClick={handleSaveEdit} disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
