
# --- !Ups

alter table user add column profile_type varchar(255);

# --- !Downs
alter table user DROP column  profile_type;
