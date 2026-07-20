import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/ThemeToggle';
import {
  LayoutDashboard, Package, ShoppingCart, History, BarChart3, Users, DollarSign,
  LogOut, Loader2, Menu, X, Calculator, ScanLine, ClipboardList
} from 'lucide-react';
import { useState, useEffect } from 'react';

export default function Layout() {
  const { user, logout, hasRole, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => { setSidebarOpen(false); }, [location.pathname]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try { await logout(); } finally { setIsLoggingOut(false); navigate('/login'); }
  };

  if (isLoading) return <div className="flex min-h-screen items-center justify-center"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>;

  const navItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/inventory', icon: Package, label: 'Inventory' },
    { to: '/record-sale', icon: ShoppingCart, label: 'Record Sale' },
    { to: '/sales', icon: History, label: 'Sales' },
    { to: '/customers', icon: Users, label: 'Customers' },
    { to: '/reports', icon: BarChart3, label: 'Reports' },
    { to: '/expenses', icon: DollarSign, label: 'Finance' },
    ...(hasRole('MANAGER') || hasRole('ADMIN') ? [
      { to: '/stock-planner', icon: Calculator, label: 'Planner' },
      { to: '/procurement', icon: ClipboardList, label: 'Procurement' },
      { to: '/receipt-scanner', icon: ScanLine, label: 'Receipts' },
    ] : []),
    ...(hasRole('ADMIN') ? [{ to: '/users', icon: Users, label: 'Users' }] : []),
  ];

  const NavItems = () => (
    <nav className="flex-1 space-y-1 p-3 md:p-4">
      {navItems.map(item => (
        <NavLink key={item.to} to={item.to} end={item.to === '/'}
          className={({ isActive }) => `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${isActive ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'}`}>
          <item.icon className="h-5 w-5" />{item.label}
        </NavLink>
      ))}
    </nav>
  );

  const UserInfo = () => (
    <div className="border-t p-4">
      <div className="mb-3 px-3">
        <p className="text-sm font-medium">{user?.username}</p>
        <p className="text-xs text-muted-foreground capitalize">{user?.role?.toLowerCase().replace('_', ' ')}</p>
      </div>
      <Button variant="outline" className="w-full" onClick={handleLogout} disabled={isLoggingOut}>
        {isLoggingOut ? <><Loader2 className="mr-2 h-4 w-4 animate-spin" />Logging out...</> : <><LogOut className="mr-2 h-4 w-4" />Logout</>}
      </Button>
    </div>
  );

  return (
    <div className="min-h-screen bg-background pb-16 md:pb-0 md:ml-64">
      {/* Mobile Header */}
      <header className="sticky top-0 z-50 flex h-14 items-center justify-between border-b bg-card px-4 md:hidden">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => setSidebarOpen(!sidebarOpen)} className="h-9 w-9">
            {sidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
          <h1 className="text-lg font-bold text-primary">Perfume Stock</h1>
        </div>
        <ThemeToggle />
      </header>

      {/* Mobile Sidebar */}
      {sidebarOpen && <div className="fixed inset-0 z-40 bg-black/50 md:hidden" onClick={() => setSidebarOpen(false)} />}
      <aside className={`fixed left-0 top-0 z-50 h-full w-64 border-r bg-card transition-transform duration-200 md:hidden ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex h-full flex-col">
          <div className="flex h-14 items-center justify-between border-b px-4">
            <h1 className="text-lg font-bold text-primary">Perfume Stock</h1>
            <Button variant="ghost" size="icon" onClick={() => setSidebarOpen(false)} className="h-8 w-8"><X className="h-4 w-4" /></Button>
          </div>
          <ThemeToggle />
          <NavItems />
          <UserInfo />
        </div>
      </aside>

      {/* Desktop Sidebar */}
      <aside className="fixed left-0 top-0 z-40 hidden h-screen w-64 border-r bg-card md:block">
        <div className="flex h-full flex-col">
          <div className="flex h-16 items-center border-b px-6"><h1 className="text-xl font-bold text-primary">Perfume Stock</h1></div>
          <ThemeToggle />
          <NavItems />
          <UserInfo />
        </div>
      </aside>

      {/* Mobile Bottom Nav */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 border-t bg-card md:hidden">
        <div className="flex items-center justify-around">
          {navItems.slice(0, 5).map(item => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'}
              className={({ isActive }) => `flex flex-col items-center gap-0.5 px-2 py-2 text-[10px] font-medium transition-colors ${isActive ? 'text-primary' : 'text-muted-foreground'}`}>
              <item.icon className="h-5 w-5" />{item.label}
            </NavLink>
          ))}
        </div>
      </nav>

      <main className="p-4 md:p-8"><Outlet /></main>
    </div>
  );
}
