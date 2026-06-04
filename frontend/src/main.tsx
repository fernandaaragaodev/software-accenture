import { StrictMode, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import { AppRoutes } from './routes/AppRoutes';
import { hydrateAuthFromStorage } from './services/authService';
import './index.css';

function AppBootstrap() {
  useEffect(() => {
    hydrateAuthFromStorage();
  }, []);

  return <AppRoutes />;
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppBootstrap />
  </StrictMode>,
);
