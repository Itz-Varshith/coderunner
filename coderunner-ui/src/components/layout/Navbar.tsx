import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Code2, Moon, Sun, Menu, X } from 'lucide-react';
import { cn } from '@/lib/utils';

interface NavbarProps {
  isDark: boolean;
  toggleDark: () => void;
}

const navLinks = [
  { path: '/problems', label: 'Problems' },
  { path: '/submissions', label: 'Submissions' },
  { path: '/admin', label: 'Admin' },
];

export function Navbar({ isDark, toggleDark }: NavbarProps) {
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const closeMobileMenu = () => setMobileMenuOpen(false);

  return (
    <header className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-14 w-full items-center justify-between px-4 md:px-6">
        <div className="flex items-center">
          <Link to="/" className="flex items-center gap-2.5 font-semibold" onClick={closeMobileMenu}>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
              <Code2 className="h-5 w-5 text-primary-foreground" />
            </div>
            <span className="text-lg tracking-tight">CodeRunner</span>
          </Link>

          <nav className="ml-8 hidden items-center gap-1 md:flex">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                className={cn(
                  'px-4 py-2 text-sm font-medium rounded-md transition-colors',
                  location.pathname === link.path || location.pathname.startsWith(link.path + '/')
                    ? 'bg-secondary text-foreground'
                    : 'text-muted-foreground hover:text-foreground hover:bg-secondary/50'
                )}
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={toggleDark}
            className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
            aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
          >
            {isDark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
          </button>

          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-muted-foreground transition-colors hover:bg-muted hover:text-foreground md:hidden"
            aria-label="Toggle menu"
          >
            {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {mobileMenuOpen && (
        <div className="border-t bg-background md:hidden">
          <nav className="flex flex-col p-4">
            {navLinks.map((link) => (
              <Link
                key={link.path}
                to={link.path}
                onClick={closeMobileMenu}
                className={cn(
                  'px-4 py-3 text-sm font-medium rounded-md transition-colors',
                  location.pathname === link.path || location.pathname.startsWith(link.path + '/')
                    ? 'bg-secondary text-foreground'
                    : 'text-muted-foreground hover:text-foreground hover:bg-secondary/50'
                )}
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>
      )}
    </header>
  );
}
