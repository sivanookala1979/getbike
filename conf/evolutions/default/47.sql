# --- !Ups

alter table ride add column ride_comments varchar(255);

# --- !Downs
alter table ride drop column ride_comments;