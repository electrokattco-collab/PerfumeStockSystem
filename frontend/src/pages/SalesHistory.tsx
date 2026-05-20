import { useEffect, useState } from 'react';
import { saleApi } from '@/services/api';
import { Sale } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { formatCurrency, formatDate } from '@/lib/utils';

export default function SalesHistory() {
  const [sales, setSales] = useState<Sale[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSales();
  }, []);

  const fetchSales = async () => {
    try {
      const response = await saleApi.getAll();
      setSales(response.data);
    } catch (error) {
      console.error('Failed to fetch sales:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="text-center">Loading sales history...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Sales History</h1>
        <p className="text-muted-foreground">View all recorded sales transactions</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Sales</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="py-3 px-2 text-left font-medium">Date</th>
                  <th className="py-3 px-2 text-left font-medium">Sale ID</th>
                  <th className="py-3 px-2 text-left font-medium">Product</th>
                  <th className="py-3 px-2 text-left font-medium">Tier</th>
                  <th className="py-3 px-2 text-right font-medium">Qty</th>
                  <th className="py-3 px-2 text-right font-medium">Unit Price</th>
                  <th className="py-3 px-2 text-right font-medium">Total</th>
                </tr>
              </thead>
              <tbody>
                {sales.map((sale) => (
                  <tr key={sale.id} className="border-b">
                    <td className="py-3 px-2 text-muted-foreground">
                      {formatDate(sale.createdAt)}
                    </td>
                    <td className="py-3 px-2 font-medium">{sale.saleId}</td>
                    <td className="py-3 px-2">{sale.productName}</td>
                    <td className="py-3 px-2">
                      <span className="inline-flex rounded-full bg-secondary px-2 py-1 text-xs font-medium capitalize">
                        {sale.customerTier.toLowerCase()}
                      </span>
                    </td>
                    <td className="py-3 px-2 text-right">{sale.quantity}</td>
                    <td className="py-3 px-2 text-right">{formatCurrency(sale.unitPrice)}</td>
                    <td className="py-3 px-2 text-right font-medium">
                      {formatCurrency(sale.unitPrice * sale.quantity)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
