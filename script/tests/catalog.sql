-- Read-only inspection of a test DB created by run.ps1; no application data.
\set ON_ERROR_STOP on
DO $$
BEGIN
    IF current_database() !~ '^java_chat_test_[0-9a-f]{32}$' THEN
        RAISE EXCEPTION 'Catalog export requires an isolated test database';
    END IF;
END;
$$;
SELECT jsonb_pretty(jsonb_build_object(
  'database',current_database(), 'server_version',current_setting('server_version'),
  'tables',(SELECT jsonb_agg(jsonb_build_object(
    'name',t.relname,
    'columns',(SELECT jsonb_agg(jsonb_build_object('name',a.attname,
      'type',format_type(a.atttypid,a.atttypmod),'not_null',a.attnotnull,
      'identity',a.attidentity,'default',pg_get_expr(d.adbin,d.adrelid)) ORDER BY a.attnum)
      FROM pg_attribute a LEFT JOIN pg_attrdef d ON d.adrelid=a.attrelid AND d.adnum=a.attnum
      WHERE a.attrelid=t.oid AND a.attnum>0 AND NOT a.attisdropped),
    'constraints',(SELECT jsonb_agg(jsonb_build_object('name',c.conname,'kind',c.contype,
      'definition',pg_get_constraintdef(c.oid,true)) ORDER BY c.contype,c.conname)
      FROM pg_constraint c WHERE c.conrelid=t.oid),
    'indexes',(SELECT jsonb_agg(jsonb_build_object('name',ci.relname,
      'definition',pg_get_indexdef(i.indexrelid)) ORDER BY ci.relname)
      FROM pg_index i JOIN pg_class ci ON ci.oid=i.indexrelid WHERE i.indrelid=t.oid),
    'triggers',(SELECT jsonb_agg(jsonb_build_object('name',g.tgname,
      'definition',pg_get_triggerdef(g.oid,true)) ORDER BY g.tgname)
      FROM pg_trigger g WHERE g.tgrelid=t.oid AND NOT g.tgisinternal)) ORDER BY t.relname)
    FROM pg_class t WHERE t.relnamespace='public'::regnamespace AND t.relkind='r')));
