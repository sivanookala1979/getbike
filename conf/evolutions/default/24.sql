
# --- !Ups
alter table ride DROP column is_paid;
alter table ride add column is_paid boolean default 'false';

# --- !Downs
