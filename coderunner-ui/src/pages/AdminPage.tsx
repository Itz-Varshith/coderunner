import { useState, useCallback } from 'react';
import { Upload, X, FileArchive, Eye, Edit3, Loader2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { api } from '@/lib/api';
import type { Difficulty } from '@/lib/types';

const SUGGESTED_TOPICS = [
  'Array',
  'String',
  'Hash Table',
  'Dynamic Programming',
  'Math',
  'Sorting',
  'Greedy',
  'Binary Search',
  'Tree',
  'Graph',
  'Linked List',
  'Stack',
  'Queue',
  'Recursion',
  'Two Pointers',
];

export function AdminPage() {
  const [title, setTitle] = useState('');
  const [markdown, setMarkdown] = useState('');
  const [difficulty, setDifficulty] = useState<Difficulty>('MEDIUM');
  const [timeLimit, setTimeLimit] = useState('1000');
  const [memoryLimit, setMemoryLimit] = useState('256');
  const [topics, setTopics] = useState<string[]>([]);
  const [topicInput, setTopicInput] = useState('');
  const [testCaseFile, setTestCaseFile] = useState<File | null>(null);
  const [customJudge, setCustomJudge] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleAddTopic = useCallback(() => {
    const topic = topicInput.trim();
    if (topic && !topics.includes(topic)) {
      setTopics((prev) => [...prev, topic]);
      setTopicInput('');
    }
  }, [topicInput, topics]);

  const handleRemoveTopic = useCallback((topic: string) => {
    setTopics((prev) => prev.filter((t) => t !== topic));
  }, []);

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.type !== 'application/zip' && !file.name.endsWith('.zip')) {
        toast.error('Please upload a ZIP file');
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        toast.error('File size must be less than 10MB');
        return;
      }
      setTestCaseFile(file);
    }
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) {
      toast.error('Please enter a title');
      return;
    }
    if (!markdown.trim()) {
      toast.error('Please enter a description');
      return;
    }
    if (!testCaseFile) {
      toast.error('Please upload test cases');
      return;
    }

    setIsSubmitting(true);

    try {
      const formData = new FormData();
      formData.append('title', title);
      formData.append('markdown', markdown);
      formData.append('difficulty', difficulty);
      formData.append('time_limit', (parseInt(timeLimit) / 1000).toString());
      formData.append('memory_limit', memoryLimit);
      formData.append('topics', JSON.stringify(topics));
      formData.append('test_cases', testCaseFile);
      formData.append('customJudge', String(customJudge));

      await api.createQuestion(formData);
      toast.success('Question created successfully!');
      
      setTitle('');
      setMarkdown('');
      setDifficulty('MEDIUM');
      setTimeLimit('1000');
      setMemoryLimit('256');
      setTopics([]);
      setTestCaseFile(null);
      setCustomJudge(false);
    } catch (error) {
      toast.error('Failed to create question');
      console.error(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 md:px-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold">Create Question</h1>
        <p className="mt-2 text-muted-foreground">
          Add a new coding problem to the platform
        </p>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="grid gap-6 lg:grid-cols-2">
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Basic Information</CardTitle>
                <CardDescription>
                  Enter the title and difficulty of the problem
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="title">Title</Label>
                  <Input
                    id="title"
                    placeholder="e.g., Two Sum"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Difficulty</Label>
                  <Select
                    value={difficulty}
                    onValueChange={(val) => setDifficulty(val as Difficulty)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="EASY">Easy</SelectItem>
                      <SelectItem value="MEDIUM">Medium</SelectItem>
                      <SelectItem value="HARD">Hard</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="timeLimit">Time Limit (ms)</Label>
                    <Input
                      id="timeLimit"
                      type="number"
                      min="100"
                      max="10000"
                      value={timeLimit}
                      onChange={(e) => setTimeLimit(e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="memoryLimit">Memory Limit (MB)</Label>
                    <Input
                      id="memoryLimit"
                      type="number"
                      min="16"
                      max="1024"
                      value={memoryLimit}
                      onChange={(e) => setMemoryLimit(e.target.value)}
                    />
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="customJudge"
                    checked={customJudge}
                    onCheckedChange={(checked) => setCustomJudge(checked === true)}
                  />
                  <Label
                    htmlFor="customJudge"
                    className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                  >
                    Custom Judge
                  </Label>
                </div>
                <p className="text-xs text-muted-foreground">
                  Enable if this problem uses a custom judge (judge.cpp) for output validation
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Topics</CardTitle>
                <CardDescription>
                  Add relevant tags for this problem
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex gap-2">
                  <Input
                    placeholder="Add a topic..."
                    value={topicInput}
                    onChange={(e) => setTopicInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleAddTopic();
                      }
                    }}
                  />
                  <Button type="button" onClick={handleAddTopic}>
                    Add
                  </Button>
                </div>

                <div className="flex flex-wrap gap-2">
                  {topics.map((topic) => (
                    <Badge key={topic} variant="secondary" className="gap-1">
                      {topic}
                      <button
                        type="button"
                        onClick={() => handleRemoveTopic(topic)}
                        className="ml-1 rounded-full hover:bg-muted"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                </div>

                <div className="space-y-2">
                  <Label className="text-xs text-muted-foreground">
                    Suggested topics:
                  </Label>
                  <div className="flex flex-wrap gap-1">
                    {SUGGESTED_TOPICS.filter((t) => !topics.includes(t)).map(
                      (topic) => (
                        <button
                          key={topic}
                          type="button"
                          onClick={() => setTopics((prev) => [...prev, topic])}
                          className="rounded-md bg-muted px-2 py-1 text-xs hover:bg-muted/80"
                        >
                          + {topic}
                        </button>
                      )
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Test Cases</CardTitle>
                <CardDescription>
                  Upload a ZIP file containing test cases
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div
                  className={`relative rounded-lg border-2 border-dashed p-8 text-center transition-colors ${
                    testCaseFile
                      ? 'border-primary bg-primary/5'
                      : 'border-muted-foreground/25 hover:border-muted-foreground/50'
                  }`}
                >
                  <input
                    type="file"
                    accept=".zip"
                    onChange={handleFileChange}
                    className="absolute inset-0 cursor-pointer opacity-0"
                  />
                  {testCaseFile ? (
                    <div className="flex flex-col items-center gap-2">
                      <FileArchive className="h-10 w-10 text-primary" />
                      <p className="font-medium">{testCaseFile.name}</p>
                      <p className="text-sm text-muted-foreground">
                        {(testCaseFile.size / 1024).toFixed(1)} KB
                      </p>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation();
                          setTestCaseFile(null);
                        }}
                      >
                        Remove
                      </Button>
                    </div>
                  ) : (
                    <div className="flex flex-col items-center gap-2">
                      <Upload className="h-10 w-10 text-muted-foreground" />
                      <p className="font-medium">Upload ZIP file</p>
                      <p className="text-sm text-muted-foreground">
                        Max 10MB. Must contain testcases/judge.cpp and input files
                      </p>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          <div className="space-y-6">
            <Card className="h-fit">
              <CardHeader>
                <CardTitle>Problem Description</CardTitle>
                <CardDescription>Write the problem statement in Markdown</CardDescription>
              </CardHeader>
              <CardContent>
                <Tabs defaultValue="write">
                  <TabsList className="mb-4">
                    <TabsTrigger value="write" className="gap-2">
                      <Edit3 className="h-4 w-4" />
                      Write
                    </TabsTrigger>
                    <TabsTrigger value="preview" className="gap-2">
                      <Eye className="h-4 w-4" />
                      Preview
                    </TabsTrigger>
                  </TabsList>
                  <TabsContent value="write">
                    <Textarea
                      placeholder={`## Problem Statement

Given an array of integers...

## Examples

**Example 1:**
\`\`\`
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
\`\`\`

## Constraints

- 2 <= nums.length <= 10^4`}
                      value={markdown}
                      onChange={(e) => setMarkdown(e.target.value)}
                      className="min-h-[400px] font-mono text-sm"
                    />
                  </TabsContent>
                  <TabsContent value="preview">
                    <div className="min-h-[400px] rounded-lg border bg-muted/30 p-4">
                      {markdown ? (
                        <div className="prose prose-sm dark:prose-invert max-w-none">
                          <ReactMarkdown
                            rehypePlugins={[rehypeRaw]}
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
                            {markdown}
                          </ReactMarkdown>
                        </div>
                      ) : (
                        <p className="text-muted-foreground">
                          Start writing to see preview...
                        </p>
                      )}
                    </div>
                  </TabsContent>
                </Tabs>
              </CardContent>
            </Card>

            <Button
              type="submit"
              size="lg"
              className="w-full"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Creating...
                </>
              ) : (
                'Create Question'
              )}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
}
