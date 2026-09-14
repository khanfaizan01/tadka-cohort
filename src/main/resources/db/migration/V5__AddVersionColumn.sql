-- Tadka V5: Add version column for optimistic locking (ADR-012 revised).
-- The original xmin system column approach had issues with @SQLUpdate bypassing
-- Hibernate's row-count checking. Use a dedicated integer version column that
-- Hibernate's @Version mechanism properly enforces via affected-row-count checks.

ALTER TABLE ordering.orders ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
