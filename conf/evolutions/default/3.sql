
# --- !Ups

alter table user add column occupation varchar(255);
alter table user add column city varchar(255);
alter table user add column year_of_birth varchar(255);
alter table user add column home_location varchar(255);
alter table user add column office_location varchar(255);
alter table user add column profile_image varchar(255);
alter table user add column mobile_verified boolean default 'false';

# --- !Downs
alter table user drop column occupation;
alter table user drop column city;
alter table user drop column year_of_birth;
alter table user drop column home_location;
alter table user drop column office_location;
alter table user drop column profile_image;
alter table user drop column mobile_verified;
