interface LoadingProps {
  message?: string;
  fullScreen?: boolean;
}

export function Loading({ message = 'Carregando...', fullScreen }: LoadingProps) {
  const content = (
    <div className="flex flex-col items-center gap-3">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-primary-100 border-t-primary-600" />
      <p className="text-sm text-slate-600">{message}</p>
    </div>
  );

  if (fullScreen) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">{content}</div>
    );
  }

  return <div className="flex justify-center py-12">{content}</div>;
}
