
# --- !Ups

create table payment_order (
  id                            bigint not null,
  user_id                       bigint,
  order_date_time               timestamp,
  amount                        double,
  order_identifier              varchar(255),
  order_type                    varchar(255),
  description                   varchar(255),
  ride_id                       bigint,
  txn_id                        varchar(255),
  status                        varchar(255),
  response_date_time            timestamp,
  pg_details                    varchar(4096),
  constraint pk_payment_order primary key (id)
);
create sequence payment_order_seq;


# --- !Downs

drop table payment_order;
drop sequence payment_order_seq;