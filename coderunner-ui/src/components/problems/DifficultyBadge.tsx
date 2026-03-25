import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { Difficulty } from '@/lib/types';

interface DifficultyBadgeProps {
  difficulty: Difficulty;
}

const difficultyConfig: Record<Difficulty, { label: string; className: string }> = {
  EASY: {
    label: 'Easy',
    className: 'bg-emerald-500/15 text-emerald-500 hover:bg-emerald-500/25 border-emerald-500/20',
  },
  MEDIUM: {
    label: 'Medium',
    className: 'bg-amber-500/15 text-amber-500 hover:bg-amber-500/25 border-amber-500/20',
  },
  HARD: {
    label: 'Hard',
    className: 'bg-red-500/15 text-red-500 hover:bg-red-500/25 border-red-500/20',
  },
};

export function DifficultyBadge({ difficulty }: DifficultyBadgeProps) {
  const config = difficultyConfig[difficulty];

  return (
    <Badge variant="outline" className={cn('font-medium', config.className)}>
      {config.label}
    </Badge>
  );
}
