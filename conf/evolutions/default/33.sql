
# --- !Ups
alter table ride add column ride_type varchar(255);
alter table ride add column parcel_pickup_number varchar(255);
alter table ride add column parcel_dropoff_number varchar(255);
alter table ride add column parcel_pickup_image_name varchar(1024);
alter table ride add column parcel_dropoff_image_name varchar(1024);

# --- !Downs
alter table ride DROP column ride_type;
alter table ride DROP column parcel_pickup_number;
alter table ride DROP column parcel_dropoff_number;
alter table ride DROP column parcel_pickup_image_name;
alter table ride DROP column parcel_dropoff_image_name;
