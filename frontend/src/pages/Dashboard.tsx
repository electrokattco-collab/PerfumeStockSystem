import { useEffect, useState } from 'react';
import { reportApi } from '@/services/api';
import { DashboardSummary } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Package, TrendingUp, AlertTriangle, DollarSign } from 'lucide-react';
import { formatCurrency } from '@/lib/utils';

export default function Dashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const response = await reportApi.getDashboard();
        setSummary(response.data);
      } catch (error) {
        console.error('Failed to fetch dashboard summary:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchSummary();
  }, []);

  const stats = [
    {
      title: 'Total Products',
      value: summary?.totalProducts || 0,
      icon: Package,
      description: 'Products in inventory',
    },
    {
      title: 'Low Stock Items',
      value: summary?.lowStockCount || 0,
      icon: AlertTriangle,
      description: 'Items below threshold',
      alert: (summary?.lowStockCount || 0) > 0,
    },
    {
      title: "Today's Sales",
      value: summary?.todaySalesCount || 0,
      icon: TrendingUp,
      description: 'Sales transactions today',
    },
    {
      title: "Today's Revenue",
      value: formatCurrency(summary?.todayRevenue || 0),
      icon: DollarSign,
      description: 'Total revenue today',
    },
  ];

  if (loading) {
    return <div className="text-center">Loading dashboard...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome back! Here's an overview of your perfume stock.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <Card key={index} className={stat.alert ? 'border-destructive' : ''}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.alert ? 'text-destructive' : 'text-muted-foreground'}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                {stat.description}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common tasks you can perform</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <p className="text-sm">• Record a new sale</p>
            <p className="text-sm">• Check low stock items</p>
            <p className="text-sm">• View sales history</p>
            <p className="text-sm">• Generate profit reports</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Stock Value</CardTitle>
            <CardDescription>Total retail value of current inventory</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {formatCurrency(summary?.totalStockValue || 0)}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
