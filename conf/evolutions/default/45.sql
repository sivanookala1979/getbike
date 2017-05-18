# --- !Ups

alter table ride add column group_ride_id bigint;

# --- !Downs
alter table ride drop column group_ride_id;