import { useEffect, useState } from 'react';
import { reportApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCurrency } from '@/lib/utils';
import {
  ShoppingCart, DollarSign, TrendingUp, AlertTriangle, Package, Users,
  Wallet, Calendar, Activity, Clock3, ReceiptText, CreditCard
} from 'lucide-react';
import type { DashboardData } from '@/types';

export default function Dashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reportApi.dashboard()
      .then((res) => setData(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="flex items-center justify-center h-64 text-muted-foreground">Loading...</div>;
  if (!data) return <div className="text-center text-muted-foreground py-8">Failed to load dashboard</div>;

  const stats = [
    { title: "Today's Sales", value: data.todaySalesCount, icon: ShoppingCart, color: 'text-blue-600' },
    { title: "Today's Revenue", value: formatCurrency(data.todayRevenue), icon: DollarSign, color: 'text-green-600' },
    { title: "Today's Profit", value: formatCurrency(data.todayProfit), icon: TrendingUp, color: 'text-emerald-600' },
    { title: 'Monthly Revenue', value: formatCurrency(data.monthRevenue), icon: Calendar, color: 'text-purple-600' },
    { title: 'Monthly Profit', value: formatCurrency(data.monthProfit), icon: Wallet, color: 'text-teal-600' },
    { title: 'Monthly Purchases', value: formatCurrency(data.monthPurchasesSpent), icon: ReceiptText, color: 'text-slate-700' },
    { title: 'Pending Purchases', value: data.pendingPurchaseConfirmations, icon: Clock3, color: 'text-orange-600' },
    { title: 'Cash Received (Month)', value: formatCurrency(data.cashReceivedMonth), icon: DollarSign, color: 'text-amber-600' },
    { title: 'Outstanding Debt', value: formatCurrency(data.totalOutstanding), icon: AlertTriangle, color: 'text-red-600' },
    { title: 'Overdue Accounts', value: data.overdueAccounts, icon: AlertTriangle, color: 'text-red-700' },
    { title: 'Total Customers', value: data.totalCustomers, icon: Users, color: 'text-slate-700' },
    { title: 'Customers Owing', value: data.customersWithOutstandingBalances, icon: Users, color: 'text-rose-600' },
    { title: 'Largest Debtor', value: data.largestDebtor ? formatCurrency(data.largestDebtor.balance) : 'None', icon: AlertTriangle, color: 'text-red-500' },
    { title: 'Customers Paid (Month)', value: data.customersPaidThisMonth, icon: CreditCard, color: 'text-cyan-600' },
    { title: 'Avg Purchase', value: formatCurrency(data.averageCustomerPurchaseValue), icon: Wallet, color: 'text-indigo-600' },
    { title: 'Inventory Value', value: formatCurrency(data.inventoryValue), icon: Package, color: 'text-indigo-600' },
    { title: 'Low Stock Items', value: data.lowStockCount, icon: AlertTriangle, color: 'text-orange-600' },
    { title: 'Total Products', value: data.totalProducts, icon: Package, color: 'text-slate-600' },
    { title: 'Debtors', value: data.totalDebtors, icon: Users, color: 'text-rose-600' },
    { title: 'Inventory Movements', value: data.inventoryMovementsMonth, icon: Activity, color: 'text-cyan-600' },
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Dashboard</h1>

      <div className="grid gap-4 grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {stats.map((s) => (
          <Card key={s.title}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{s.title}</CardTitle>
              <s.icon className={`h-4 w-4 ${s.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{s.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
        </CardHeader>
        <CardContent>
          {data.recentActivity && data.recentActivity.length > 0 ? (
            <div className="space-y-3">
              {data.recentActivity.map((event: any) => (
                <div key={event.id} className="flex items-center justify-between border-b pb-2 last:border-0">
                  <div>
                    <p className="text-sm font-medium">{event.eventType}</p>
                    <p className="text-xs text-muted-foreground">
                      {event.referenceType} #{event.referenceId} - {new Date(event.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold">{formatCurrency(event.amount || 0)}</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No recent activity</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
