import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import {
  LayoutDashboard, Package, ShoppingCart, Users, BarChart3, DollarSign,
  LogOut, Menu, X, ClipboardList, Settings
} from 'lucide-react';
import { useState, useEffect } from 'react';

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/purchases', icon: ClipboardList, label: 'Purchases' },
  { to: '/inventory', icon: Package, label: 'Inventory' },
  { to: '/sales', icon: ShoppingCart, label: 'Sales' },
  { to: '/customers', icon: Users, label: 'Customers' },
  { to: '/payments', icon: DollarSign, label: 'Payments' },
  { to: '/reports', icon: BarChart3, label: 'Reports' },
  { to: '/settings', icon: Settings, label: 'Settings' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = window.location;

  useEffect(() => { setSidebarOpen(false); }, [location.pathname]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-background pb-16 md:pb-0 md:ml-64">
      {/* Mobile Header */}
      <header className="sticky top-0 z-50 flex h-14 items-center justify-between border-b bg-card px-4 md:hidden">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => setSidebarOpen(!sidebarOpen)}>
            {sidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
          <h1 className="text-lg font-semibold">ArthurFord Manager</h1>
        </div>
      </header>

      {/* Mobile Sidebar Overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-40 bg-black/50 md:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 border-r bg-card transition-transform md:translate-x-0 ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex h-full flex-col">
          <div className="flex h-14 items-center border-b px-4">
            <h1 className="text-lg font-bold text-primary">ArthurFord Manager</h1>
          </div>
          <nav className="flex-1 space-y-1 p-3">
            {navItems.map((item) => (
              <NavLink key={item.to} to={item.to} end={item.to === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${
                    isActive ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-accent'
                  }`
                }>
                <item.icon className="h-5 w-5" />{item.label}
              </NavLink>
            ))}
          </nav>
          <div className="border-t p-4">
            <p className="px-3 text-sm font-medium">{user?.username}</p>
            <Button variant="outline" className="mt-3 w-full" onClick={handleLogout}>
              <LogOut className="mr-2 h-4 w-4" />Logout
            </Button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="p-4 md:p-6">
        <Outlet />
      </main>

      {/* Mobile Bottom Nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 flex border-t bg-card md:hidden">
        {navItems.slice(0, 5).map((item) => (
          <NavLink key={item.to} to={item.to} end={item.to === '/'}
            className={({ isActive }) =>
              `flex flex-1 flex-col items-center gap-1 py-2 text-xs ${
                isActive ? 'text-primary' : 'text-muted-foreground'
              }`
            }>
            <item.icon className="h-5 w-5" />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
