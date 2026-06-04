import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { useAuth } from '../hooks/useAuth';
import { login as loginService } from '../services/authService';

const loginSchema = z.object({
  email: z.string().email('Informe um e-mail válido.'),
  senha: z.string().min(8, 'A senha deve ter no mínimo 8 caracteres.'),
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const from =
    (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/dashboard';

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', senha: '' },
  });

  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  const onSubmit = async (data: LoginForm) => {
    setError(null);
    try {
      await loginService(data);
      navigate(from, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Erro ao fazer login.';
      setError(message);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary-800 to-slate-900 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-900">SGSP / OfficeHub</h1>
          <p className="mt-1 text-sm text-slate-500">Gestão de salas e reservas</p>
        </div>

        {error && (
          <div className="mb-4">
            <ErrorMessage message={error} />
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="E-mail"
            type="email"
            autoComplete="email"
            error={errors.email?.message}
            {...register('email')}
          />
          <Input
            label="Senha"
            type="password"
            autoComplete="current-password"
            error={errors.senha?.message}
            {...register('senha')}
          />
          <Button type="submit" className="w-full" loading={isSubmitting}>
            Entrar
          </Button>
        </form>
      </div>
    </div>
  );
}
