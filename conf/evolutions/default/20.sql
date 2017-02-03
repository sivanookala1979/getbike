
# --- !Ups

create table rider_position (
  id                            bigint not null,
  user_id                       bigint,
  last_known_latitude           double,
  last_known_longitude          double,
  last_location_time            timestamp,
  constraint pk_rider_locations primary key (id)
);
create sequence rider_position_seq;


# --- !Downs

drop table rider_position;
drop sequence  rider_position_seq;