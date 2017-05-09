
# --- !Ups
create table leave_in_advance (
  id                            bigint not null,

  rider_mobile_number               varchar(255),
  rider_name                        varchar(255),
  rider_id                          bigint,
  request_status                    boolean,
  rider_description                 varchar(255),
  admin_description                 varchar(255),
  leaves_required                   varchar(255),
  from_date                         varchar(255),
  to_date                           varchar(255),
  requested_at                      timestamp,

  constraint pk_leave_in_advance primary key (id)
);
create sequence leave_in_advance_seq;


# --- !Downs