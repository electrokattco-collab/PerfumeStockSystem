import { useEffect, useState } from 'react';
import { paymentApi, customerApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatCurrency } from '@/lib/utils';
import { DollarSign } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type { Customer, Payment, PaymentMethod } from '@/types';

export default function Payments() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<number | ''>('');
  const [payments, setPayments] = useState<Payment[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState<{ amount: string; paymentMethod: PaymentMethod; notes: string }>({
    amount: '',
    paymentMethod: 'CASH',
    notes: '',
  });
  const { toast } = useToast();

  useEffect(() => {
    customerApi.getDebtors().then((res) => {
      setCustomers(res.data);
      if (res.data.length > 0) setSelectedCustomer(res.data[0].id);
    }).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (selectedCustomer) {
      paymentApi.getByCustomer(Number(selectedCustomer)).then((res) => setPayments(res.data));
    }
  }, [selectedCustomer]);

  const handlePayment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCustomer) return;
    setSaving(true);
    try {
      await paymentApi.record({
        customerId: Number(selectedCustomer),
        amount: Number(form.amount),
        paymentMethod: form.paymentMethod,
        notes: form.notes,
      });
      toast({ title: 'Payment recorded' });
      setForm({ amount: '', paymentMethod: 'CASH', notes: '' });
      const res = await customerApi.getDebtors();
      setCustomers(res.data);
      const pRes = await paymentApi.getByCustomer(Number(selectedCustomer));
      setPayments(pRes.data);
    } catch (err: any) {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    } finally { setSaving(false); }
  };

  const selectedCustomerData = customers.find(c => c.id === Number(selectedCustomer));

  if (loading) return <div className="flex items-center justify-center h-64 text-muted-foreground">Loading...</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Payments</h1>

      <div className="flex gap-2 items-center">
        <Label className="text-sm">Customer:</Label>
        <select className="rounded-md border bg-background px-3 py-2 text-sm" value={selectedCustomer}
          onChange={(e) => setSelectedCustomer(Number(e.target.value))}>
          {customers.map(c => <option key={c.id} value={c.id}>{c.name} - Owing {formatCurrency(c.outstandingBalance)}</option>)}
        </select>
      </div>

      {selectedCustomerData && selectedCustomerData.outstandingBalance > 0 && (
        <Card>
          <CardHeader><CardTitle>Record Payment</CardTitle></CardHeader>
          <CardContent>
            <form onSubmit={handlePayment} className="space-y-4">
              <div className="grid grid-cols-3 gap-4">
                <div><Label>Amount (R)</Label><Input type="number" step="0.01" min="0.01" max={selectedCustomerData.outstandingBalance} value={form.amount} onChange={(e) => setForm({...form, amount: e.target.value})} required /></div>
                <div>
                  <Label>Method</Label>
                  <select
                    className="w-full rounded-md border bg-background px-3 py-2 text-sm"
                    value={form.paymentMethod}
                    onChange={(e) => setForm({ ...form, paymentMethod: e.target.value as PaymentMethod })}
                  >
                    <option value="CASH">Cash</option>
                    <option value="EFT">EFT</option>
                    <option value="TRANSFER">Transfer</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                <div><Label>Notes</Label><Input value={form.notes} onChange={(e) => setForm({...form, notes: e.target.value})} placeholder="Optional" /></div>
              </div>
              <Button type="submit" disabled={saving}>
                <DollarSign className="mr-2 h-4 w-4" />
                {saving ? 'Processing...' : 'Record Payment'}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader><CardTitle>Payment History</CardTitle></CardHeader>
        <CardContent>
          {payments.length > 0 ? (
            <div className="space-y-2">
              {payments.map((p) => (
                <div key={p.id} className="flex items-center justify-between border-b pb-2 last:border-0">
                  <div>
                    <p className="font-medium">{formatCurrency(p.amount)}</p>
                    <p className="text-xs text-muted-foreground">{p.paymentMethod} - {new Date(p.createdAt).toLocaleDateString()}</p>
                  </div>
                  {p.notes && <p className="text-xs text-muted-foreground">{p.notes}</p>}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No payments recorded</p>
          )}
        </CardContent>
      </Card>

      {customers.length === 0 && (
        <Card>
          <CardContent className="pt-4 text-center text-muted-foreground">
            No customers with outstanding balances
          </CardContent>
        </Card>
      )}
    </div>
  );
}
