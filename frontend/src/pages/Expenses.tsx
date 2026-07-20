import { useEffect, useState, useCallback } from 'react';
import { expenseApi, financeApi } from '@/services/api';
import { Expense, FinancialSummary, PaginatedResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useAuth } from '@/context/AuthContext';
import { Plus, Trash2, Pencil, X, DollarSign, TrendingUp, TrendingDown, Wallet, CreditCard, Banknote, ArrowUpCircle, Landmark } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';
import { SkeletonStats } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import { Pagination } from '@/components/Pagination';

export default function Expenses() {
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN') || hasRole('MANAGER');
  const [tab, setTab] = useState<'dashboard' | 'transactions' | 'list'>('dashboard');
  const [summary, setSummary] = useState<FinancialSummary | null>(null);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [showTxForm, setShowTxForm] = useState(false);
  const [editingExpense, setEditingExpense] = useState<Expense | null>(null);
  const [saving, setSaving] = useState(false);
  const { toast } = useToast();
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const expForm = { category: '', description: '', amount: '', expenseDate: new Date().toISOString().slice(0, 16) };
  const [form, setForm] = useState(expForm);
  const txForm = { transactionType: 'STIPEND', category: '', description: '', amount: '', transactionDate: new Date().toISOString().slice(0, 16) };
  const [txState, setTxState] = useState(txForm);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [sumRes, txRes] = await Promise.all([
        financeApi.getSummary(),
        financeApi.getAll(),
      ]);
      setSummary(sumRes.data);
      setTransactions(txRes.data);
    } catch { /* silent */ }
    try {
      const expRes = await expenseApi.getAll({ page, size: 20, sort: 'expenseDate', direction: 'desc' });
      const data: PaginatedResponse<Expense> = expRes.data;
      setExpenses(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setLoading(false); }
  }, [page, toast]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  const handleCreateExpense = async (e: React.FormEvent) => {
    e.preventDefault(); setSaving(true);
    try { await expenseApi.create({ category: form.category.trim(), description: form.description.trim(), amount: parseFloat(form.amount), expenseDate: form.expenseDate }); toast({ title: 'Expense added' }); setShowForm(false); setForm(expForm); fetchAll(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setSaving(false); }
  };

  const handleCreateTx = async (e: React.FormEvent) => {
    e.preventDefault(); setSaving(true);
    try { await financeApi.create({ transactionType: txState.transactionType, category: txState.category.trim(), description: txState.description.trim(), amount: parseFloat(txState.amount), transactionDate: txState.transactionDate }); toast({ title: 'Transaction recorded' }); setShowTxForm(false); setTxState(txForm); fetchAll(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setSaving(false); }
  };

  const handleDeleteTx = async (id: number) => {
    if (!confirm('Delete this transaction?')) return;
    try { await financeApi.delete(id); toast({ title: 'Deleted' }); fetchAll(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
  };

  const handleDeleteExpense = async (id: number) => {
    if (!confirm('Delete this expense?')) return;
    try { await expenseApi.delete(id); toast({ title: 'Deleted' }); fetchAll(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
  };

  if (loading) return <div className="space-y-4 md:space-y-6"><SkeletonStats /><div className="h-64 animate-pulse rounded-lg border bg-card" /></div>;

  const txTypes = [
    { value: 'STIPEND', label: 'Monthly Stipend', icon: Banknote, color: 'text-green-600' },
    { value: 'CASH_INJECTED', label: 'Cash Injected', icon: ArrowUpCircle, color: 'text-blue-600' },
    { value: 'MONEY_COLLECTED', label: 'Money Collected', icon: DollarSign, color: 'text-emerald-600' },
    { value: 'OTHER_INCOME', label: 'Other Income', icon: TrendingUp, color: 'text-purple-600' },
    { value: 'EXPENSE', label: 'Business Expense', icon: CreditCard, color: 'text-red-600' },
    { value: 'TRANSPORT', label: 'Transport', icon: CreditCard, color: 'text-orange-600' },
    { value: 'MARKETING', label: 'Marketing', icon: CreditCard, color: 'text-pink-600' },
    { value: 'RENT', label: 'Rent', icon: Landmark, color: 'text-gray-600' },
    { value: 'UTILITIES', label: 'Utilities', icon: CreditCard, color: 'text-yellow-600' },
  ];

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Financial Dashboard</h1><p className="text-sm text-muted-foreground">Cash flow, expenses, and business transactions</p></div>
        {canManage && <div className="flex gap-2">
          <Button onClick={() => { setTxState(txForm); setShowTxForm(true); }} size="sm"><Plus className="mr-1 h-4 w-4" />Record Transaction</Button>
          <Button onClick={() => { setForm(expForm); setEditingExpense(null); setShowForm(true); }} variant="outline" size="sm"><Plus className="mr-1 h-4 w-4" />Add Expense</Button>
        </div>}
      </div>

      {/* Tab Navigation */}
      <div className="flex gap-1 rounded-lg border bg-muted p-1">
        {(['dashboard', 'transactions', 'list'] as const).map(t => (
          <button key={t} onClick={() => setTab(t)} className={`flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${tab === t ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground'}`}>{t === 'dashboard' ? 'Dashboard' : t === 'transactions' ? 'Transactions' : 'Expenses'}</button>
        ))}
      </div>

      {/* Financial Dashboard Tab */}
      {tab === 'dashboard' && summary && (
        <div className="space-y-4">
          {/* Income vs Expenses */}
          <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
            <Card><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">Total Income</CardTitle><TrendingUp className="h-4 w-4 text-green-600" /></CardHeader><CardContent><div className="text-2xl font-bold text-green-600">{formatCurrency(summary.totalIncome)}</div></CardContent></Card>
            <Card><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">Total Expenses</CardTitle><TrendingDown className="h-4 w-4 text-red-600" /></CardHeader><CardContent><div className="text-2xl font-bold text-red-600">{formatCurrency(summary.totalExpenses)}</div></CardContent></Card>
            <Card><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">Net Position</CardTitle><Wallet className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className={`text-2xl font-bold ${summary.netPosition >= 0 ? 'text-green-600' : 'text-red-600'}`}>{formatCurrency(summary.netPosition)}</div></CardContent></Card>
            <Card><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">Available Cash</CardTitle><DollarSign className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className="text-2xl font-bold">{formatCurrency(summary.totalIncome - summary.totalExpenses)}</div></CardContent></Card>
          </div>

          {/* Income Breakdown */}
          <Card><CardHeader><CardTitle className="text-base">Income Sources (This Month)</CardTitle></CardHeader><CardContent><div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Stipend</p><p className="text-lg font-bold">{formatCurrency(summary.monthStipend)}</p></div>
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Cash Injected</p><p className="text-lg font-bold">{formatCurrency(summary.monthCashInjected)}</p></div>
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Money Collected</p><p className="text-lg font-bold">{formatCurrency(summary.monthMoneyCollected)}</p></div>
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Other Income</p><p className="text-lg font-bold">{formatCurrency(summary.monthOtherIncome)}</p></div>
          </div></CardContent></Card>

          {/* Expense Breakdown */}
          <Card><CardHeader><CardTitle className="text-base">Expense Breakdown (This Month)</CardTitle></CardHeader><CardContent><div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Business Expenses</p><p className="text-lg font-bold text-red-600">{formatCurrency(summary.monthExpenses)}</p></div>
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Transport</p><p className="text-lg font-bold text-red-600">{formatCurrency(summary.monthTransport)}</p></div>
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Marketing</p><p className="text-lg font-bold text-red-600">{formatCurrency(summary.monthMarketing)}</p></div>
            <div className="space-y-1"><p className="text-xs text-muted-foreground">Rent & Utilities</p><p className="text-lg font-bold text-red-600">{formatCurrency(summary.monthRent + summary.monthUtilities)}</p></div>
          </div></CardContent></Card>
        </div>
      )}

      {/* Transactions Tab */}
      {tab === 'transactions' && (
        <Card><CardHeader><CardTitle className="text-base">Business Transactions</CardTitle></CardHeader><CardContent>
          {transactions.length === 0 ? <EmptyState title="No transactions" description="Record your first transaction" /> : (
            <div className="space-y-2">
              {transactions.slice(0, 50).map(tx => {
                const typeInfo = txTypes.find(t => t.value === tx.transactionType) || { label: tx.transactionType, color: 'text-muted-foreground' };
                return (
                  <div key={tx.id} className="flex items-center justify-between rounded-lg border p-3">
                    <div className="flex items-center gap-3">
                      <div className={`flex h-10 w-10 items-center justify-center rounded-lg bg-muted`}><DollarSign className={`h-5 w-5 ${typeInfo.color}`} /></div>
                      <div><div className="text-sm font-medium">{tx.category}</div><div className="text-xs text-muted-foreground">{typeInfo.label} · {new Date(tx.transactionDate).toLocaleDateString('en-ZA')}</div></div>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className={`text-sm font-bold ${tx.transactionType.includes('EXPENSE') || tx.transactionType.includes('TRANSPORT') || tx.transactionType.includes('MARKETING') || tx.transactionType.includes('RENT') || tx.transactionType.includes('UTILITIES') ? 'text-red-600' : 'text-green-600'}`}>
                        {tx.transactionType.includes('EXPENSE') || tx.transactionType.includes('TRANSPORT') || tx.transactionType.includes('MARKETING') || tx.transactionType.includes('RENT') || tx.transactionType.includes('UTILITIES') ? '-' : '+'}{formatCurrency(tx.amount)}
                      </span>
                      {canManage && <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => handleDeleteTx(tx.id)}><Trash2 className="h-3 w-3 text-destructive" /></Button>}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent></Card>
      )}

      {/* Expenses List Tab */}
      {tab === 'list' && (
        <Card><CardHeader><CardTitle className="text-base">Expense Records ({totalElements})</CardTitle></CardHeader><CardContent>
          {expenses.length === 0 ? <EmptyState title="No expenses" description="Add your first expense" /> : (
            <div className="space-y-2">
              {expenses.map(e => (
                <div key={e.id} className="flex items-center justify-between rounded-lg border p-3">
                  <div><div className="text-sm font-medium">{e.category}</div><div className="text-xs text-muted-foreground">{e.description || 'No description'} · {new Date(e.expenseDate).toLocaleDateString('en-ZA')}</div></div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-red-600">-{formatCurrency(e.amount)}</span>
                    {canManage && <>
                      <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => { setEditingExpense(e); setForm({ category: e.category, description: e.description || '', amount: e.amount.toString(), expenseDate: e.expenseDate.slice(0, 16) }); setShowForm(true); }}><Pencil className="h-3 w-3" /></Button>
                      <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => handleDeleteExpense(e.id)}><Trash2 className="h-3 w-3 text-destructive" /></Button>
                    </>}
                  </div>
                </div>
              ))}
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
            </div>
          )}
        </CardContent></Card>
      )}

      {/* Transaction Form Modal */}
      {showTxForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg max-h-[90vh] overflow-y-auto">
            <div className="mb-4 flex items-center justify-between"><h3 className="text-lg font-semibold">Record Transaction</h3><Button variant="ghost" size="icon" onClick={() => setShowTxForm(false)}><X className="h-4 w-4" /></Button></div>
            <form onSubmit={handleCreateTx} className="space-y-3">
              <div className="space-y-1"><Label className="text-xs">Transaction Type *</Label>
                <select className="flex h-9 w-full rounded-md border border-input bg-background px-3 text-sm" value={txState.transactionType} onChange={e => setTxState({ ...txState, transactionType: e.target.value })}>
                  {txTypes.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                </select>
              </div>
              <div className="space-y-1"><Label className="text-xs">Category *</Label><Input value={txState.category} onChange={e => setTxState({ ...txState, category: e.target.value })} placeholder="e.g. Staff salary, Stock purchase" required /></div>
              <div className="space-y-1"><Label className="text-xs">Description</Label><Input value={txState.description} onChange={e => setTxState({ ...txState, description: e.target.value })} placeholder="Optional" /></div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><Label className="text-xs">Amount (R) *</Label><Input type="number" step="0.01" min="0" value={txState.amount} onChange={e => setTxState({ ...txState, amount: e.target.value })} required /></div>
                <div className="space-y-1"><Label className="text-xs">Date *</Label><Input type="datetime-local" value={txState.transactionDate} onChange={e => setTxState({ ...txState, transactionDate: e.target.value })} required /></div>
              </div>
              <div className="flex gap-2 pt-2"><Button type="button" variant="outline" className="flex-1" onClick={() => setShowTxForm(false)}>Cancel</Button><Button type="submit" className="flex-1" disabled={saving}>{saving ? 'Saving...' : 'Save'}</Button></div>
            </form>
          </div>
        </div>
      )}

      {/* Expense Form Modal */}
      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg">
            <div className="mb-4 flex items-center justify-between"><h3 className="text-lg font-semibold">{editingExpense ? 'Edit Expense' : 'Add Expense'}</h3><Button variant="ghost" size="icon" onClick={() => { setShowForm(false); setEditingExpense(null); }}><X className="h-4 w-4" /></Button></div>
            <form onSubmit={handleCreateExpense} className="space-y-3">
              <div className="space-y-1"><Label className="text-xs">Category *</Label><Input value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} placeholder="e.g. Transport, Marketing" required /></div>
              <div className="space-y-1"><Label className="text-xs">Description</Label><Input value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Optional" /></div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1"><Label className="text-xs">Amount (R) *</Label><Input type="number" step="0.01" min="0" value={form.amount} onChange={e => setForm({ ...form, amount: e.target.value })} required /></div>
                <div className="space-y-1"><Label className="text-xs">Date *</Label><Input type="datetime-local" value={form.expenseDate} onChange={e => setForm({ ...form, expenseDate: e.target.value })} required /></div>
              </div>
              <div className="flex gap-2 pt-2"><Button type="button" variant="outline" className="flex-1" onClick={() => { setShowForm(false); setEditingExpense(null); }}>Cancel</Button><Button type="submit" className="flex-1" disabled={saving}>{saving ? 'Saving...' : 'Save'}</Button></div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
