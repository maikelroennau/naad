SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND arrival_delay <= 0;
SELECT COUNT(arrival_delay) FROM flights WHERE year = 1987 AND arrival_delay > 0;
SELECT AVG(arrival_delay) FROM flights WHERE year = 1987 AND arrival_delay > 0;

SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND departure_delay <= 0;
SELECT COUNT(departure_delay) FROM flights WHERE year = 1987 AND departure_delay > 0;
SELECT AVG(departure_delay) FROM flights WHERE year = 1987 AND departure_delay > 0;