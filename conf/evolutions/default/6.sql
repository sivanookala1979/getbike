
# --- !Ups

alter table user add column role varchar(255);

# --- !Downs
alter table user drop column role;
