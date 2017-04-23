
# --- !Ups
create table pricing_profile (
    id                                bigint not null,
    fixed_price                       tinyint(1) default 0,
    has_base_package                  tinyint(1) default 0,
    name                              varchar(255),
    fixed_price_amount                double,
    base_package_amount               double,
    base_package_kilometers           double,
    base_package_minutes              double,
    additional_per_kilometer          double,
    additional_per_minute             double,
  constraint pk_pricing_profile primary key (id));
create sequence pricing_profile_seq;

# --- !Downs

drop table if exists pricing_profile;
drop sequence if exists pricing_profile_seq;
