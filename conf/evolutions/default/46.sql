# --- !Ups

alter table ride add column is_group_ride boolean default 'false';

# --- !Downs
alter table ride drop column is_group_ride;