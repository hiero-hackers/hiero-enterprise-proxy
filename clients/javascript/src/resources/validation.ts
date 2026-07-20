/**
 * Validates that a string parameter is non-empty and returns it URL-encoded.
 * Used across all resource modules for path parameter validation.
 */
export function requireId(value: string, paramName = "id"): string {
  if (!value || typeof value !== "string" || !value.trim()) {
    throw new Error(`${paramName} is required and must be a non-empty string`);
  }
  return encodeURIComponent(value.trim());
}
