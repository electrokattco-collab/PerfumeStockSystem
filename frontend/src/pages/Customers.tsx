import { useEffect, useState, useCallback } from 'react';
import { customerApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatCurrency } from '@/lib/utils';
import { Search, Plus, X, BookOpen } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type { Customer } from '@/types';
import { Link } from 'react-router-dom';

export default function Customers() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ name: '', phone: '', address: '', notes: '' });
  const { toast } = useToast();

  useEffect(() => { const t = setTimeout(() => setDebouncedSearch(search), 300); return () => clearTimeout(t); }, [search]);

  const fetchCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const res = debouncedSearch
        ? await customerApi.search(debouncedSearch, { page: 0, size: 100 })
        : await customerApi.getAll({ page: 0, size: 100 });
      setCustomers(res.data.content);
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setLoading(false); }
  }, [debouncedSearch, toast]);

  useEffect(() => { fetchCustomers(); }, [fetchCustomers]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await customerApi.create(form);
      toast({ title: 'Customer created' });
      setShowForm(false);
      setForm({ name: '', phone: '', address: '', notes: '' });
      fetchCustomers();
    } catch (err: any) { toast({ title: 'Error', description: err.message, variant: 'destructive' }); }
    finally { setSaving(false); }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Customers</h1>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? <X className="mr-2 h-4 w-4" /> : <Plus className="mr-2 h-4 w-4" />}
          {showForm ? 'Cancel' : 'Add Customer'}
        </Button>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
        <Input className="pl-9" placeholder="Search customers..." value={search} onChange={(e) => setSearch(e.target.value)} />
      </div>

      {showForm && (
        <Card>
          <CardHeader><CardTitle>Add Customer</CardTitle></CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div><Label>Name</Label><Input value={form.name} onChange={(e) => setForm({...form, name: e.target.value})} required /></div>
                <div><Label>Phone</Label><Input value={form.phone} onChange={(e) => setForm({...form, phone: e.target.value})} /></div>
              </div>
              <div><Label>Address</Label><Input value={form.address} onChange={(e) => setForm({...form, address: e.target.value})} /></div>
              <div><Label>Notes</Label><Input value={form.notes} onChange={(e) => setForm({...form, notes: e.target.value})} /></div>
              <Button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save Customer'}</Button>
            </form>
          </CardContent>
        </Card>
      )}

      {loading ? <div className="text-center py-8 text-muted-foreground">Loading...</div> : (
        <div className="space-y-3">
          {customers.map((c) => (
            <Card key={c.id} className={c.outstandingBalance > 0 ? 'border-red-300' : ''}>
              <CardContent className="pt-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">{c.name}</p>
                    <p className="text-sm text-muted-foreground">{c.phone || 'No phone'}</p>
                  </div>
                  <div className="text-right">
                    {c.outstandingBalance > 0 ? (
                      <p className="text-red-600 font-bold">{formatCurrency(c.outstandingBalance)}</p>
                    ) : (
                      <p className="text-green-600 text-sm">No balance</p>
                    )}
                    <Button asChild variant="outline" size="sm" className="mt-2">
                      <Link to={`/customers/${c.id}/ledger`}>
                        <BookOpen className="mr-2 h-4 w-4" />
                        Ledger
                      </Link>
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
          {customers.length === 0 && <p className="text-center text-muted-foreground py-8">No customers yet</p>}
        </div>
      )}
    </div>
  );
}
