## Here are lists of SQL queries that shall be used

- This is a SQL query that returns the list of number of residents present in the house for every change in the 'enter' and 'leave' state
```sql
SELECT resident_id, timestamp, action, SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) OVER (ORDER BY timestamp) AS net_presence FROM access_log;
```

- This adds the start and end timestamp of periods where no residents are at home to the table of total_hour_suggestions, with resident_id set to '0' (indicating nobody)
```sql
WITH resident_presence AS (
    SELECT 
        timestamp, 
        action, 
        SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) 
        OVER (ORDER BY timestamp) AS net_presence
    FROM access_log
),
zero_to_one_transitions AS (
    SELECT 
        r1.timestamp AS start_time, 
        r2.timestamp AS end_time
    FROM resident_presence r1
    JOIN resident_presence r2
    ON r1.timestamp < r2.timestamp
    WHERE r1.net_presence = 0 AND r2.net_presence = 1
    AND NOT EXISTS (
        SELECT 1 
        FROM resident_presence r3
        WHERE r3.timestamp > r1.timestamp AND r3.timestamp < r2.timestamp AND r3.net_presence != 0
    )
)
INSERT INTO total_hour_suggestions (resident_id, start_timestamp, end_timestamp)
SELECT 0 AS resident_id, start_time, end_time
FROM zero_to_one_transitions
ORDER BY start_time;
```

- This is a SQL query that insert the list of timestamps for residents where such resident is alone at the household by himself/herself into the table of total_hour_suggestions, with resident_id set to the relevant resident
```sql
WITH resident_presence AS (
    SELECT 
        resident_id, 
        timestamp, 
        action, 
        SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) 
        OVER (ORDER BY timestamp) AS net_presence
    FROM access_log
),
transitions AS (
    SELECT 
        resident_id, 
        timestamp AS start_timestamp, 
        LEAD(timestamp) OVER (ORDER BY timestamp) AS end_timestamp,
        net_presence,
        LEAD(net_presence) OVER (ORDER BY timestamp) AS next_net_presence
    FROM resident_presence
),
filtered_periods AS (
    SELECT 
        resident_id, 
        start_timestamp, 
        end_timestamp
    FROM transitions
    WHERE net_presence = 1 
      AND (next_net_presence IS DISTINCT FROM 1 OR next_net_presence IS NULL)
)
INSERT INTO total_hour_suggestions (resident_id, start_timestamp, end_timestamp)
SELECT resident_id, start_timestamp, end_timestamp
FROM filtered_periods
WHERE end_timestamp IS NOT NULL
ORDER BY start_timestamp;
```

| resident_id |      timestamp      | action | net_presence |
|-------------|---------------------|--------|--------------|
|      1      | 2024-12-02 07:12:34 | enter  |      1       |
|      2      | 2024-12-02 07:58:19 | enter  |      2       |
|      3      | 2024-12-02 08:27:43 | enter  |      3       |
|      1      | 2024-12-02 09:41:12 | leave  |      2       |
|      4      | 2024-12-02 10:24:57 | enter  |      3       |
|      3      | 2024-12-02 11:13:49 | leave  |      2       |
|      5      | 2024-12-02 11:29:21 | enter  |      3       |
|      2      | 2024-12-02 13:15:56 | leave  |      2       |
|      1      | 2024-12-03 06:50:42 | enter  |      3       |
|      2      | 2024-12-03 07:33:14 | enter  |      4       |
|      3      | 2024-12-03 08:19:47 | enter  |      5       |
|      4      | 2024-12-03 09:23:31 | leave  |      4       |
|      1      | 2024-12-03 10:17:42 | leave  |      3       |
|      5      | 2024-12-03 12:43:18 | leave  |      2       |
|      2      | 2024-12-03 13:09:51 | leave  |      1       |
|      3      | 2024-12-03 15:29:36 | leave  |      0       |
|      4      | 2024-12-03 17:15:12 | enter  |      1       |
|      3      | 2024-12-04 07:13:25 | enter  |      2       |
|      4      | 2024-12-04 07:49:39 | leave  |      1       |
|      1      | 2024-12-04 08:22:57 | enter  |      2       |
|      2      | 2024-12-04 08:53:16 | enter  |      3       |
|      5      | 2024-12-04 09:12:45 | enter  |      4       |
|      3      | 2024-12-04 09:48:33 | leave  |      3       |
|      4      | 2024-12-04 10:29:50 | enter  |      4       |
|      1      | 2024-12-04 11:43:44 | leave  |      3       |
|      2      | 2024-12-04 14:35:56 | leave  |      2       |
|      1      | 2024-12-05 06:48:15 | enter  |      3       |
|      2      | 2024-12-05 07:29:58 | enter  |      4       |
|      3      | 2024-12-05 08:14:32 | enter  |      5       |
|      4      | 2024-12-05 08:39:03 | leave  |      4       |
|      5      | 2024-12-05 10:34:14 | leave  |      3       |
|      1      | 2024-12-05 12:12:07 | leave  |      2       |
|      2      | 2024-12-06 07:03:38 | leave  |      1       |
|      3      | 2024-12-06 07:43:47 | leave  |      0       |
|      1      | 2024-12-06 08:52:15 | enter  |      1       |
|      4      | 2024-12-06 09:37:26 | enter  |      2       |
|      5      | 2024-12-06 11:45:52 | enter  |      3       |
|      1      | 2024-12-06 13:16:23 | leave  |      2       |
|      2      | 2024-12-06 15:24:17 | enter  |      3       |
|      4      | 2024-12-06 17:56:44 | leave  |      2       |
|      1      | 2024-12-07 07:18:57 | enter  |      3       |
|      2      | 2024-12-07 08:02:48 | leave  |      2       |
|      3      | 2024-12-07 09:25:34 | enter  |      3       |
|      4      | 2024-12-07 10:14:46 | enter  |      4       |
|      5      | 2024-12-07 11:30:58 | leave  |      3       |
|      3      | 2024-12-07 13:42:37 | leave  |      2       |
|      2      | 2024-12-07 15:50:14 | enter  |      3       |
|      1      | 2024-12-07 18:22:59 | leave  |      2       |
|      3      | 2024-12-08 06:59:13 | enter  |      3       |
|      4      | 2024-12-08 08:11:34 | leave  |      2       |
|      1      | 2024-12-08 09:33:47 | enter  |      3       |
|      2      | 2024-12-08 10:18:22 | leave  |      2       |
|      5      | 2024-12-08 12:00:49 | enter  |      3       |
|      4      | 2024-12-08 13:49:21 | enter  |      4       |
|      3      | 2024-12-08 15:35:04 | leave  |      3       |
|      2      | 2024-12-08 17:42:58 | enter  |      4       |
(56 rows)

- First SQL query output (Occupancy = 0) where nobody is at the household. (This output has yet to be inserted)
| resident_id |      start_time      |       end_time       |
|-------------|----------------------|----------------------|
|      0      | 2024-12-03 15:29:36 | 2024-12-03 17:15:12  |
|      0      | 2024-12-06 07:43:47 | 2024-12-06 08:52:15  |
(2 rows)

- Second SQL query output (Occupancy = 1) where only one resident is present in the household. (This output has yet to be inserted)
| resident_id |   start_timestamp    |     end_timestamp    |
|-------------|----------------------|----------------------|
|      1      | 2024-12-02 07:12:34 | 2024-12-02 07:58:19  |
|      2      | 2024-12-03 13:09:51 | 2024-12-03 15:29:36  |
|      4      | 2024-12-03 17:15:12 | 2024-12-04 07:13:25  |
|      4      | 2024-12-04 07:49:39 | 2024-12-04 08:22:57  |
|      2      | 2024-12-06 07:03:38 | 2024-12-06 07:43:47  |
|      1      | 2024-12-06 08:52:15 | 2024-12-06 09:37:26  |
(6 rows)