import { useEffect, useState, useCallback } from 'react';
import { customerApi, paymentApi } from '@/services/api';
import { Customer, PaymentHistory, PaginatedResponse } from '@/types';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/context/AuthContext';
import { Search, Users, Plus, X, DollarSign, History, CreditCard } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';
import { SkeletonTable } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import { Pagination } from '@/components/Pagination';

export default function Customers() {
  const { hasRole } = useAuth();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const { toast } = useToast();
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [saving, setSaving] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [paymentHistory, setPaymentHistory] = useState<PaymentHistory[]>([]);
  const [showPayment, setShowPayment] = useState(false);
  const [payForm, setPayForm] = useState({ amount: '', paymentMethod: 'CASH', notes: '' });
  const formEmpty = { name: '', phone: '', address: '', notes: '' };
  const [form, setForm] = useState(formEmpty);

  useEffect(() => { const t = setTimeout(() => { setDebouncedSearch(search); setPage(0); }, 300); return () => clearTimeout(t); }, [search]);

  const fetchCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20, sort: 'name', direction: 'asc' as const };
      const res = debouncedSearch ? await customerApi.search(debouncedSearch, params) : await customerApi.getAll(params);
      const data: PaginatedResponse<Customer> = res.data;
      setCustomers(data.content); setTotalPages(data.totalPages); setTotalElements(data.totalElements);
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setLoading(false); }
  }, [page, debouncedSearch, toast]);

  useEffect(() => { fetchCustomers(); }, [fetchCustomers]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault(); setSaving(true);
    try { await customerApi.create({ name: form.name.trim(), phone: form.phone.trim() }); toast({ title: 'Customer created' }); setShowForm(false); setForm(formEmpty); fetchCustomers(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setSaving(false); }
  };

  const handleRecordPayment = async (e: React.FormEvent) => {
    e.preventDefault(); if (!selectedCustomer) return; setSaving(true);
    try {
      await paymentApi.record({ customerId: selectedCustomer.id, amount: parseFloat(payForm.amount), paymentMethod: payForm.paymentMethod, notes: payForm.notes });
      toast({ title: 'Payment recorded' }); setShowPayment(false); setPayForm({ amount: '', paymentMethod: 'CASH', notes: '' }); fetchCustomers();
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setSaving(false); }
  };

  const viewHistory = async (customer: Customer) => {
    setSelectedCustomer(customer);
    try { const res = await paymentApi.getByCustomer(customer.id); setPaymentHistory(res.data); } catch { setPaymentHistory([]); }
  };

  if (loading) return <div className="space-y-4 md:space-y-6"><SkeletonTable rows={5} /></div>;

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Customers</h1><p className="text-sm text-muted-foreground">{totalElements} customers</p></div>
        {hasRole('SALES_REP') || hasRole('MANAGER') || hasRole('ADMIN') ? <Button onClick={() => { setEditingCustomer(null); setForm(formEmpty); setShowForm(true); }} size="sm"><Plus className="mr-1 h-4 w-4" />Add Customer</Button> : null}
      </div>

      <Card>
        <CardHeader className="pb-3"><div className="relative max-w-sm"><Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" /><Input placeholder="Search customers..." value={search} onChange={e => setSearch(e.target.value)} className="pl-9" /></div></CardHeader>
        <CardContent>
          {customers.length === 0 ? <EmptyState icon={<Users className="h-8 w-8 text-muted-foreground" />} title="No customers" description="Customers appear after first sale" /> : (
            <>
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full text-sm">
                  <thead><tr className="border-b"><th className="py-3 px-2 text-left font-medium">Name</th><th className="py-3 px-2 text-left font-medium">Phone</th><th className="py-3 px-2 text-right font-medium">Outstanding</th><th className="py-3 px-2 text-center font-medium">Actions</th></tr></thead>
                  <tbody>{customers.map(c => (
                    <tr key={c.id} className="border-b">
                      <td className="py-3 px-2 font-medium">{c.name}</td>
                      <td className="py-3 px-2 text-muted-foreground">{c.phone || '-'}</td>
                      <td className={`py-3 px-2 text-right font-medium ${c.outstandingBalance > 0 ? 'text-amber-600' : 'text-green-600'}`}>{c.outstandingBalance > 0 ? formatCurrency(c.outstandingBalance) : 'Paid'}</td>
                      <td className="py-3 px-2 text-center">
                        <div className="flex justify-center gap-1">
                          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => viewHistory(c)} title="Payment history"><History className="h-3.5 w-3.5" /></Button>
                          {c.outstandingBalance > 0 && <Button variant="outline" size="sm" className="h-7 text-xs" onClick={() => { setSelectedCustomer(c); setPayForm({ amount: c.outstandingBalance.toString(), paymentMethod: 'CASH', notes: '' }); setShowPayment(true); }}><CreditCard className="mr-1 h-3 w-3" />Pay</Button>}
                        </div>
                      </td>
                    </tr>
                  ))}</tbody>
                </table>
              </div>
              <div className="space-y-3 md:hidden">{customers.map(c => (
                <div key={c.id} className="rounded-lg border p-3">
                  <div className="flex items-start justify-between">
                    <div><div className="text-sm font-medium">{c.name}</div><div className="text-xs text-muted-foreground">{c.phone || 'No phone'}</div></div>
                    <div className={`text-xs font-medium ${c.outstandingBalance > 0 ? 'text-amber-600' : 'text-green-600'}`}>{c.outstandingBalance > 0 ? formatCurrency(c.outstandingBalance) : 'Paid'}</div>
                  </div>
                  <div className="mt-2 flex gap-2">
                    <Button variant="ghost" size="sm" className="h-6 text-xs" onClick={() => viewHistory(c)}>History</Button>
                    {c.outstandingBalance > 0 && <Button variant="outline" size="sm" className="h-6 text-xs" onClick={() => { setSelectedCustomer(c); setPayForm({ amount: c.outstandingBalance.toString(), paymentMethod: 'CASH', notes: '' }); setShowPayment(true); }}>Pay</Button>}
                  </div>
                </div>
              ))}</div>
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
            </>
          )}
        </CardContent>
      </Card>

      {/* Customer Form Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg">
            <div className="mb-4 flex items-center justify-between"><h3 className="text-lg font-semibold">{editingCustomer ? 'Edit Customer' : 'Add Customer'}</h3><Button variant="ghost" size="icon" onClick={() => setShowForm(false)}><X className="h-4 w-4" /></Button></div>
            <form onSubmit={handleCreate} className="space-y-3">
              <div className="space-y-1"><Label className="text-xs">Name *</Label><Input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required /></div>
              <div className="space-y-1"><Label className="text-xs">Phone</Label><Input value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="Optional" /></div>
              <div className="flex gap-2 pt-2"><Button type="button" variant="outline" className="flex-1" onClick={() => setShowForm(false)}>Cancel</Button><Button type="submit" className="flex-1" disabled={saving}>{saving ? 'Saving...' : 'Save'}</Button></div>
            </form>
          </div>
        </div>
      )}

      {/* Payment Modal */}
      {showPayment && selectedCustomer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg">
            <div className="mb-4 flex items-center justify-between"><h3 className="text-lg font-semibold">Record Payment — {selectedCustomer.name}</h3><Button variant="ghost" size="icon" onClick={() => setShowPayment(false)}><X className="h-4 w-4" /></Button></div>
            <form onSubmit={handleRecordPayment} className="space-y-3">
              <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Outstanding Balance</p><p className="text-xl font-bold text-amber-600">{formatCurrency(selectedCustomer.outstandingBalance)}</p></div>
              <div className="space-y-1"><Label className="text-xs">Amount (R) *</Label><Input type="number" step="0.01" min="0" max={selectedCustomer.outstandingBalance} value={payForm.amount} onChange={e => setPayForm({ ...payForm, amount: e.target.value })} required /></div>
              <div className="space-y-1"><Label className="text-xs">Payment Method</Label>
                <select className="flex h-9 w-full rounded-md border border-input bg-background px-3 text-sm" value={payForm.paymentMethod} onChange={e => setPayForm({ ...payForm, paymentMethod: e.target.value })}>
                  <option value="CASH">Cash</option><option value="CARD">Card</option><option value="EFT">EFT</option><option value="OTHER">Other</option>
                </select>
              </div>
              <div className="space-y-1"><Label className="text-xs">Notes</Label><Input value={payForm.notes} onChange={e => setPayForm({ ...payForm, notes: e.target.value })} placeholder="Optional" /></div>
              <div className="flex gap-2 pt-2"><Button type="button" variant="outline" className="flex-1" onClick={() => setShowPayment(false)}>Cancel</Button><Button type="submit" className="flex-1" disabled={saving}><DollarSign className="mr-1 h-4 w-4" />{saving ? 'Saving...' : 'Record Payment'}</Button></div>
            </form>
          </div>
        </div>
      )}

      {/* Payment History Modal */}
      {selectedCustomer && !showPayment && paymentHistory.length >= 0 && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg max-h-[80vh] overflow-y-auto">
            <div className="mb-4 flex items-center justify-between"><h3 className="text-lg font-semibold">Payment History — {selectedCustomer.name}</h3><Button variant="ghost" size="icon" onClick={() => setSelectedCustomer(null)}><X className="h-4 w-4" /></Button></div>
            {paymentHistory.length === 0 ? <p className="text-sm text-muted-foreground">No payments recorded</p> : (
              <div className="space-y-2">
                {paymentHistory.map(p => (
                  <div key={p.id} className="flex items-center justify-between rounded border p-2">
                    <div><div className="text-sm font-medium">{formatCurrency(p.amount)}</div><div className="text-xs text-muted-foreground">{p.paymentMethod} · {p.paymentType}</div></div>
                    <div className="text-xs text-muted-foreground">{new Date(p.createdAt).toLocaleDateString('en-ZA')}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
