-- Run only after database.sql in a NEW disposable database named
-- java_chat_email_test_<12 lowercase hex digits>. All test rows roll back.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path = public, pg_catalog;

DO $$
BEGIN
    IF current_database() !~ '^java_chat_email_test_[0-9a-f]{12}$' THEN
        RAISE EXCEPTION 'Use a dedicated java_chat_email_test_<12 hex digits> database';
    END IF;
    IF EXISTS (SELECT 1 FROM users) THEN
        RAISE EXCEPTION 'Expected an empty users table in the disposable test database';
    END IF;
END;
$$;

CREATE TEMP TABLE email_check_results (label TEXT NOT NULL) ON COMMIT DROP;

CREATE FUNCTION pg_temp.email_check(ok BOOLEAN, label TEXT) RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
    IF ok IS DISTINCT FROM TRUE THEN
        RAISE EXCEPTION 'FAIL: %', label;
    END IF;
    INSERT INTO email_check_results VALUES (label);
END;
$$;

CREATE FUNCTION pg_temp.email_expect_unique(statement TEXT, expected_constraint TEXT, label TEXT)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE actual_constraint TEXT;
BEGIN
    BEGIN
        EXECUTE statement;
    EXCEPTION WHEN unique_violation THEN
        GET STACKED DIAGNOSTICS actual_constraint = CONSTRAINT_NAME;
        PERFORM pg_temp.email_check(actual_constraint = expected_constraint, label);
        RETURN;
    END;
    RAISE EXCEPTION 'FAIL: expected unique_violation: %', label;
END;
$$;

SELECT pg_temp.email_check(
    (SELECT count(*) = 13 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
     WHERE n.nspname = 'public' AND c.relkind = 'r'), '13 application tables');
SELECT pg_temp.email_check(
    (SELECT count(*) = 65 FROM information_schema.columns WHERE table_schema = 'public'),
    '65 application columns');
SELECT pg_temp.email_check(
    (SELECT count(*) = 21 FROM pg_constraint WHERE connamespace = 'public'::regnamespace AND contype = 'f'),
    '21 foreign keys');
SELECT pg_temp.email_check(
    (SELECT count(*) = 31 FROM pg_constraint WHERE connamespace = 'public'::regnamespace AND contype = 'c'),
    '31 check constraints');
SELECT pg_temp.email_check(
    (SELECT count(*) = 33 FROM pg_index i JOIN pg_class t ON t.oid = i.indrelid
     WHERE t.relnamespace = 'public'::regnamespace), '33 application indexes');
SELECT pg_temp.email_check(
    (SELECT count(*) = 2 FROM pg_trigger g JOIN pg_class t ON t.oid = g.tgrelid
     WHERE t.relnamespace = 'public'::regnamespace AND NOT g.tgisinternal), '2 application triggers');
SELECT pg_temp.email_check(
    NOT EXISTS (SELECT 1 FROM pg_constraint
                WHERE conrelid = 'users'::regclass AND conname = 'uq_users_email'),
    'no global email unique constraint');

-- Deliberately unusable authentication markers, never real passwords/hashes.
INSERT INTO users (username, password_hash, full_name, email, status) VALUES
    ('email.active', 'test-only-not-for-login', 'Active owner', 'active@example.test', 'ACTIVE'),
    ('email.locked', 'test-only-not-for-login', 'Locked owner', 'locked@example.test', 'LOCKED'),
    ('email.deleted.one', 'test-only-not-for-login', 'Deleted owner one', 'reusable@example.test', 'DELETED'),
    ('email.deleted.two', 'test-only-not-for-login', 'Deleted owner two', 'reusable@example.test', 'DELETED');

SELECT pg_temp.email_check(
    (SELECT count(*) = 2 FROM users WHERE email = 'reusable@example.test' AND status = 'DELETED'),
    'multiple deleted users may share an email');

INSERT INTO users (username, password_hash, full_name, email)
VALUES ('email.new', 'test-only-not-for-login', 'New owner', 'reusable@example.test');
SELECT pg_temp.email_check(
    (SELECT count(DISTINCT id) = 3 FROM users WHERE email = 'reusable@example.test'),
    'new account gets a distinct identity');

SELECT pg_temp.email_expect_unique(
    $sql$INSERT INTO users (username, password_hash, full_name, email)
         VALUES ('email.duplicate', 'test-only-not-for-login', 'Duplicate', 'reusable@example.test')$sql$,
    'uq_users_email_not_deleted', 'active email cannot be reused by another active account');
SELECT pg_temp.email_expect_unique(
    $sql$INSERT INTO users (username, password_hash, full_name, email, status)
         VALUES ('email.duplicate', 'test-only-not-for-login', 'Duplicate', 'active@example.test', 'LOCKED')$sql$,
    'uq_users_email_not_deleted', 'locked account cannot claim an active email');
SELECT pg_temp.email_expect_unique(
    $sql$INSERT INTO users (username, password_hash, full_name, email)
         VALUES ('email.duplicate', 'test-only-not-for-login', 'Duplicate', 'locked@example.test')$sql$,
    'uq_users_email_not_deleted', 'locked email cannot be reused by an active account');

INSERT INTO users (username, password_hash, full_name, email, status)
VALUES ('email.deleted.active', 'test-only-not-for-login', 'Deleted duplicate', 'active@example.test', 'DELETED');
SELECT pg_temp.email_check(
    (SELECT count(*) = 2 FROM users WHERE email = 'active@example.test'),
    'deleted record can coexist with the current email owner');

SELECT pg_temp.email_expect_unique(
    $sql$UPDATE users SET email = 'locked@example.test' WHERE username = 'email.active'$sql$,
    'uq_users_email_not_deleted', 'profile update cannot claim another locked email');
SELECT pg_temp.email_check(
    (SELECT email = 'active@example.test' FROM users WHERE username = 'email.active'),
    'rejected update leaves original email unchanged');
SELECT pg_temp.email_expect_unique(
    $sql$UPDATE users SET email = 'active@example.test' WHERE username = 'email.locked'$sql$,
    'uq_users_email_not_deleted', 'locked profile update cannot claim another active email');

UPDATE users SET full_name = 'Updated owner', email = 'active@example.test' WHERE username = 'email.active';
SELECT pg_temp.email_check(
    (SELECT full_name = 'Updated owner' FROM users WHERE username = 'email.active'),
    'profile may retain its own email');

-- Login history must keep pointing to the old ID after soft deletion.
INSERT INTO login_history (user_id) SELECT id FROM users WHERE username = 'email.new';
UPDATE users SET status = 'DELETED' WHERE username = 'email.new';
INSERT INTO users (username, password_hash, full_name, email)
VALUES ('email.next', 'test-only-not-for-login', 'Next owner', 'reusable@example.test');
SELECT pg_temp.email_check(
    (SELECT count(*) = 1 FROM users WHERE email = 'reusable@example.test' AND status <> 'DELETED'),
    'soft deletion releases email to exactly one new non-deleted account');
SELECT pg_temp.email_check(
    (SELECT count(*) = 3 FROM users WHERE email = 'reusable@example.test' AND status = 'DELETED'),
    'all deleted owners remain present');
SELECT pg_temp.email_check(
    EXISTS (SELECT 1 FROM login_history h JOIN users u ON u.id = h.user_id
            WHERE u.username = 'email.new' AND u.status = 'DELETED'),
    'history remains attached to the deleted identity');
SELECT pg_temp.email_check(
    NOT EXISTS (SELECT 1 FROM login_history h JOIN users u ON u.id = h.user_id
                WHERE u.username = 'email.next'), 'new identity does not inherit old history');
SELECT pg_temp.email_expect_unique(
    $sql$INSERT INTO users (username, password_hash, full_name, email)
         VALUES ('email.deleted.one', 'test-only-not-for-login', 'Duplicate username', 'unused@example.test')$sql$,
    'uq_users_username', 'deleted username remains reserved');

-- Low-level index protection only; the application still does not allow restore.
SELECT pg_temp.email_expect_unique(
    $sql$UPDATE users SET status = 'ACTIVE' WHERE username = 'email.deleted.one'$sql$,
    'uq_users_email_not_deleted', 'direct status change cannot create a duplicate live email');
UPDATE users SET status = 'ACTIVE' WHERE username = 'email.locked';
UPDATE users SET status = 'LOCKED' WHERE username = 'email.locked';
SELECT pg_temp.email_check(
    (SELECT status = 'LOCKED' FROM users WHERE username = 'email.locked'),
    'lock and unlock retain ownership of the same email');
UPDATE users SET status = 'DELETED' WHERE username = 'email.locked';
INSERT INTO users (username, password_hash, full_name, email)
VALUES ('email.after.lock', 'test-only-not-for-login', 'After locked deletion', 'locked@example.test');
SELECT pg_temp.email_check(
    (SELECT count(*) = 2 FROM users WHERE email = 'locked@example.test'),
    'soft deleting a locked account releases its email');

SELECT format('PASS: %s PostgreSQL email/schema checks; test data rolled back.', count(*))
FROM email_check_results;
ROLLBACK;
