import { useEffect, useState, useCallback } from 'react';
import { userApi } from '@/services/api';
import { User, PaginatedResponse } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/hooks/use-toast';
import { SkeletonTable } from '@/components/LoadingSkeleton';
import { Pagination } from '@/components/Pagination';

export default function Users() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const response = await userApi.getAll({ page, size: 20, sort: 'id', direction: 'asc' });
      const data: PaginatedResponse<User> = response.data;
      setUsers(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (error) {
      console.error('Failed to fetch users:', error);
    } finally {
      setLoading(false);
    }
  }, [page, toast]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleToggleActive = async (user: User) => {
    try {
      if (user.active) {
        await userApi.delete(user.id);
        toast({ title: 'User deactivated' });
      } else {
        await userApi.activate(user.id);
        toast({ title: 'User activated' });
      }
      fetchUsers();
    } catch (error: any) {
      toast({ title: 'Error', description: error.response?.data?.message || 'Failed to update user', variant: 'destructive' });
    }
  };

  if (loading) {
    return (
      <div className="space-y-4 md:space-y-6">
        <div><div className="h-8 w-36 animate-pulse rounded bg-muted" /></div>
        <SkeletonTable rows={5} />
      </div>
    );
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight md:text-3xl">User Management</h1>
        <p className="text-sm text-muted-foreground md:text-base">{totalElements} users</p>
      </div>
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-base md:text-lg">All Users</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="hidden overflow-x-auto md:block">
            <table className="w-full text-sm">
              <thead><tr className="border-b">
                <th className="py-3 px-2 text-left font-medium">Username</th>
                <th className="py-3 px-2 text-left font-medium">Email</th>
                <th className="py-3 px-2 text-left font-medium">Role</th>
                <th className="py-3 px-2 text-left font-medium">Status</th>
                <th className="py-3 px-2 text-right font-medium">Actions</th>
              </tr></thead>
              <tbody>{users.map((user) => (
                <tr key={user.id} className="border-b">
                  <td className="py-3 px-2 font-medium">{user.username}</td>
                  <td className="py-3 px-2 text-muted-foreground">{user.email}</td>
                  <td className="py-3 px-2"><span className="inline-flex rounded-full bg-secondary px-2 py-1 text-xs font-medium capitalize">{user.role.toLowerCase().replace('_', ' ')}</span></td>
                  <td className="py-3 px-2"><span className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${user.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>{user.active ? 'Active' : 'Inactive'}</span></td>
                  <td className="py-3 px-2 text-right">
                    <Button variant={user.active ? 'destructive' : 'outline'} size="sm" onClick={() => handleToggleActive(user)}>{user.active ? 'Deactivate' : 'Activate'}</Button>
                  </td>
                </tr>
              ))}</tbody>
            </table>
          </div>
          <div className="space-y-3 md:hidden">{users.map((user) => (
            <div key={user.id} className="rounded-lg border p-3">
              <div className="mb-2 flex items-start justify-between">
                <div><div className="text-sm font-medium">{user.username}</div><div className="text-xs text-muted-foreground truncate max-w-[200px]">{user.email}</div></div>
                <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-medium ${user.active ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>{user.active ? 'Active' : 'Inactive'}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="inline-flex rounded-full bg-secondary px-2 py-0.5 text-[10px] font-medium capitalize">{user.role.toLowerCase().replace('_', ' ')}</span>
                <Button variant={user.active ? 'destructive' : 'outline'} size="sm" onClick={() => handleToggleActive(user)} className="h-7 text-xs">{user.active ? 'Deactivate' : 'Activate'}</Button>
              </div>
            </div>
          ))}</div>
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        </CardContent>
      </Card>
    </div>
  );
}
