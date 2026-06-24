interface BrandIconProps {
  className?: string;
}

export function BrandIcon({ className = 'brand-icon' }: BrandIconProps) {
  return (
    <img
      src="/accendesk-logo.png"
      alt=""
      className={className}
      aria-hidden="true"
    />
  );
}
