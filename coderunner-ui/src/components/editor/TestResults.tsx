import {
  CheckCircle2,
  XCircle,
  Clock,
  AlertTriangle,
  Bug,
  Loader2,
  Terminal,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import type { Submission, SubmissionStatus } from '@/lib/types';

interface TestResultsProps {
  submission?: Submission | null;
  isLoading: boolean;
}

const statusConfig: Record<
  SubmissionStatus,
  { icon: typeof CheckCircle2; label: string; className: string }
> = {
  PENDING: {
    icon: Clock,
    label: 'Pending',
    className: 'text-muted-foreground',
  },
  RUNNING: {
    icon: Loader2,
    label: 'Running',
    className: 'text-blue-500',
  },
  ACCEPTED: {
    icon: CheckCircle2,
    label: 'Accepted',
    className: 'text-emerald-500',
  },
  WRONG_ANSWER: {
    icon: XCircle,
    label: 'Wrong Answer',
    className: 'text-red-500',
  },
  TLE: {
    icon: Clock,
    label: 'Time Limit Exceeded',
    className: 'text-amber-500',
  },
  MLE: {
    icon: AlertTriangle,
    label: 'Memory Limit Exceeded',
    className: 'text-amber-500',
  },
  RUNTIME_ERROR: {
    icon: Bug,
    label: 'Runtime Error',
    className: 'text-red-500',
  },
  COMPILATION_ERROR: {
    icon: XCircle,
    label: 'Compilation Error',
    className: 'text-red-500',
  },
};

export function TestResults({ submission, isLoading }: TestResultsProps) {
  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center border-t bg-muted/30">
        <div className="flex items-center gap-2 text-muted-foreground">
          <Loader2 className="h-5 w-5 animate-spin" />
          <span>Processing...</span>
        </div>
      </div>
    );
  }

  if (!submission) {
    return (
      <div className="flex h-full items-center justify-center border-t bg-muted/30">
        <div className="flex flex-col items-center gap-2 text-muted-foreground">
          <Terminal className="h-8 w-8" />
          <span className="text-sm">Submit your code to see results</span>
        </div>
      </div>
    );
  }

  const config = statusConfig[submission.status];
  const Icon = config.icon;

  return (
    <div className="h-full overflow-auto border-t bg-muted/30 p-4">
      <div className="flex items-center gap-3">
        <Icon
          className={cn(
            'h-6 w-6',
            config.className,
            submission.status === 'RUNNING' && 'animate-spin'
          )}
        />
        <span className={cn('text-lg font-semibold', config.className)}>
          {config.label}
        </span>
      </div>

      {(submission.status === 'ACCEPTED' ||
        submission.status === 'WRONG_ANSWER') && (
        <div className="mt-4 flex gap-6 text-sm">
          <div>
            <span className="text-muted-foreground">Runtime: </span>
            <span className="font-mono">{submission.timeTaken}ms</span>
          </div>
          <div>
            <span className="text-muted-foreground">Memory: </span>
            <span className="font-mono">{submission.memoryTaken}MB</span>
          </div>
        </div>
      )}

      {submission.judgeMessage && (
        <div className="mt-4">
          <p className="text-sm text-muted-foreground">Message:</p>
          <pre className="mt-2 rounded bg-background p-3 text-sm">
            {submission.judgeMessage}
          </pre>
        </div>
      )}
    </div>
  );
}
