import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productApi, stockMovementApi } from '@/services/api';
import { Product, StockMovement } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { formatCurrency } from '@/lib/utils';
import { ArrowLeft, Package, TrendingUp, ShoppingCart } from 'lucide-react';

export default function ProductHistory() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [movements, setMovements] = useState<StockMovement[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    const fetchData = async () => {
      try {
        const [prodRes, movRes] = await Promise.all([
          productApi.getById(Number(id)),
          stockMovementApi.getByProduct(Number(id)),
        ]);
        setProduct(prodRes.data); setMovements(movRes.data);
      } catch { /* silent */ }
      finally { setLoading(false); }
    };
    fetchData();
  }, [id]);

  if (loading) return <div className="py-8 text-center">Loading product history...</div>;
  if (!product) return <div className="py-8 text-center">Product not found</div>;

  const totalPurchased = movements.filter(m => m.movementType === 'PURCHASE').reduce((s, m) => s + m.quantity, 0);
  const totalSold = movements.filter(m => m.movementType === 'SALE').reduce((s, m) => s + m.quantity, 0);

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}><ArrowLeft className="h-5 w-5" /></Button>
        <div><h1 className="text-2xl font-bold tracking-tight">{product.name}</h1><p className="text-sm text-muted-foreground">{product.productId} · {product.category}</p></div>
      </div>

      <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
        <Card><CardContent className="pt-6"><div className="text-2xl font-bold">{product.stockQuantity}</div><p className="text-xs text-muted-foreground">Current Stock</p></CardContent></Card>
        <Card><CardContent className="pt-6"><div className="text-2xl font-bold text-green-600">{totalPurchased}</div><p className="text-xs text-muted-foreground">Total Purchased</p></CardContent></Card>
        <Card><CardContent className="pt-6"><div className="text-2xl font-bold text-blue-600">{totalSold}</div><p className="text-xs text-muted-foreground">Total Sold</p></CardContent></Card>
        <Card><CardContent className="pt-6"><div className="text-2xl font-bold">{formatCurrency(product.stockQuantity * product.sellPrice)}</div><p className="text-xs text-muted-foreground">Stock Value</p></CardContent></Card>
      </div>

      <Card><CardHeader><CardTitle className="text-base">Price Information</CardTitle></CardHeader><CardContent>
        <div className="grid grid-cols-2 gap-4">
          <div><p className="text-xs text-muted-foreground">Buying Price</p><p className="text-lg font-bold">{formatCurrency(product.buyPrice)}</p></div>
          <div><p className="text-xs text-muted-foreground">Selling Price</p><p className="text-lg font-bold">{formatCurrency(product.sellPrice)}</p></div>
          <div><p className="text-xs text-muted-foreground">Profit Per Item</p><p className="text-lg font-bold text-green-600">{formatCurrency(product.sellPrice - product.buyPrice)}</p></div>
          <div><p className="text-xs text-muted-foreground">Expected Total Profit</p><p className="text-lg font-bold text-green-600">{formatCurrency((product.sellPrice - product.buyPrice) * product.stockQuantity)}</p></div>
        </div>
      </CardContent></Card>

      <Card><CardHeader><CardTitle className="text-base">Stock Movement History ({movements.length})</CardTitle></CardHeader><CardContent>
        {movements.length === 0 ? <p className="text-sm text-muted-foreground">No stock movements recorded</p> : (
          <div className="space-y-2">
            {movements.map(m => (
              <div key={m.id} className="flex items-center justify-between rounded-lg border p-3">
                <div className="flex items-center gap-3">
                  <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${m.movementType === 'PURCHASE' ? 'bg-green-100 dark:bg-green-900/20' : m.movementType === 'SALE' ? 'bg-blue-100 dark:bg-blue-900/20' : 'bg-muted'}`}>
                    {m.movementType === 'PURCHASE' ? <TrendingUp className="h-4 w-4 text-green-600" /> : m.movementType === 'SALE' ? <ShoppingCart className="h-4 w-4 text-blue-600" /> : <Package className="h-4 w-4" />}
                  </div>
                  <div><div className="text-sm font-medium">{m.movementType}</div><div className="text-xs text-muted-foreground">{m.notes || 'No notes'} · {new Date(m.createdAt).toLocaleDateString('en-ZA')}</div></div>
                </div>
                <div className="text-right">
                  <span className={`text-sm font-bold ${m.movementType === 'PURCHASE' ? 'text-green-600' : 'text-red-600'}`}>{m.movementType === 'PURCHASE' ? '+' : '-'}{m.quantity}</span>
                  {m.unitCost && <div className="text-xs text-muted-foreground">{formatCurrency(m.unitCost)}/unit</div>}
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent></Card>
    </div>
  );
}
