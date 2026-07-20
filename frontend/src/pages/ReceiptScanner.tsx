import { useState, useCallback } from 'react';
import { receiptApi } from '@/services/api';
import { PurchaseReceipt, PaginatedResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';
import { Upload, Camera, Check, FileText, Trash2, Plus } from 'lucide-react';
import { EmptyState } from '@/components/EmptyState';
import { Pagination } from '@/components/Pagination';

export default function ReceiptScanner() {
  const [tab, setTab] = useState<'scan' | 'history'>('scan');
  const [scanning, setScanning] = useState(false);
  const [ocrResult, setOcrResult] = useState<any>(null);
  const [extractedItems, setExtractedItems] = useState<{ productName: string; quantity: number; unitCost: number; totalCost: number }[]>([]);
  const [supplierName, setSupplierName] = useState('');
  const [receipts, setReceipts] = useState<PurchaseReceipt[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const { toast } = useToast();

  const fetchReceipts = useCallback(async () => {
    try {
      const res = await receiptApi.getAll({ page, size: 20 });
      const data: PaginatedResponse<PurchaseReceipt> = res.data;
      setReceipts(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch { /* silent */ }
  }, [page]);

  const handleScan = async (file: File) => {
    setScanning(true); setOcrResult(null);
    try {
      const res = await receiptApi.scan(file);
      setOcrResult(res.data);
      setSupplierName(res.data.supplier || '');
      setExtractedItems((res.data.items || []).map((item: any) => ({
        productName: item.productName || '',
        quantity: item.quantity || 1,
        unitCost: parseFloat(item.unitCost) || 0,
        totalCost: parseFloat(item.totalCost) || 0,
      })));
      toast({ title: 'Receipt scanned', description: `Found ${res.data.itemCount || 0} items` });
    } catch (err: any) { toast({ title: 'Scan failed', description: err.message, variant: 'destructive' }); }
    finally { setScanning(false); }
  };

  const handleSaveReceipt = async () => {
    try {
      const receipt = {
        supplierName, totalAmount: extractedItems.reduce((s, i) => s + i.totalCost, 0),
        subtotal: extractedItems.reduce((s, i) => s + i.totalCost, 0), taxAmount: 0,
        ocrRawText: ocrResult?.rawText || '', status: 'PENDING',
      };
      const res = await receiptApi.create(receipt);
      if (extractedItems.length > 0) {
        await receiptApi.updateItems(res.data.id, extractedItems);
      }
      toast({ title: 'Receipt saved' }); setOcrResult(null); setExtractedItems([]);
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
  };

  const handleProcessReceipt = async (id: number) => {
    try { await receiptApi.process(id); toast({ title: 'Receipt processed — stock updated' }); fetchReceipts(); }
    catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
  };

  const handleFileDrop = (e: React.DragEvent) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file && file.type.startsWith('image/')) handleScan(file);
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Purchase Receipt Scanner</h1><p className="text-sm text-muted-foreground">Scan supplier receipts with OCR</p></div>
      <div className="flex gap-1 rounded-lg border bg-muted p-1">
        {(['scan', 'history'] as const).map(t => (
          <button key={t} onClick={() => { setTab(t); if (t === 'history') fetchReceipts(); }} className={`flex-1 rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${tab === t ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground'}`}>{t === 'scan' ? 'Scan Receipt' : 'Receipt History'}</button>
        ))}
      </div>

      {tab === 'scan' && (
        <div className="space-y-4">
          <Card onDragOver={e => e.preventDefault()} onDrop={handleFileDrop}>
            <CardHeader><CardTitle className="text-base">Upload Receipt</CardTitle></CardHeader>
            <CardContent>
              <div className="rounded-lg border-2 border-dashed p-8 text-center">
                <Upload className="mx-auto h-12 w-12 text-muted-foreground mb-3" />
                <p className="text-sm text-muted-foreground mb-3">Drag & drop a receipt image, or</p>
                <label className="inline-flex cursor-pointer items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm text-primary-foreground hover:bg-primary/90">
                  <Camera className="h-4 w-4" />Choose File
                  <input type="file" accept="image/*" className="hidden" onChange={e => { const f = e.target.files?.[0]; if (f) handleScan(f); }} />
                </label>
                <p className="mt-2 text-xs text-muted-foreground">Supports JPG, PNG, WebP</p>
              </div>
              {scanning && <div className="mt-4 text-center"><div className="h-2 w-full animate-pulse rounded bg-primary/20" /><p className="mt-2 text-sm text-muted-foreground">Processing receipt with OCR...</p></div>}
            </CardContent>
          </Card>

          {/* OCR Results */}
          {ocrResult && (
            <Card><CardHeader><CardTitle className="text-base">Extracted Data</CardTitle></CardHeader><CardContent>
              <div className="space-y-3">
                <div className="space-y-1"><Label className="text-xs">Supplier Name</Label><Input value={supplierName} onChange={e => setSupplierName(e.target.value)} /></div>
                {ocrResult.rawText && <details className="rounded-lg border p-3"><summary className="text-sm font-medium cursor-pointer">Raw OCR Text</summary><pre className="mt-2 text-xs text-muted-foreground whitespace-pre-wrap max-h-40 overflow-y-auto">{ocrResult.rawText}</pre></details>}
                <div className="space-y-2">
                  <Label className="text-xs font-medium">Line Items ({extractedItems.length})</Label>
                  {extractedItems.map((item, i) => (
                    <div key={i} className="flex items-center gap-2 rounded border p-2">
                      <Input value={item.productName} onChange={e => { const newItems = [...extractedItems]; newItems[i].productName = e.target.value; setExtractedItems(newItems); }} className="flex-1 h-8 text-xs" placeholder="Product" />
                      <Input type="number" min="1" value={item.quantity} onChange={e => { const newItems = [...extractedItems]; newItems[i].quantity = parseInt(e.target.value) || 1; newItems[i].totalCost = newItems[i].unitCost * newItems[i].quantity; setExtractedItems(newItems); }} className="w-16 h-8 text-xs" />
                      <Input type="number" step="0.01" value={item.unitCost} onChange={e => { const newItems = [...extractedItems]; newItems[i].unitCost = parseFloat(e.target.value) || 0; newItems[i].totalCost = newItems[i].unitCost * newItems[i].quantity; setExtractedItems(newItems); }} className="w-24 h-8 text-xs" />
                      <span className="text-xs font-medium w-20 text-right">{formatCurrency(item.totalCost)}</span>
                      <Button variant="ghost" size="icon" className="h-6 w-6" onClick={() => setExtractedItems(p => p.filter((_, idx) => idx !== i))}><Trash2 className="h-3 w-3 text-destructive" /></Button>
                    </div>
                  ))}
                  <Button variant="outline" size="sm" onClick={() => setExtractedItems(p => [...p, { productName: '', quantity: 1, unitCost: 0, totalCost: 0 }])} className="w-full"><Plus className="mr-1 h-3 w-3" />Add Item</Button>
                </div>
                <div className="flex items-center justify-between rounded-lg bg-muted p-3"><span className="text-sm font-medium">Total</span><span className="text-lg font-bold">{formatCurrency(extractedItems.reduce((s, i) => s + i.totalCost, 0))}</span></div>
                <div className="flex gap-2">
                  <Button variant="outline" className="flex-1" onClick={() => { setOcrResult(null); setExtractedItems([]); }}>Cancel</Button>
                  <Button className="flex-1" onClick={handleSaveReceipt}><Check className="mr-1 h-4 w-4" />Save Receipt</Button>
                </div>
              </div>
            </CardContent></Card>
          )}
        </div>
      )}

      {tab === 'history' && (
        <Card><CardHeader><CardTitle className="text-base">Receipt History ({totalElements})</CardTitle></CardHeader><CardContent>
          {receipts.length === 0 ? <EmptyState icon={<FileText className="h-8 w-8 text-muted-foreground" />} title="No receipts" description="Scan your first receipt" /> : (
            <div className="space-y-2">
              {receipts.map(r => (
                <div key={r.id} className="flex items-center justify-between rounded-lg border p-3">
                  <div><div className="text-sm font-medium">{r.supplierName || 'Unknown supplier'}</div><div className="text-xs text-muted-foreground">{formatCurrency(r.totalAmount || 0)} · {new Date(r.createdAt).toLocaleDateString('en-ZA')}</div></div>
                  <div className="flex items-center gap-2">
                    <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-medium ${r.status === 'PROCESSED' ? 'bg-green-100 text-green-800' : r.status === 'REJECTED' ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'}`}>{r.status}</span>
                    {r.status === 'PENDING' && <Button variant="outline" size="sm" className="h-7 text-xs" onClick={() => handleProcessReceipt(r.id)}>Process</Button>}
                  </div>
                </div>
              ))}
              <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
            </div>
          )}
        </CardContent></Card>
      )}
    </div>
  );
}
