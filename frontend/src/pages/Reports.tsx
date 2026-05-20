import { useEffect, useState } from 'react';
import { reportApi, productApi } from '@/services/api';
import { ProfitReport, Product } from '@/types';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCurrency } from '@/lib/utils';
import { AlertTriangle, TrendingUp, DollarSign, Package } from 'lucide-react';

export default function Reports() {
  const [profitReport, setProfitReport] = useState<ProfitReport | null>(null);
  const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      const [profitRes, lowStockRes] = await Promise.all([
        reportApi.getProfit(),
        productApi.getLowStock(),
      ]);
      setProfitReport(profitRes.data);
      setLowStockProducts(lowStockRes.data);
    } catch (error) {
      console.error('Failed to fetch reports:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="text-center">Loading reports...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Reports</h1>
        <p className="text-muted-foreground">View profit summaries and stock alerts</p>
      </div>

      {/* Profit Summary */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(profitReport?.totalRevenue || 0)}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Cost</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(profitReport?.totalCost || 0)}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Profit</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(profitReport?.totalProfit || 0)}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Sales</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{profitReport?.totalSales || 0}</div>
          </CardContent>
        </Card>
      </div>

      {/* Profit by Tier */}
      <Card>
        <CardHeader>
          <CardTitle>Profit by Customer Tier</CardTitle>
          <CardDescription>Revenue breakdown by pricing tier</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-4">
            {profitReport?.profitByTier && Object.entries(profitReport.profitByTier).map(([tier, profit]) => (
              <div key={tier} className="rounded-lg border p-4">
                <div className="text-sm font-medium capitalize">{tier.toLowerCase()}</div>
                <div className="text-2xl font-bold">{formatCurrency(profit)}</div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Low Stock Alert */}
      <Card className={lowStockProducts.length > 0 ? 'border-destructive' : ''}>
        <CardHeader>
          <div className="flex items-center gap-2">
            {lowStockProducts.length > 0 && <AlertTriangle className="h-5 w-5 text-destructive" />}
            <CardTitle>Low Stock Alert</CardTitle>
          </div>
          <CardDescription>Products below their stock threshold</CardDescription>
        </CardHeader>
        <CardContent>
          {lowStockProducts.length === 0 ? (
            <p className="text-sm text-muted-foreground">No low stock items</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b">
                    <th className="py-2 px-2 text-left font-medium">Product</th>
                    <th className="py-2 px-2 text-left font-medium">Category</th>
                    <th className="py-2 px-2 text-right font-medium">Current Stock</th>
                    <th className="py-2 px-2 text-right font-medium">Threshold</th>
                  </tr>
                </thead>
                <tbody>
                  {lowStockProducts.map((product) => (
                    <tr key={product.id} className="border-b">
                      <td className="py-2 px-2">{product.name}</td>
                      <td className="py-2 px-2 text-muted-foreground">{product.category}</td>
                      <td className="py-2 px-2 text-right font-medium text-destructive">{product.stockQuantity}</td>
                      <td className="py-2 px-2 text-right">{product.lowStockThreshold}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
