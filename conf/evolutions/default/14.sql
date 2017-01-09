
# --- !Ups

alter table ride add column ride_started boolean default 'false';

# --- !Downs
alter table ride drop column ride_started;
