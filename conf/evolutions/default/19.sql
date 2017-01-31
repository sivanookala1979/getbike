
# --- !Ups

alter table user add column app_version varchar(255);

# --- !Downs
alter table user drop column app_version;
