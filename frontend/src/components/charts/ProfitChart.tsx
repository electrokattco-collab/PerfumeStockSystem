import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { chartColors, tooltipStyle } from './ChartTheme';
import { PieChartIcon } from 'lucide-react';
import { formatCurrency } from '@/lib/utils';

interface ProfitChartProps {
  revenue: number;
  cost: number;
  title?: string;
}

export function ProfitChart({ revenue, cost, title = 'Profit Breakdown' }: ProfitChartProps) {
  const profit = revenue - cost;
  const data = [
    { name: 'Profit', value: Math.max(profit, 0) },
    { name: 'Cost', value: cost },
  ].filter(d => d.value > 0);

  const COLORS = [chartColors.accent, chartColors.muted];

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <PieChartIcon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <div className="h-[200px] md:h-[250px] flex items-center justify-center">
          {data.length === 0 ? (
            <p className="text-sm text-muted-foreground">No data yet</p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={2}
                  dataKey="value"
                >
                  {data.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={tooltipStyle}
                  formatter={(value: number) => [formatCurrency(value)]}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
        <div className="flex justify-center gap-4 mt-2">
          <div className="flex items-center gap-1 text-xs">
            <div className="h-2 w-2 rounded-full" style={{ backgroundColor: chartColors.accent }} />
            <span className="text-muted-foreground">Profit: {formatCurrency(Math.max(profit, 0))}</span>
          </div>
          <div className="flex items-center gap-1 text-xs">
            <div className="h-2 w-2 rounded-full" style={{ backgroundColor: chartColors.muted }} />
            <span className="text-muted-foreground">Cost: {formatCurrency(cost)}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
