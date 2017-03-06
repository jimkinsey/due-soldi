CREATE TABLE access_record (
  path character varying not null,
  timestamp timestamp not null,
  referer character varying,
  user_agent character varying,
  duration NUMERIC not null
);

CREATE TABLE blog_entry (
  id character varying not null,
  published timestamp not null,
  content character varying not null
);