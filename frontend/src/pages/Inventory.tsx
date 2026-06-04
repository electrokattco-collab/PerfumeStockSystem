import { useEffect, useState } from 'react';
import { productApi } from '@/services/api';
import { Product } from '@/types';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Search, Package, AlertTriangle } from 'lucide-react';
import { formatCurrency } from '@/lib/utils';

export default function Inventory() {
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    const filtered = products.filter(
      (p) =>
        p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.category.toLowerCase().includes(searchTerm.toLowerCase())
    );
    setFilteredProducts(filtered);
  }, [searchTerm, products]);

  const fetchProducts = async () => {
    try {
      const response = await productApi.getAll();
      setProducts(response.data);
      setFilteredProducts(response.data);
    } catch (error) {
      console.error('Failed to fetch products:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="text-center">Loading inventory...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Inventory</h1>
          <p className="text-muted-foreground">Manage your perfume products</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search products..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="py-3 px-2 text-left font-medium">Product</th>
                  <th className="py-3 px-2 text-left font-medium">Category</th>
                  <th className="py-3 px-2 text-left font-medium">Stock</th>
                  <th className="py-3 px-2 text-right font-medium">Retail</th>
                  <th className="py-3 px-2 text-right font-medium">Rewards</th>
                  <th className="py-3 px-2 text-right font-medium">Gold</th>
                  <th className="py-3 px-2 text-right font-medium">VIP</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.map((product) => (
                  <tr
                    key={product.id}
                    className={`border-b ${
                      product.isLowStock ? 'bg-destructive/10' : ''
                    }`}
                  >
                    <td className="py-3 px-2">
                      <div className="flex items-center gap-2">
                        {product.isLowStock && (
                          <AlertTriangle className="h-4 w-4 text-destructive" />
                        )}
                        <div>
                          <div className="font-medium">{product.name}</div>
                          <div className="text-xs text-muted-foreground">{product.productId}</div>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-2 text-muted-foreground">{product.category}</td>
                    <td className="py-3 px-2">
                      <span className={`inline-flex items-center gap-1 rounded-full px-2 py-1 text-xs font-medium ${
                        product.stockQuantity === 0
                          ? 'bg-destructive/10 text-destructive'
                          : product.stockQuantity <= product.lowStockThreshold
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-green-100 text-green-800'
                      }`}>
                        <Package className="h-3 w-3" />
                        {product.stockQuantity}
                      </span>
                    </td>
                    <td className="py-3 px-2 text-right">{formatCurrency(product.retailPrice)}</td>
                    <td className="py-3 px-2 text-right">{formatCurrency(product.rewardsPrice)}</td>
                    <td className="py-3 px-2 text-right">{formatCurrency(product.goldPrice)}</td>
                    <td className="py-3 px-2 text-right">{formatCurrency(product.vipPrice)}</td>
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
