
# --- !Ups
alter table ride add column is_paid varchar(255);

# --- !Downs
alter table ride DROP column is_paid;
