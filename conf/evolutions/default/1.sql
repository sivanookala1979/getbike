# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table login_otp (
  id                            bigint not null,
  user_id                       bigint,
  generated_otp                 varchar(255),
  created_at                    timestamp,
  constraint pk_login_otp primary key (id)
);
create sequence login_otp_seq;

create table user (
  id                            bigint not null,
  name                          varchar(255),
  email                         varchar(255),
  phone_number                  varchar(255),
  gender                        varchar(255),
  constraint pk_user primary key (id)
);
create sequence user_seq;


# --- !Downs

drop table if exists login_otp;
drop sequence if exists login_otp_seq;

drop table if exists user;
drop sequence if exists user_seq;

