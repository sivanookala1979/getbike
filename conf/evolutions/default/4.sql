
# --- !Ups

alter table user alter column mobile_verified boolean default 'false';
alter table user alter column is_ride_in_progress boolean default 'false';

# --- !Downs

