# --- !Ups

alter table ride add column parcel_re_order_id varchar(255);

# --- !Downs
alter table ride drop column parcel_re_order_id;