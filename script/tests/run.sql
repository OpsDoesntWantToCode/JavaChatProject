\set ON_ERROR_STOP on
-- Entry point: ONLY a newly created java_chat_test_<hex> database.
-- This guard executes before database.sql creates anything.
DO $$
BEGIN
    IF current_database() !~ '^java_chat_test_[0-9a-f]{32}$' THEN
        RAISE EXCEPTION 'Refusing non-test database: %', current_database();
    END IF;
    IF EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
               WHERE n.nspname NOT IN ('pg_catalog','information_schema')
                 AND n.nspname NOT LIKE 'pg_toast%' AND n.nspname NOT LIKE 'pg_temp%'
                 AND c.relkind IN ('r','p','v','m','S','f')) THEN
        RAISE EXCEPTION 'Test database must be empty';
    END IF;
END;
$$;
\ir ../database.sql
\ir constraints.sql
\echo Schema and constraint tests completed successfully. No concurrency or AEAD tests were run.
