import type {
  APIResponse,
  QuestionFetchResponse,
  QuestionFetchAllResponse,
  Submission,
  SubmissionCreateRequest,
} from './types';

const API_BASE = 'http://localhost:8081';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || `HTTP error ${response.status}`);
  }
  return response.json();
}

export const api = {
  getQuestions: async (): Promise<QuestionFetchAllResponse> => {
    const response = await fetch(`${API_BASE}/question/get-all`);
    return handleResponse(response);
  },

  getQuestion: async (id: string): Promise<QuestionFetchResponse> => {
    const response = await fetch(`${API_BASE}/question/get/${id}`);
    return handleResponse(response);
  },

  createQuestion: async (data: FormData): Promise<APIResponse<string>> => {
    const response = await fetch(`${API_BASE}/question/create-question`, {
      method: 'POST',
      body: data,
    });
    return handleResponse(response);
  },

  submitCode: async (data: SubmissionCreateRequest): Promise<APIResponse<string>> => {
    const response = await fetch(`${API_BASE}/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse(response);
  },

  getSubmission: async (id: string): Promise<Submission> => {
    const response = await fetch(`${API_BASE}/submit/details/${id}`);
    return handleResponse(response);
  },

  getSubmissionsByUser: async (userId: string): Promise<Submission[]> => {
    const response = await fetch(`${API_BASE}/submit/user/${userId}`);
    return handleResponse(response);
  },
};
