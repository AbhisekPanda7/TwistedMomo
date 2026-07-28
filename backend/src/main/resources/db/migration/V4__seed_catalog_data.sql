-- Seeds the real Twisted Momos menu (ported from the frontend's former
-- static src/lib/menuData.ts) so the catalog is immediately demoable.
INSERT INTO categories (name, slug, description, display_order, active, created_at, updated_at) VALUES
('Steam Momo''s', 'steam', 'The classic. Soft, juicy, no drama.', 1, TRUE, NOW(6), NOW(6)),
('Fried Momo''s', 'fried', 'Golden, crisp, zero regrets.', 2, TRUE, NOW(6), NOW(6)),
('Kurkure Momo''s', 'kurkure', 'Extra crunch, built for the crack.', 3, TRUE, NOW(6), NOW(6)),
('Schezwan Momo''s', 'schezwan', 'Tossed in fire. Not for the faint-hearted.', 4, TRUE, NOW(6), NOW(6)),
('Afghani Momo''s', 'afghani', 'Creamy, smoky, ridiculously rich.', 5, TRUE, NOW(6), NOW(6)),
('Tandoori Momo''s', 'tandoori', 'Smoke. Spice. Pure satisfaction.', 6, TRUE, NOW(6), NOW(6)),
('Chilli Butter Garlic Momo''s', 'cbg', 'The twist that started it all.', 7, TRUE, NOW(6), NOW(6)),
('Chowmein / Noodles', 'chowmein', 'Because momos need a wingman.', 8, TRUE, NOW(6), NOW(6));

INSERT INTO menu_items (category_id, name, slug, price, is_veg, spicy_level, tag, available, display_order, created_at, updated_at) VALUES
((SELECT id FROM categories WHERE slug = 'steam'), 'Veg Steam Momo''s', 'veg-steam-momos', 80.00, TRUE, NULL, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'steam'), 'Soya & Paneer Steam Momo''s', 'soya-paneer-steam-momos', 90.00, TRUE, NULL, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'steam'), 'Chicken Steam Momo''s', 'chicken-steam-momos', 100.00, FALSE, NULL, 'BESTSELLER', TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'steam'), 'Corn Cheese Steam Momo''s', 'corn-cheese-steam-momos', 120.00, TRUE, NULL, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'fried'), 'Veg Fried Momo''s', 'veg-fried-momos', 90.00, TRUE, NULL, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'fried'), 'Soya & Paneer Fried Momo''s', 'soya-paneer-fried-momos', 100.00, TRUE, NULL, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'fried'), 'Chicken Fried Momo''s', 'chicken-fried-momos', 110.00, FALSE, NULL, NULL, TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'fried'), 'Corn Cheese Fried Momo''s', 'corn-cheese-fried-momos', 130.00, TRUE, NULL, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'kurkure'), 'Veg Kurkure Momo''s', 'veg-kurkure-momos', 120.00, TRUE, NULL, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'kurkure'), 'Soya & Paneer Kurkure Momo''s', 'soya-paneer-kurkure-momos', 130.00, TRUE, NULL, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'kurkure'), 'Chicken Kurkure Momo''s', 'chicken-kurkure-momos', 140.00, FALSE, NULL, 'BESTSELLER', TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'kurkure'), 'Corn Cheese Kurkure Momo''s', 'corn-cheese-kurkure-momos', 150.00, TRUE, NULL, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'schezwan'), 'Veg Schezwan Momo''s', 'veg-schezwan-momos', 120.00, TRUE, 3, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'schezwan'), 'Soya & Paneer Schezwan Momo''s', 'soya-paneer-schezwan-momos', 130.00, TRUE, 3, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'schezwan'), 'Chicken Schezwan Momo''s', 'chicken-schezwan-momos', 140.00, FALSE, 3, NULL, TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'schezwan'), 'Corn Cheese Schezwan Momo''s', 'corn-cheese-schezwan-momos', 150.00, TRUE, 2, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'afghani'), 'Veg Afghani Momo''s', 'veg-afghani-momos', 140.00, TRUE, NULL, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'afghani'), 'Soya & Paneer Afghani Momo''s', 'soya-paneer-afghani-momos', 160.00, TRUE, NULL, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'afghani'), 'Chicken Afghani Momo''s', 'chicken-afghani-momos', 170.00, FALSE, NULL, 'SIGNATURE', TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'afghani'), 'Corn Cheese Afghani Momo''s', 'corn-cheese-afghani-momos', 170.00, TRUE, NULL, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'tandoori'), 'Veg Tandoori Momo''s', 'veg-tandoori-momos', 140.00, TRUE, 2, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'tandoori'), 'Soya & Paneer Tandoori Momo''s', 'soya-paneer-tandoori-momos', 160.00, TRUE, 2, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'tandoori'), 'Chicken Tandoori Momo''s', 'chicken-tandoori-momos', 170.00, FALSE, 2, 'SIGNATURE', TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'tandoori'), 'Corn Cheese Tandoori Momo''s', 'corn-cheese-tandoori-momos', 170.00, TRUE, 1, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'cbg'), 'Veg C.B.G Momo''s', 'veg-cbg-momos', 140.00, TRUE, 2, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'cbg'), 'Soya & Paneer C.B.G Momo''s', 'soya-paneer-cbg-momos', 150.00, TRUE, 2, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'cbg'), 'Chicken C.B.G Momo''s', 'chicken-cbg-momos', 160.00, FALSE, 2, 'BESTSELLER', TRUE, 3, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'cbg'), 'Corn Cheese C.B.G Momo''s', 'corn-cheese-cbg-momos', 160.00, TRUE, 1, NULL, TRUE, 4, NOW(6), NOW(6)),

((SELECT id FROM categories WHERE slug = 'chowmein'), 'Veg Chowmein', 'veg-chowmein', 110.00, TRUE, NULL, NULL, TRUE, 1, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'chowmein'), 'Egg Chowmein', 'egg-chowmein', 130.00, FALSE, NULL, NULL, TRUE, 2, NOW(6), NOW(6)),
((SELECT id FROM categories WHERE slug = 'chowmein'), 'Chicken Chowmein', 'chicken-chowmein', 150.00, FALSE, NULL, NULL, TRUE, 3, NOW(6), NOW(6));
