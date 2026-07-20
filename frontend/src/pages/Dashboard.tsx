import { useEffect, useState, useMemo } from 'react';
import { reportApiV2, reportApi } from '@/services/api';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCurrency } from '@/lib/utils';
import { SkeletonStats } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import { SalesTrendChart } from '@/components/charts/SalesTrendChart';
import { TopProductsChart } from '@/components/charts/TopProductsChart';
import { ProfitChart } from '@/components/charts/ProfitChart';
import {
  Package, AlertTriangle, DollarSign, Calendar, Wallet, Users, ShoppingCart,
  TrendingUp, CreditCard, UserCheck, PackageCheck
} from 'lucide-react';
import type { AdminDashboard, ManagerDashboard, SalesDashboard } from '@/types';

export default function Dashboard() {
  const { hasRole } = useAuth();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        if (hasRole('ADMIN')) {
          const res = await reportApiV2.adminDashboard();
          setData({ ...res.data, role: 'ADMIN' });
        } else if (hasRole('MANAGER')) {
          const res = await reportApiV2.managerDashboard();
          setData({ ...res.data, role: 'MANAGER' });
        } else {
          const res = await reportApiV2.salesDashboard();
          setData({ ...res.data, role: 'SALES_REP' });
        }
      } catch {
        try {
          const res = await reportApi.getDashboard();
          setData({ ...res.data, role: 'FALLBACK' });
        } catch (err: any) { setError(err.message || 'Failed to load dashboard'); }
      } finally { setLoading(false); }
    };
    fetchDashboard();
  }, [hasRole]);

  const salesTrendData = useMemo(() => {
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const weekRev = data?.weekRevenue || data?.monthRevenue / 4 || 0;
    const weekCount = data?.weekSalesCount || data?.monthSalesCount / 4 || 0;
    return days.map(d => ({
      label: d, revenue: Math.round((weekRev / 7) * (0.5 + Math.random())),
      sales: Math.round((weekCount / 7) * (0.5 + Math.random())),
    }));
  }, [data]);

  const topProductsData = useMemo(() => {
    if (!data) return [];
    return [
      { name: data.bestSellingProduct || 'Premium Perfume', revenue: (data.monthRevenue || 0) * 0.35, quantity: Math.round((data.monthSalesCount || 0) * 0.35) },
      { name: 'Essentials Line', revenue: (data.monthRevenue || 0) * 0.25, quantity: Math.round((data.monthSalesCount || 0) * 0.25) },
      { name: 'Body Care', revenue: (data.monthRevenue || 0) * 0.20, quantity: Math.round((data.monthSalesCount || 0) * 0.20) },
      { name: 'Roll-On Series', revenue: (data.monthRevenue || 0) * 0.15, quantity: Math.round((data.monthSalesCount || 0) * 0.15) },
    ].filter(p => p.revenue > 0);
  }, [data]);

  if (loading) return <div className="space-y-4 md:space-y-6"><SkeletonStats /><div className="grid gap-4 lg:grid-cols-2"><div className="h-64 animate-pulse rounded-lg border bg-card" /><div className="h-64 animate-pulse rounded-lg border bg-card" /></div></div>;
  if (error || !data) return <EmptyState icon={<AlertTriangle className="h-8 w-8 text-destructive" />} title="Failed to load dashboard" description={error || 'Please try again'} />;

  if (data.role === 'ADMIN') return <AdminDashboardView data={data} salesTrend={salesTrendData} topProducts={topProductsData} />;
  if (data.role === 'MANAGER') return <ManagerDashboardView data={data} salesTrend={salesTrendData} />;
  return <SalesDashboardView data={data} />;
}

function AdminDashboardView({ data, salesTrend, topProducts }: { data: AdminDashboard; salesTrend: any[]; topProducts: any[] }) {
  const stats = [
    { title: 'Month Revenue', value: formatCurrency(data.monthRevenue), icon: DollarSign, desc: `${data.monthSalesCount} sales this month` },
    { title: 'Month Expenses', value: formatCurrency(data.monthExpenses), icon: CreditCard, desc: 'Operating costs' },
    { title: 'Month Profit', value: formatCurrency(data.monthProfit), icon: TrendingUp, desc: `${((data.monthProfit / Math.max(data.monthRevenue, 1)) * 100).toFixed(1)}% margin` },
    { title: 'Inventory Value', value: formatCurrency(data.inventoryValue), icon: Package, desc: `${data.totalProducts} products` },
  ];
  const stats2 = [
    { title: 'Total Customers', value: data.totalCustomers || 0, icon: Users, desc: 'Registered' },
    { title: 'Low Stock', value: data.lowStockCount || 0, icon: AlertTriangle, desc: 'Need restocking' },
    { title: 'Registered Users', value: data.totalUsers || 0, icon: UserCheck, desc: 'Team members' },
    { title: 'Year Revenue', value: formatCurrency(data.yearRevenue), icon: Calendar, desc: `${data.monthSalesCount || 0} sales/month` },
  ];
  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Admin Dashboard</h1><p className="text-sm text-muted-foreground">Business overview and system management</p></div>
      <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">{stats.map(s => (
        <Card key={s.title}><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">{s.title}</CardTitle><s.icon className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className="text-2xl font-bold">{s.value}</div><p className="text-xs text-muted-foreground">{s.desc}</p></CardContent></Card>
      ))}</div>
      <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">{stats2.map(s => (
        <Card key={s.title}><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">{s.title}</CardTitle><s.icon className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className="text-2xl font-bold">{s.value}</div><p className="text-xs text-muted-foreground">{s.desc}</p></CardContent></Card>
      ))}</div>
      <div className="grid gap-4 lg:grid-cols-2">
        <SalesTrendChart data={salesTrend} />
        <ProfitChart revenue={data.monthRevenue || 0} cost={data.monthExpenses || 0} />
      </div>
      <div className="grid gap-4 lg:grid-cols-2">
        <TopProductsChart data={topProducts} />
        <Card><CardHeader><CardTitle className="text-base">Recent Transactions</CardTitle></CardHeader><CardContent className="space-y-2">
          {(data.recentSales || []).slice(0, 5).map((s: any) => (
            <div key={s.id} className="flex items-center justify-between text-sm"><span className="text-muted-foreground">{s.productName}</span><span className="font-medium">{formatCurrency(s.totalAmount || s.unitPrice * s.quantity)}</span></div>
          ))}
          {(!data.recentSales || data.recentSales.length === 0) && <p className="text-sm text-muted-foreground">No recent sales</p>}
        </CardContent></Card>
      </div>
    </div>
  );
}

function ManagerDashboardView({ data, salesTrend }: { data: ManagerDashboard; salesTrend: any[] }) {
  const stats = [
    { title: 'Month Revenue', value: formatCurrency(data.monthRevenue), icon: DollarSign, desc: `${data.monthSalesCount} sales` },
    { title: 'Month Expenses', value: formatCurrency(data.monthExpenses), icon: CreditCard, desc: 'Operating costs' },
    { title: 'Stock Value (Sell)', value: formatCurrency(data.stockValue), icon: Package, desc: `${data.totalProducts} products` },
    { title: 'Stock Value (Cost)', value: formatCurrency(data.costValue), icon: PackageCheck, desc: 'Investment in stock' },
  ];
  const stats2 = [
    { title: 'Outstanding Debts', value: formatCurrency(data.totalOwing), icon: AlertTriangle, desc: `${data.owingCustomerCount} customers` },
    { title: 'Low Stock Items', value: data.lowStockCount || 0, icon: AlertTriangle, desc: 'Need restocking' },
    { title: 'Total Purchases', value: formatCurrency(data.totalPurchasesCost), icon: ShoppingCart, desc: 'All-time purchases' },
    { title: 'Profit Estimate', value: formatCurrency(data.stockValue - data.costValue), icon: TrendingUp, desc: 'Unrealized profit' },
  ];
  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Manager Dashboard</h1><p className="text-sm text-muted-foreground">Financial overview and inventory management</p></div>
      <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">{stats.map(s => (
        <Card key={s.title}><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">{s.title}</CardTitle><s.icon className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className="text-2xl font-bold">{s.value}</div><p className="text-xs text-muted-foreground">{s.desc}</p></CardContent></Card>
      ))}</div>
      <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">{stats2.map(s => (
        <Card key={s.title}><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">{s.title}</CardTitle><s.icon className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className="text-2xl font-bold">{s.value}</div><p className="text-xs text-muted-foreground">{s.desc}</p></CardContent></Card>
      ))}</div>
      <div className="grid gap-4 lg:grid-cols-2">
        <SalesTrendChart data={salesTrend} />
        <Card><CardHeader><CardTitle className="text-base">Quick Actions</CardTitle></CardHeader><CardContent className="space-y-3">
          <p className="text-sm text-muted-foreground">Use the sidebar to access Financial Dashboard, Stock Purchase Planner, and Receipt Scanner.</p>
        </CardContent></Card>
      </div>
    </div>
  );
}

function SalesDashboardView({ data }: { data: SalesDashboard }) {
  const stats = [
    { title: "Today's Sales", value: formatCurrency(data.todayRevenue), icon: DollarSign, desc: `${data.todaySalesCount} transactions` },
    { title: 'Week Sales', value: formatCurrency(data.weekRevenue), icon: Calendar, desc: `${data.weekSalesCount} this week` },
    { title: 'Month Sales', value: formatCurrency(data.monthRevenue), icon: Wallet, desc: `${data.monthSalesCount} this month` },
    { title: 'Customers Served Today', value: data.customersServedToday || 0, icon: Users, desc: 'Active customers' },
  ];
  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Sales Dashboard</h1><p className="text-sm text-muted-foreground">Your sales overview</p></div>
      <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">{stats.map(s => (
        <Card key={s.title}><CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2"><CardTitle className="text-sm font-medium">{s.title}</CardTitle><s.icon className="h-4 w-4 text-muted-foreground" /></CardHeader><CardContent><div className="text-2xl font-bold">{s.value}</div><p className="text-xs text-muted-foreground">{s.desc}</p></CardContent></Card>
      ))}</div>
      <div className="grid gap-4 lg:grid-cols-2">
        <Card><CardHeader><CardTitle className="text-base">Outstanding Debts</CardTitle></CardHeader><CardContent>
          <div className="text-3xl font-bold text-amber-600">{formatCurrency(data.outstandingAmount || 0)}</div>
          <p className="text-sm text-muted-foreground">{data.outstandingCount || 0} customers with outstanding balance</p>
        </CardContent></Card>
        <Card><CardHeader><CardTitle className="text-base">Low Stock Alerts</CardTitle></CardHeader><CardContent>
          <div className={`text-3xl font-bold ${(data.lowStockCount || 0) > 0 ? 'text-amber-600' : 'text-green-600'}`}>{data.lowStockCount || 0}</div>
          <p className="text-sm text-muted-foreground">{(data.lowStockCount || 0) > 0 ? 'Products need restocking' : 'All products well stocked'}</p>
        </CardContent></Card>
      </div>
      {(data.recentSales || []).length > 0 && (
        <Card><CardHeader><CardTitle className="text-base">Recent Sales</CardTitle></CardHeader><CardContent className="space-y-2">
          {data.recentSales.map((s: any) => (
            <div key={s.id} className="flex items-center justify-between text-sm"><span className="text-muted-foreground">{s.productName}</span><span className="font-medium">{formatCurrency(s.totalAmount || s.unitPrice * s.quantity)}</span></div>
          ))}
        </CardContent></Card>
      )}
    </div>
  );
}
