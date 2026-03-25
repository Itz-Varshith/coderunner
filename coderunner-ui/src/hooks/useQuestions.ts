import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';

export function useQuestions() {
  return useQuery({
    queryKey: ['questions'],
    queryFn: api.getQuestions,
  });
}

export function useQuestion(id: string) {
  return useQuery({
    queryKey: ['question', id],
    queryFn: () => api.getQuestion(id),
    enabled: !!id,
  });
}
