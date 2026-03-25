import { useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Play, Loader2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { useQuestion } from '@/hooks/useQuestions';
import { useSubmission, useSubmitCode } from '@/hooks/useSubmission';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '@/components/ui/resizable';
import { CodeEditor } from '@/components/editor/CodeEditor';
import { LanguageSelector } from '@/components/editor/LanguageSelector';
import { TestResults } from '@/components/editor/TestResults';
import { DifficultyBadge } from '@/components/problems/DifficultyBadge';
import { TopicTag } from '@/components/problems/TopicTag';

const DEFAULT_CODE: Record<string, string> = {
  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    // Your code here
    return 0;
}`,
  python: `# Your code here
`,
  java: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Your code here
    }
}`,
};

export function ProblemPage() {
  const { id } = useParams<{ id: string }>();
  const { data, isLoading, error } = useQuestion(id || '');
  const [language, setLanguage] = useState('cpp');
  const [code, setCode] = useState(DEFAULT_CODE['cpp']);
  const [submissionId, setSubmissionId] = useState<string | null>(null);
  const { data: submission, isLoading: submissionLoading } = useSubmission(submissionId);
  const submitMutation = useSubmitCode();

  const handleLanguageChange = useCallback((newLang: string) => {
    setLanguage(newLang);
    setCode(DEFAULT_CODE[newLang] || '');
  }, []);

  const handleSubmit = async () => {
    if (!id) return;
    try {
      const result = await submitMutation.mutateAsync({
        code,
        language,
        userId: 'dummy-user',
        questionId: id,
      });
      if (result.data) {
        setSubmissionId(result.data);
      }
    } catch (err) {
      console.error('Submission failed:', err);
    }
  };

  if (error) {
    return (
      <div className="container px-4 py-8">
        <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-destructive">
          Failed to load problem. Please try again later.
        </div>
      </div>
    );
  }

  const question = data?.questionModel;

  return (
    <div className="flex h-[calc(100vh-3.5rem)] flex-col">
      <div className="flex items-center justify-between border-b px-4 py-2">
        <div className="flex items-center gap-4">
          <Link
            to="/problems"
            className="inline-flex h-8 items-center justify-center gap-1 rounded-lg px-3 text-sm font-medium transition-colors hover:bg-muted"
          >
            <ArrowLeft className="h-4 w-4" />
            Back
          </Link>
          {isLoading ? (
            <Skeleton className="h-6 w-48" />
          ) : (
            <h1 className="text-lg font-semibold">{question?.title}</h1>
          )}
          {question && <DifficultyBadge difficulty={question.difficulty} />}
        </div>
        <Button
          onClick={handleSubmit}
          disabled={submitMutation.isPending || !code.trim()}
        >
          {submitMutation.isPending ? (
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <Play className="mr-2 h-4 w-4" />
          )}
          Submit
        </Button>
      </div>

      <ResizablePanelGroup className="flex-1">
        <ResizablePanel defaultSize={45} minSize={30}>
          <div className="h-full overflow-auto p-6">
            {isLoading ? (
              <div className="space-y-4">
                <Skeleton className="h-8 w-3/4" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-2/3" />
              </div>
            ) : question ? (
              <div className="prose prose-sm dark:prose-invert max-w-none">
                <div className="mb-4 flex flex-wrap gap-2">
                  {question.topics?.map((topic) => (
                    <TopicTag key={topic} topic={topic} />
                  ))}
                </div>
                <ReactMarkdown
                  components={{
                    pre: ({ children }) => (
                      <pre className="rounded-lg bg-muted p-4 overflow-x-auto">
                        {children}
                      </pre>
                    ),
                    code: ({ children, className }) => {
                      const isInline = !className;
                      return isInline ? (
                        <code className="rounded bg-muted px-1.5 py-0.5 text-sm">
                          {children}
                        </code>
                      ) : (
                        <code className={className}>{children}</code>
                      );
                    },
                  }}
                >
                  {question.markdown}
                </ReactMarkdown>
                <div className="mt-6 flex gap-6 text-sm text-muted-foreground">
                  <span>Time Limit: {question.timeLimit}ms</span>
                  <span>Memory Limit: {question.memoryLimit}MB</span>
                </div>
              </div>
            ) : null}
          </div>
        </ResizablePanel>

        <ResizableHandle withHandle />

        <ResizablePanel defaultSize={55} minSize={35}>
          <ResizablePanelGroup orientation="vertical">
            <ResizablePanel defaultSize={65} minSize={15}>
              <div className="flex h-full flex-col bg-[#0a0a0a]">
                <div className="flex items-center justify-between border-b border-zinc-800 bg-[#0a0a0a] px-4 py-2">
                  <LanguageSelector value={language} onChange={handleLanguageChange} />
                  <div className="flex items-center gap-2 text-xs text-zinc-500">
                    <span className="rounded bg-zinc-800 px-2 py-0.5">Tab: 4</span>
                    <span className="rounded bg-zinc-800 px-2 py-0.5">UTF-8</span>
                  </div>
                </div>
                <div className="flex-1 overflow-hidden">
                  <CodeEditor
                    value={code}
                    onChange={setCode}
                    language={language}
                  />
                </div>
              </div>
            </ResizablePanel>

            <ResizableHandle withHandle orientation="vertical" />

            <ResizablePanel defaultSize={35} minSize={10}>
              <TestResults
                submission={submission}
                isLoading={submissionLoading || submitMutation.isPending}
              />
            </ResizablePanel>
          </ResizablePanelGroup>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}
