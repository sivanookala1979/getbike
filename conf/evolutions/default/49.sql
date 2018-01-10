
# --- !Ups
ALTER TABLE ride ALTER COLUMN source_address text;
ALTER TABLE ride ALTER COLUMN destination_address text;

# --- !Downs
