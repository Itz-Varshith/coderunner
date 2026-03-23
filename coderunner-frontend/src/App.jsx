import { useState, useEffect } from 'react'
import './App.css'

const STORAGE_KEY = 'coderunner_submissions'
const MAX_STORED_SUBMISSIONS = 10

// Direct API URL - make sure CORS is enabled on backend
// Or use '/api' to use Vite's proxy (configured in vite.config.js)
const API_BASE = 'http://localhost:8081'

function App() {
  const [activeTab, setActiveTab] = useState('questions')
  const [selectedQuestionForSubmit, setSelectedQuestionForSubmit] = useState(null)

  const handleSubmitForQuestion = (question) => {
    setSelectedQuestionForSubmit(question)
    setActiveTab('submit')
  }

  return (
    <div className="app">
      <header className="header">
        <h1>CodeRunner</h1>
        <p>Online Judge System</p>
      </header>

      <nav className="tabs">
        <button 
          className={activeTab === 'questions' ? 'active' : ''} 
          onClick={() => setActiveTab('questions')}
        >
          Questions
        </button>
        <button 
          className={activeTab === 'submit' ? 'active' : ''} 
          onClick={() => setActiveTab('submit')}
          disabled={!selectedQuestionForSubmit}
          title={!selectedQuestionForSubmit ? 'Select a question first' : ''}
        >
          Submit Code
        </button>
        <button 
          className={activeTab === 'create' ? 'active' : ''} 
          onClick={() => setActiveTab('create')}
        >
          Create Question
        </button>
      </nav>

      <main className="content">
        {activeTab === 'questions' && <QuestionsList onSubmit={handleSubmitForQuestion} />}
        {activeTab === 'submit' && <SubmitCode question={selectedQuestionForSubmit} />}
        {activeTab === 'create' && <CreateQuestion />}
      </main>
    </div>
  )
}

function SubmitCode({ question }) {
  const [form, setForm] = useState({
    code: '#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "Hello World";\n    return 0;\n}',
    language: 'cpp',
    userId: ''
  })
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(false)
  const [recentSubmissions, setRecentSubmissions] = useState([])
  const [selectedSubmission, setSelectedSubmission] = useState(null)
  const [submissionLoading, setSubmissionLoading] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      setRecentSubmissions(JSON.parse(stored))
    }
  }, [])

  const saveSubmissionToStorage = (submissionId, questionTitle) => {
    const newSubmission = {
      id: submissionId,
      questionTitle,
      timestamp: Date.now()
    }
    const updated = [newSubmission, ...recentSubmissions].slice(0, MAX_STORED_SUBMISSIONS)
    setRecentSubmissions(updated)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setResponse(null)
    setSelectedSubmission(null)

    try {
      const res = await fetch(`${API_BASE}/submit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          questionId: question.questionId
        })
      })
      const data = await res.json()
      setResponse({ status: res.status, data })
      
      if (data.success && data.data) {
        saveSubmissionToStorage(data.data, question.title)
      }
    } catch (err) {
      setResponse({ status: 'error', data: { message: err.message } })
    } finally {
      setLoading(false)
    }
  }

  const fetchSubmissionDetails = async (submissionId) => {
    setSubmissionLoading(true)
    setSelectedSubmission(null)
    try {
      const res = await fetch(`${API_BASE}/submit/details/${submissionId}`)
      const data = await res.json()
      setSelectedSubmission(data)
    } catch (err) {
      console.error('Failed to fetch submission:', err)
    } finally {
      setSubmissionLoading(false)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACCEPTED': return 'accepted'
      case 'PENDING': case 'RUNNING': return 'pending'
      default: return 'rejected'
    }
  }

  if (!question) {
    return (
      <div className="form-container">
        <p className="empty-state">Please select a question from the Questions tab first.</p>
      </div>
    )
  }

  return (
    <div className="form-container">
      <div className="submit-layout">
        <div className="submit-main">
          <h2>Submit Code</h2>
          <p className="endpoint">POST /submit</p>

          <div className="submit-question-info">
            <span className="submit-question-title">{question.title}</span>
            <span className={`difficulty-badge ${question.difficulty?.toLowerCase()}`}>
              {question.difficulty}
            </span>
          </div>
          
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>User ID</label>
              <input
                type="text"
                value={form.userId}
                onChange={(e) => setForm({ ...form, userId: e.target.value })}
                placeholder="Enter user ID"
                required
              />
            </div>

            <div className="form-group">
              <label>Language</label>
              <select
                value={form.language}
                onChange={(e) => setForm({ ...form, language: e.target.value })}
              >
                <option value="cpp">C++</option>
                <option value="java">Java</option>
                <option value="python">Python</option>
              </select>
            </div>

            <div className="form-group">
              <label>Code</label>
              <textarea
                value={form.code}
                onChange={(e) => setForm({ ...form, code: e.target.value })}
                rows={12}
                placeholder="Enter your code here..."
                required
              />
            </div>

            <button type="submit" className="submit-btn" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Code'}
            </button>
          </form>

          {response && <ResponseDisplay response={response} />}
        </div>

        <div className="submissions-sidebar">
          <h3>Recent Submissions</h3>
          <p className="endpoint">GET /submit/details/:id</p>
          
          {recentSubmissions.length === 0 ? (
            <p className="empty-state-small">No submissions yet</p>
          ) : (
            <div className="submissions-list">
              {recentSubmissions.map((sub) => (
                <div
                  key={sub.id + sub.timestamp}
                  className={`submission-item ${selectedSubmission?.submissionId == sub.id ? 'selected' : ''}`}
                  onClick={() => fetchSubmissionDetails(sub.id)}
                >
                  <span className="submission-question">{sub.questionTitle}</span>
                  <span className="submission-id">#{sub.id}</span>
                  <span className="submission-time">
                    {new Date(sub.timestamp).toLocaleTimeString()}
                  </span>
                </div>
              ))}
            </div>
          )}

          {submissionLoading && (
            <div className="submission-detail">
              <p className="loading-state">Loading...</p>
            </div>
          )}

          {selectedSubmission && !submissionLoading && (
            <div className="submission-detail">
              <div className="submission-detail-header">
                <span className={`status-badge-large ${getStatusColor(selectedSubmission.status)}`}>
                  {selectedSubmission.status}
                </span>
              </div>

              <div className="submission-stats">
                <div className="stat-item">
                  <span className="stat-label">Time</span>
                  <span className="stat-value">{selectedSubmission.timeTaken} ms</span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Memory</span>
                  <span className="stat-value">{selectedSubmission.memoryTaken} KB</span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Language</span>
                  <span className="stat-value">{selectedSubmission.language?.languageName}</span>
                </div>
              </div>

              {selectedSubmission.judgeMessage && (
                <div className="judge-message">
                  <span className="stat-label">Judge Message</span>
                  <pre>{selectedSubmission.judgeMessage}</pre>
                </div>
              )}

              <div className="submission-meta">
                <span>Submitted: {new Date(selectedSubmission.submittedAt).toLocaleString()}</span>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function CreateQuestion() {
  const [form, setForm] = useState({
    title: '',
    markdown: '## Problem Description\n\nWrite your problem statement here.\n\n## Input\n\n## Output\n\n## Examples',
    time_limit: 2,
    memory_limit: 256,
    topics: '',
    difficulty: 'EASY'
  })
  const [testCases, setTestCases] = useState(null)
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setResponse(null)

    const formData = new FormData()
    formData.append('title', form.title)
    formData.append('markdown', form.markdown)
    formData.append('time_limit', form.time_limit)
    formData.append('memory_limit', form.memory_limit)
    formData.append('difficulty', form.difficulty)
    
    const topicsArray = form.topics.split(',').map(t => t.trim()).filter(t => t)
    topicsArray.forEach(topic => formData.append('topics', topic))
    
    if (testCases) {
      formData.append('test_cases', testCases)
    }

    try {
      const res = await fetch(`${API_BASE}/question/create-question`, {
        method: 'POST',
        body: formData
      })
      const data = await res.json()
      setResponse({ status: res.status, data })
    } catch (err) {
      setResponse({ status: 'error', data: { message: err.message } })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="form-container">
      <h2>Create Question</h2>
      <p className="endpoint">POST /question/create-question</p>
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Title</label>
          <input
            type="text"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            placeholder="Two Sum"
            required
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Time Limit (seconds)</label>
            <input
              type="number"
              value={form.time_limit}
              onChange={(e) => setForm({ ...form, time_limit: parseFloat(e.target.value) })}
              min="0.1"
              step="0.1"
              required
            />
          </div>

          <div className="form-group">
            <label>Memory Limit (MB)</label>
            <input
              type="number"
              value={form.memory_limit}
              onChange={(e) => setForm({ ...form, memory_limit: parseInt(e.target.value) })}
              min="1"
              required
            />
          </div>

          <div className="form-group">
            <label>Difficulty</label>
            <select
              value={form.difficulty}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
            >
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label>Topics (comma separated)</label>
          <input
            type="text"
            value={form.topics}
            onChange={(e) => setForm({ ...form, topics: e.target.value })}
            placeholder="arrays, hash-table, two-pointers"
          />
        </div>

        <div className="form-group">
          <label>Problem Markdown</label>
          <textarea
            value={form.markdown}
            onChange={(e) => setForm({ ...form, markdown: e.target.value })}
            rows={10}
            required
          />
        </div>

        <div className="form-group">
          <label>Test Cases (ZIP file)</label>
          <input
            type="file"
            accept=".zip"
            onChange={(e) => setTestCases(e.target.files[0])}
            required
          />
          <small>ZIP should contain: testcases/judge.cpp, testcases/input/*.txt</small>
        </div>

        <button type="submit" className="submit-btn" disabled={loading}>
          {loading ? 'Creating...' : 'Create Question'}
        </button>
      </form>

      {response && <ResponseDisplay response={response} />}
    </div>
  )
}

function QuestionsList({ onSubmit }) {
  const [questions, setQuestions] = useState(null)
  const [selectedQuestion, setSelectedQuestion] = useState(null)
  const [loading, setLoading] = useState(false)
  const [detailLoading, setDetailLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetchAllQuestions = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`${API_BASE}/question/get-all`)
      const data = await res.json()
      setQuestions(data.question || {})
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchQuestionDetails = async (id) => {
    setDetailLoading(true)
    try {
      const res = await fetch(`${API_BASE}/question/get/${id}`)
      const data = await res.json()
      setSelectedQuestion(data.questionModel)
    } catch (err) {
      setError(err.message)
    } finally {
      setDetailLoading(false)
    }
  }

  return (
    <div className="form-container">
      <div className="questions-header">
        <div>
          <h2>Questions</h2>
          <p className="endpoint">GET /question/get-all</p>
        </div>
        <button onClick={fetchAllQuestions} className="refresh-btn" disabled={loading}>
          {loading ? 'Loading...' : questions ? 'Refresh' : 'Load Questions'}
        </button>
      </div>

      {error && (
        <div className="response error">
          <div className="response-header">
            <span className="status-badge">Error</span>
            <span>{error}</span>
          </div>
        </div>
      )}

      <div className="questions-layout">
        <div className="questions-list">
          {!questions && !loading && (
            <p className="empty-state">Click "Load Questions" to fetch all questions</p>
          )}
          
          {questions && Object.keys(questions).length === 0 && (
            <p className="empty-state">No questions found. Create one first!</p>
          )}

          {questions && Object.entries(questions).map(([id, title]) => (
            <div
              key={id}
              className={`question-item ${selectedQuestion?.questionId === id ? 'selected' : ''}`}
              onClick={() => fetchQuestionDetails(id)}
            >
              <span className="question-title">{title}</span>
              <span className="question-id">{id.substring(0, 8)}...</span>
            </div>
          ))}
        </div>

        <div className="question-detail">
          {detailLoading && <p className="loading-state">Loading question details...</p>}
          
          {!selectedQuestion && !detailLoading && (
            <p className="empty-state">Select a question to view details</p>
          )}

          {selectedQuestion && !detailLoading && (
            <div className="question-display">
              <div className="question-header">
                <h3>{selectedQuestion.title}</h3>
                <span className={`difficulty-badge ${selectedQuestion.difficulty?.toLowerCase()}`}>
                  {selectedQuestion.difficulty}
                </span>
              </div>
              
              <div className="question-meta">
                <div className="meta-item">
                  <span className="meta-label">Time Limit</span>
                  <span className="meta-value">{selectedQuestion.timeLimit / 1000}s</span>
                </div>
                <div className="meta-item">
                  <span className="meta-label">Memory</span>
                  <span className="meta-value">{selectedQuestion.memoryLimit} MB</span>
                </div>
                <div className="meta-item">
                  <span className="meta-label">Test Cases</span>
                  <span className="meta-value">{selectedQuestion.testcasesCount}</span>
                </div>
                <div className="meta-item">
                  <span className="meta-label">Submissions</span>
                  <span className="meta-value">{selectedQuestion.submissions}</span>
                </div>
                <div className="meta-item">
                  <span className="meta-label">Accepted</span>
                  <span className="meta-value">{selectedQuestion.accepted}</span>
                </div>
              </div>

              {selectedQuestion.topics?.length > 0 && (
                <div className="question-topics">
                  {selectedQuestion.topics.map((topic, i) => (
                    <span key={i} className="topic-tag">{topic}</span>
                  ))}
                </div>
              )}

              <div className="question-markdown">
                <pre>{selectedQuestion.markdown}</pre>
              </div>

              <button 
                className="submit-btn solve-btn"
                onClick={() => onSubmit(selectedQuestion)}
              >
                Solve Challenge
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function ResponseDisplay({ response }) {
  const isSuccess = response.status >= 200 && response.status < 300
  
  return (
    <div className={`response ${isSuccess ? 'success' : 'error'}`}>
      <div className="response-header">
        <span className="status-badge">
          {typeof response.status === 'number' ? `${response.status}` : 'Error'}
        </span>
        <span>{isSuccess ? 'Success' : 'Failed'}</span>
      </div>
      <pre>{JSON.stringify(response.data, null, 2)}</pre>
    </div>
  )
}

export default App
