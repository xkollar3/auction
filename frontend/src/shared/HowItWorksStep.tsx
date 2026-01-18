import type { LucideIcon } from 'lucide-react';

interface HowItWorksStepProps {
  stepNumber: number;
  icon: LucideIcon;
  title: string;
  description: string;
}

const stepColors: Record<number, { badge: string; iconBg: string; iconColor: string }> = {
  1: { badge: 'bg-blue-500', iconBg: 'bg-blue-50', iconColor: 'text-blue-500' },
  2: { badge: 'bg-yellow-500', iconBg: 'bg-yellow-50', iconColor: 'text-yellow-600' },
  3: { badge: 'bg-green-500', iconBg: 'bg-green-50', iconColor: 'text-green-500' },
  4: { badge: 'bg-purple-500', iconBg: 'bg-purple-50', iconColor: 'text-purple-500' },
};

export function HowItWorksStep({ stepNumber, icon: Icon, title, description }: HowItWorksStepProps) {
  const colors = stepColors[stepNumber] || stepColors[1];

  return (
    <div className="flex flex-col items-center text-center">
      {/* Icon with badge */}
      <div className="relative mb-4">
        <div className={`w-20 h-20 rounded-full ${colors.iconBg} flex items-center justify-center`}>
          <Icon className={`w-8 h-8 ${colors.iconColor}`} />
        </div>
        {/* Step number badge */}
        <div className={`absolute -top-1 -right-1 w-7 h-7 rounded-full ${colors.badge} text-white text-sm font-bold flex items-center justify-center`}>
          {stepNumber}
        </div>
      </div>

      {/* Title */}
      <h3 className="font-semibold text-gray-900 mb-2">{title}</h3>

      {/* Description */}
      <p className="text-sm text-gray-500 max-w-xs">{description}</p>
    </div>
  );
}
