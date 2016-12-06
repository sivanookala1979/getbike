
# --- !Ups

create table login_otp (
  id                            bigint not null,
  user_id                       bigint,
  generated_otp                 varchar(255),
  created_at                    timestamp,
  constraint pk_login_otp primary key (id)
);
create sequence login_otp_seq;

create table ride (
  id                            bigint not null,
  requestor_id                  bigint,
  rider_id                      bigint,
  ride_status                   varchar(13),
  order_distance                double,
  order_amount                  double,
  requested_at                  timestamp,
  accepted_at                   timestamp,
  ride_started_at               timestamp,
  ride_ended_at                 timestamp,
  start_latitude                double,
  start_longitude               double,
  source_address                varchar(255),
  destination_address           varchar(255),
  total_fare                    double,
  taxes_and_fees                double,
  sub_total                     double,
  rounding_off                  double,
  total_bill                    double,
  constraint ck_ride_ride_status check (ride_status in ('RideRequested','RideAccepted','RideClosed')),
  constraint pk_ride primary key (id)
);
create sequence ride_seq;

create table ride_location (
  id                            bigint not null,
  ride_id                       bigint,
  posted_by_id                  bigint,
  location_time                 timestamp,
  received_at                   timestamp,
  latitude                      double,
  longitude                     double,
  before_ride                   boolean,
  during_ride                   boolean,
  constraint pk_ride_location primary key (id)
);
create sequence ride_location_seq;

create table system_settings (
  id                            bigint not null,
  key                           varchar(255),
  value                         varchar(255),
  constraint pk_system_settings primary key (id)
);
create sequence system_settings_seq;

create table user (
  id                            bigint not null,
  name                          varchar(255),
  email                         varchar(255),
  password                      varchar(255),
  phone_number                  varchar(255),
  gender                        varchar(255),
  auth_token                    varchar(255),
  gcm_code                      varchar(1024),
  vehicle_plate_image_name      varchar(255),
  vehicle_number                varchar(255),
  driving_license_image_name    varchar(255),
  driving_license_number        varchar(255),
  valid_proofs_uploaded         boolean,
  constraint pk_user primary key (id)
);
create sequence user_seq;

create table user_login (
  id                            bigint not null,
  username                      varchar(255),
  password                      varchar(255),
  constraint pk_user_login primary key (id)
);
create sequence user_login_seq;


# --- !Downs

drop table if exists login_otp;
drop sequence if exists login_otp_seq;

drop table if exists ride;
drop sequence if exists ride_seq;

drop table if exists ride_location;
drop sequence if exists ride_location_seq;

drop table if exists system_settings;
drop sequence if exists system_settings_seq;

drop table if exists user;
drop sequence if exists user_seq;

drop table if exists user_login;
drop sequence if exists user_login_seq;

