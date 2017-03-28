
# --- !Ups
alter table user add column prime_rider boolean default 'false';
alter table user add column vendor boolean default 'false';

# --- !Downs
alter table user DROP column prime_rider;
alter table user DROP column vendor;
