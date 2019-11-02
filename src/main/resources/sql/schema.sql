-- ---
-- Table 'Users'
--
-- ---

CREATE DATABASE restapi;
USE restapi;

DROP TABLE IF EXISTS [Users];

CREATE TABLE [Users] (
  id INTEGER PRIMARY KEY,
  username VARCHAR,
  [password] VARCHAR,
  score INTEGER
);