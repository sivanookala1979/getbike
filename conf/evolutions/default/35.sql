
# --- !Ups
create table roaster_record (
  id                            bigint not null,
  ride_id                       bigint,
  rider_id                      bigint,
  customer_order_number         varchar(255),
  source_address                varchar(255),
  destination_address           varchar(255),
  distance                      double,
  amount_collected              double,
  delivery_date                 timestamp,
  constraint pk_roaster_record primary key (id)
);
create sequence roaster_record_seq;


# --- !Downs