import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { SubmissionCreateRequest } from '@/lib/types';

export function useSubmission(id: string | null) {
  return useQuery({
    queryKey: ['submission', id],
    queryFn: () => api.getSubmission(id!),
    enabled: !!id,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (status === 'PENDING' || status === 'RUNNING') {
        return 2000;
      }
      return false;
    },
  });
}

export function useUserSubmissions(userId: string) {
  return useQuery({
    queryKey: ['submissions', userId],
    queryFn: () => api.getSubmissionsByUser(userId),
    enabled: !!userId,
  });
}

export function useSubmitCode() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: SubmissionCreateRequest) => api.submitCode(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['submissions'] });
    },
  });
}
