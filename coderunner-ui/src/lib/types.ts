export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export type SubmissionStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'ACCEPTED'
  | 'WRONG_ANSWER'
  | 'TLE'
  | 'MLE'
  | 'RUNTIME_ERROR'
  | 'COMPILATION_ERROR';

export interface Question {
  questionId: string;
  title: string;
  markdown: string;
  testcasesPath: string;
  testcasesCount: number;
  submissions: number;
  accepted: number;
  timeLimit: number;
  memoryLimit: number;
  topics: string[];
  difficulty: Difficulty;
}

export interface QuestionSummary {
  questionId: string;
  title: string;
}

export interface Language {
  languageId: number;
  languageName: string;
  languageMultiplier: number;
}

export interface Submission {
  submissionId: number;
  language: Language;
  question: Question;
  user: { id: string };
  code: string;
  status: SubmissionStatus;
  timeTaken: number;
  memoryTaken: number;
  submittedAt: number;
  judgeMessage: string;
}

export interface APIResponse<T> {
  success: boolean;
  message: string;
  data: T;
  date: string;
}

export interface QuestionFetchResponse {
  questionModel: Question;
}

export interface QuestionFetchAllResponse {
  question: Record<string, string>;
}

export interface SubmissionCreateRequest {
  code: string;
  language: string;
  userId: string;
  questionId: string;
}

export interface QuestionCreateRequest {
  title: string;
  markdown: string;
  time_limit: number;
  memory_limit: number;
  topics: string[];
  difficulty: Difficulty;
  test_cases: File;
}
