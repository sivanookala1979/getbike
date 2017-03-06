
# --- !Ups
alter table system_settings add column description varchar(255);

# --- !Downs
alter table system_settings DROP column description;
