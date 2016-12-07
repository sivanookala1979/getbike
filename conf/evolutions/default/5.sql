
# --- !Ups

alter table user_login add column role varchar(255);

# --- !Downs
alter table user_login drop column role;
