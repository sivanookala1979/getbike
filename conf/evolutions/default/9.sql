
# --- !Ups

alter table user add column promo_code varchar(255);

# --- !Downs
alter table user drop column promo_code;
