import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { Toaster } from '@/components/ui/toaster';
import { ErrorBoundary } from '@/components/ErrorBoundary';

import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
import Purchases from '@/pages/Purchases';
import Inventory from '@/pages/Inventory';
import Sales from '@/pages/Sales';
import Customers from '@/pages/Customers';
import CustomerLedger from '@/pages/CustomerLedger';
import Payments from '@/pages/Payments';
import Reports from '@/pages/Reports';
import Settings from '@/pages/Settings';
import Layout from '@/components/Layout';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <div className="flex min-h-screen items-center justify-center">Loading...</div>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
              <Route index element={<Dashboard />} />
              <Route path="purchases" element={<Purchases />} />
              <Route path="inventory" element={<Inventory />} />
              <Route path="sales" element={<Sales />} />
              <Route path="customers" element={<Customers />} />
              <Route path="customers/:id/ledger" element={<CustomerLedger />} />
              <Route path="payments" element={<Payments />} />
              <Route path="reports" element={<Reports />} />
              <Route path="settings" element={<Settings />} />
            </Route>
          </Routes>
          <Toaster />
        </Router>
      </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;
