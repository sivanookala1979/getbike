
# --- !Ups
alter table ride drop constraint ck_ride_ride_status;
alter table ride add constraint ck_ride_ride_status check (ride_status in ('RideRequested','RideAccepted','RideClosed', 'RideCancelled', 'Rescheduled', 'RideStarted'));

alter table ride add column parcel_request_raised_at timestamp;

# --- !Downs
alter table ride DROP column parcel_request_raised_at;
