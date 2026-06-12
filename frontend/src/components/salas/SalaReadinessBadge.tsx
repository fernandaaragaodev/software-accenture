import { readinessBadgeClass, readinessLabel, type SalaReadiness } from '../../utils/salas';

interface SalaReadinessBadgeProps {
  readiness: SalaReadiness;
}

export function SalaReadinessBadge({ readiness }: SalaReadinessBadgeProps) {
  return (
    <span className={`badge badge-${readinessBadgeClass(readiness)}`}>
      {readinessLabel(readiness)}
    </span>
  );
}
