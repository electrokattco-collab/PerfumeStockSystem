import { Card, CardContent, CardHeader } from '@/components/ui/card';

interface SkeletonProps {
  lines?: number;
  className?: string;
}

export function SkeletonLine({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse rounded bg-muted h-4 w-full ${className}`} />;
}

export function SkeletonCard({ lines = 3 }: SkeletonProps) {
  return (
    <Card>
      <CardHeader className="pb-3">
        <SkeletonLine className="h-6 w-1/3" />
      </CardHeader>
      <CardContent className="space-y-3">
        {Array.from({ length: lines }).map((_, i) => (
          <SkeletonLine key={i} className={i === lines - 1 ? 'w-2/3' : ''} />
        ))}
      </CardContent>
    </Card>
  );
}

export function SkeletonStats() {
  return (
    <div className="grid gap-4 grid-cols-2 lg:grid-cols-4">
      {Array.from({ length: 4 }).map((_, i) => (
        <Card key={i}>
          <CardHeader className="pb-2">
            <SkeletonLine className="h-4 w-1/2" />
          </CardHeader>
          <CardContent>
            <SkeletonLine className="h-8 w-2/3 mb-2" />
            <SkeletonLine className="h-3 w-1/3" />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex items-center space-x-4 rounded-lg border p-4">
          <div className="flex-1 space-y-2">
            <SkeletonLine className="h-4 w-1/4" />
            <SkeletonLine className="h-3 w-1/2" />
          </div>
          <SkeletonLine className="h-6 w-20" />
        </div>
      ))}
    </div>
  );
}
