import { useEffect, useState, type FormEvent } from "react";
import AdminLayout from "../../components/admin/AdminLayout";
import Modal from "../../components/admin/Modal";
import { Button } from "../../components/ui/Button";
import {
  createCategory,
  deleteCategory,
  fetchAdminCategories,
  updateCategory,
  type CategoryPayload,
} from "../../lib/admin";
import { extractErrorMessage } from "../../lib/cart";
import type { ApiCategory } from "../../lib/catalog";

const inputClass =
  "w-full rounded-xl border border-ink-600 bg-ink-950 px-4 py-2.5 font-sans text-sm text-paper-50 placeholder:text-paper-200/30 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";
const labelClass = "mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60";

const emptyForm: CategoryPayload = { name: "", slug: "", description: "", displayOrder: 0, active: true };

export default function AdminCategories() {
  const [categories, setCategories] = useState<ApiCategory[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<CategoryPayload>(emptyForm);
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  function load() {
    fetchAdminCategories()
      .then(setCategories)
      .catch((err) => setError(extractErrorMessage(err, "Couldn't load categories.")));
  }

  useEffect(load, []);

  function openCreate() {
    setEditingId(null);
    setForm(emptyForm);
    setFormError(null);
    setModalOpen(true);
  }

  function openEdit(category: ApiCategory) {
    setEditingId(category.id);
    setForm({
      name: category.name,
      slug: category.slug,
      description: category.description ?? "",
      displayOrder: category.displayOrder,
      active: category.active,
    });
    setFormError(null);
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFormError(null);
    setSaving(true);
    try {
      if (editingId) {
        await updateCategory(editingId, form);
      } else {
        await createCategory(form);
      }
      setModalOpen(false);
      load();
    } catch (err) {
      setFormError(extractErrorMessage(err, "Couldn't save this category."));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(category: ApiCategory) {
    if (!window.confirm(`Delete category "${category.name}"? This only works if it has no menu items.`)) return;
    try {
      await deleteCategory(category.id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't delete this category."));
    }
  }

  return (
    <AdminLayout>
      <div className="mb-6 flex items-center justify-between">
        <h2 className="font-display text-xl uppercase tracking-wide text-paper-50">Categories</h2>
        <Button variant="outline" magnetic={false} onClick={openCreate}>
          + New Category
        </Button>
      </div>

      {error && (
        <p className="mb-4 rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
          {error}
        </p>
      )}

      {!categories && !error && (
        <p className="font-sans text-sm uppercase tracking-widest text-paper-200/50">Loading…</p>
      )}

      {categories && (
        <div className="overflow-x-auto rounded-2xl border border-ink-600">
          <table className="w-full min-w-[640px] border-collapse font-sans text-sm">
            <thead>
              <tr className="border-b border-ink-600 bg-ink-900 text-left text-xs font-bold uppercase tracking-wider text-paper-200/50">
                <th className="px-4 py-3">Name</th>
                <th className="px-4 py-3">Slug</th>
                <th className="px-4 py-3">Order</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((cat) => (
                <tr key={cat.id} className="border-b border-ink-700 bg-ink-950 last:border-0">
                  <td className="px-4 py-3 text-paper-50">{cat.name}</td>
                  <td className="px-4 py-3 text-paper-200/60">{cat.slug}</td>
                  <td className="px-4 py-3 text-paper-200/60">{cat.displayOrder}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider ${
                        cat.active ? "bg-green-500/20 text-green-500" : "bg-paper-200/15 text-paper-200/60"
                      }`}
                    >
                      {cat.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button onClick={() => openEdit(cat)} data-cursor-hover className="mr-4 font-bold uppercase tracking-wider text-xs text-gold-400 hover:underline">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(cat)} data-cursor-hover className="font-bold uppercase tracking-wider text-xs text-chili-500 hover:underline">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {categories.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-paper-200/50">
                    No categories yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editingId ? "Edit Category" : "New Category"}>
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <p className="rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
              {formError}
            </p>
          )}
          <div>
            <label className={labelClass}>Name</label>
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className={inputClass} />
          </div>
          <div>
            <label className={labelClass}>Slug</label>
            <input
              required
              value={form.slug}
              onChange={(e) => setForm({ ...form, slug: e.target.value })}
              className={inputClass}
              placeholder="lowercase-kebab-case"
            />
          </div>
          <div>
            <label className={labelClass}>Description</label>
            <textarea
              rows={2}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              className={inputClass}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={labelClass}>Display Order</label>
              <input
                type="number"
                value={form.displayOrder}
                onChange={(e) => setForm({ ...form, displayOrder: Number(e.target.value) })}
                className={inputClass}
              />
            </div>
            <div className="flex items-end pb-2.5">
              <label className="flex items-center gap-2 font-sans text-sm text-paper-100">
                <input
                  type="checkbox"
                  checked={form.active ?? true}
                  onChange={(e) => setForm({ ...form, active: e.target.checked })}
                  className="h-4 w-4 accent-gold-400"
                />
                Active
              </label>
            </div>
          </div>
          <Button type="submit" className="w-full" disabled={saving}>
            {saving ? "Saving…" : "Save Category"}
          </Button>
        </form>
      </Modal>
    </AdminLayout>
  );
}
