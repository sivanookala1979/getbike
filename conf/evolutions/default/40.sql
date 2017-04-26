
# --- !Ups
alter table cash_in_advance add column rider_id bigint;

alter table ride drop constraint ck_ride_ride_status;
alter table ride add constraint ck_ride_ride_status check (ride_status in ('RideRequested','RideAccepted','RideClosed', 'RideCancelled', 'Rescheduled'));

alter table ride add column cod_amount double;

# --- !Downs
alter table cash_in_advance DROP column rider_id;

alter table ride DROP column cod_amount;
