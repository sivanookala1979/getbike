
# --- !Ups

alter table ride drop constraint ck_ride_ride_status;
alter table ride add constraint ck_ride_ride_status check (ride_status in ('RideRequested','RideAccepted','RideClosed', 'RideCancelled'));

# --- !Downs
