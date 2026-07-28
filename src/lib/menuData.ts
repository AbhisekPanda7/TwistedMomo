export type MenuItem = {
  name: string;
  price: number;
  veg: boolean;
  spicy?: 1 | 2 | 3;
  tag?: "bestseller" | "new" | "signature";
};

export type MenuCategory = {
  id: string;
  title: string;
  subtitle: string;
  items: MenuItem[];
};

export const menuCategories: MenuCategory[] = [
  {
    id: "steam",
    title: "Steam Momo's",
    subtitle: "The classic. Soft, juicy, no drama.",
    items: [
      { name: "Veg Steam Momo's", price: 80, veg: true },
      { name: "Soya & Paneer Steam Momo's", price: 90, veg: true },
      { name: "Chicken Steam Momo's", price: 100, veg: false, tag: "bestseller" },
      { name: "Corn Cheese Steam Momo's", price: 120, veg: true },
    ],
  },
  {
    id: "fried",
    title: "Fried Momo's",
    subtitle: "Golden, crisp, zero regrets.",
    items: [
      { name: "Veg Fried Momo's", price: 90, veg: true },
      { name: "Soya & Paneer Fried Momo's", price: 100, veg: true },
      { name: "Chicken Fried Momo's", price: 110, veg: false },
      { name: "Corn Cheese Fried Momo's", price: 130, veg: true },
    ],
  },
  {
    id: "kurkure",
    title: "Kurkure Momo's",
    subtitle: "Extra crunch, built for the crack.",
    items: [
      { name: "Veg Kurkure Momo's", price: 120, veg: true },
      { name: "Soya & Paneer Kurkure Momo's", price: 130, veg: true },
      { name: "Chicken Kurkure Momo's", price: 140, veg: false, tag: "bestseller" },
      { name: "Corn Cheese Kurkure Momo's", price: 150, veg: true },
    ],
  },
  {
    id: "schezwan",
    title: "Schezwan Momo's",
    subtitle: "Tossed in fire. Not for the faint-hearted.",
    items: [
      { name: "Veg Schezwan Momo's", price: 120, veg: true, spicy: 3 },
      { name: "Soya & Paneer Schezwan Momo's", price: 130, veg: true, spicy: 3 },
      { name: "Chicken Schezwan Momo's", price: 140, veg: false, spicy: 3 },
      { name: "Corn Cheese Schezwan Momo's", price: 150, veg: true, spicy: 2 },
    ],
  },
  {
    id: "afghani",
    title: "Afghani Momo's",
    subtitle: "Creamy, smoky, ridiculously rich.",
    items: [
      { name: "Veg Afghani Momo's", price: 140, veg: true },
      { name: "Soya & Paneer Afghani Momo's", price: 160, veg: true },
      { name: "Chicken Afghani Momo's", price: 170, veg: false, tag: "signature" },
      { name: "Corn Cheese Afghani Momo's", price: 170, veg: true },
    ],
  },
  {
    id: "tandoori",
    title: "Tandoori Momo's",
    subtitle: "Smoke. Spice. Pure satisfaction.",
    items: [
      { name: "Veg Tandoori Momo's", price: 140, veg: true, spicy: 2 },
      { name: "Soya & Paneer Tandoori Momo's", price: 160, veg: true, spicy: 2 },
      { name: "Chicken Tandoori Momo's", price: 170, veg: false, spicy: 2, tag: "signature" },
      { name: "Corn Cheese Tandoori Momo's", price: 170, veg: true, spicy: 1 },
    ],
  },
  {
    id: "cbg",
    title: "Chilli Butter Garlic Momo's",
    subtitle: "The twist that started it all.",
    items: [
      { name: "Veg C.B.G Momo's", price: 140, veg: true, spicy: 2 },
      { name: "Soya & Paneer C.B.G Momo's", price: 150, veg: true, spicy: 2 },
      { name: "Chicken C.B.G Momo's", price: 160, veg: false, spicy: 2, tag: "bestseller" },
      { name: "Corn Cheese C.B.G Momo's", price: 160, veg: true, spicy: 1 },
    ],
  },
  {
    id: "chowmein",
    title: "Chowmein / Noodles",
    subtitle: "Because momos need a wingman.",
    items: [
      { name: "Veg Chowmein", price: 110, veg: true },
      { name: "Egg Chowmein", price: 130, veg: false },
      { name: "Chicken Chowmein", price: 150, veg: false },
    ],
  },
];

export const signatureSpecials = [
  {
    id: "cbg-chicken",
    name: "Chilli Butter Garlic",
    desc: "Our founding twist — tossed in molten garlic butter and cracked chilli until it screams.",
    price: 160,
    badge: "The Original Twist",
  },
  {
    id: "tandoori-chicken",
    name: "Tandoori Momos",
    desc: "Marinated overnight, grilled over open flame. Smoky outside, juicy riot inside.",
    price: 170,
    badge: "Fan Favourite",
  },
  {
    id: "afghani-chicken",
    name: "Afghani Momos",
    desc: "A creamy, smoky hug of a momo. Rich enough to ruin all other momos for you.",
    price: 170,
    badge: "Boozi's Pick",
  },
  {
    id: "kurkure-chicken",
    name: "Kurkure Momos",
    desc: "Double-crunch shell, molten centre. The one that makes noise when you bite it.",
    price: 140,
    badge: "Most Addictive",
  },
];
