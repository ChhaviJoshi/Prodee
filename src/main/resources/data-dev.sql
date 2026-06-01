-- ============================
-- Prodee Seed Data (H2-compatible)
-- ============================

-- Roles
MERGE INTO roles (id, name) KEY(id) VALUES (1, 'ROLE_USER');
MERGE INTO roles (id, name) KEY(id) VALUES (2, 'ROLE_ADMIN');

-- Shop Items
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(1, 'Dark Dungeon Theme', 'A moody dungeon theme for your dashboard', 'THEME', 50, 1);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(2, 'Cozy Cottage Theme', 'Warm fireplace vibes for your workspace', 'THEME', 75, 2);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(3, 'Neon Cyberpunk Theme', 'Futuristic neon colors', 'THEME', 120, 5);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(4, 'Pixel Knight Helmet', 'A shiny 8-bit helmet for your avatar', 'AVATAR_PROP', 30, 1);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(5, 'Mage Staff', 'A mystical staff prop', 'AVATAR_PROP', 60, 3);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(6, 'Dragon Wings', 'Epic dragon wings for your avatar', 'AVATAR_PROP', 200, 8);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(7, 'Health Potion', 'Restores energy in cohort battles', 'POTION', 15, 1);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(8, 'Speed Boost Potion', 'Run faster in Ghost Mode races!', 'POTION', 25, 2);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(9, 'Shield Potion', 'Protects you from one hit in battle', 'POTION', 40, 4);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(10, 'Sudoku Token', 'Unlock a 60-second Sudoku brain-break', 'MINI_GAME_TOKEN', 10, 1);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(11, 'Snake Token', 'Unlock a 60-second Snake brain-break', 'MINI_GAME_TOKEN', 10, 1);
MERGE INTO shop_items (id, name, description, category, price, level_required) KEY(id) VALUES
(12, 'Tic-Tac-Toe Token', 'Challenge the AI to a quick game', 'MINI_GAME_TOKEN', 10, 1);

-- Scrapbook Stickers
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(1, 'Star Burst', '/stickers/star-burst.svg', 12);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(2, 'Pixel Heart', '/stickers/pixel-heart.svg', 15);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(3, 'Magic Spark', '/stickers/magic-spark.svg', 18);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(4, 'Pinecone Charm', 'nature://pinecone', 16);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(5, 'Tiny Sapling', 'nature://sapling', 16);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(6, 'Emerald Oak', 'nature://emerald-oak', 20);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(7, 'Bonsai Grove', 'nature://bonsai-grove', 20);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(8, 'Budding Branch', 'nature://budding-branch', 18);
MERGE INTO stickers (id, name, image_url, price) KEY(id) VALUES
(9, 'Blossom Tree', 'nature://blossom-tree', 22);
