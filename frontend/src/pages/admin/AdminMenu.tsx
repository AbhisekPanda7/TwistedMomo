import { useEffect, useRef, useState, type FormEvent } from "react";
import AdminLayout from "../../components/admin/AdminLayout";
import Modal from "../../components/admin/Modal";
import { Button } from "../../components/ui/Button";
import {
  createMenuItem,
  deleteMenuItem,
  fetchAdminCategories,
  fetchAdminMenuItems,
  setMenuItemAvailability,
  updateMenuItem,
  uploadMenuItemImage,
  type MenuItemPayload,
} from "../../lib/admin";
import { extractErrorMessage } from "../../lib/cart";
import type { ApiCategory, ApiMenuItem } from "../../lib/catalog";

const inputClass =
  "w-full rounded-xl border border-ink-600 bg-ink-950 px-4 py-2.5 font-sans text-sm text-paper-50 placeholder:text-paper-200/30 outline-none transition-all duration-200 focus:border-gold-400 focus:ring-4 focus:ring-gold-400/10";
const labelClass = "mb-1.5 block font-sans text-xs font-bold uppercase tracking-wider text-paper-200/60";

function emptyForm(categoryId?: number): MenuItemPayload {
  return {
    categoryId: categoryId ?? 0,
    name: "",
    slug: "",
    description: "",
    price: 0,
    veg: true,
    spicyLevel: undefined,
    tag: undefined,
    available: true,
    displayOrder: 0,
  };
}

export default function AdminMenu() {
  const [categories, setCategories] = useState<ApiCategory[]>([]);
  const [items, setItems] = useState<ApiMenuItem[] | null>(null);
  const [categoryFilter, setCategoryFilter] = useState<number | "">("");
  const [error, setError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<MenuItemPayload>(emptyForm());
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [uploadingId, setUploadingId] = useState<number | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const uploadTargetRef = useRef<number | null>(null);

  function load() {
    fetchAdminMenuItems(categoryFilter || undefined)
      .then(setItems)
      .catch((err) => setError(extractErrorMessage(err, "Couldn't load menu items.")));
  }

  useEffect(() => {
    fetchAdminCategories().then(setCategories).catch(() => {});
  }, []);

  useEffect(load, [categoryFilter]);

  function openCreate() {
    setEditingId(null);
    setForm(emptyForm(categories[0]?.id));
    setFormError(null);
    setModalOpen(true);
  }

  function openEdit(item: ApiMenuItem) {
    setEditingId(item.id);
    setForm({
      categoryId: item.categoryId,
      name: item.name,
      slug: item.slug,
      description: item.description ?? "",
      price: item.price,
      veg: item.veg,
      spicyLevel: item.spicyLevel ?? undefined,
      tag: item.tag ?? undefined,
      available: item.available,
      displayOrder: item.displayOrder,
    });
    setFormError(null);
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setFormError(null);
    setSaving(true);
    try {
      const payload: MenuItemPayload = { ...form, categoryId: Number(form.categoryId) };
      if (editingId) {
        await updateMenuItem(editingId, payload);
      } else {
        await createMenuItem(payload);
      }
      setModalOpen(false);
      load();
    } catch (err) {
      setFormError(extractErrorMessage(err, "Couldn't save this item."));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(item: ApiMenuItem) {
    if (!window.confirm(`Delete "${item.name}"? This cannot be undone.`)) return;
    try {
      await deleteMenuItem(item.id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't delete this item."));
    }
  }

  async function handleToggleAvailability(item: ApiMenuItem) {
    try {
      const updated = await setMenuItemAvailability(item.id, !item.available);
      setItems((prev) => prev?.map((i) => (i.id === item.id ? updated : i)) ?? prev);
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't update availability."));
    }
  }

  function triggerUpload(item: ApiMenuItem) {
    uploadTargetRef.current = item.id;
    fileInputRef.current?.click();
  }

  async function handleFileSelected(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    const targetId = uploadTargetRef.current;
    e.target.value = "";
    if (!file || !targetId) return;
    setUploadingId(targetId);
    try {
      const updated = await uploadMenuItemImage(targetId, file);
      setItems((prev) => prev?.map((i) => (i.id === targetId ? updated : i)) ?? prev);
    } catch (err) {
      setError(extractErrorMessage(err, "Couldn't upload that image."));
    } finally {
      setUploadingId(null);
    }
  }

  return (
    <AdminLayout>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h2 className="font-display text-xl uppercase tracking-wide text-paper-50">Menu Items</h2>
        <div className="flex items-center gap-3">
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value ? Number(e.target.value) : "")}
            className={`${inputClass} w-auto`}
          >
            <option value="">All categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <Button variant="outline" magnetic={false} onClick={openCreate}>
            + New Item
          </Button>
        </div>
      </div>

      <input ref={fileInputRef} type="file" accept="image/jpeg,image/png,image/webp" className="hidden" onChange={handleFileSelected} />

      {error && (
        <p className="mb-4 rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
          {error}
        </p>
      )}

      {!items && !error && <p className="font-sans text-sm uppercase tracking-widest text-paper-200/50">Loading…</p>}

      {items && (
        <div className="overflow-x-auto rounded-2xl border border-ink-600">
          <table className="w-full min-w-[860px] border-collapse font-sans text-sm">
            <thead>
              <tr className="border-b border-ink-600 bg-ink-900 text-left text-xs font-bold uppercase tracking-wider text-paper-200/50">
                <th className="px-4 py-3">Item</th>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3">Price</th>
                <th className="px-4 py-3">Type</th>
                <th className="px-4 py-3">Available</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className="border-b border-ink-700 bg-ink-950 last:border-0">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      {item.imageUrl ? (
                        <img src={item.imageUrl} alt="" className="h-10 w-10 rounded-lg object-cover" />
                      ) : (
                        <div className="h-10 w-10 rounded-lg bg-ink-800" />
                      )}
                      <div className="min-w-0">
                        <p className="truncate text-paper-50">{item.name}</p>
                        <p className="truncate text-xs text-paper-200/40">{item.slug}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-paper-200/60">{item.categoryName}</td>
                  <td className="px-4 py-3 text-gold-400">₹{item.price}</td>
                  <td className="px-4 py-3">
                    <span className={`h-2.5 w-2.5 inline-block rounded-full ${item.veg ? "bg-green-500" : "bg-chili-500"}`} />
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => handleToggleAvailability(item)}
                      data-cursor-hover
                      className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider transition-colors ${
                        item.available ? "bg-green-500/20 text-green-500" : "bg-paper-200/15 text-paper-200/60"
                      }`}
                    >
                      {item.available ? "Available" : "Disabled"}
                    </button>
                  </td>
                  <td className="px-4 py-3 text-right whitespace-nowrap">
                    <button
                      onClick={() => triggerUpload(item)}
                      disabled={uploadingId === item.id}
                      data-cursor-hover
                      className="mr-4 font-bold uppercase tracking-wider text-xs text-paper-200/60 hover:text-gold-400 disabled:opacity-50"
                    >
                      {uploadingId === item.id ? "Uploading…" : "Image"}
                    </button>
                    <button onClick={() => openEdit(item)} data-cursor-hover className="mr-4 font-bold uppercase tracking-wider text-xs text-gold-400 hover:underline">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(item)} data-cursor-hover className="font-bold uppercase tracking-wider text-xs text-chili-500 hover:underline">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {items.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-paper-200/50">
                    No items found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editingId ? "Edit Item" : "New Item"}>
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <p className="rounded-lg border border-chili-500/40 bg-chili-500/10 px-4 py-3 font-sans text-sm text-chili-500">
              {formError}
            </p>
          )}
          <div>
            <label className={labelClass}>Category</label>
            <select
              required
              value={form.categoryId || ""}
              onChange={(e) => setForm({ ...form, categoryId: Number(e.target.value) })}
              className={inputClass}
            >
              <option value="" disabled>
                Select a category
              </option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
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
            <textarea rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} className={inputClass} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={labelClass}>Price (₹)</label>
              <input
                required
                type="number"
                min="0.01"
                step="0.01"
                value={form.price}
                onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
                className={inputClass}
              />
            </div>
            <div>
              <label className={labelClass}>Display Order</label>
              <input
                type="number"
                value={form.displayOrder}
                onChange={(e) => setForm({ ...form, displayOrder: Number(e.target.value) })}
                className={inputClass}
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={labelClass}>Spice Level</label>
              <select
                value={form.spicyLevel ?? ""}
                onChange={(e) => setForm({ ...form, spicyLevel: e.target.value ? Number(e.target.value) : undefined })}
                className={inputClass}
              >
                <option value="">Not spicy</option>
                <option value="1">1 — Mild</option>
                <option value="2">2 — Medium</option>
                <option value="3">3 — Hot</option>
              </select>
            </div>
            <div>
              <label className={labelClass}>Tag</label>
              <select
                value={form.tag ?? ""}
                onChange={(e) => setForm({ ...form, tag: e.target.value || undefined })}
                className={inputClass}
              >
                <option value="">None</option>
                <option value="BESTSELLER">Bestseller</option>
                <option value="SIGNATURE">Signature</option>
                <option value="NEW">New</option>
              </select>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <label className="flex items-center gap-2 font-sans text-sm text-paper-100">
              <input type="checkbox" checked={form.veg} onChange={(e) => setForm({ ...form, veg: e.target.checked })} className="h-4 w-4 accent-gold-400" />
              Vegetarian
            </label>
            <label className="flex items-center gap-2 font-sans text-sm text-paper-100">
              <input
                type="checkbox"
                checked={form.available ?? true}
                onChange={(e) => setForm({ ...form, available: e.target.checked })}
                className="h-4 w-4 accent-gold-400"
              />
              Available
            </label>
          </div>
          <Button type="submit" className="w-full" disabled={saving}>
            {saving ? "Saving…" : "Save Item"}
          </Button>
        </form>
      </Modal>
    </AdminLayout>
  );
}
