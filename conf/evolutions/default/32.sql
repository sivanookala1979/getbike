
# --- !Ups
alter table user add column driver_availability boolean default 'false';

# --- !Downs
alter table user drop column driver_availability;
