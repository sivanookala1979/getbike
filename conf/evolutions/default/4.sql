
# --- !Ups

update user set mobile_verified = 'false' where mobile_verified is null;
update user set is_ride_in_progress = 'false' where is_ride_in_progress is null;

# --- !Downs

