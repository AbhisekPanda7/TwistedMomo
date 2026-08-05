-- The user_roles join table has been the source of truth since V8. This column
-- has had no Java mapping since the multi-role migration; drop the FK first.
ALTER TABLE users DROP FOREIGN KEY fk_users_role;
ALTER TABLE users DROP COLUMN role_id;
