import { useEffect, useState } from "react";
import { api } from "./api";
import type { MenuCategory, MenuItem as LegacyMenuItem } from "./menuData";

export type ApiCategory = {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
};

export type ApiMenuItemTag = "BESTSELLER" | "SIGNATURE" | "NEW";

export type ApiMenuItem = {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  price: number;
  imageUrl: string | null;
  veg: boolean;
  spicyLevel: number | null;
  tag: ApiMenuItemTag | null;
  available: boolean;
  displayOrder: number;
  categoryId: number;
  categoryName: string;
  categorySlug: string;
};

export type ApiPageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export async function fetchCategories(): Promise<ApiCategory[]> {
  const { data } = await api.get<ApiCategory[]>("/categories");
  return data;
}

/** Pulls the whole catalog in one page — the site has ~30 items today and the Menu page has no pagination UI. */
export async function fetchAllMenuItems(): Promise<ApiMenuItem[]> {
  const { data } = await api.get<ApiPageResponse<ApiMenuItem>>("/menu", { params: { size: 100 } });
  return data.content;
}

/** Reshapes the flat API response into the MenuCategory[] shape MenuSection/MenuCategoriesPreview already render, so those components need no changes. */
export function toMenuCategories(categories: ApiCategory[], items: ApiMenuItem[]): MenuCategory[] {
  return categories
    .map((category): MenuCategory => ({
      id: category.slug,
      title: category.name,
      subtitle: category.description ?? "",
      items: items
        .filter((item) => item.categoryId === category.id)
        .map((item): LegacyMenuItem => ({
          id: item.id,
          name: item.name,
          price: item.price,
          veg: item.veg,
          spicy: item.spicyLevel === 1 || item.spicyLevel === 2 || item.spicyLevel === 3 ? item.spicyLevel : undefined,
          tag: item.tag ? (item.tag.toLowerCase() as "bestseller" | "new" | "signature") : undefined,
        })),
    }))
    .filter((category) => category.items.length > 0);
}

type UseMenuCategoriesResult = {
  categories: MenuCategory[] | null;
  loading: boolean;
  error: string | null;
};

/** Shared data hook for any component that needs the full menu grouped by category (Menu page, home-page teaser). */
export function useMenuCategories(): UseMenuCategoriesResult {
  const [categories, setCategories] = useState<MenuCategory[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const [apiCategories, apiItems] = await Promise.all([fetchCategories(), fetchAllMenuItems()]);
        if (!cancelled) {
          setCategories(toMenuCategories(apiCategories, apiItems));
        }
      } catch {
        if (!cancelled) {
          setError("Couldn't load the menu right now. Please try again shortly.");
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  return { categories, loading: categories === null && error === null, error };
}
