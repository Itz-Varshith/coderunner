import { useState } from 'react';
import {
  CheckCircle2,
  XCircle,
  Clock,
  AlertTriangle,
  Bug,
  ChevronDown,
  ChevronUp,
  Code2,
  RefreshCw,
} from 'lucide-react';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';
import { useUserSubmissions } from '@/hooks/useSubmission';
import type { Submission, SubmissionStatus } from '@/lib/types';

const USER_ID = 'dummy-user';

const statusConfig: Record<
  SubmissionStatus,
  { icon: typeof CheckCircle2; label: string; className: string; bgClass: string }
> = {
  PENDING: {
    icon: Clock,
    label: 'Pending',
    className: 'text-muted-foreground',
    bgClass: 'border-l-muted-foreground',
  },
  RUNNING: {
    icon: Clock,
    label: 'Running',
    className: 'text-blue-500',
    bgClass: 'border-l-blue-500',
  },
  ACCEPTED: {
    icon: CheckCircle2,
    label: 'Accepted',
    className: 'text-emerald-500',
    bgClass: 'border-l-emerald-500',
  },
  WRONG_ANSWER: {
    icon: XCircle,
    label: 'Wrong Answer',
    className: 'text-red-500',
    bgClass: 'border-l-red-500',
  },
  TLE: {
    icon: Clock,
    label: 'Time Limit Exceeded',
    className: 'text-amber-500',
    bgClass: 'border-l-amber-500',
  },
  MLE: {
    icon: AlertTriangle,
    label: 'Memory Limit Exceeded',
    className: 'text-amber-500',
    bgClass: 'border-l-amber-500',
  },
  RUNTIME_ERROR: {
    icon: Bug,
    label: 'Runtime Error',
    className: 'text-red-500',
    bgClass: 'border-l-red-500',
  },
  COMPILATION_ERROR: {
    icon: XCircle,
    label: 'Compilation Error',
    className: 'text-red-500',
    bgClass: 'border-l-red-500',
  },
};

const languageDisplayMap: Record<string, string> = {
  cpp: 'C++',
  python: 'Python',
  java: 'Java',
};

function formatTimeAgo(timestamp: number): string {
  const seconds = Math.floor((Date.now() - timestamp) / 1000);
  if (seconds < 60) return 'Just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
  const days = Math.floor(hours / 24);
  return `${days} day${days > 1 ? 's' : ''} ago`;
}

function SubmissionCard({ submission }: { submission: Submission }) {
  const [expanded, setExpanded] = useState(false);
  const config = statusConfig[submission.status] || statusConfig.PENDING;
  const Icon = config.icon;

  return (
    <Card className={cn('border-l-4', config.bgClass)}>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <Icon className={cn('h-5 w-5', config.className)} />
              <span className={cn('font-semibold', config.className)}>
                {config.label}
              </span>
            </div>
            <span className="text-lg font-medium">
              {submission.question?.title || `Question #${submission.question?.questionId}`}
            </span>
          </div>
          <div className="flex items-center gap-4">
            <Badge variant="secondary">
              {languageDisplayMap[submission.language?.languageName] || submission.language?.languageName}
            </Badge>
            {submission.timeTaken > 0 && (
              <span className="text-sm text-muted-foreground">
                {submission.timeTaken}ms
              </span>
            )}
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setExpanded(!expanded)}
            >
              {expanded ? (
                <ChevronUp className="h-4 w-4" />
              ) : (
                <ChevronDown className="h-4 w-4" />
              )}
            </Button>
          </div>
        </div>
        <p className="text-sm text-muted-foreground">
          {formatTimeAgo(submission.submittedAt)}
        </p>
      </CardHeader>
      {expanded && (
        <CardContent>
          <div className="rounded-lg bg-[#0a0a0a] p-4">
            <div className="mb-2 flex items-center gap-2 text-sm text-muted-foreground">
              <Code2 className="h-4 w-4" />
              <span>Submitted Code</span>
            </div>
            <pre className="overflow-x-auto text-sm font-mono text-zinc-300">
              <code>{submission.code}</code>
            </pre>
          </div>
          {submission.judgeMessage && (
            <div className="mt-4 rounded-lg border border-zinc-800 bg-zinc-900/50 p-4">
              <p className="text-sm font-medium text-muted-foreground">Judge Message:</p>
              <p className="mt-1 text-sm">{submission.judgeMessage}</p>
            </div>
          )}
        </CardContent>
      )}
    </Card>
  );
}

export function SubmissionsPage() {
  const { data: submissions, isLoading, error, refetch, isRefetching } = useUserSubmissions(USER_ID);

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 md:px-6">
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold">My Submissions</h1>
          <p className="mt-2 text-muted-foreground">
            View your submission history and results
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => refetch()}
          disabled={isRefetching}
        >
          <RefreshCw className={cn('mr-2 h-4 w-4', isRefetching && 'animate-spin')} />
          Refresh
        </Button>
      </div>

      {error ? (
        <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-destructive">
          Failed to load submissions. Please try again later.
        </div>
      ) : (
        <div className="space-y-4">
          {isLoading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <Card key={i}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <Skeleton className="h-5 w-5" />
                      <Skeleton className="h-5 w-24" />
                      <Skeleton className="h-5 w-32" />
                    </div>
                    <Skeleton className="h-6 w-16" />
                  </div>
                  <Skeleton className="mt-2 h-4 w-24" />
                </CardHeader>
              </Card>
            ))
          ) : !submissions || submissions.length === 0 ? (
            <div className="py-12 text-center text-muted-foreground">
              <Code2 className="mx-auto h-12 w-12 opacity-50" />
              <p className="mt-4">No submissions yet</p>
              <p className="text-sm">Solve some problems to see your submissions here</p>
            </div>
          ) : (
            submissions.map((submission) => (
              <SubmissionCard key={submission.submissionId} submission={submission} />
            ))
          )}
        </div>
      )}
    </div>
  );
}
