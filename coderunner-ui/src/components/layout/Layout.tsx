import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';

interface LayoutProps {
  isDark: boolean;
  toggleDark: () => void;
}

export function Layout({ isDark, toggleDark }: LayoutProps) {
  return (
    <div className="min-h-screen bg-background">
      <Navbar isDark={isDark} toggleDark={toggleDark} />
      <main>
        <Outlet />
      </main>
    </div>
  );
}
