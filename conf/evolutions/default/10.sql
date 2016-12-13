
# --- !Ups

alter table ride add column ride_gender varchar(255);

# --- !Downs
alter table ride drop column ride_gender;
