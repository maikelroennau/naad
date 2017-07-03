-- Indexes creation

CREATE INDEX year_ar ON naad.flights(year, arrival_delay, departure_delay);
CREATE INDEX year_month_ar ON naad.flights(year, month, arrival_delay, departure_delay);
CREATE INDEX year_month_day_ar ON naad.flights(year, month, day, arrival_delay, departure_delay);

CREATE INDEX year_airport_ar ON naad.flights(year, destination(3), arrival_delay, departure_delay);
CREATE INDEX year_month_airport_ar ON naad.flights(year, month, destination(3), arrival_delay, departure_delay);
CREATE INDEX year_month_day_airport_ar ON naad.flights(year, month, day, destination(3), arrival_delay, departure_delay);

CREATE INDEX year_airport_carrier_ar ON naad.flights(year, destination(3), carrier(7), arrival_delay, departure_delay);
CREATE INDEX year_month_airport_carrier_ar ON naad.flights(year, month, destination(3), carrier(7), arrival_delay, departure_delay);
CREATE INDEX year_month_day_airport_carrier_ar ON naad.flights(year, month, day, destination(3), carrier(7), arrival_delay, departure_delay);

-- Indexes drop

ALTER TABLE flights DROP INDEX year_ar;
ALTER TABLE flights DROP INDEX year_month_ar;
ALTER TABLE flights DROP INDEX year_month_day_ar;

ALTER TABLE flights DROP INDEX year_airport_ar;
ALTER TABLE flights DROP INDEX year_month_airport_ar;
ALTER TABLE flights DROP INDEX year_month_day_airport_ar;

ALTER TABLE flights DROP INDEX year_airport_carrier_ar;
ALTER TABLE flights DROP INDEX year_month_airport_carrier_ar;
ALTER TABLE flights DROP INDEX year_month_day_airport_carrier_ar;

-- Simple queries - Arrival delay

SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND month = 10 AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND month = 10 AND day = 15 AND arrival_delay <= 0;

SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND destination = 'SFO' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND arrival_delay <= 0;

SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND destination = 'SFO' AND carrier = 'AA' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND carrier = 'AA' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND carrier = 'AA' AND arrival_delay <= 0;

-- Indexed queries - Arrival delay

SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_ar) WHERE year = 1987 AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_month_ar) WHERE year = 1987 AND month = 10 AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_month_day_ar) WHERE year = 1987 AND month = 10 AND day = 15 AND arrival_delay <= 0;

SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_airport_ar) WHERE year = 1987 AND destination = 'SFO' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_month_airport_ar) WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_month_day_airport_ar) WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND arrival_delay <= 0;

SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_airport_carrier_ar) WHERE year = 1987 AND destination = 'SFO' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_month_airport_carrier_ar) WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights USE INDEX(year_month_day_airport_carrier_ar) WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND arrival_delay <= 0;

-- Simple queries - Departure delay

SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND month = 10 AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND month = 10 AND day = 15 AND departure_delay <= 0;

SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND destination = 'SFO' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND departure_delay <= 0;

SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND destination = 'SFO' AND carrier = 'AA' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND carrier = 'AA' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND carrier = 'AA' AND departure_delay <= 0;

-- Indexed queries - Departure delay

SELECT COUNT(departure_delay) FROM flights USE INDEX(year_ar) WHERE year = 1987 AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights USE INDEX(year_month_ar) WHERE year = 1987 AND month = 10 AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights USE INDEX(year_month_day_ar) WHERE year = 1987 AND month = 10 AND day = 15 AND departure_delay <= 0;

SELECT COUNT(departure_delay) FROM flights USE INDEX(year_airport_ar) WHERE year = 1987 AND destination = 'SFO' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights USE INDEX(year_month_airport_ar) WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights USE INDEX(year_month_day_airport_ar) WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND departure_delay <= 0;

SELECT COUNT(departure_delay) FROM flights USE INDEX(year_airport_carrier_ar) WHERE year = 1987 AND destination = 'SFO' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights USE INDEX(year_month_airport_carrier_ar) WHERE year = 1987 AND month = 10 AND destination = 'SFO' AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights USE INDEX(year_month_day_airport_carrier_ar) WHERE year = 1987 AND month = 10 AND day = 15 AND destination = 'SFO' AND departure_delay <= 0;