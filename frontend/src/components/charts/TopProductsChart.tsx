import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { tooltipStyle, gridStyle, axisStyle } from './ChartTheme';
import { BarChart3 } from 'lucide-react';
import { formatCurrency } from '@/lib/utils';

interface TopProductsChartProps {
  data: { name: string; revenue: number; quantity: number }[];
  title?: string;
}

const BAR_COLORS = ['#8b5cf6', '#a78bfa', '#c4b5fd', '#ddd6fe', '#ede9fe'];

export function TopProductsChart({ data, title = 'Top Products' }: TopProductsChartProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <BarChart3 className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <div className="h-[200px] md:h-[250px]">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} layout="vertical" margin={{ top: 0, right: 5, left: 0, bottom: 0 }}>
              <CartesianGrid {...gridStyle} horizontal={false} />
              <XAxis type="number" {...axisStyle} tickFormatter={(v) => `R${v >= 1000 ? (v / 1000).toFixed(0) + 'k' : v}`} />
              <YAxis type="category" dataKey="name" {...axisStyle} width={100} tick={{ fontSize: 11 }} />
              <Tooltip
                contentStyle={tooltipStyle}
                formatter={(value: number, name: string) => [
                  name === 'revenue' ? formatCurrency(value) : value + ' units',
                  name === 'revenue' ? 'Revenue' : 'Quantity Sold',
                ]}
              />
              <Bar dataKey="revenue" radius={[0, 4, 4, 0]} barSize={20}>
                {data.map((_, index) => (
                  <Cell key={index} fill={BAR_COLORS[index % BAR_COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
