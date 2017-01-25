
# --- !Ups

alter table user add column is_request_in_progress boolean default 'false';
alter table user add column current_request_ride_id bigint;

# --- !Downs
alter table user drop column is_request_in_progress;
alter table user drop column current_request_ride_id;
