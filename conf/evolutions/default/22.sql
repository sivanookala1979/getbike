
# --- !Ups
alter table ride add column mode_of_payment varchar(255);

# --- !Downs
alter table ride DROP column mode_of_payment;
