import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { chartColors, tooltipStyle, gridStyle, axisStyle } from './ChartTheme';
import { TrendingUp } from 'lucide-react';
import { formatCurrency } from '@/lib/utils';

interface SalesTrendChartProps {
  data: { label: string; revenue: number; sales: number }[];
  title?: string;
}

export function SalesTrendChart({ data, title = 'Sales Trend' }: SalesTrendChartProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <TrendingUp className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <div className="h-[200px] md:h-[250px]">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ top: 5, right: 5, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="revenueGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={chartColors.primary} stopOpacity={0.3} />
                  <stop offset="95%" stopColor={chartColors.primary} stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid {...gridStyle} />
              <XAxis dataKey="label" {...axisStyle} />
              <YAxis {...axisStyle} tickFormatter={(v) => `R${v >= 1000 ? (v / 1000).toFixed(0) + 'k' : v}`} />
              <Tooltip
                contentStyle={tooltipStyle}
                formatter={(value: number, name: string) => [
                  name === 'revenue' ? formatCurrency(value) : value,
                  name === 'revenue' ? 'Revenue' : 'Sales Count',
                ]}
              />
              <Area type="monotone" dataKey="revenue" stroke={chartColors.primary} fill="url(#revenueGradient)" strokeWidth={2} />
              <Area type="monotone" dataKey="sales" stroke={chartColors.accent} fill="transparent" strokeWidth={2} strokeDasharray="4 4" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
