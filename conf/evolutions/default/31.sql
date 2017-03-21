
# --- !Ups
alter table user add column app_tutorial_status boolean default 'false';

# --- !Downs
alter table user drop column app_tutorial_status;
