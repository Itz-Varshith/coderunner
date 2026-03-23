# CodeRunner Frontend

A simple React interface for the CodeRunner online judge system.

## Quick Start

```bash
npm install
npm run dev
```

The frontend runs on `http://localhost:3000`

## API Endpoints

This frontend interfaces with the following backend endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/submit` | Submit code for evaluation |
| `POST` | `/question/create-question` | Create a new programming problem |
| `GET` | `/question/get/{id}` | Retrieve question details |
| `GET` | `/question/get-stats/{id}` | Get submission statistics |

## Configuration

### CORS

By default, the frontend calls `http://localhost:8080` directly. If you encounter CORS issues, you have two options:

1. **Enable CORS on backend** - Add `@CrossOrigin` annotations to your Spring controllers
2. **Use Vite proxy** - Change `API_BASE` in `App.jsx` to `/api` to use the configured proxy

### Proxy Configuration

The Vite dev server is configured to proxy `/api/*` requests to `http://localhost:8080`. To use this:

```javascript
// In src/App.jsx
const API_BASE = '/api'  // Instead of 'http://localhost:8080'
```

## Test Cases ZIP Format

When creating questions, the ZIP file should have this structure:

```
testcases/
├── judge.cpp        # Custom judge program
└── input/
    ├── 1.txt        # Test case 1 input
    ├── 2.txt        # Test case 2 input
    └── ...
```
