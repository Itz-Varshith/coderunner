import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter } from 'lucide-react';
import { useQuestions } from '@/hooks/useQuestions';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Skeleton } from '@/components/ui/skeleton';
import { DifficultyBadge } from '@/components/problems/DifficultyBadge';

export function ProblemsPage() {
  const { data, isLoading, error } = useQuestions();
  const [search, setSearch] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState<string>('all');

  const questions = useMemo(() => {
    if (!data?.question) return [];
    return Object.entries(data.question).map(([id, title], index) => ({
      id,
      title,
      number: index + 1,
    }));
  }, [data]);

  const filteredQuestions = useMemo(() => {
    return questions.filter((q) =>
      q.title.toLowerCase().includes(search.toLowerCase())
    );
  }, [questions, search]);

  if (error) {
    return (
      <div className="container px-4 py-8">
        <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4 text-destructive">
          Failed to load problems. Please try again later.
        </div>
      </div>
    );
  }

  return (
    <div className="container px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold">Problems</h1>
        <p className="mt-2 text-muted-foreground">
          Browse and solve coding challenges
        </p>
      </div>

      <div className="mb-6 flex flex-col gap-4 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search problems..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={difficultyFilter} onValueChange={(val) => val && setDifficultyFilter(val)}>
          <SelectTrigger className="w-full sm:w-[180px]">
            <Filter className="mr-2 h-4 w-4" />
            <SelectValue placeholder="Difficulty" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Difficulties</SelectItem>
            <SelectItem value="EASY">Easy</SelectItem>
            <SelectItem value="MEDIUM">Medium</SelectItem>
            <SelectItem value="HARD">Hard</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-16">#</TableHead>
              <TableHead>Title</TableHead>
              <TableHead className="w-32 text-center">Difficulty</TableHead>
              <TableHead className="w-32 text-right">Acceptance</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell><Skeleton className="h-4 w-8" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-48" /></TableCell>
                  <TableCell><Skeleton className="h-6 w-16 mx-auto" /></TableCell>
                  <TableCell><Skeleton className="h-4 w-12 ml-auto" /></TableCell>
                </TableRow>
              ))
            ) : filteredQuestions.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} className="h-24 text-center text-muted-foreground">
                  No problems found
                </TableCell>
              </TableRow>
            ) : (
              filteredQuestions.map((question) => (
                <TableRow key={question.id} className="cursor-pointer hover:bg-muted/50">
                  <TableCell className="font-mono text-muted-foreground">
                    {question.number}
                  </TableCell>
                  <TableCell>
                    <Link
                      to={`/problems/${question.id}`}
                      className="font-medium hover:text-primary hover:underline"
                    >
                      {question.title}
                    </Link>
                  </TableCell>
                  <TableCell className="text-center">
                    <DifficultyBadge difficulty="MEDIUM" />
                  </TableCell>
                  <TableCell className="text-right text-muted-foreground">
                    --
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
}
