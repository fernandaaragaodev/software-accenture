interface SkeletonProps {
  className?: string;
  width?: string;
  height?: string;
}

export function Skeleton({ className = '', width, height }: SkeletonProps) {
  return (
    <div
      className={`skeleton ${className}`.trim()}
      style={{ width, height }}
      aria-hidden="true"
    />
  );
}

export function SalaCardSkeleton() {
  return (
    <div className="sala-card skeleton-card">
      <Skeleton height="1.25rem" width="60%" />
      <Skeleton height="0.875rem" width="40%" className="mt-sm" />
      <div className="sala-card-meta">
        <Skeleton height="0.75rem" width="30%" />
        <Skeleton height="0.75rem" width="25%" />
      </div>
      <Skeleton height="2rem" width="100%" className="mt-md" />
    </div>
  );
}

export function DetailCardSkeleton() {
  return (
    <div className="card">
      <Skeleton height="1.125rem" width="45%" />
      <Skeleton height="0.875rem" width="80%" className="mt-md" />
      <Skeleton height="0.875rem" width="65%" className="mt-sm" />
      <Skeleton height="0.875rem" width="70%" className="mt-sm" />
    </div>
  );
}
