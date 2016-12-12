
# --- !Ups

alter table ride add column rating int;

# --- !Downs
alter table ride drop column rating;
