import { useEffect, useState, useCallback } from 'react';
import { procurementApi } from '@/services/api';
import type { Procurement, ProcurementItem, PaginatedResponse } from '@/types';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import {
  Search, Plus, X, Pencil, Trash2, CheckCircle,
  Package, DollarSign, Users, Calendar, ChevronDown, ChevronUp, Loader2, AlertTriangle
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency, formatDate } from '@/lib/utils';
import { SkeletonTable } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import { Pagination } from '@/components/Pagination';

const emptyItem: Omit<ProcurementItem, 'id' | 'lineTotal'> = {
  productName: '', brand: '', category: '', quantityPurchased: 1, buyPrice: 0,
  suggestedSellingPrice: undefined, expectedProfit: undefined, barcode: '',
};

const emptyForm = {
  supplierName: '', supplierContact: '', invoiceNumber: '',
  purchaseDate: new Date().toISOString().slice(0, 10),
  notes: '', vatAmount: '0',
};

export default function Procurement() {
  const [procurements, setProcurements] = useState<Procurement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [items, setItems] = useState<any[]>([{ ...emptyItem }]);
  const [dashboard, setDashboard] = useState<any>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const { toast } = useToast();

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => { setDebouncedSearch(searchTerm); setPage(0); }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      let response;
      if (debouncedSearch || statusFilter) {
        response = await procurementApi.search({
          supplierName: debouncedSearch || undefined,
          status: statusFilter || undefined,
          page, size: 20, sort: 'id', direction: 'desc',
        });
      } else {
        response = await procurementApi.getAll({ page, size: 20, sort: 'id', direction: 'desc' });
      }
      const data: PaginatedResponse<Procurement> = response.data;
      setProcurements(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err: any) {
      setError(err.message || 'Failed to load procurements');
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearch, statusFilter, toast]);

  const fetchDashboard = useCallback(async () => {
    try {
      const res = await procurementApi.dashboard();
      setDashboard(res.data);
    } catch {}
  }, []);

  useEffect(() => { fetchData(); fetchDashboard(); }, [fetchData, fetchDashboard]);

  const resetForm = () => { setForm(emptyForm); setItems([{ ...emptyItem }]); setEditingId(null); setShowForm(false); };

  const handleAddItem = () => setItems([...items, { ...emptyItem }]);
  const handleRemoveItem = (idx: number) => { if (items.length > 1) setItems(items.filter((_, i) => i !== idx)); };
  const handleItemChange = (idx: number, field: string, value: any) => {
    const updated = [...items];
    updated[idx] = { ...updated[idx], [field]: value };
    setItems(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.supplierName.trim()) { setError('Supplier name is required'); return; }
    const validItems = items.filter(i => i.productName.trim() && i.buyPrice > 0 && i.quantityPurchased > 0);
    if (validItems.length === 0) { setError('At least one valid product item is required'); return; }

    setSaving(true);
    setError('');
    try {
      const payload = {
        supplierName: form.supplierName.trim(),
        supplierContact: form.supplierContact.trim() || undefined,
        invoiceNumber: form.invoiceNumber.trim() || undefined,
        purchaseDate: form.purchaseDate + 'T00:00:00',
        vatAmount: parseFloat(form.vatAmount) || 0,
        notes: form.notes.trim() || undefined,
        items: validItems.map(i => ({
          productName: i.productName.trim(),
          brand: i.brand?.trim() || undefined,
          category: i.category?.trim() || undefined,
          quantityPurchased: parseInt(String(i.quantityPurchased)) || 1,
          buyPrice: parseFloat(String(i.buyPrice)) || 0,
          suggestedSellingPrice: i.suggestedSellingPrice ? parseFloat(String(i.suggestedSellingPrice)) : undefined,
          barcode: i.barcode?.trim() || undefined,
        })),
      };

      if (editingId) {
        await procurementApi.update(editingId, payload);
        toast({ title: 'Success', description: 'Procurement updated' });
      } else {
        await procurementApi.create(payload);
        toast({ title: 'Success', description: 'Procurement created' });
      }
      resetForm();
      fetchData();
      fetchDashboard();
    } catch (err: any) {
      setError(err.message || 'Failed to save procurement');
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (proc: Procurement) => {
    setEditingId(proc.id);
    setForm({
      supplierName: proc.supplierName,
      supplierContact: proc.supplierContact || '',
      invoiceNumber: proc.invoiceNumber || '',
      purchaseDate: proc.purchaseDate.slice(0, 10),
      notes: proc.notes || '',
      vatAmount: String(proc.vatAmount || 0),
    });
    setItems(proc.items.map(i => ({
      productName: i.productName,
      brand: i.brand || '',
      category: i.category || '',
      quantityPurchased: i.quantityPurchased,
      buyPrice: i.buyPrice,
      suggestedSellingPrice: i.suggestedSellingPrice,
      expectedProfit: i.expectedProfit,
      barcode: i.barcode || '',
    })));
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this procurement?')) return;
    try {
      await procurementApi.delete(id);
      toast({ title: 'Deleted', description: 'Procurement deleted' });
      fetchData();
      fetchDashboard();
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    }
  };

  const handleConfirm = async (id: number) => {
    if (!confirm('Confirm this procurement? This will update inventory.')) return;
    try {
      await procurementApi.confirm(id);
      toast({ title: 'Confirmed', description: 'Procurement confirmed and inventory updated' });
      fetchData();
      fetchDashboard();
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    }
  };

  const statusBadge = (status: string) => {
    const colors: Record<string, string> = {
      DRAFT: 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300',
      REVIEWING: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300',
      CONFIRMED: 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300',
      CANCELLED: 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300',
    };
    return <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${colors[status] || colors.DRAFT}`}>{status}</span>;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Procurement</h1>
        <Button onClick={() => { resetForm(); setShowForm(true); }}>
          <Plus className="mr-2 h-4 w-4" /> New Procurement
        </Button>
      </div>

      {/* Dashboard Cards */}
      {dashboard && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <p className="text-sm font-medium text-muted-foreground">Today's Purchases</p>
              <Package className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{dashboard.todayPurchases}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <p className="text-sm font-medium text-muted-foreground">This Month</p>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{dashboard.monthPurchases}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <p className="text-sm font-medium text-muted-foreground">Month Cost</p>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatCurrency(Number(dashboard.monthCost) || 0)}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <p className="text-sm font-medium text-muted-foreground">Suppliers</p>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{dashboard.supplierCount}</div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Search and Filter */}
      <div className="flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input placeholder="Search by supplier..." value={searchTerm} onChange={e => setSearchTerm(e.target.value)} className="pl-9" />
        </div>
        <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); }}
          className="rounded-md border bg-background px-3 py-2 text-sm">
          <option value="">All Status</option>
          <option value="DRAFT">Draft</option>
          <option value="REVIEWING">Reviewing</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      {/* Create/Edit Form */}
      {showForm && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <h2 className="text-lg font-semibold">{editingId ? 'Edit Procurement' : 'New Procurement'}</h2>
            <Button variant="ghost" size="icon" onClick={resetForm}><X className="h-4 w-4" /></Button>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && <div className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">{error}</div>}

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3">
                <div>
                  <Label>Supplier Name *</Label>
                  <Input value={form.supplierName} onChange={e => setForm({ ...form, supplierName: e.target.value })} required />
                </div>
                <div>
                  <Label>Supplier Contact</Label>
                  <Input value={form.supplierContact} onChange={e => setForm({ ...form, supplierContact: e.target.value })} />
                </div>
                <div>
                  <Label>Invoice Number</Label>
                  <Input value={form.invoiceNumber} onChange={e => setForm({ ...form, invoiceNumber: e.target.value })} />
                </div>
                <div>
                  <Label>Purchase Date *</Label>
                  <Input type="date" value={form.purchaseDate} onChange={e => setForm({ ...form, purchaseDate: e.target.value })} required />
                </div>
                <div>
                  <Label>VAT Amount</Label>
                  <Input type="number" step="0.01" min="0" value={form.vatAmount} onChange={e => setForm({ ...form, vatAmount: e.target.value })} />
                </div>
                <div>
                  <Label>Notes</Label>
                  <Input value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
                </div>
              </div>

              {/* Items */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Label className="text-base font-semibold">Products</Label>
                  <Button type="button" variant="outline" size="sm" onClick={handleAddItem}><Plus className="mr-1 h-3 w-3" /> Add Item</Button>
                </div>
                {items.map((item, idx) => (
                  <div key={idx} className="grid grid-cols-2 gap-2 rounded-lg border p-3 sm:grid-cols-3 md:grid-cols-6">
                    <div>
                      <Label className="text-xs">Product Name *</Label>
                      <Input value={item.productName} onChange={e => handleItemChange(idx, 'productName', e.target.value)} placeholder="Name" />
                    </div>
                    <div>
                      <Label className="text-xs">Category</Label>
                      <Input value={item.category} onChange={e => handleItemChange(idx, 'category', e.target.value)} placeholder="Category" />
                    </div>
                    <div>
                      <Label className="text-xs">Qty *</Label>
                      <Input type="number" min="1" value={item.quantityPurchased} onChange={e => handleItemChange(idx, 'quantityPurchased', e.target.value)} />
                    </div>
                    <div>
                      <Label className="text-xs">Buy Price *</Label>
                      <Input type="number" step="0.01" min="0" value={item.buyPrice || ''} onChange={e => handleItemChange(idx, 'buyPrice', e.target.value)} />
                    </div>
                    <div>
                      <Label className="text-xs">Sell Price</Label>
                      <Input type="number" step="0.01" min="0" value={item.suggestedSellingPrice || ''} onChange={e => handleItemChange(idx, 'suggestedSellingPrice', e.target.value)} />
                    </div>
                    <div className="flex items-end">
                      {items.length > 1 && (
                        <Button type="button" variant="ghost" size="icon" onClick={() => handleRemoveItem(idx)} className="text-destructive">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={resetForm}>Cancel</Button>
                <Button type="submit" disabled={saving}>
                  {saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  {editingId ? 'Update' : 'Create'} Procurement
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Procurement Table */}
      {loading ? (
        <SkeletonTable rows={5} />
      ) : error && procurements.length === 0 ? (
        <EmptyState icon={<AlertTriangle className="h-8 w-8" />} title="Error" description={error} />
      ) : procurements.length === 0 ? (
        <EmptyState icon={<Package className="h-8 w-8" />} title="No Procurements" description="Create your first procurement to get started" />
      ) : (
        <>
          <div className="rounded-lg border">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b bg-muted/50">
                    <th className="px-4 py-3 text-left font-medium">ID</th>
                    <th className="px-4 py-3 text-left font-medium">Supplier</th>
                    <th className="px-4 py-3 text-left font-medium">Invoice</th>
                    <th className="px-4 py-3 text-left font-medium">Date</th>
                    <th className="px-4 py-3 text-right font-medium">Total</th>
                    <th className="px-4 py-3 text-center font-medium">Status</th>
                    <th className="px-4 py-3 text-center font-medium">Items</th>
                    <th className="px-4 py-3 text-right font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {procurements.map(proc => (
                    <>
                      <tr key={proc.id} className="border-b hover:bg-muted/30 cursor-pointer" onClick={() => setExpandedId(expandedId === proc.id ? null : proc.id)}>
                        <td className="px-4 py-3 font-medium">#{proc.id}</td>
                        <td className="px-4 py-3">{proc.supplierName}</td>
                        <td className="px-4 py-3">{proc.invoiceNumber || '—'}</td>
                        <td className="px-4 py-3">{formatDate(proc.purchaseDate)}</td>
                        <td className="px-4 py-3 text-right font-medium">{formatCurrency(proc.totalAmount)}</td>
                        <td className="px-4 py-3 text-center">{statusBadge(proc.status)}</td>
                        <td className="px-4 py-3 text-center">{proc.items.length}</td>
                        <td className="px-4 py-3 text-right" onClick={e => e.stopPropagation()}>
                          <div className="flex items-center justify-end gap-1">
                            {expandedId === proc.id ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                            {proc.status !== 'CONFIRMED' && (
                              <>
                                <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => handleEdit(proc)}><Pencil className="h-3 w-3" /></Button>
                                <Button variant="ghost" size="icon" className="h-8 w-8 text-green-600" onClick={() => handleConfirm(proc.id)}><CheckCircle className="h-3 w-3" /></Button>
                                <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive" onClick={() => handleDelete(proc.id)}><Trash2 className="h-3 w-3" /></Button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                      {expandedId === proc.id && (
                        <tr key={`${proc.id}-detail`}>
                          <td colSpan={8} className="bg-muted/20 px-4 py-3">
                            <div className="space-y-2">
                              {proc.supplierContact && <p className="text-sm"><span className="font-medium">Contact:</span> {proc.supplierContact}</p>}
                              {proc.notes && <p className="text-sm"><span className="font-medium">Notes:</span> {proc.notes}</p>}
                              <p className="text-sm"><span className="font-medium">Subtotal:</span> {formatCurrency(proc.subtotal)} | <span className="font-medium">VAT:</span> {formatCurrency(proc.vatAmount)}</p>
                              <table className="mt-2 w-full text-xs">
                                <thead>
                                  <tr className="border-b">
                                    <th className="py-1 text-left">Product</th>
                                    <th className="py-1 text-left">Category</th>
                                    <th className="py-1 text-right">Qty</th>
                                    <th className="py-1 text-right">Buy Price</th>
                                    <th className="py-1 text-right">Sell Price</th>
                                    <th className="py-1 text-right">Line Total</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  {proc.items.map(item => (
                                    <tr key={item.id} className="border-b">
                                      <td className="py-1">{item.productName}</td>
                                      <td className="py-1">{item.category || '—'}</td>
                                      <td className="py-1 text-right">{item.quantityPurchased}</td>
                                      <td className="py-1 text-right">{formatCurrency(item.buyPrice)}</td>
                                      <td className="py-1 text-right">{item.suggestedSellingPrice ? formatCurrency(item.suggestedSellingPrice) : '—'}</td>
                                      <td className="py-1 text-right font-medium">{formatCurrency(item.lineTotal)}</td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          </td>
                        </tr>
                      )}
                    </>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
