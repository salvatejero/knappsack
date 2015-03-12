DROP TABLE IF EXISTS `oauth_access_token`;
DROP TABLE IF EXISTS `oauth_refresh_token`;

CREATE TABLE IF NOT EXISTS oauth_client_details (
  `id`                    BIGINT(20) NOT NULL AUTO_INCREMENT,
  client_id               VARCHAR(255),
  resource_ids            VARCHAR(255),
  client_secret           VARCHAR(255),
  scope                   VARCHAR(255),
  authorized_grant_types  VARCHAR(255),
  web_server_redirect_uri VARCHAR(255),
  authorities             VARCHAR(255),
  access_token_validity   INTEGER,
  refresh_token_validity  INTEGER,
  additional_information  VARCHAR(4096),
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS oauth_access_token (
  `id`              BIGINT(20) NOT NULL AUTO_INCREMENT,
  token_id          VARCHAR(255),
  token             BLOB,
  authentication_id VARCHAR(255),
  user_name         VARCHAR(255),
  client_id         VARCHAR(255),
  authentication    BLOB,
  refresh_token     VARCHAR(255),
  PRIMARY KEY (`id`),
  CONSTRAINT uc_user_client UNIQUE (user_name, client_id)
);

CREATE TABLE IF NOT EXISTS oauth_refresh_token (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT,
  token_id       VARCHAR(255),
  token          BLOB,
  authentication BLOB,
  PRIMARY KEY (`id`)
);

INSERT INTO oauth_client_details
(client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities)
  VALUES ('mobile_api_client', 'kzI7QNsbne8KOlS', 'knappsack', 'read,write', 'password,refresh_token', '', 'ROLE_USER');

INSERT INTO oauth_client_details
(client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities)
  VALUES ('jenkins_api_client', 'fjw8MHSI7HOTtBb', 'knappsack', 'read,write', 'password,refresh_token', '', 'ROLE_USER');

INSERT INTO oauth_client_details
(client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities)
  VALUES ('api_client', 'd56974Vd7hRo7er', 'knappsack', 'read,write', 'authorization_code', '', 'ROLE_USER');