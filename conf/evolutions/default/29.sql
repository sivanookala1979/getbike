
# --- !Ups
create table non_geo_fencing_location (
  id                                    bigint not null,

  mobile_number                         varchar(255),
  latitude                              double,
  longitude                             double,
  address_area                          varchar(255),
  requested_at                          timestamp,

  constraint pk_non_geo_fencing_location primary key (id)
);
create sequence non_geo_fencing_location_seq;


# --- !Downs