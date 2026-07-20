import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { ThemeProvider } from '@/context/ThemeContext';
import { Toaster } from '@/components/ui/toaster';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { Loader2 } from 'lucide-react';

import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
import Inventory from '@/pages/Inventory';
import RecordSale from '@/pages/RecordSale';
import SalesHistory from '@/pages/SalesHistory';
import Reports from '@/pages/Reports';
import Users from '@/pages/Users';
import Customers from '@/pages/Customers';
import Expenses from '@/pages/Expenses';
import StockPlanner from '@/pages/StockPlanner';
import ReceiptScanner from '@/pages/ReceiptScanner';
import ProductHistory from '@/pages/ProductHistory';
import Procurement from '@/pages/Procurement';
import Layout from '@/components/Layout';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <div className="flex min-h-screen items-center justify-center"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <AuthProvider>
          <Router>
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                <Route index element={<Dashboard />} />
                <Route path="inventory" element={<Inventory />} />
                <Route path="inventory/:id/history" element={<ProductHistory />} />
                <Route path="record-sale" element={<RecordSale />} />
                <Route path="sales" element={<SalesHistory />} />
                <Route path="reports" element={<Reports />} />
                <Route path="users" element={<Users />} />
                <Route path="customers" element={<Customers />} />
                <Route path="expenses" element={<Expenses />} />
                <Route path="stock-planner" element={<StockPlanner />} />
                <Route path="receipt-scanner" element={<ReceiptScanner />} />
                <Route path="procurement" element={<Procurement />} />
              </Route>
            </Routes>
            <Toaster />
          </Router>
        </AuthProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}

export default App;
