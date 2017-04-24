
# --- !Ups
alter table ride add column parcel_order_id varchar(255);

# --- !Downs
alter table ride DROP column parcel_order_id;
