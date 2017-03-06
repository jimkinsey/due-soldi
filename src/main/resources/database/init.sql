CREATE TABLE access_record (
  path character varying not null,
  timestamp timestamp not null,
  referer character varying,
  user_agent character varying,
  duration NUMERIC
);