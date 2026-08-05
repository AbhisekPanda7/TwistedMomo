import { hasRole, type AuthUser } from "./tokenStorage";

/**
 * Where a user lands when they sign in without a page in mind. Highest privilege wins,
 * so someone holding every role starts on the dashboard rather than the shop.
 */
export function landingPathFor(user: AuthUser | null | undefined): string {
  if (hasRole(user, "ADMIN")) return "/admin";
  if (hasRole(user, "RESTAURANT_EMP")) return "/admin/orders";
  return "/";
}
