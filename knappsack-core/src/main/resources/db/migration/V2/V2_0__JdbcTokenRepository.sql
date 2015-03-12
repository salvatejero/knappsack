create table IF NOT EXISTS persistent_logins (
    username varchar(64) not null,
    series varchar(64) primary key,
    token varchar(64) not null,
    last_used timestamp not null
)  ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;