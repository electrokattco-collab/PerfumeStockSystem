import { useEffect, useState, useCallback } from 'react';
import { productApi, barcodeApi } from '@/services/api';
import { Product, PaginatedResponse } from '@/types';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Search, Package, AlertTriangle, Plus, X, Pencil, QrCode } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';
import { SkeletonTable } from '@/components/LoadingSkeleton';
import { EmptyState } from '@/components/EmptyState';
import { Pagination } from '@/components/Pagination';

export default function Inventory() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [saving, setSaving] = useState(false);
  const [barcodeModalProduct, setBarcodeModalProduct] = useState<Product | null>(null);
  const { toast } = useToast();

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  const emptyForm = { productId: '', name: '', category: '', size: '', buyPrice: '', sellPrice: '', stockQuantity: '0', lowStockThreshold: '5', imageUrl: '' };
  const [form, setForm] = useState(emptyForm);

  // Debounce search
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = debouncedSearch
        ? await productApi.search(debouncedSearch, undefined, { page, size: 20, sort: 'id', direction: 'asc' })
        : await productApi.getAll({ page, size: 20, sort: 'id', direction: 'asc' });
      const data: PaginatedResponse<Product> = response.data;
      setProducts(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err: any) {
      const msg = err.message || 'Failed to load products';
      setError(msg);
      toast({ title: 'Error', description: msg, variant: 'destructive' });
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearch, toast]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  const resetForm = () => setForm(emptyForm);

  const handleAddProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!form.name.trim()) { setError('Product name is required'); return; }
    if (!form.buyPrice || parseFloat(form.buyPrice) <= 0) { setError('Buy price must be greater than 0'); return; }
    if (!form.sellPrice || parseFloat(form.sellPrice) <= 0) { setError('Sell price must be greater than 0'); return; }

    setSaving(true);
    try {
      const productId = form.productId.trim() || form.name.trim().replace(/\s+/g, '-').toLowerCase();
      await productApi.create({
        productId,
        name: form.name.trim(),
        category: form.category.trim() || 'General',
        size: form.size.trim(),
        buyPrice: parseFloat(form.buyPrice),
        sellPrice: parseFloat(form.sellPrice),
        stockQuantity: parseInt(form.stockQuantity) || 0,
        lowStockThreshold: parseInt(form.lowStockThreshold) || 5,
        imageUrl: form.imageUrl || undefined,
      });
      toast({ title: 'Product added successfully' });
      setShowAddForm(false);
      resetForm();
      fetchProducts();
    } catch (err: any) {
      const msg = err.message || 'Failed to add product';
      setError(msg);
      toast({ title: 'Error', description: msg, variant: 'destructive' });
    } finally {
      setSaving(false);
    }
  };

  const handleEditProduct = (product: Product) => {
    setEditingProduct(product);
    setForm({
      productId: product.productId,
      name: product.name,
      category: product.category,
      size: product.size || '',
      buyPrice: product.buyPrice.toString(),
      sellPrice: product.sellPrice.toString(),
      stockQuantity: product.stockQuantity.toString(),
      lowStockThreshold: product.lowStockThreshold.toString(),
      imageUrl: product.imageUrl || '',
    });
    setShowAddForm(true);
  };

  const handleUpdateProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingProduct) return;
    setSaving(true);
    try {
      await productApi.update(editingProduct.id, {
        productId: form.productId.trim(),
        name: form.name.trim(),
        category: form.category.trim() || 'General',
        size: form.size.trim(),
        buyPrice: parseFloat(form.buyPrice),
        sellPrice: parseFloat(form.sellPrice),
        stockQuantity: parseInt(form.stockQuantity) || 0,
        lowStockThreshold: parseInt(form.lowStockThreshold) || 5,
        imageUrl: form.imageUrl || undefined,
      });
      toast({ title: 'Product updated' });
      setShowAddForm(false);
      setEditingProduct(null);
      resetForm();
      fetchProducts();
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-4 md:space-y-6">
        <div>
          <div className="h-8 w-36 animate-pulse rounded bg-muted" />
          <div className="mt-1 h-4 w-48 animate-pulse rounded bg-muted" />
        </div>
        <SkeletonTable rows={5} />
      </div>
    );
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight md:text-3xl">Inventory</h1>
          <p className="text-sm text-muted-foreground md:text-base">{totalElements} products</p>
        </div>
        <Button onClick={() => { setEditingProduct(null); resetForm(); setShowAddForm(true); }}>
          <Plus className="mr-2 h-4 w-4" /> Add Product
        </Button>
      </div>

      <Card>
        <CardHeader className="pb-3">
          <div className="relative max-w-sm">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-9"
            />
          </div>
        </CardHeader>
        <CardContent>
          {products.length === 0 ? (
            <EmptyState
              icon={<Package className="h-8 w-8 text-muted-foreground" />}
              title={searchTerm ? 'No products found' : 'No products yet'}
              description={searchTerm ? 'Try a different search term' : 'Add your first product to get started'}
            />
          ) : (
            <>
              {/* Desktop Table */}
              <div className="hidden md:block overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b">
                      <th className="py-3 px-2 text-left font-medium">Product</th>
                      <th className="py-3 px-2 text-left font-medium">Category</th>
                      <th className="py-3 px-2 text-right font-medium">Buy</th>
                      <th className="py-3 px-2 text-right font-medium">Sell</th>
                      <th className="py-3 px-2 text-right font-medium">Stock</th>
                      <th className="py-3 px-2 text-center font-medium">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map((p) => (
                      <tr key={p.id} className="border-b">
                        <td className="py-3 px-2">
                          <div className="font-medium">{p.name}</div>
                          <div className="text-xs text-muted-foreground">{p.productId}</div>
                        </td>
                        <td className="py-3 px-2 text-muted-foreground">{p.category}</td>
                        <td className="py-3 px-2 text-right">{formatCurrency(p.buyPrice)}</td>
                        <td className="py-3 px-2 text-right font-medium">{formatCurrency(p.sellPrice)}</td>
                        <td className="py-3 px-2 text-right">
                          <span className={p.isLowStock ? 'text-amber-600 font-medium' : ''}>
                            {p.stockQuantity}
                          </span>
                          {p.isLowStock && <AlertTriangle className="inline ml-1 h-3 w-3 text-amber-500" />}
                        </td>
                        <td className="py-3 px-2 text-center">
                          <div className="flex justify-center gap-1">
                            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => setBarcodeModalProduct(p)} title="View barcode">
                              <QrCode className="h-4 w-4" />
                            </Button>
                            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => handleEditProduct(p)}>
                              <Pencil className="h-4 w-4" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Mobile Cards */}
              <div className="space-y-3 md:hidden">
                {products.map((p) => (
                  <div key={p.id} className="rounded-lg border p-3">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center gap-3 min-w-0 flex-1">
                        {p.imageUrl ? (
                          <img src={p.imageUrl} alt={p.name} className="h-10 w-10 shrink-0 rounded-lg object-cover" />
                        ) : (
                          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary/10">
                            <Package className="h-5 w-5 text-primary" />
                          </div>
                        )}
                        <div className="min-w-0 flex-1">
                          <div className="text-sm font-medium">{p.name}</div>
                          <div className="text-xs text-muted-foreground">{p.productId} · {p.category}</div>
                        </div>
                      </div>
                      <div className="flex gap-1">
                        <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => setBarcodeModalProduct(p)} title="View barcode">
                          <QrCode className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-8 w-8" onClick={() => handleEditProduct(p)}>
                          <Pencil className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                    <div className="mt-2 flex items-center justify-between text-xs">
                      <span className="text-muted-foreground">Stock: <span className={p.isLowStock ? 'text-amber-600 font-medium' : 'font-medium'}>{p.stockQuantity}</span></span>
                      <span className="font-medium">{formatCurrency(p.sellPrice)}</span>
                    </div>
                  </div>
                ))}
              </div>

              <Pagination
                page={page}
                totalPages={totalPages}
                totalElements={totalElements}
                onPageChange={setPage}
              />
            </>
          )}
        </CardContent>
      </Card>

      {/* Add/Edit Modal */}
      {showAddForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg max-h-[90vh] overflow-y-auto">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold">{editingProduct ? 'Edit Product' : 'Add Product'}</h3>
              <Button variant="ghost" size="icon" onClick={() => { setShowAddForm(false); setEditingProduct(null); resetForm(); }}>
                <X className="h-4 w-4" />
              </Button>
            </div>
            <form onSubmit={editingProduct ? handleUpdateProduct : handleAddProduct} className="space-y-3">
              {error && <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-3 text-sm text-destructive">{error}</div>}
              <div className="space-y-1">
                <Label className="text-xs">Product ID (optional)</Label>
                <Input value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })} placeholder="Auto-generated if empty" />
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Name *</Label>
                <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Product name" required />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label className="text-xs">Category</Label>
                  <Input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} placeholder="Category" />
                </div>
                <div className="space-y-1">
                  <Label className="text-xs">Size</Label>
                  <Input value={form.size} onChange={(e) => setForm({ ...form, size: e.target.value })} placeholder="e.g. 50mL" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label className="text-xs">Buy Price (R) *</Label>
                  <Input type="number" step="0.01" min="0" value={form.buyPrice} onChange={(e) => setForm({ ...form, buyPrice: e.target.value })} required />
                </div>
                <div className="space-y-1">
                  <Label className="text-xs">Sell Price (R) *</Label>
                  <Input type="number" step="0.01" min="0" value={form.sellPrice} onChange={(e) => setForm({ ...form, sellPrice: e.target.value })} required />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <Label className="text-xs">Stock Quantity</Label>
                  <Input type="number" min="0" value={form.stockQuantity} onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })} />
                </div>
                <div className="space-y-1">
                  <Label className="text-xs">Low Stock Threshold</Label>
                  <Input type="number" min="1" value={form.lowStockThreshold} onChange={(e) => setForm({ ...form, lowStockThreshold: e.target.value })} />
                </div>
              </div>
              <div className="space-y-1">
                <Label className="text-xs">Product Image</Label>
                {form.imageUrl && (
                  <div className="mb-2">
                    <img src={form.imageUrl} alt="Product" className="h-16 w-16 rounded-lg object-cover" />
                  </div>
                )}
                <Input
                  value={form.imageUrl}
                  onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                  placeholder="Image URL (or upload below)"
                />
              </div>
              <div className="flex gap-2 pt-2">
                <Button type="button" variant="outline" className="flex-1" onClick={() => { setShowAddForm(false); setEditingProduct(null); resetForm(); }}>Cancel</Button>
                <Button type="submit" className="flex-1" disabled={saving}>
                  {saving ? 'Saving...' : editingProduct ? 'Update' : 'Add'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
      {/* Barcode Modal */}
      {barcodeModalProduct && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
          <div className="w-full max-w-sm rounded-lg bg-card p-6 shadow-lg">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-semibold">Product Code</h3>
              <Button variant="ghost" size="icon" onClick={() => setBarcodeModalProduct(null)}>
                <X className="h-4 w-4" />
              </Button>
            </div>
            <div className="space-y-4">
              <div className="text-center">
                <p className="text-sm font-medium mb-2">{barcodeModalProduct.name}</p>
                <p className="text-xs text-muted-foreground mb-4">{barcodeModalProduct.productId}</p>
                <div className="flex justify-center">
                  <img
                    src={barcodeApi.getQRCodeUrl(barcodeModalProduct.productId)}
                    alt="QR Code"
                    className="rounded-lg border"
                    style={{ imageRendering: 'pixelated' }}
                  />
                </div>
              </div>
              <div className="border-t pt-4">
                <p className="text-xs text-muted-foreground text-center mb-2">Code128 Barcode</p>
                <div className="flex justify-center">
                  <img
                    src={barcodeApi.getBarcodeUrl(barcodeModalProduct.productId)}
                    alt="Barcode"
                    className="rounded border"
                  />
                </div>
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  className="flex-1"
                  onClick={() => {
                    const a = document.createElement('a');
                    a.href = barcodeApi.getQRCodeUrl(barcodeModalProduct.productId, 500);
                    a.download = `qr-${barcodeModalProduct.productId}.png`;
                    a.click();
                  }}
                >
                  Download QR
                </Button>
                <Button
                  variant="outline"
                  className="flex-1"
                  onClick={() => {
                    const a = document.createElement('a');
                    a.href = barcodeApi.getBarcodeUrl(barcodeModalProduct.productId, 500, 120);
                    a.download = `barcode-${barcodeModalProduct.productId}.png`;
                    a.click();
                  }}
                >
                  Download Barcode
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
