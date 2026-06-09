#!/bin/bash
set -e

# Initialize database schema and grant permissions
echo "Running database initialization script..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Any custom postgres setup can go here
EOSQL

echo "Initialization script completed successfully."
