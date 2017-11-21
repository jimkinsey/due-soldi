CREATE TABLE access_record (
  path        character varying     not null,
  timestamp   timestamp             not null,
  referer     character varying,
  user_agent  character varying,
  duration    numeric               not null,
  client_ip   character varying(15),
  country     character(2),
  status_code numeric               not null
);

CREATE TABLE blog_entry (
  id          character varying not null PRIMARY KEY,
  published   timestamp         not null,
  content     text              not null,
  description text              null
);
