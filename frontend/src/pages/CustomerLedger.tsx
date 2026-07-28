import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { customerApi } from '@/services/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatCurrency } from '@/lib/utils';
import { ArrowLeft, Printer, BookOpen, Wallet, CreditCard, Filter, ChevronLeft, ChevronRight } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import type {
  Customer,
  CustomerBalanceResponse,
  CustomerLedgerEntry,
  CustomerStatementResponse,
  SpringPage,
  CustomerLedgerEventType,
} from '@/types';

type LedgerFilter = 'ALL' | CustomerLedgerEventType;

const transactionTypeOptions: Array<{ value: LedgerFilter; label: string }> = [
  { value: 'ALL', label: 'All' },
  { value: 'SALE_RECORDED', label: 'Sales' },
  { value: 'PAYMENT_RECEIVED', label: 'Payments' },
  { value: 'SALE_REVERSED', label: 'Sale Reversals' },
  { value: 'PAYMENT_REVERSED', label: 'Payment Reversals' },
  { value: 'PURCHASE_RECORDED', label: 'Purchases' },
  { value: 'PURCHASE_CONFIRMED', label: 'Purchase Confirmed' },
  { value: 'PURCHASE_REVERSED', label: 'Purchase Reversed' },
  { value: 'INVENTORY_ADJUSTMENT', label: 'Inventory Adjustments' },
  { value: 'CUSTOMER_REFUND', label: 'Customer Refunds' },
];

function toDateTime(value: string, endOfDay = false) {
  if (!value) return undefined;
  const suffix = endOfDay ? 'T23:59:59' : 'T00:00:00';
  return `${value}${suffix}`;
}

export default function CustomerLedger() {
  const { id } = useParams();
  const customerId = Number(id);
  const { toast } = useToast();

  const [customer, setCustomer] = useState<Customer | null>(null);
  const [balance, setBalance] = useState<CustomerBalanceResponse | null>(null);
  const [ledgerPage, setLedgerPage] = useState<SpringPage<CustomerLedgerEntry> | null>(null);
  const [statement, setStatement] = useState<CustomerStatementResponse | null>(null);
  const [loadingCustomer, setLoadingCustomer] = useState(true);
  const [loadingLedger, setLoadingLedger] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [transactionType, setTransactionType] = useState<LedgerFilter>('ALL');

  useEffect(() => {
    if (!Number.isFinite(customerId)) return;
    setLoadingCustomer(true);
    Promise.all([
      customerApi.getById(customerId),
      customerApi.getBalance(customerId),
    ]).then(([customerRes, balanceRes]) => {
      setCustomer(customerRes.data);
      setBalance(balanceRes.data);
    }).catch((err) => {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    }).finally(() => setLoadingCustomer(false));
  }, [customerId, toast]);

  useEffect(() => {
    if (!Number.isFinite(customerId)) return;
    setLoadingLedger(true);
    const params = {
      page,
      size,
      startDate: toDateTime(dateFrom),
      endDate: toDateTime(dateTo, true),
      transactionType: transactionType === 'ALL' ? undefined : transactionType,
    };
    Promise.all([
      customerApi.getLedger(customerId, params),
      customerApi.getStatement(customerId, {
        startDate: toDateTime(dateFrom),
        endDate: toDateTime(dateTo, true),
        transactionType: transactionType === 'ALL' ? undefined : transactionType,
      }),
    ]).then(([ledgerRes, statementRes]) => {
      setLedgerPage(ledgerRes.data);
      setStatement(statementRes.data);
    }).catch((err) => {
      toast({ title: 'Error', description: err.message, variant: 'destructive' });
    }).finally(() => setLoadingLedger(false));
  }, [customerId, page, size, dateFrom, dateTo, transactionType, toast]);

  const printStatement = () => window.print();

  const isLoading = loadingCustomer || loadingLedger;

  if (isLoading) {
    return <div className="flex items-center justify-center h-64 text-muted-foreground">Loading...</div>;
  }

  if (!customer || !balance || !statement || !ledgerPage) {
    return <div className="text-center text-muted-foreground py-8">Customer not found</div>;
  }

  return (
    <div className="space-y-6 print:space-y-4">
      <div className="flex items-center justify-between print:hidden">
        <div className="flex items-center gap-3">
          <Button asChild variant="outline">
            <Link to="/customers">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold">Customer Ledger</h1>
            <p className="text-sm text-muted-foreground">{customer.name}</p>
          </div>
        </div>
        <Button onClick={printStatement}>
          <Printer className="mr-2 h-4 w-4" />
          Print Statement
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-muted-foreground">Outstanding Balance</CardTitle>
          </CardHeader>
          <CardContent className="text-2xl font-bold">{formatCurrency(balance.outstandingBalance)}</CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-muted-foreground">Total Purchases</CardTitle>
          </CardHeader>
          <CardContent className="text-2xl font-bold">{formatCurrency(balance.totalPurchases)}</CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-muted-foreground">Total Payments</CardTitle>
          </CardHeader>
          <CardContent className="text-2xl font-bold">{formatCurrency(balance.totalPayments)}</CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-muted-foreground">Last Payment</CardTitle>
          </CardHeader>
          <CardContent className="text-base font-semibold">
            {balance.lastPaymentDate ? new Date(balance.lastPaymentDate).toLocaleString() : 'No payments yet'}
          </CardContent>
        </Card>
      </div>

      <Card className="print:border-0 print:shadow-none">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BookOpen className="h-5 w-5" />
            Statement Overview
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-4">
          <div>
            <p className="text-xs text-muted-foreground">Business</p>
            <p className="font-medium">{statement.businessName}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Customer</p>
            <p className="font-medium">{statement.customerName}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Opening Balance</p>
            <p className="font-medium">{formatCurrency(statement.openingBalance)}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Closing Balance</p>
            <p className="font-medium">{formatCurrency(statement.closingBalance)}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Total Debits</p>
            <p className="font-medium">{formatCurrency(statement.totalDebits)}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Total Credits</p>
            <p className="font-medium">{formatCurrency(statement.totalCredits)}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Transactions</p>
            <p className="font-medium">{statement.transactionCount}</p>
          </div>
          <div>
            <p className="text-xs text-muted-foreground">Generated</p>
            <p className="font-medium">{new Date(statement.generatedDate).toLocaleString()}</p>
          </div>
        </CardContent>
      </Card>

      <Card className="print:border-0 print:shadow-none">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-5">
          <div>
            <Label>From</Label>
            <Input
              type="date"
              value={dateFrom}
              onChange={(e) => {
                setPage(0);
                setDateFrom(e.target.value);
              }}
            />
          </div>
          <div>
            <Label>To</Label>
            <Input
              type="date"
              value={dateTo}
              onChange={(e) => {
                setPage(0);
                setDateTo(e.target.value);
              }}
            />
          </div>
          <div>
            <Label>Transaction Type</Label>
            <select
              className="w-full rounded-md border bg-background px-3 py-2 text-sm"
              value={transactionType}
              onChange={(e) => {
                setPage(0);
                setTransactionType(e.target.value as LedgerFilter);
              }}
            >
              {transactionTypeOptions.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </div>
          <div>
            <Label>Page Size</Label>
            <select
              className="w-full rounded-md border bg-background px-3 py-2 text-sm"
              value={size}
              onChange={(e) => {
                setPage(0);
                setSize(Number(e.target.value));
              }}
            >
              {[10, 20, 50, 100].map((value) => (
                <option key={value} value={value}>{value}</option>
              ))}
            </select>
          </div>
          <div className="flex items-end">
            <Button
              variant="outline"
              className="w-full"
              onClick={() => {
                setPage(0);
                setDateFrom('');
                setDateTo('');
                setTransactionType('ALL');
              }}
            >
              Reset Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card className="print:border-0 print:shadow-none">
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Wallet className="h-5 w-5" />
            Customer Ledger
          </CardTitle>
          <div className="text-sm text-muted-foreground">
            Page {ledgerPage.number + 1} of {ledgerPage.totalPages || 1}
          </div>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b text-left text-muted-foreground">
                  <th className="py-2 pr-4 font-medium">Date</th>
                  <th className="py-2 pr-4 font-medium">Event</th>
                  <th className="py-2 pr-4 font-medium">Description</th>
                  <th className="py-2 pr-4 font-medium text-right">Debit</th>
                  <th className="py-2 pr-4 font-medium text-right">Credit</th>
                  <th className="py-2 pr-4 font-medium text-right">Running Balance</th>
                  <th className="py-2 pr-4 font-medium">Ref</th>
                </tr>
              </thead>
              <tbody>
                {ledgerPage.content.map((entry, index) => (
                  <tr key={`${entry.businessEventId}-${index}`} className="border-b last:border-0">
                    <td className="py-2 pr-4 whitespace-nowrap">{new Date(entry.date).toLocaleString()}</td>
                    <td className="py-2 pr-4 whitespace-nowrap">{entry.eventType.replace('_', ' ')}</td>
                    <td className="py-2 pr-4">{entry.description}</td>
                    <td className="py-2 pr-4 text-right">{entry.debit > 0 ? formatCurrency(entry.debit) : '-'}</td>
                    <td className="py-2 pr-4 text-right">{entry.credit > 0 ? formatCurrency(entry.credit) : '-'}</td>
                    <td className="py-2 pr-4 text-right font-semibold">{formatCurrency(entry.runningBalance)}</td>
                    <td className="py-2 pr-4 whitespace-nowrap">{entry.referenceId ?? '-'}</td>
                  </tr>
                ))}
                {ledgerPage.content.length === 0 && (
                  <tr>
                    <td colSpan={7} className="py-6 text-center text-muted-foreground">No ledger entries match the current filters</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <div className="mt-4 flex items-center justify-between gap-3 print:hidden">
            <p className="text-sm text-muted-foreground">
              {ledgerPage.totalElements} transactions
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={ledgerPage.first}
              >
                <ChevronLeft className="mr-1 h-4 w-4" />
                Prev
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage((p) => p + 1)}
                disabled={ledgerPage.last}
              >
                Next
                <ChevronRight className="ml-1 h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="print:border-0 print:shadow-none">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <CreditCard className="h-5 w-5" />
            Printable Statement
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <p className="text-sm text-muted-foreground">
            Generated {new Date(statement.generatedDate).toLocaleString()}
          </p>
          <div className="rounded-lg border p-4">
            <p className="font-semibold">{statement.businessName}</p>
            <p className="text-sm">{statement.customerName}</p>
            <p className="text-sm text-muted-foreground">Period: {statement.statementPeriod}</p>
            <p className="mt-3 text-sm">Opening Balance: {formatCurrency(statement.openingBalance)}</p>
            <p className="text-sm">Closing Balance: {formatCurrency(statement.closingBalance)}</p>
            <p className="text-sm">Total Debits: {formatCurrency(statement.totalDebits)}</p>
            <p className="text-sm">Total Credits: {formatCurrency(statement.totalCredits)}</p>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b text-left text-muted-foreground">
                  <th className="py-2 pr-4 font-medium">Date</th>
                  <th className="py-2 pr-4 font-medium">Event</th>
                  <th className="py-2 pr-4 font-medium">Description</th>
                  <th className="py-2 pr-4 font-medium text-right">Debit</th>
                  <th className="py-2 pr-4 font-medium text-right">Credit</th>
                  <th className="py-2 pr-4 font-medium text-right">Running Balance</th>
                </tr>
              </thead>
              <tbody>
                {statement.transactions.map((entry, index) => (
                  <tr key={`${entry.businessEventId}-${index}`} className="border-b last:border-0">
                    <td className="py-2 pr-4 whitespace-nowrap">{new Date(entry.date).toLocaleString()}</td>
                    <td className="py-2 pr-4 whitespace-nowrap">{entry.eventType.replace('_', ' ')}</td>
                    <td className="py-2 pr-4">{entry.description}</td>
                    <td className="py-2 pr-4 text-right">{entry.debit > 0 ? formatCurrency(entry.debit) : '-'}</td>
                    <td className="py-2 pr-4 text-right">{entry.credit > 0 ? formatCurrency(entry.credit) : '-'}</td>
                    <td className="py-2 pr-4 text-right font-semibold">{formatCurrency(entry.runningBalance)}</td>
                  </tr>
                ))}
                {statement.transactions.length === 0 && (
                  <tr>
                    <td colSpan={6} className="py-6 text-center text-muted-foreground">No transactions in statement period</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
