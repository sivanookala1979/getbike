
# --- !Ups

alter table wallet add column notification_seen boolean default 'true';

# --- !Downs
alter table wallet drop column notification_seen;
