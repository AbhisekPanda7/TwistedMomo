-- Spring Modulith event publication registry — the transactional outbox.
--
-- A row is written here in the SAME transaction as the business change that
-- published the event, so a crash between commit and listener execution cannot
-- lose it: incomplete publications are replayed on restart.
--
-- The two CREATE TABLE statements below are copied VERBATIM from Spring
-- Modulith 2.1.0, schemas/v2/schema-mysql.sql and schema-mysql-archive.sql
-- inside spring-modulith-events-jdbc (also published in the reference
-- appendix). Do not reformat, rename, or "improve" them — keeping them
-- byte-identical is what makes a Modulith upgrade a mechanical diff against
-- the jar's current schemas/ directory rather than a judgement call.
--
-- Upgrade checklist: on a Modulith version bump, extract schemas/v{n}/ from
-- the new spring-modulith-events-jdbc jar and diff against this file. A
-- changed schema means a new V{n}__ migration, never an edit to this one.
--
-- We are on starter-jdbc rather than starter-jpa deliberately. Only the JDBC
-- store has a published, versioned DDL; the JPA store derives its schema from
-- entity mappings Modulith is free to change between releases, and
-- spring-modulith#1543 was exactly that — ddl-auto=validate failing on their
-- entity's nullability. Since these tables back no @Entity of ours, they stay
-- off Hibernate's validate surface entirely.
--
-- Creation is Flyway's alone: spring.modulith.events.jdbc.schema-initialization
-- .enabled is explicitly false in application.yml, so Modulith never races
-- Flyway on startup. (It would back off when the tables exist, but relying on
-- that ordering is not the same as forbidding it.)

CREATE TABLE IF NOT EXISTS EVENT_PUBLICATION
(
  ID                     VARCHAR(36) NOT NULL,
  LISTENER_ID            VARCHAR(512) NOT NULL,
  EVENT_TYPE             VARCHAR(512) NOT NULL,
  SERIALIZED_EVENT       VARCHAR(4000) NOT NULL,
  PUBLICATION_DATE       TIMESTAMP(6) NOT NULL,
  COMPLETION_DATE        TIMESTAMP(6) DEFAULT NULL NULL,
  STATUS                 VARCHAR(20),
  COMPLETION_ATTEMPTS    INT,
  LAST_RESUBMISSION_DATE TIMESTAMP(6) DEFAULT NULL NULL,
  PRIMARY KEY (ID),
  INDEX EVENT_PUBLICATION_BY_COMPLETION_DATE_IDX (COMPLETION_DATE)
);

-- Completed publications are moved here rather than deleted, so the hot table
-- stays small while the audit trail survives. Archiving is opt-in via
-- spring.modulith.events.completion-mode, but the table is created now so
-- turning it on later needs no migration.
CREATE TABLE IF NOT EXISTS EVENT_PUBLICATION_ARCHIVE
(
  ID                     VARCHAR(36) NOT NULL,
  LISTENER_ID            VARCHAR(512) NOT NULL,
  EVENT_TYPE             VARCHAR(512) NOT NULL,
  SERIALIZED_EVENT       VARCHAR(4000) NOT NULL,
  PUBLICATION_DATE       TIMESTAMP(6) NOT NULL,
  COMPLETION_DATE        TIMESTAMP(6) DEFAULT NULL NULL,
  STATUS                 VARCHAR(20),
  COMPLETION_ATTEMPTS    INT,
  LAST_RESUBMISSION_DATE TIMESTAMP(6) DEFAULT NULL NULL,
  PRIMARY KEY (ID),
  INDEX EVENT_PUBLICATION_ARCHIVE_BY_COMPLETION_DATE_IDX (COMPLETION_DATE)
);
