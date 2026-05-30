/** Format ISO datetime string to readable date */
export function formatDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
  });
}

/** Truncate text to maxLength with ellipsis */
export function truncate(text, maxLength = 120) {
  if (!text) return '';
  return text.length > maxLength ? text.slice(0, maxLength) + '…' : text;
}

/** Format rating to 1 decimal place */
export function formatRating(rating) {
  if (rating === null || rating === undefined) return 'N/A';
  return Number(rating).toFixed(1);
}

/** Generate a consistent placeholder gradient color from a movie id */
export function posterGradient(id) {
  const colors = [
    ['#1a1a2e','#16213e'], ['#0f3460','#533483'],
    ['#2d132c','#ee4540'], ['#1b262c','#0f3460'],
    ['#231942','#5e548e'], ['#1d3557','#457b9d'],
  ];
  const idx = (parseInt(id || '0', 10)) % colors.length;
  return `linear-gradient(135deg, ${colors[idx][0]}, ${colors[idx][1]})`;
}
