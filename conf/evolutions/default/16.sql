
# --- !Ups
alter table login_otp add column phone_number varchar(4096);
alter table user add column spe_price double;
alter table user add column is_special_price boolean default 'false';


# --- !Downs
alter table login_otp drop column phone_number;
alter table user add column spe_price;
alter table user add column is_special_price;
