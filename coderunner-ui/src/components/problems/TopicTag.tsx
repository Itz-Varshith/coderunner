import { Badge } from '@/components/ui/badge';

interface TopicTagProps {
  topic: string;
}

export function TopicTag({ topic }: TopicTagProps) {
  return (
    <Badge variant="secondary" className="text-xs">
      {topic}
    </Badge>
  );
}
