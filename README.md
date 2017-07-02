# NAAD
North American Airport Delays

## Description
This project aims to create a distributed system to calculate the arrivals and depatures delays of North American airports between 1987 and 2008.

## Motivation
This project is an universitary study for Distributed Systems subject, ministred by Prof. Tales Tales Bitelo Viegas.

## Architecture
The distributed system is composed of three main parts: client, server and shared server memory. The communication among the components is made using Socket, which can be made through Telnet.

## Features
The features were developed accordingly to the requisits that can be found [here](https://github.com/selatotal/SistemasDistribuidos/blob/master/Trabalhos/201701/Trabalho2.md). The Server must attend requisitions from the client on the shortest time possible. 

More than one Server could be online at the same time, taking care of more than one year of data. A Server not necessarily needs to provide information off all years of data, but if it receives as requisition that is beyond its scope, the Server must find another online Server that contains the requested information and request this to the client on his behavior.

**Exemple of communication between Client and Server** (all data is sent on JSON format)
- Message from the Client: GETAVAILABLEYEARS
- Response from the Server: { "years": [ 1999, 2000, 2001, 2002] }

**Server features**
Client parameters requests:
- *GETAVAILABLEYEARS* - returns a JSON with all available years for research
- *GETAIRPORTS* - return a JSON with all available airports available for research
- *GETCARRIERS* - return a JSON with all available carriers available for research

The Server gets the information registered by every Server on memcached and then creates the JSON.

Client researches requests:
The Client can make requests of information using a specific period, airport, or carrier. Any combination of these three parameters is valid.

- GETDELAYDATA <period> <airport> <carrier>
    - *period*: YYYY, YYYYMM, YYYYMMDD
    - *airport* (optional): IATA code of the airport (check the [database description](http://stat-computing.org/dataexpo/2009/supplemental-data.html)
    - *carrier* (optional): carrier code (check the [database description](http://stat-computing.org/dataexpo/2009/supplemental-data.html)

Exemples:
- GETDELAYDATA 1999
- GETDELAYDATA 199902 *(where 1999 is the year and 02 the month)*
- GETDELAYDATA 19990215 *(where 1999 is the year and 02 the month and 15 the day)*
- GETDELAYDATA 1999 SFO *(where SFO is the airport IATA)*
- GETDELAYDATA 1999 LAN *(where LAN is the carrier)* 
- GETDELAYDATA \*\*\* \*\*\* \*\*\* *(it is possible to use wildcars as well)*

**Server features**
The memcached server stores all Servers data, like the Server's name, IP address, port, etc. Also stores the results of previous requests made by the Clients, in order to improve the response time.

Every Server must register itsel to memcached, sending a JSON file with its name, localtion (IP address), years the Server is working with, and status (active or inative).

- SD_ListServers - all Servers must be registered to memcached under this key
- SD_Airports - when a Server goes online, it should register the airports it contains
- SD_Carrier - when a Server goes online, it should register the carriers it contains

## Database setup
- Download the csv files from the [database](http://stat-computing.org/dataexpo/2009/the-data.html)
- Run the SQL script *database.sql* on *data* folder to create the database and import the csv files.
- If necessary, add more sections of the code below to load more csv file. Add this in the indicated area on the script *database.sql*:

```
LOAD DATA LOCAL INFILE 'C:\\Path\\to\\the\\file\\1987.csv' 
INTO TABLE flights
COLUMNS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
ESCAPED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES;
```
## References
- [Proposal and requisits](https://github.com/selatotal/SistemasDistribuidos/blob/master/Trabalhos/201701/Trabalho2.md)
- [Database](http://stat-computing.org/dataexpo/2009/the-data.html)
- [Database description](http://stat-computing.org/dataexpo/2009/supplemental-data.html)
 
## Authors
- Maikel Maciel Rönnau
- Bruno Accioli
- Lucas Silveira
