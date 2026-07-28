import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';

export default function Settings() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Settings</h1>

      <Card>
        <CardHeader><CardTitle>Account</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div>
            <p className="text-sm text-muted-foreground">Username</p>
            <p className="font-medium">{user?.username}</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Email</p>
            <p className="font-medium">{user?.email}</p>
          </div>
          <Button variant="destructive" onClick={handleLogout}>Sign Out</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle>About</CardTitle></CardHeader>
        <CardContent className="space-y-2">
          <p className="text-sm"><strong>ArthurFord Business Manager</strong></p>
          <p className="text-sm text-muted-foreground">Gold Agent inventory, sales & customer management</p>
          <p className="text-xs text-muted-foreground">Version 1.0.0</p>
        </CardContent>
      </Card>
    </div>
  );
}
