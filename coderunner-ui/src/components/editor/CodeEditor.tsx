import { useRef, useEffect } from 'react';
import Editor, { loader, type Monaco } from '@monaco-editor/react';
import type { editor } from 'monaco-editor';
import { Loader2 } from 'lucide-react';

interface CodeEditorProps {
  value: string;
  onChange: (value: string) => void;
  language: string;
  onSubmit?: () => void;
}

const languageMap: Record<string, string> = {
  cpp: 'cpp',
  python: 'python',
  java: 'java',
};

loader.init().then((monaco) => {
  monaco.editor.defineTheme('coderunner-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'comment', foreground: '6A9955', fontStyle: 'italic' },
      { token: 'keyword', foreground: 'C586C0' },
      { token: 'string', foreground: 'CE9178' },
      { token: 'number', foreground: 'B5CEA8' },
      { token: 'type', foreground: '4EC9B0' },
      { token: 'function', foreground: 'DCDCAA' },
      { token: 'variable', foreground: '9CDCFE' },
    ],
    colors: {
      'editor.background': '#0a0a0a',
      'editor.foreground': '#d4d4d4',
      'editor.lineHighlightBackground': '#1a1a1a',
      'editor.selectionBackground': '#264f78',
      'editor.inactiveSelectionBackground': '#3a3d41',
      'editorLineNumber.foreground': '#505050',
      'editorLineNumber.activeForeground': '#c6c6c6',
      'editorCursor.foreground': '#aeafad',
      'editor.selectionHighlightBackground': '#add6ff26',
      'editorIndentGuide.background1': '#404040',
      'editorIndentGuide.activeBackground1': '#707070',
      'editorGutter.background': '#0a0a0a',
      'scrollbar.shadow': '#00000000',
      'scrollbarSlider.background': '#79797966',
      'scrollbarSlider.hoverBackground': '#646464b3',
      'scrollbarSlider.activeBackground': '#bfbfbf66',
    },
  });
});

function EditorLoader() {
  return (
    <div className="flex h-full w-full items-center justify-center bg-[#0a0a0a]">
      <div className="flex flex-col items-center gap-3">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <span className="text-sm text-muted-foreground">Loading editor...</span>
      </div>
    </div>
  );
}

export function CodeEditor({ value, onChange, language, onSubmit }: CodeEditorProps) {
  const onSubmitRef = useRef(onSubmit);

  useEffect(() => {
    onSubmitRef.current = onSubmit;
  }, [onSubmit]);

  const handleEditorMount = (editorInstance: editor.IStandaloneCodeEditor, monaco: Monaco) => {
    editorInstance.addAction({
      id: 'submit-code',
      label: 'Submit Code',
      keybindings: [
        monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter,
      ],
      run: () => {
        onSubmitRef.current?.();
      },
    });
  };

  return (
    <div className="h-full w-full overflow-hidden rounded-none border-0 bg-[#0a0a0a]">
      <Editor
        height="100%"
        width="100%"
        language={languageMap[language] || 'plaintext'}
        value={value}
        onChange={(val) => onChange(val || '')}
        theme="coderunner-dark"
        loading={<EditorLoader />}
        onMount={handleEditorMount}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          fontFamily: "'JetBrains Mono', 'Fira Code', 'Cascadia Code', 'Consolas', monospace",
          fontLigatures: true,
          lineNumbers: 'on',
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          wordWrap: 'on',
          padding: { top: 16, bottom: 16 },
          smoothScrolling: true,
          cursorBlinking: 'smooth',
          cursorSmoothCaretAnimation: 'on',
          renderLineHighlight: 'line',
          renderLineHighlightOnlyWhenFocus: false,
          bracketPairColorization: { enabled: true },
          guides: {
            bracketPairs: true,
            indentation: true,
          },
          scrollbar: {
            vertical: 'auto',
            horizontal: 'auto',
            verticalScrollbarSize: 10,
            horizontalScrollbarSize: 10,
            useShadows: false,
          },
          overviewRulerLanes: 0,
          hideCursorInOverviewRuler: true,
          overviewRulerBorder: false,
          contextmenu: true,
          mouseWheelZoom: true,
          suggest: {
            showKeywords: true,
            showSnippets: true,
          },
        }}
      />
    </div>
  );
}
