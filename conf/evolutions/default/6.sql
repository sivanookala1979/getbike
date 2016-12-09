
# --- !Ups

alter table user add column role varchar(255);
alter table user add column account_holder_name varchar(255);
alter table user add column account_number varchar(255);
alter table user add column ifsc_code varchar(255);
alter table user add column bank_name varchar(255);
alter table user add column branch_name varchar(255);

# --- !Downs
alter table user drop column role;
alter table user add column account_holder_name varchar(255);
alter table user add column account_number varchar(255);
alter table user add column ifsc_code varchar(255);
alter table user add column bank_name varchar(255);
alter table user add column branch_name varchar(255);
