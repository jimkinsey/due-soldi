CREATE TABLE access_record (
  path        character varying     not null,
  timestamp   timestamp             not null,
  referer     character varying,
  user_agent  character varying,
  duration    numeric               not null,
  client_ip   character varying(15),
  country     character(2),
  status_code numeric               not null,
  request_id  character varying
);

CREATE TABLE access_record_archive (
  id       character varying not null PRIMARY KEY,
  fromTime timestamp         not null,
  toTime   timestamp         not null,
  csv      text              not null
);

CREATE TABLE blog_entry (
  id          character varying not null PRIMARY KEY,
  published   timestamp         not null,
  content     text              not null,
  description text              null
);

CREATE TABLE artwork(
    id            character varying not null PRIMARY KEY,
    title         character varying not null,
    last_modified timestamp         not null,
    description   text              null,
    timeframe     character varying null,
    materials     character varying null,
    image_url     character varying not null,
    series_id     character varying null
);

CREATE TABLE series(
    id          character varying not null PRIMARY KEY,
    title       character varying not null,
    description text              null
);
