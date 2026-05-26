const MINSK_TZ = 'Europe/Minsk';

function normalize(dateStr: string | undefined | null): Date | null {
  if (!dateStr) {
    return null;
  }
  const hasTimezone = /[zZ]|[+-]\d{2}:?\d{2}$/.test(dateStr);
  const normalized = hasTimezone ? dateStr : dateStr + 'Z';
  const date = new Date(normalized);
  return isNaN(date.getTime()) ? null : date;
}

export function formatChatDateTime(dateStr: string | undefined | null): string {
  const date = normalize(dateStr);
  if (!date) {
    return '—';
  }
  return date.toLocaleString('ru-RU', {
    timeZone: MINSK_TZ,
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatChatTime(dateStr: string | undefined | null): string {
  const date = normalize(dateStr);
  if (!date) {
    return '—';
  }
  const now = new Date();
  const isToday = date.toLocaleDateString('ru-RU', { timeZone: MINSK_TZ })
    === now.toLocaleDateString('ru-RU', { timeZone: MINSK_TZ });
  return date.toLocaleString('ru-RU', isToday
    ? { timeZone: MINSK_TZ, hour: '2-digit', minute: '2-digit' }
    : { timeZone: MINSK_TZ, day: '2-digit', month: '2-digit' });
}

export function compareBySentAt(a: { sentAt: string }, b: { sentAt: string }): number {
  const aTime = normalize(a.sentAt)?.getTime() ?? 0;
  const bTime = normalize(b.sentAt)?.getTime() ?? 0;
  return aTime - bTime;
}
