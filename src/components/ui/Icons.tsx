type IconProps = { className?: string };

export function DumplingIcon({ className = "w-8 h-8" }: IconProps) {
  return (
    <svg viewBox="0 0 48 48" fill="currentColor" className={className}>
      <path d="M6 22 Q8 16 12 22 Q14 16 18 22 Q20 16 24 22 Q26 16 30 22 Q32 16 36 22 Q40 16 42 22 C42 34 34 40 24 40 C14 40 6 34 6 22 Z" />
      <circle cx="24" cy="15.5" r="2.2" />
    </svg>
  );
}

export function ChiliIcon({ className = "w-8 h-8" }: IconProps) {
  return (
    <svg viewBox="0 0 48 48" fill="currentColor" className={className}>
      <path d="M17 7c1.6-2 4.2-3.3 6-2.7-.6 1.6-2 2.6-3.4 3.4 2.2-.4 4.6.2 6 1.6 5.6 5.6 4.4 16.4-2.6 23.4-7 7-17.8 8.2-23.4 2.6C-4.9 30.8-2.4 20 5 12.6 8.6 9 13.4 7.2 17 7Z" />
    </svg>
  );
}

export function StarIcon({ className = "w-4 h-4" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
      <path d="M12 1.5l3.09 6.26 6.91 1-5 4.87 1.18 6.87L12 17.27l-6.18 3.23L7 13.63l-5-4.87 6.91-1z" />
    </svg>
  );
}

export function SteamIcon({ className = "w-6 h-6" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} stroke="currentColor" strokeWidth="1.6">
      <path d="M7 3c0 2-2 2-2 4s2 2 2 4" strokeLinecap="round" />
      <path d="M12 3c0 2-2 2-2 4s2 2 2 4" strokeLinecap="round" />
      <path d="M17 3c0 2-2 2-2 4s2 2 2 4" strokeLinecap="round" />
    </svg>
  );
}

export function MotorcycleIcon({ className = "w-8 h-8" }: IconProps) {
  return (
    <svg viewBox="0 0 48 48" fill="none" className={className} stroke="currentColor" strokeWidth="1.6">
      <circle cx="10" cy="34" r="6" />
      <circle cx="36" cy="34" r="6" />
      <path d="M10 34h6l6-12h9l7 12h4" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M22 22l6-6h6" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M16 34h14" strokeLinecap="round" />
    </svg>
  );
}

export function FlameIcon({ className = "w-4 h-4" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
      <path d="M12 2c1 3-3 4-3 8a3 3 0 0 0 6 0c1 1 2 2.5 2 4.5A5.5 5.5 0 0 1 6 14.5C6 9 12 7 12 2Z" />
    </svg>
  );
}

export function LeafIcon({ className = "w-3.5 h-3.5" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
      <path d="M4 20C4 10 10 4 20 4c0 10-6 16-16 16Z" />
    </svg>
  );
}

export function InstagramIcon({ className = "w-4 h-4" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} stroke="currentColor" strokeWidth="1.8">
      <rect x="3" y="3" width="18" height="18" rx="5" />
      <circle cx="12" cy="12" r="4.2" />
      <circle cx="17.2" cy="6.8" r="1.1" fill="currentColor" stroke="none" />
    </svg>
  );
}

export function FacebookIcon({ className = "w-4 h-4" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
      <path d="M14 22v-8h2.7l.4-3.2H14V8.6c0-.9.3-1.6 1.7-1.6h1.5V4.2C16.9 4.1 15.8 4 14.6 4 12 4 10.2 5.6 10.2 8.3v2.5H7.5V14h2.7v8Z" />
    </svg>
  );
}

export function WhatsAppIcon({ className = "w-4 h-4" }: IconProps) {
  return (
    <svg viewBox="0 0 24 24" fill="none" className={className} stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 20l1.4-4.2A8 8 0 1 1 8.9 19.2Z" />
      <circle cx="9" cy="12" r="0.9" fill="currentColor" stroke="none" />
      <circle cx="12" cy="12" r="0.9" fill="currentColor" stroke="none" />
      <circle cx="15" cy="12" r="0.9" fill="currentColor" stroke="none" />
    </svg>
  );
}
