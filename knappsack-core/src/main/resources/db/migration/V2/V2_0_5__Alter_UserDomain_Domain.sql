SET FOREIGN_KEY_CHECKS = 0;

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

DROP PROCEDURE IF EXISTS `drop_index_if_exists` $$
CREATE PROCEDURE `drop_index_if_exists`(in param_table_name varchar(128), in param_index_name varchar(128) )
BEGIN
set @ParamTable = param_table_name ; 
set @ParamIndex = param_index_name ;

 IF((SELECT COUNT(*) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name =
@ParamTable AND index_name = @ParamIndex) > 0) THEN
   SET @s = CONCAT('DROP INDEX ' , @ParamIndex , ' ON ' , @ParamTable);
   PREPARE stmt FROM @s;
   EXECUTE stmt;
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

CALL drop_index_if_exists('USER_DOMAIN', 'UNQ_USER_DOMAIN_0');
ALTER TABLE `USER_DOMAIN` ADD UNIQUE `UNQ_USER_DOMAIN_0` (`DOMAIN_ID`,`ROLE_ID`,`USER_ID`);
CALL drop_fk_if_exists('USER_DOMAIN', 'FK_USER_DOMAIN_DOMAIN_ID');
ALTER TABLE `USER_DOMAIN` ADD CONSTRAINT `FK_USER_DOMAIN_DOMAIN_ID` FOREIGN KEY (`DOMAIN_ID`) REFERENCES `DOMAIN` (`ID`);
CALL drop_index_if_exists('USER_DOMAIN', 'UUID');
ALTER TABLE `USER_DOMAIN` ADD UNIQUE `UUID` (`UUID`);
SET FOREIGN_KEY_CHECKS = 1;
CALL drop_column_if_exists('USER_DOMAIN', 'DOMAIN_TYPE');