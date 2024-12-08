-- SSH Smart Scheduling Assistant Database
-- File: ssh_smart_scheduling.sql

-- WITH resident_presence AS (SELECT timestamp, action, SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) OVER (ORDER BY timestamp) AS net_presence FROM access_log), empty_periods AS (SELECT timestamp AS start_time, LEAD(timestamp) OVER (ORDER BY timestamp) AS end_time FROM resident_presence WHERE net_presence = 0) SELECT start_time, end_time FROM empty_periods WHERE end_time IS NOT NULL;

-- Below is the SQL query for finding the start_time and end_time of the empty period where no residents are present in the household
--
-- WITH resident_presence AS (
--     SELECT 
--         timestamp, 
--         action, 
--         SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) 
--         OVER (ORDER BY timestamp) AS net_presence
--     FROM access_log
-- ),
-- zero_to_one_transitions AS (
--     SELECT 
--         r1.timestamp AS start_time, 
--         r2.timestamp AS end_time
--     FROM resident_presence r1
--     JOIN resident_presence r2
--     ON r1.timestamp < r2.timestamp
--     WHERE r1.net_presence = 0 AND r2.net_presence = 1
--     AND NOT EXISTS (
--         SELECT 1 
--         FROM resident_presence r3
--         WHERE r3.timestamp > r1.timestamp AND r3.timestamp < r2.timestamp AND r3.net_presence != 0
--     )
-- )
-- SELECT start_time, end_time
-- FROM zero_to_one_transitions
-- ORDER BY start_time;
--
--
-- The result is returned below
--
--      start_time      |      end_time       
-- ---------------------+---------------------
--  2024-12-03 15:29:36 | 2024-12-03 17:15:12
--  2024-12-06 07:43:47 | 2024-12-06 08:52:15
-- (2 rows)

---------------------------------------------------------
-- Define ENUM types
---------------------------------------------------------
CREATE TYPE action_type AS ENUM ('enter', 'leave');
CREATE TYPE chore_action AS ENUM ('start', 'finish');

---------------------------------------------------------
-- Tables creation
---------------------------------------------------------
-- Create the 'access_log' table
CREATE TABLE access_log (
    access_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    action action_type NOT NULL
);

-- Create the 'chores_log' table
CREATE TABLE chores_log (
    chore_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    chore_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    action chore_action NOT NULL
);

CREATE TABLE total_hour_suggestions (
    resident_id INT NOT NULL,
    start_timestamp TIMESTAMP NOT NULL,
    end_timestamp TIMESTAMP NOT NULL
);

-- Create the 'alone_hour_suggestions' table
CREATE TABLE alone_hour_suggestions (
    hour_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    start_timestamp TIMESTAMP NOT NULL,
    end_timestamp TIMESTAMP NOT NULL
);

-- Create the 'chore_shift_suggestions' table
CREATE TABLE chore_shift_suggestions (
    chore_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    chore_type VARCHAR(50) NOT NULL,
    start_timestamp TIMESTAMP NOT NULL,
    end_timestamp TIMESTAMP NOT NULL
);

---------------------------------------------------------
-- Data population
---------------------------------------------------------
-- Populate 'access_log' table
INSERT INTO access_log (access_id, resident_id, timestamp, action) VALUES
-- Day 1: Monday
(1, 1, '2024-12-02 07:12:34', 'enter'),
(2, 2, '2024-12-02 07:58:19', 'enter'),
(3, 3, '2024-12-02 08:27:43', 'enter'),
(4, 1, '2024-12-02 09:41:12', 'leave'),
(5, 4, '2024-12-02 10:24:57', 'enter'),
(6, 3, '2024-12-02 11:13:49', 'leave'),
(7, 5, '2024-12-02 11:29:21', 'enter'),
(8, 2, '2024-12-02 13:15:56', 'leave'),
-- Day 2: Tuesday
(9, 1, '2024-12-03 06:50:42', 'enter'),
(10, 2, '2024-12-03 07:33:14', 'enter'),
(11, 3, '2024-12-03 08:19:47', 'enter'),
(12, 4, '2024-12-03 09:23:31', 'leave'),
(13, 1, '2024-12-03 10:17:42', 'leave'),
(14, 5, '2024-12-03 12:43:18', 'leave'),
(15, 2, '2024-12-03 13:09:51', 'leave'),
(16, 3, '2024-12-03 15:29:36', 'leave'),
(17, 4, '2024-12-03 17:15:12', 'enter'),
-- Day 3: Wednesday
(18, 3, '2024-12-04 07:13:25', 'enter'),
(19, 4, '2024-12-04 07:49:39', 'leave'),
(20, 1, '2024-12-04 08:22:57', 'enter'),
(21, 2, '2024-12-04 08:53:16', 'enter'),
(22, 5, '2024-12-04 09:12:45', 'enter'),
(23, 3, '2024-12-04 09:48:33', 'leave'),
(24, 4, '2024-12-04 10:29:50', 'enter'),
(25, 1, '2024-12-04 11:43:44', 'leave'),
(26, 2, '2024-12-04 14:35:56', 'leave'),
-- Day 4: Thursday
(27, 1, '2024-12-05 06:48:15', 'enter'),
(28, 2, '2024-12-05 07:29:58', 'enter'),
(29, 3, '2024-12-05 08:14:32', 'enter'),
(30, 4, '2024-12-05 08:39:03', 'leave'),
(31, 5, '2024-12-05 10:34:14', 'leave'),
(32, 1, '2024-12-05 12:12:07', 'leave'),
-- Day 5: Friday
(33, 2, '2024-12-06 07:03:38', 'leave'),
(34, 3, '2024-12-06 07:43:47', 'leave'),
(35, 1, '2024-12-06 08:52:15', 'enter'),
(36, 4, '2024-12-06 09:37:26', 'enter'),
(37, 5, '2024-12-06 11:45:52', 'enter'),
(38, 1, '2024-12-06 13:16:23', 'leave'),
(39, 2, '2024-12-06 15:24:17', 'enter'),
(40, 4, '2024-12-06 17:56:44', 'leave'),
-- Day 6: Saturday
(41, 1, '2024-12-07 07:18:57', 'enter'),
(42, 2, '2024-12-07 08:02:48', 'leave'),
(43, 3, '2024-12-07 09:25:34', 'enter'),
(44, 4, '2024-12-07 10:14:46', 'enter'),
(45, 5, '2024-12-07 11:30:58', 'leave'),
(46, 3, '2024-12-07 13:42:37', 'leave'),
(47, 2, '2024-12-07 15:50:14', 'enter'),
(48, 1, '2024-12-07 18:22:59', 'leave'),
-- Day 7: Sunday
(49, 3, '2024-12-08 06:59:13', 'enter'),
(50, 4, '2024-12-08 08:11:34', 'leave'),
(51, 1, '2024-12-08 09:33:47', 'enter'),
(52, 2, '2024-12-08 10:18:22', 'leave'),
(53, 5, '2024-12-08 12:00:49', 'enter'),
(54, 4, '2024-12-08 13:49:21', 'enter'),
(55, 3, '2024-12-08 15:35:04', 'leave'),
(56, 2, '2024-12-08 17:42:58', 'enter');

-- Populate 'chores_log' table
INSERT INTO chores_log (chore_id, resident_id, chore_type, timestamp, action) VALUES
-- Day 1: Monday
(1, 1, 'Dishwashing', '2024-12-02 07:30:15', 'start'),
(2, 1, 'Dishwashing', '2024-12-02 07:45:50', 'finish'),
(3, 2, 'Vacuuming', '2024-12-02 08:15:30', 'start'),
(4, 2, 'Vacuuming', '2024-12-02 08:35:45', 'finish'),
(5, 3, 'Laundry', '2024-12-02 09:00:12', 'start'),
(6, 3, 'Laundry', '2024-12-02 09:40:10', 'finish'),
(7, 4, 'Cooking', '2024-12-02 10:35:30', 'start'),
(8, 4, 'Cooking', '2024-12-02 11:00:45', 'finish'),
-- Day 2: Tuesday
(9, 1, 'Gardening', '2024-12-03 07:05:00', 'start'),
(10, 1, 'Gardening', '2024-12-03 07:25:40', 'finish'),
(11, 2, 'Dishwashing', '2024-12-03 07:40:15', 'start'),
(12, 2, 'Dishwashing', '2024-12-03 07:55:25', 'finish'),
(13, 3, 'Vacuuming', '2024-12-03 08:30:50', 'start'),
(14, 3, 'Vacuuming', '2024-12-03 08:50:30', 'finish'),
(15, 5, 'Laundry', '2024-12-03 12:45:20', 'start'),
(16, 5, 'Laundry', '2024-12-03 13:10:10', 'finish'),
-- Day 3: Wednesday
(17, 3, 'Cooking', '2024-12-04 07:15:50', 'start'),
(18, 3, 'Cooking', '2024-12-04 07:45:30', 'finish'),
(19, 4, 'Dishwashing', '2024-12-04 07:55:00', 'start'),
(20, 4, 'Dishwashing', '2024-12-04 08:15:15', 'finish'),
(21, 1, 'Gardening', '2024-12-04 08:30:25', 'start'),
(22, 1, 'Gardening', '2024-12-04 09:00:40', 'finish'),
(23, 5, 'Vacuuming', '2024-12-04 09:15:50', 'start'),
(24, 5, 'Vacuuming', '2024-12-04 09:45:15', 'finish'),
-- Day 4: Thursday
(25, 1, 'Laundry', '2024-12-05 06:50:25', 'start'),
(26, 1, 'Laundry', '2024-12-05 07:10:35', 'finish'),
(27, 3, 'Cooking', '2024-12-05 08:15:50', 'start'),
(28, 3, 'Cooking', '2024-12-05 08:35:20', 'finish'),
(29, 5, 'Gardening', '2024-12-05 10:40:15', 'start'),
(30, 5, 'Gardening', '2024-12-05 11:10:10', 'finish'),
-- Day 5: Friday
(31, 2, 'Dishwashing', '2024-12-06 07:10:15', 'start'),
(32, 2, 'Dishwashing', '2024-12-06 07:25:30', 'finish'),
(33, 3, 'Vacuuming', '2024-12-06 08:15:50', 'start'),
(34, 3, 'Vacuuming', '2024-12-06 08:35:40', 'finish'),
(35, 4, 'Laundry', '2024-12-06 09:45:25', 'start'),
(36, 4, 'Laundry', '2024-12-06 10:15:50', 'finish'),
-- Day 6: Saturday
(37, 1, 'Cooking', '2024-12-07 07:20:00', 'start'),
(38, 1, 'Cooking', '2024-12-07 07:45:15', 'finish'),
(39, 2, 'Vacuuming', '2024-12-07 08:05:30', 'start'),
(40, 2, 'Vacuuming', '2024-12-07 08:30:40', 'finish'),
(41, 3, 'Gardening', '2024-12-07 09:30:25', 'start'),
(42, 3, 'Gardening', '2024-12-07 09:55:50', 'finish'),
(43, 5, 'Dishwashing', '2024-12-07 11:35:15', 'start'),
(44, 5, 'Dishwashing', '2024-12-07 12:00:30', 'finish'),
-- Day 7: Sunday
(45, 3, 'Cooking', '2024-12-08 07:00:15', 'start'),
(46, 3, 'Cooking', '2024-12-08 07:25:35', 'finish'),
(47, 4, 'Dishwashing', '2024-12-08 08:15:20', 'start'),
(48, 4, 'Dishwashing', '2024-12-08 08:45:10', 'finish'),
(49, 1, 'Laundry', '2024-12-08 09:40:30', 'start'),
(50, 1, 'Laundry', '2024-12-08 10:05:40', 'finish'),
(51, 2, 'Gardening', '2024-12-08 10:20:15', 'start'),
(52, 2, 'Gardening', '2024-12-08 10:50:00', 'finish');

-- -- Populate 'alone_hour_suggestions' table
-- INSERT INTO alone_hour_suggestions (resident_id, start_timestamp, end_timestamp) VALUES
-- (1, '2024-12-01 09:00:00', '2024-12-01 10:00:00'),
-- (2, '2024-12-01 09:30:00', '2024-12-01 10:30:00'),
-- (3, '2024-12-01 12:00:00', '2024-12-01 13:00:00'),
-- (4, '2024-12-01 08:45:00', '2024-12-01 09:15:00'),
-- (5, '2024-12-01 13:00:00', '2024-12-01 14:00:00');

-- -- Populate 'chore_shift_suggestions' table
-- INSERT INTO chore_shift_suggestions (resident_id, chore_type, start_timestamp, end_timestamp) VALUES
-- (1, 'Dishwashing', '2024-12-01 08:15:00', '2024-12-01 08:30:00'),
-- (2, 'Vacuuming', '2024-12-01 08:45:00', '2024-12-01 09:00:00'),
-- (3, 'Laundry', '2024-12-01 10:15:00', '2024-12-01 11:30:00'),
-- (4, 'Cooking', '2024-12-01 08:20:00', '2024-12-01 08:40:00'),
-- (5, 'Gardening', '2024-12-01 11:15:00', '2024-12-01 12:45:00');