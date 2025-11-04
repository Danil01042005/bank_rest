DO $$
BEGIN
  IF NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'bankdb'
  ) THEN
    CREATE DATABASE bankdb;
    ALTER DATABASE bankdb OWNER TO bankuser;
  END IF;
END$$;




