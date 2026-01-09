import { useNavigate } from 'react-router-dom';
import type { CategoryData } from '../types/category';
import {
  Watch,
  Camera,
  Shirt,
  Armchair,
  Palette,
  Gem,
  Monitor,
  Music,
  type LucideIcon,
} from 'lucide-react';

const iconMap: Record<string, LucideIcon> = {
  watch: Watch,
  watches: Watch,
  camera: Camera,
  cameras: Camera,
  shirt: Shirt,
  fashion: Shirt,
  armchair: Armchair,
  furniture: Armchair,
  palette: Palette,
  art: Palette,
  gem: Gem,
  jewelry: Gem,
  monitor: Monitor,
  electronics: Monitor,
  music: Music,
};

const colorMap: Record<string, { bg: string; icon: string }> = {
  watches: { bg: 'bg-blue-50', icon: 'text-blue-500' },
  cameras: { bg: 'bg-teal-50', icon: 'text-teal-500' },
  fashion: { bg: 'bg-pink-50', icon: 'text-pink-500' },
  furniture: { bg: 'bg-emerald-50', icon: 'text-emerald-500' },
  art: { bg: 'bg-orange-50', icon: 'text-orange-500' },
  jewelry: { bg: 'bg-red-50', icon: 'text-red-500' },
  electronics: { bg: 'bg-indigo-50', icon: 'text-indigo-500' },
  music: { bg: 'bg-blue-50', icon: 'text-blue-500' },
};

interface CategoryCardProps {
  category: CategoryData;
}

export function CategoryCard({ category }: CategoryCardProps) {
  const navigate = useNavigate();
  const iconKey = category.icon.toLowerCase();
  const categoryKey = category.name.toLowerCase();
  const Icon = iconMap[iconKey] || iconMap[categoryKey] || Monitor;
  const colors = colorMap[categoryKey] || { bg: 'bg-gray-50', icon: 'text-gray-500' };

  const handleClick = () => {
    navigate(`/listings?category=${encodeURIComponent(category.name)}`);
  };

  return (
    <button
      onClick={handleClick}
      className="w-full bg-white rounded-xl p-6 border border-gray-100 hover:border-gray-200 hover:shadow-sm transition-all text-left"
    >
      <div className={`w-12 h-12 rounded-full ${colors.bg} flex items-center justify-center mb-4`}>
        <Icon className={`w-6 h-6 ${colors.icon}`} />
      </div>
      <h3 className="font-semibold text-gray-900 mb-1">{category.name}</h3>
      <p className="text-sm text-gray-500">
        {category.itemCount.toLocaleString()} items
      </p>
    </button>
  );
}
