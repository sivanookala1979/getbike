
# --- !Ups

alter table user add column mobile_battery_level int;
alter table user add column mobile_signal_level int;
alter table user add column mobile_call_status varchar(255);
alter table user add column mobile_network_operator varchar(255);
alter table user add column mobile_service_state varchar(255);
alter table user add column mobile_operating_system varchar(255);
alter table user add column mobile_IMEI varchar(255);
alter table user add column mobile_brand varchar(255);
alter table user add column mobile_model varchar(255);
alter table user add column mobile_data_connection varchar(255);
alter table user add column last_known_address varchar(255);
# --- !Downs
alter table user DROP column  mobile_battery_level;
alter table user DROP column  mobile_signal_level;
alter table user DROP column  mobile_call_status;
alter table user DROP column  mobile_network_operator;
alter table user DROP column  mobile_service_state;
alter table user DROP column  mobile_operating_system;
alter table user DROP column  mobile_IMEI;
alter table user DROP column  mobile_brand;
alter table user DROP column  mobile_model;
alter table user DROP column  mobile_data_connection;
alter table user DROP column  last_know_address;
