import { useEffect, useState } from 'react';
import { reportApiV2 } from '@/services/api';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCurrency } from '@/lib/utils';
import { SalesTrendChart } from '@/components/charts/SalesTrendChart';
import { ProfitChart } from '@/components/charts/ProfitChart';
import { Package, BarChart3, CreditCard, Calendar } from 'lucide-react';
import type { InventoryReport, DebtReport } from '@/types';

export default function Reports() {
  const { hasRole } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [daily, setDaily] = useState<any>(null);
  const [weekly, setWeekly] = useState<any>(null);
  const [monthly, setMonthly] = useState<any>(null);
  const [inventoryReport, setInventoryReport] = useState<InventoryReport | null>(null);
  const [debtReport, setDebtReport] = useState<DebtReport | null>(null);
  const [trend, setTrend] = useState<any[]>([]);
  const [expenses, setExpenses] = useState<any[]>([]);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [d, w, m, t, e] = await Promise.all([
          reportApiV2.daily(), reportApiV2.weekly(), reportApiV2.monthly(),
          reportApiV2.salesTrend(), reportApiV2.expenseBreakdown(),
        ]);
        setDaily(d.data); setWeekly(w.data); setMonthly(m.data);
        setTrend(t.data); setExpenses(e.data);
        if (hasRole('ADMIN')) {
          const [inv, debt] = await Promise.all([reportApiV2.inventoryReport(), reportApiV2.debtReport()]);
          setInventoryReport(inv.data); setDebtReport(debt.data);
        }
      } catch { /* silent */ }
      finally { setLoading(false); }
    };
    fetch();
  }, [hasRole]);

  if (loading) return <div className="py-8 text-center">Loading reports...</div>;

  const tabs = [
    { id: 'overview', label: 'Overview', icon: BarChart3 },
    { id: 'periods', label: 'Periods', icon: Calendar },
    { id: 'inventory', label: 'Inventory', icon: Package },
    { id: 'debt', label: 'Debt', icon: CreditCard },
  ];

  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Reports & Analytics</h1><p className="text-sm text-muted-foreground">Business intelligence and insights</p></div>
      <div className="flex gap-1 rounded-lg border bg-muted p-1 overflow-x-auto">
        {tabs.filter(t => t.id !== 'inventory' && t.id !== 'debt' || hasRole('ADMIN')).map(t => (
          <button key={t.id} onClick={() => setActiveTab(t.id)} className={`flex items-center gap-1.5 whitespace-nowrap rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${activeTab === t.id ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground'}`}><t.icon className="h-4 w-4" />{t.label}</button>
        ))}
      </div>

      {activeTab === 'overview' && (
        <div className="space-y-4">
          <div className="grid gap-4 lg:grid-cols-2">
            <SalesTrendChart data={trend.map((d: any) => ({ label: d.day, revenue: d.revenue, sales: d.count }))} />
            <ProfitChart revenue={monthly?.revenue || 0} cost={monthly?.expenses || 0} />
          </div>
          {expenses.length > 0 && (
            <Card><CardHeader><CardTitle className="text-base">Expense Breakdown (This Month)</CardTitle></CardHeader><CardContent>
              <div className="space-y-2">{expenses.map((e: any, i: number) => (
                <div key={i} className="flex items-center justify-between"><span className="text-sm">{e.category}</span><span className="text-sm font-medium text-red-600">{formatCurrency(e.amount)}</span></div>
              ))}</div>
            </CardContent></Card>
          )}
        </div>
      )}

      {activeTab === 'periods' && (
        <div className="space-y-4">
          {[daily, weekly, monthly].filter(Boolean).map((report: any, i: number) => (
            <Card key={i}><CardHeader><CardTitle className="text-base">{report.period}</CardTitle></CardHeader><CardContent>
              <div className="grid grid-cols-3 gap-4">
                <div><p className="text-xs text-muted-foreground">Revenue</p><p className="text-lg font-bold">{formatCurrency(report.revenue)}</p></div>
                <div><p className="text-xs text-muted-foreground">Expenses</p><p className="text-lg font-bold text-red-600">{formatCurrency(report.expenses)}</p></div>
                <div><p className="text-xs text-muted-foreground">Profit</p><p className={`text-lg font-bold ${report.profit >= 0 ? 'text-green-600' : 'text-red-600'}`}>{formatCurrency(report.profit)}</p></div>
              </div>
              <p className="mt-2 text-xs text-muted-foreground">{report.salesCount} sales</p>
            </CardContent></Card>
          ))}
        </div>
      )}

      {activeTab === 'inventory' && inventoryReport && (
        <div className="space-y-4">
          <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
            <Card><CardContent className="pt-6"><div className="text-2xl font-bold">{inventoryReport.totalProducts}</div><p className="text-xs text-muted-foreground">Total Products</p></CardContent></Card>
            <Card><CardContent className="pt-6"><div className="text-2xl font-bold text-amber-600">{inventoryReport.lowStock}</div><p className="text-xs text-muted-foreground">Low Stock</p></CardContent></Card>
            <Card><CardContent className="pt-6"><div className="text-2xl font-bold text-red-600">{inventoryReport.outOfStock}</div><p className="text-xs text-muted-foreground">Out of Stock</p></CardContent></Card>
            <Card><CardContent className="pt-6"><div className="text-2xl font-bold">{formatCurrency(inventoryReport.totalSellValue)}</div><p className="text-xs text-muted-foreground">Sell Value</p></CardContent></Card>
          </div>
          {Object.keys(inventoryReport.byCategory || {}).length > 0 && (
            <Card><CardHeader><CardTitle className="text-base">By Category</CardTitle></CardHeader><CardContent>
              <div className="space-y-2">{Object.entries(inventoryReport.byCategory).map(([cat, count]) => (
                <div key={cat} className="flex items-center justify-between"><span className="text-sm">{cat}</span><span className="text-sm font-medium">{count} products</span></div>
              ))}</div>
            </CardContent></Card>
          )}
        </div>
      )}

      {activeTab === 'debt' && debtReport && (
        <div className="space-y-4">
          <div className="grid gap-4 grid-cols-2">
            <Card><CardContent className="pt-6"><div className="text-2xl font-bold text-amber-600">{formatCurrency(debtReport.totalOwing)}</div><p className="text-xs text-muted-foreground">Total Outstanding</p></CardContent></Card>
            <Card><CardContent className="pt-6"><div className="text-2xl font-bold">{debtReport.debtorCount}</div><p className="text-xs text-muted-foreground">Customers Owing</p></CardContent></Card>
          </div>
          {debtReport.debtors.length > 0 ? (
            <Card><CardHeader><CardTitle className="text-base">Debtors</CardTitle></CardHeader><CardContent>
              <div className="space-y-2">{debtReport.debtors.map((d: any) => (
                <div key={d.id} className="flex items-center justify-between rounded border p-2">
                  <div><span className="text-sm font-medium">{d.name}</span>{d.phone && <span className="ml-2 text-xs text-muted-foreground">{d.phone}</span>}</div>
                  <span className="text-sm font-bold text-amber-600">{formatCurrency(d.balance)}</span>
                </div>
              ))}</div>
            </CardContent></Card>
          ) : <Card><CardContent><p className="text-sm text-muted-foreground">No outstanding debts</p></CardContent></Card>}
        </div>
      )}
    </div>
  );
}
