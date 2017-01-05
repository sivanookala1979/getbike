
# --- !Ups

alter table wallet add column pg_details varchar(4096);

# --- !Downs
alter table wallet drop column pg_details;
