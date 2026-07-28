-- Baseline migration. Proves the Flyway pipeline runs identically against
-- local MySQL and AWS RDS MySQL before any real schema is introduced.
-- Domain tables (Role, User, MenuItem, ...) arrive starting with V2 in
-- Phase 1.
SELECT 1;
