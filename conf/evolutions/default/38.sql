
# --- !Ups
create table cash_in_advance (
  id                            bigint not null,

  rider_mobile_number               varchar(255),
  rider_name                        varchar(255),
  request_status                    boolean,
  rider_description                 varchar(255),
  admin_description                 varchar(255),
  amount                            double,
  requested_at                      timestamp,

  constraint pk_cash_in_advance primary key (id)
);
create sequence cash_in_advance_seq;


# --- !Downs