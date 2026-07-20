import { useState } from 'react';
import { planningApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { formatCurrency } from '@/lib/utils';
import { Plus, Trash2, Calculator, TrendingUp } from 'lucide-react';

interface PlanItem { supplier: string; product: string; quantity: string; costPerItem: string; sellingPrice: string; }
const emptyItem = (): PlanItem => ({ supplier: '', product: '', quantity: '1', costPerItem: '', sellingPrice: '' });

export default function StockPlanner() {
  const [items, setItems] = useState<PlanItem[]>([emptyItem()]);
  const [calcResult, setCalcResult] = useState<any>(null);
  const [simResult, setSimResult] = useState<any>(null);
  const [availableCash, setAvailableCash] = useState('10000');
  const [cashInjected, setCashInjected] = useState('0');
  const [loading, setLoading] = useState(false);
  const { toast } = useToast();

  const updateItem = (i: number, field: keyof PlanItem, value: string) => {
    setItems(prev => prev.map((item, idx) => idx === i ? { ...item, [field]: value } : item));
  };

  const calculate = async () => {
    setLoading(true);
    try {
      const payload = items.filter(i => i.product.trim()).map(i => ({
        supplier: i.supplier, product: i.product, quantity: parseInt(i.quantity) || 1,
        costPerItem: parseFloat(i.costPerItem) || 0, sellingPrice: parseFloat(i.sellingPrice) || 0,
      }));
      const res = await planningApi.calculate(payload);
      setCalcResult(res.data);
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setLoading(false); }
  };

  const simulate = async () => {
    setLoading(true);
    try {
      const payload = {
        availableCash: parseFloat(availableCash) || 0,
        cashInjected: parseFloat(cashInjected) || 0,
        purchases: items.filter(i => i.product.trim()).map(i => ({
          product: i.product, quantity: parseInt(i.quantity) || 1,
          costPerItem: parseFloat(i.costPerItem) || 0,
        })),
      };
      const res = await planningApi.simulate(payload);
      setSimResult(res.data);
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setLoading(false); }
  };

  return (
    <div className="space-y-4 md:space-y-6">
      <div><h1 className="text-2xl font-bold tracking-tight md:text-3xl">Stock Purchase Planner</h1><p className="text-sm text-muted-foreground">Plan inventory purchases and simulate next month</p></div>

      <Card><CardHeader><CardTitle className="text-base">Purchase Items</CardTitle></CardHeader><CardContent>
        <div className="space-y-3">
          {items.map((item, i) => (
            <div key={i} className="rounded-lg border p-3 space-y-2">
              <div className="flex items-center justify-between"><span className="text-xs font-medium text-muted-foreground">Item {i + 1}</span>{items.length > 1 && <Button variant="ghost" size="icon" className="h-6 w-6" onClick={() => setItems(p => p.filter((_, idx) => idx !== i))}><Trash2 className="h-3 w-3 text-destructive" /></Button>}</div>
              <div className="grid grid-cols-2 gap-2"><div><Label className="text-[10px]">Supplier</Label><Input value={item.supplier} onChange={e => updateItem(i, 'supplier', e.target.value)} placeholder="Supplier" className="h-8 text-xs" /></div><div><Label className="text-[10px]">Product</Label><Input value={item.product} onChange={e => updateItem(i, 'product', e.target.value)} placeholder="Product name" className="h-8 text-xs" /></div></div>
              <div className="grid grid-cols-3 gap-2"><div><Label className="text-[10px]">Qty</Label><Input type="number" min="1" value={item.quantity} onChange={e => updateItem(i, 'quantity', e.target.value)} className="h-8 text-xs" /></div><div><Label className="text-[10px]">Cost/Item (R)</Label><Input type="number" step="0.01" min="0" value={item.costPerItem} onChange={e => updateItem(i, 'costPerItem', e.target.value)} className="h-8 text-xs" /></div><div><Label className="text-[10px]">Sell Price (R)</Label><Input type="number" step="0.01" min="0" value={item.sellingPrice} onChange={e => updateItem(i, 'sellingPrice', e.target.value)} className="h-8 text-xs" /></div></div>
            </div>
          ))}
          <Button variant="outline" size="sm" onClick={() => setItems(p => [...p, emptyItem()])} className="w-full"><Plus className="mr-1 h-3.5 w-3.5" />Add Item</Button>
        </div>
        <div className="mt-4 flex gap-2">
          <Button onClick={calculate} disabled={loading} className="flex-1"><Calculator className="mr-2 h-4 w-4" />Calculate</Button>
          <Button onClick={simulate} disabled={loading} variant="outline" className="flex-1"><TrendingUp className="mr-2 h-4 w-4" />Simulate</Button>
        </div>
      </CardContent></Card>

      {/* Cash Flow Simulation */}
      <Card><CardHeader><CardTitle className="text-base">Next Month Simulation</CardTitle></CardHeader><CardContent>
        <div className="grid grid-cols-2 gap-3 mb-4">
          <div className="space-y-1"><Label className="text-xs">Available Cash (R)</Label><Input type="number" step="0.01" value={availableCash} onChange={e => setAvailableCash(e.target.value)} /></div>
          <div className="space-y-1"><Label className="text-xs">Cash to Inject (R)</Label><Input type="number" step="0.01" value={cashInjected} onChange={e => setCashInjected(e.target.value)} /></div>
        </div>
        {simResult && (
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Money Available</p><p className="text-lg font-bold">{formatCurrency(simResult.totalMoneyAvailable)}</p></div>
              <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Money Needed</p><p className="text-lg font-bold">{formatCurrency(simResult.totalMoneyNeeded)}</p></div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className={`rounded-lg p-3 ${simResult.canAfford ? 'bg-green-50 dark:bg-green-900/20' : 'bg-red-50 dark:bg-red-900/20'}`}>
                <p className="text-xs text-muted-foreground">Remaining Balance</p>
                <p className={`text-lg font-bold ${simResult.canAfford ? 'text-green-600' : 'text-red-600'}`}>{formatCurrency(simResult.remainingBalance)}</p>
              </div>
              <div className={`rounded-lg p-3 ${simResult.shortfall > 0 ? 'bg-amber-50 dark:bg-amber-900/20' : 'bg-muted'}`}>
                <p className="text-xs text-muted-foreground">Shortfall</p>
                <p className={`text-lg font-bold ${simResult.shortfall > 0 ? 'text-amber-600' : 'text-muted-foreground'}`}>{formatCurrency(simResult.shortfall)}</p>
              </div>
            </div>
            <div className={`rounded-lg p-3 text-center ${simResult.canAfford ? 'bg-green-100 dark:bg-green-900/30' : 'bg-red-100 dark:bg-red-900/30'}`}>
              <p className={`text-sm font-medium ${simResult.canAfford ? 'text-green-800 dark:text-green-200' : 'text-red-800 dark:text-red-200'}`}>
                {simResult.canAfford ? '✓ Can afford all planned purchases' : '✗ Insufficient funds — need more capital'}
              </p>
            </div>
          </div>
        )}
      </CardContent></Card>

      {/* Calculation Results */}
      {calcResult && (
        <Card><CardHeader><CardTitle className="text-base">Calculation Results</CardTitle></CardHeader><CardContent>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4 mb-4">
            <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Total Cost</p><p className="text-lg font-bold">{formatCurrency(calcResult.totalCost)}</p></div>
            <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Expected Revenue</p><p className="text-lg font-bold text-green-600">{formatCurrency(calcResult.expectedRevenue)}</p></div>
            <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Expected Profit</p><p className="text-lg font-bold text-green-600">{formatCurrency(calcResult.expectedProfit)}</p></div>
            <div className="rounded-lg bg-muted p-3"><p className="text-xs text-muted-foreground">Profit Margin</p><p className="text-lg font-bold">{calcResult.overallMargin}%</p></div>
          </div>
          {calcResult.items && (
            <div className="space-y-2">
              {calcResult.items.map((item: any, i: number) => (
                <div key={i} className="flex items-center justify-between rounded-lg border p-2 text-sm">
                  <div><span className="font-medium">{item.product}</span><span className="ml-2 text-muted-foreground">x{item.quantity}</span></div>
                  <div className="text-right"><span className="font-medium">{formatCurrency(item.expectedRevenue)}</span><span className="ml-2 text-xs text-green-600">{item.profitMargin}% margin</span></div>
                </div>
              ))}
            </div>
          )}
        </CardContent></Card>
      )}
    </div>
  );
}
