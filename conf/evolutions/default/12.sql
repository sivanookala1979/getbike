
# --- !Ups

alter table ride add column actual_source_address varchar(255);
alter table ride add column actual_destination_address varchar(255);

# --- !Downs
alter table ride drop column actual_source_address;
alter table ride drop column actual_destination_address;
