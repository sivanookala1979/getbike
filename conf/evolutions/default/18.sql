
# --- !Ups

alter table user add column signup_promo_code varchar(255);
alter table user add column free_rides_earned int default 0;
alter table user add column free_rides_spent int default 0;
alter table ride add column free_ride boolean default false;
alter table ride add column free_ride_discount double;

# --- !Downs
alter table user drop column signup_promo_code;
alter table user drop column free_rides_earned;
alter table user drop column free_rides_spent;
alter table ride drop column free_ride;
alter table ride drop column free_ride_discount;
