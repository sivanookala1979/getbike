
# --- !Ups
create table promotions_banner (
  id                            bigint not null,

  hdpi_promotional_banner           varchar(255),
  ldpi_promotional_banner           varchar(255),
  mdpi_promotional_banner           varchar(255),
  xhdpi_promotional_banner          varchar(255),
  xxhdpi_promotional_banner         varchar(255),
  promotions_url                    varchar(255),
  show_this_banner                  boolean default 'false',

  constraint pk_promotions_banner primary key (id)
);
create sequence promotions_banner_seq;


# --- !Downs