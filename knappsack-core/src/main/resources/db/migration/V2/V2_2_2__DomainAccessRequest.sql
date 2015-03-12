DELIMITER $$

DROP PROCEDURE IF EXISTS `drop_fk_if_exists` $$
CREATE PROCEDURE `drop_fk_if_exists` (
IN param_table_name VARCHAR(100),
IN param_key_name VARCHAR(100)
)
BEGIN
-- Verify the foreign key exists
IF EXISTS (SELECT NULL FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = param_key_name) THEN
-- Turn the parameters into local variables
set @ParamTable = param_table_name ;
set @ParamKey = param_key_name ;
-- Create the full statement to execute
set @StatementToExecute = concat('ALTER TABLE ',@ParamTable,' DROP FOREIGN KEY ',@ParamKey);
-- Prepare and execute the statement that was built
prepare DynamicStatement from @StatementToExecute ;
execute DynamicStatement ;
-- Cleanup the prepared statement
deallocate prepare DynamicStatement ;
END IF;
END $$

DROP PROCEDURE IF EXISTS `drop_column_if_exists` $$
CREATE PROCEDURE `drop_column_if_exists`(in param_table_name varchar(128), in param_column_name varchar(128))
BEGIN
set @ParamTable = param_table_name ;
set @ParamColumn = param_column_name ;

/* delete columns if they exist */
    if exists (select * from information_schema.columns where table_name = @ParamTable and column_name = @ParamColumn) then
       SET @s = CONCAT('ALTER TABLE ', @ParamTable, ' DROP COLUMN ', @ParamColumn);
		PREPARE stmt FROM @s;
		EXECUTE stmt;
    end if;
END $$

DELIMITER ;

--
-- Alter GROUP_USER_REQUEST table structure to table `DOMAIN_USER_REQUEST`
--

CALL drop_fk_if_exists('GROUP_USER_REQUEST', 'FK_GROUP_USER_REQUEST_ORG_GROUP_ID');

ALTER TABLE GROUP_USER_REQUEST CHANGE ORG_GROUP_ID DOMAIN_ID bigint (20);

ALTER TABLE GROUP_USER_REQUEST ADD CONSTRAINT FK_DOMAIN_USER_REQUEST_DOMAIN_ID FOREIGN KEY (DOMAIN_ID) REFERENCES DOMAIN(ID);

ALTER TABLE GROUP_USER_REQUEST RENAME TO DOMAIN_USER_REQUEST;

CALL drop_column_if_exists('DOMAIN', 'ACCESS_CODE');

--
-- Table structure for table `DOMAIN_REQUEST`
--

DROP TABLE IF EXISTS `DOMAIN_REQUEST`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DOMAIN_REQUEST` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `CHANGED_BY` varchar(255) DEFAULT NULL,
  `CREATE_DATE` date DEFAULT NULL,
  `LAST_UPDATE` date DEFAULT NULL,
  `UUID` varchar(255) NOT NULL,
  `VERSION` int(11) DEFAULT NULL,
  `ADDRESS` varchar(255) DEFAULT NULL,
  `COMPANY_NAME` varchar(255) DEFAULT NULL,
  `DEVICE_TYPE` varchar(255) DEFAULT NULL,
  `EMAIL_ADDRESS` varchar(255) NOT NULL,
  `FIRST_NAME` varchar(255) NOT NULL,
  `LAST_NAME` varchar(255) NOT NULL,
  `PHONE_NUMBER` varchar(255) DEFAULT NULL,
  `STATUS` varchar(255) DEFAULT NULL,
  `DOMAIN_ID` bigint(20) NOT NULL,
  `REGION_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UUID` (`UUID`),
  UNIQUE KEY `DOMAIN_ID` (`DOMAIN_ID`,`EMAIL_ADDRESS`),
  KEY `FK864DE9F4780B256F` (`DOMAIN_ID`),
  KEY `FK864DE9F46CE7B76F` (`REGION_ID`),
  CONSTRAINT `FK864DE9F46CE7B76F` FOREIGN KEY (`REGION_ID`) REFERENCES `REGION` (`ID`),
  CONSTRAINT `FK864DE9F4780B256F` FOREIGN KEY (`DOMAIN_ID`) REFERENCES `DOMAIN` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


DROP TABLE IF EXISTS `DOMAIN_REQUEST_LANGUAGE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DOMAIN_REQUEST_LANGUAGE` (
  `DOMAIN_REQUEST_ID` bigint(20) NOT NULL,
  `LANGUAGE` varchar(255) DEFAULT NULL,
  KEY `FKC225BD4371D6607C` (`DOMAIN_REQUEST_ID`),
  CONSTRAINT `FKC225BD4371D6607C` FOREIGN KEY (`DOMAIN_REQUEST_ID`) REFERENCES `DOMAIN_REQUEST` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;