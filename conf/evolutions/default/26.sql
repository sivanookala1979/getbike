
# --- !Ups
create table geo_fencing_location (
  id                            bigint not null,

  latitude                        double,
  longitude                       double,
  radius                          int,
  address_area                    varchar(255),

  constraint pk_geo_fencing_locationt primary key (id)
);
create sequence geo_fencing_location_seq;


# --- !Downs