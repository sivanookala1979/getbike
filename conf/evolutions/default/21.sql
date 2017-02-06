
# --- !Ups
alter table wallet add column is_amount_paid_status varchar(255);
alter table wallet add column status_acted_at timestamp;

# --- !Downs
alter table wallet DROP column is_amount_paid_status;
alter table wallet DROP column status_acted_at;
