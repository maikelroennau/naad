-- Database creation

CREATE DATABASE `naad`;
USE `naad`;

-- End database creation

-- Carriers table

CREATE TABLE `carriers` (
  `Code` text,
  `Description` text) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOAD DATA LOCAL INFILE 'C:\\carriers.csv' 
INTO TABLE carriers
COLUMNS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

ALTER TABLE `naad`.`carriers` 
CHANGE COLUMN `Code` `code` TEXT NULL DEFAULT NULL ,
CHANGE COLUMN `Description` `name` TEXT NULL DEFAULT NULL ;

-- End carriers table

-- Aiports table

CREATE TABLE `airports` (
    `iata` text,
    `airport` text,
    `city` text,
    `state` text,
    `country` text,
    `lat` double DEFAULT NULL,
    `long` double DEFAULT NULL ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOAD DATA LOCAL INFILE 'C:\\airports.csv' 
INTO TABLE carriers
COLUMNS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

ALTER TABLE `naad`.`airports` 
DROP COLUMN `country`,
DROP COLUMN `state`,
CHANGE COLUMN `airport` `name` TEXT NULL DEFAULT NULL ,
CHANGE COLUMN `long` `lng` DOUBLE NULL DEFAULT NULL ;

-- End aiports table

-- Flights table

CREATE TABLE `flights` (
  `Year` int(11) DEFAULT NULL,
  `Month` int(11) DEFAULT NULL,
  `DayofMonth` int(11) DEFAULT NULL,
  `DayOfWeek` int(11) DEFAULT NULL,
  `DepTime` int(11) DEFAULT NULL,
  `CRSDepTime` int(11) DEFAULT NULL,
  `ArrTime` int(11) DEFAULT NULL,
  `CRSArrTime` int(11) DEFAULT NULL,
  `UniqueCarrier` text,
  `FlightNum` int(11) DEFAULT NULL,
  `TailNum` text,
  `ActualElapsedTime` int(11) DEFAULT NULL,
  `CRSElapsedTime` int(11) DEFAULT NULL,
  `AirTime` int(11) DEFAULT NULL,
  `ArrDelay` int(11) DEFAULT NULL,
  `DepDelay` int(11) DEFAULT NULL,
  `Origin` text,
  `Dest` text,
  `Distance` int(11) DEFAULT NULL,
  `TaxiIn` int(11) DEFAULT NULL,
  `TaxiOut` int(11) DEFAULT NULL,
  `Cancelled` int(11) DEFAULT NULL,
  `CancellationCode` text,
  `Diverted` int(11) DEFAULT NULL,
  `CarrierDelay` text,
  `WeatherDelay` text,
  `NASDelay` text,
  `SecurityDelay` text,
  `LateAircraftDelay` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Flights information import (add more of this section if you have more cvs files)

LOAD DATA LOCAL INFILE 'C:\\1999.csv' 
INTO TABLE flights
COLUMNS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

LOAD DATA LOCAL INFILE 'C:\\2000.csv' 
INTO TABLE flights
COLUMNS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;

/*
LOAD DATA LOCAL INFILE 'C:\\2001.csv' 
INTO TABLE flights
COLUMNS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;
*/

-- End flights information import

ALTER TABLE `naad`.`flights` 
DROP COLUMN `LateAircraftDelay`,
DROP COLUMN `SecurityDelay`,
DROP COLUMN `NASDelay`,
DROP COLUMN `WeatherDelay`,
DROP COLUMN `CarrierDelay`,
DROP COLUMN `Diverted`,
DROP COLUMN `CancellationCode`,
DROP COLUMN `Cancelled`,
DROP COLUMN `TaxiOut`,
DROP COLUMN `TaxiIn`,
DROP COLUMN `Distance`,
DROP COLUMN `Dest`,
DROP COLUMN `Origin`,
DROP COLUMN `AirTime`,
DROP COLUMN `CRSElapsedTime`,
DROP COLUMN `ActualElapsedTime`,
DROP COLUMN `TailNum`,
DROP COLUMN `FlightNum`,
DROP COLUMN `UniqueCarrier`,
DROP COLUMN `CRSArrTime`,
DROP COLUMN `ArrTime`,
DROP COLUMN `CRSDepTime`,
DROP COLUMN `DepTime`,
DROP COLUMN `DayOfWeek`;

ALTER TABLE `naad`.`flights` 
CHANGE COLUMN `Year` `year` INT(11) NULL DEFAULT NULL,
CHANGE COLUMN `Month` `month` INT(11) NULL DEFAULT NULL,
CHANGE COLUMN `DayofMonth` `day` INT(11) NULL DEFAULT NULL,
CHANGE COLUMN `ArrDelay` `arrival_delay` INT(11) NULL DEFAULT NULL,
CHANGE COLUMN `DepDelay` `departure_delay` INT(11) NULL DEFAULT NULL;
