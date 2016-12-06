
# --- !Ups

alter table user add column current_ride_id bigint;
alter table user add column  is_ride_in_progress boolean default 'false';
alter table user add column  last_known_latitude double;
alter table user add column  last_known_longitude double;
alter table user add column  last_location_time timestamp;

# --- !Downs
alter table user DROP column  current_ride_id;
alter table user DROP column  is_ride_in_progress;
alter table user DROP column  last_known_latitude;
alter table user DROP column  last_known_longitude;
alter table user DROP column  last_location_time;
