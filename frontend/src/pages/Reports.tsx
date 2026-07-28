import { useEffect, useState } from 'react';
import { reportApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { formatCurrency } from '@/lib/utils';
import type { PeriodReport, InventoryReport, DebtReport } from '@/types';

export default function Reports() {
  const [activeTab, setActiveTab] = useState<'daily' | 'weekly' | 'monthly' | 'yearly' | 'inventory' | 'debt'>('monthly');
  const [periodData, setPeriodData] = useState<PeriodReport | null>(null);
  const [invData, setInvData] = useState<InventoryReport | null>(null);
  const [debtData, setDebtData] = useState<DebtReport | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    if (activeTab === 'inventory') {
      reportApi.inventory().then((res) => setInvData(res.data)).finally(() => setLoading(false));
    } else if (activeTab === 'debt') {
      reportApi.debt().then((res) => setDebtData(res.data)).finally(() => setLoading(false));
    } else {
      reportApi.period(activeTab).then((res) => setPeriodData(res.data)).finally(() => setLoading(false));
    }
  }, [activeTab]);

  const tabs = [
    { key: 'daily' as const, label: 'Daily' },
    { key: 'weekly' as const, label: 'Weekly' },
    { key: 'monthly' as const, label: 'Monthly' },
    { key: 'yearly' as const, label: 'Yearly' },
    { key: 'inventory' as const, label: 'Inventory' },
    { key: 'debt' as const, label: 'Debt' },
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Reports</h1>

      <div className="flex gap-2 flex-wrap">
        {tabs.map((t) => (
          <Button key={t.key} variant={activeTab === t.key ? 'default' : 'outline'} size="sm" onClick={() => setActiveTab(t.key)}>
            {t.label}
          </Button>
        ))}
      </div>

      {loading ? <div className="text-center py-8 text-muted-foreground">Loading...</div> : (
        <>
          {periodData && (
            <div className="space-y-4">
              <div className="grid gap-4 grid-cols-2 md:grid-cols-4">
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Sales</p><p className="text-2xl font-bold">{periodData.salesCount}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Revenue</p><p className="text-2xl font-bold">{formatCurrency(periodData.totalRevenue)}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Cost</p><p className="text-2xl font-bold">{formatCurrency(periodData.totalCost)}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Profit</p><p className="text-2xl font-bold text-green-600">{formatCurrency(periodData.totalProfit)}</p></CardContent></Card>
              </div>

              {periodData.topSellingProducts.length > 0 && (
                <Card>
                  <CardHeader><CardTitle>Top Selling Products</CardTitle></CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      {periodData.topSellingProducts.map((p, i) => (
                        <div key={i} className="flex items-center justify-between border-b pb-1 last:border-0">
                          <span className="text-sm">{p.name}</span>
                          <span className="font-medium">{p.quantity} sold</span>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          )}

          {invData && (
            <div className="space-y-4">
              <div className="grid gap-4 grid-cols-2 md:grid-cols-4">
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Products</p><p className="text-2xl font-bold">{invData.totalProducts}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Low Stock</p><p className="text-2xl font-bold text-orange-600">{invData.lowStock}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Out of Stock</p><p className="text-2xl font-bold text-red-600">{invData.outOfStock}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Sell Value</p><p className="text-2xl font-bold">{formatCurrency(invData.totalSellValue)}</p></CardContent></Card>
              </div>
              <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Cost Value</p><p className="text-2xl font-bold">{formatCurrency(invData.totalCostValue)}</p></CardContent></Card>
            </div>
          )}

          {debtData && (
            <div className="space-y-4">
              <div className="grid gap-4 grid-cols-2">
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Total Owing</p><p className="text-2xl font-bold text-red-600">{formatCurrency(debtData.totalOwing)}</p></CardContent></Card>
                <Card><CardContent className="pt-4"><p className="text-xs text-muted-foreground">Debtors</p><p className="text-2xl font-bold">{debtData.debtorCount}</p></CardContent></Card>
              </div>
              {debtData.debtors.length > 0 && (
                <Card>
                  <CardHeader><CardTitle>Debtor List</CardTitle></CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      {debtData.debtors.map((d) => (
                        <div key={d.id} className="flex items-center justify-between border-b pb-2 last:border-0">
                          <div>
                            <p className="font-medium">{d.name}</p>
                            <p className="text-xs text-muted-foreground">{d.phone}</p>
                          </div>
                          <p className="font-bold text-red-600">{formatCurrency(d.balance)}</p>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
