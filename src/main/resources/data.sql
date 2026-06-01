-- ============================
-- Prodee Seed Data
-- ============================

-- Roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER') ON CONFLICT (id) DO NOTHING;
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN') ON CONFLICT (id) DO NOTHING;

-- Shop Items
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(1, 'Dark Dungeon Theme', 'A moody dungeon theme for your dashboard', 'THEME', 50, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(2, 'Cozy Cottage Theme', 'Warm fireplace vibes for your workspace', 'THEME', 75, 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(3, 'Neon Cyberpunk Theme', 'Futuristic neon colors', 'THEME', 120, 5) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(4, 'Pixel Knight Helmet', 'A shiny 8-bit helmet for your avatar', 'AVATAR_PROP', 30, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(5, 'Mage Staff', 'A mystical staff prop', 'AVATAR_PROP', 60, 3) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(6, 'Dragon Wings', 'Epic dragon wings for your avatar', 'AVATAR_PROP', 200, 8) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(7, 'Health Potion', 'Restores energy in cohort battles', 'POTION', 15, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(8, 'Speed Boost Potion', 'Run faster in Ghost Mode races!', 'POTION', 25, 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(9, 'Shield Potion', 'Protects you from one hit in battle', 'POTION', 40, 4) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(10, 'Sudoku Token', 'Unlock a 60-second Sudoku brain-break', 'MINI_GAME_TOKEN', 10, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(11, 'Snake Token', 'Unlock a 60-second Snake brain-break', 'MINI_GAME_TOKEN', 10, 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO shop_items (id, name, description, category, price, level_required) VALUES
(12, 'Tic-Tac-Toe Token', 'Challenge the AI to a quick game', 'MINI_GAME_TOKEN', 10, 1) ON CONFLICT (id) DO NOTHING;

-- Scrapbook Stickers
INSERT INTO stickers (id, name, image_url, price) VALUES
(1, 'Star Burst', '/stickers/star-burst.svg', 12) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(2, 'Pixel Heart', '/stickers/pixel-heart.svg', 15) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(3, 'Magic Spark', '/stickers/magic-spark.svg', 18) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(4, 'Pinecone Charm', 'nature://pinecone', 16) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(5, 'Tiny Sapling', 'nature://sapling', 16) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(6, 'Emerald Oak', 'nature://emerald-oak', 20) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(7, 'Bonsai Grove', 'nature://bonsai-grove', 20) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(8, 'Budding Branch', 'nature://budding-branch', 18) ON CONFLICT (id) DO NOTHING;
INSERT INTO stickers (id, name, image_url, price) VALUES
(9, 'Blossom Tree', 'nature://blossom-tree', 22) ON CONFLICT (id) DO NOTHING;
