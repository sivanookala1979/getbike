
# --- !Ups
alter table ride add column parcel_pickup_details varchar(255);
alter table ride add column parcel_dropoff_details varchar(255);


# --- !Downs
alter table ride DROP column parcel_pickup_details;
alter table ride DROP column parcel_dropoff_details;
