import { useEffect, useState } from 'react';
import { productApi, saleApi } from '@/services/api';
import { Product } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/hooks/use-toast';

const tiers = [
  { value: 'RETAIL', label: 'Retail', getPrice: (p: Product) => p.retailPrice },
  { value: 'REWARDS', label: 'Rewards', getPrice: (p: Product) => p.rewardsPrice },
  { value: 'GOLD', label: 'Gold', getPrice: (p: Product) => p.goldPrice },
  { value: 'VIP', label: 'VIP', getPrice: (p: Product) => p.vipPrice },
];

export default function RecordSale() {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [tier, setTier] = useState('RETAIL');
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await productApi.getAll();
      setProducts(response.data.filter((p) => p.stockQuantity > 0));
    } catch (error) {
      console.error('Failed to fetch products:', error);
    }
  };

  const selectedTier = tiers.find((t) => t.value === tier);
  const unitPrice = selectedProduct && selectedTier ? selectedTier.getPrice(selectedProduct) : 0;
  const totalPrice = unitPrice * quantity;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProduct) return;

    setLoading(true);
    try {
      await saleApi.create({
        productId: selectedProduct.productId,
        quantity,
        customerTier: tier,
      });
      toast({
        title: 'Sale recorded successfully',
        description: `Sold ${quantity} x ${selectedProduct.name} for $${totalPrice.toFixed(2)}`,
      });
      setSelectedProduct(null);
      setQuantity(1);
      fetchProducts();
    } catch (error: any) {
      toast({
        title: 'Error recording sale',
        description: error.response?.data?.message || 'Something went wrong',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Record Sale</h1>
        <p className="text-muted-foreground">Record a new sales transaction</p>
      </div>

      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle>New Sale</CardTitle>
          <CardDescription>Select product, quantity, and customer tier</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label>Product</Label>
              <select
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                value={selectedProduct?.id || ''}
                onChange={(e) => {
                  const product = products.find((p) => p.id === Number(e.target.value));
                  setSelectedProduct(product || null);
                }}
                required
              >
                <option value="">Select a product</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} (Stock: {product.stockQuantity})
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-2">
              <Label>Customer Tier</Label>
              <div className="grid grid-cols-2 gap-2">
                {tiers.map((t) => (
                  <button
                    key={t.value}
                    type="button"
                    onClick={() => setTier(t.value)}
                    className={`rounded-md border px-3 py-2 text-sm ${
                      tier === t.value
                        ? 'border-primary bg-primary text-primary-foreground'
                        : 'border-input hover:bg-accent'
                    }`}
                  >
                    {t.label}
                    {selectedProduct && (
                      <div className="text-xs opacity-80">
                        ${t.getPrice(selectedProduct).toFixed(2)}
                      </div>
                    )}
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="quantity">Quantity</Label>
              <Input
                id="quantity"
                type="number"
                min={1}
                max={selectedProduct?.stockQuantity}
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                required
              />
            </div>

            {selectedProduct && (
              <div className="rounded-lg bg-muted p-4">
                <div className="flex justify-between text-sm">
                  <span>Unit Price:</span>
                  <span>${unitPrice.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-lg font-bold">
                  <span>Total:</span>
                  <span>${totalPrice.toFixed(2)}</span>
                </div>
              </div>
            )}

            <Button type="submit" className="w-full" disabled={!selectedProduct || loading}>
              {loading ? 'Recording...' : 'Record Sale'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
