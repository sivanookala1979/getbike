
# --- !Ups


create table wallet (
  id                            bigint not null,
  user_id                        bigint,
  transaction_date_time          timestamp,
  amount                        double,
  description                   varchar(255),
  type                          varchar(255),
  mobile_number                  varchar(255),
  operator                      varchar(1024),
  circle                        varchar(255),
  wallet_name                    varchar(255),
  constraint pk_wallet primary key (id)
);
create sequence wallet_seq;


# --- !Downs