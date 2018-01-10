
# --- !Ups
ALTER TABLE ride ALTER COLUMN source_address VARCHAR(MAX);
ALTER TABLE ride ALTER COLUMN destination_address VARCHAR(MAX);

# --- !Downs
