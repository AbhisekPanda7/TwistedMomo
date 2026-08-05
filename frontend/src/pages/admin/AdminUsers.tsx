import { useEffect, useState } from "react";
import AdminLayout from "../../components/admin/AdminLayout";
import { fetchAdminUsers, grantRole, revokeRole, type AdminUser } from "../../lib/users";
import { extractErrorMessage } from "../../lib/cart";

// CUSTOMER is granted at registration for everyone; toggling it here would only confuse.
const ASSIGNABLE_ROLES = ["ADMIN", "RESTAURANT_EMP"] as const;

export default function AdminUsers() {
  const [users, setUsers] = useState<AdminUser[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState<string | null>(null);

  useEffect(() => {
    fetchAdminUsers()
      .then((page) => setUsers(page.content))
      .catch((err) => setError(extractErrorMessage(err, "Couldn't load users.")));
  }, []);

  async function toggle(user: AdminUser, role: string) {
    const key = `${user.id}:${role}`;
    setPending(key);
    setError(null);
    try {
      const updated = user.roles.includes(role)
        ? await revokeRole(user.id, role)
        : await grantRole(user.id, role);
      setUsers((current) => current?.map((u) => (u.id === updated.id ? updated : u)) ?? current);
    } catch (err) {
      // The last-admin guard answers 409 with a message worth showing verbatim.
      setError(extractErrorMessage(err, "Couldn't change that role."));
    } finally {
      setPending(null);
    }
  }

  return (
    <AdminLayout>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h2 className="font-display text-xl uppercase tracking-wide text-paper-50">Users</h2>
      </div>

      {error && (
        <p className="mb-4 rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
          {error}
        </p>
      )}

      {!users && !error && <p className="font-sans text-sm uppercase tracking-widest text-paper-200/50">Loading…</p>}

      {users && (
        <div className="overflow-x-auto rounded-2xl border border-ink-600">
          <table className="w-full min-w-[760px] border-collapse font-sans text-sm">
            <thead>
              <tr className="border-b border-ink-600 bg-ink-900 text-left text-xs font-bold uppercase tracking-wider text-paper-200/50">
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">Phone</th>
                <th className="px-4 py-3">Roles</th>
                <th className="px-4 py-3">Joined</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b border-ink-700 bg-ink-950 last:border-0">
                  <td className="px-4 py-3 font-semibold text-paper-50">{user.name}</td>
                  <td className="px-4 py-3 text-paper-100/80">{user.email}</td>
                  <td className="px-4 py-3 text-paper-200/60">{user.phone ?? "—"}</td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-2">
                      {ASSIGNABLE_ROLES.map((role) => {
                        const held = user.roles.includes(role);
                        const key = `${user.id}:${role}`;
                        return (
                          <button
                            key={role}
                            type="button"
                            disabled={pending === key}
                            onClick={() => toggle(user, role)}
                            data-cursor-hover
                            className={`rounded-full border px-3 py-1 font-sans text-[10px] font-bold uppercase tracking-wider transition-colors disabled:cursor-not-allowed disabled:opacity-50 ${
                              held
                                ? "border-gold-400 bg-gold-400/10 text-gold-400"
                                : "border-ink-600 text-paper-200/60 hover:border-gold-400/40"
                            }`}
                          >
                            {role}
                          </button>
                        );
                      })}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-paper-200/50">{new Date(user.createdAt).toLocaleString()}</td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-paper-200/50">
                    No users found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </AdminLayout>
  );
}
