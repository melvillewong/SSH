## Following are the queries planned to be used

- This is a SQL query that returns the list of number of residents present in the house for every change in the 'enter' and 'leave' state
```sql
SELECT resident_id, timestamp, action, SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) OVER (ORDER BY timestamp) AS net_presence FROM access_log;
```

```sql
WITH chore_records_durations AS (
    SELECT
        start_log.resident_id,
        start_log.chore_type,
        start_log.timestamp AS start_time, 
        finish_log.timestamp AS finish_time,
        finish_log.timestamp - start_log.timestamp AS duration
    
    FROM chores_log AS start_log
    JOIN chores_log AS finish_log
        ON 
        start_log.resident_id = finish_log.resident_id
        AND start_log.chore_type = finish_log.chore_type
        AND start_log.action = 'start'
        AND finish_log.action = 'finish'
        AND start_log.timestamp < finish_log.timestamp

    WHERE 
        NOT EXISTS(
            SELECT 1 
            FROM chores_log AS any_log
            WHERE any_log.resident_id = start_log.resident_id
            AND any_log.chore_type = start_log.chore_type
            AND any_log.action = 'start'
            AND any_log.timestamp > start_log.timestamp
            AND any_log.timestamp < finish_log.timestamp
        )
),
SELECT * FROM chore_records_durations ORDER BY resident_id;
```

The chore_records_durations is produced as follow: 
| Resident ID | Chore Type   | Start Time           | Finish Time          | Duration   |
|-------------|--------------|----------------------|----------------------|------------|
| 1           | Cooking      | 2024-12-07 07:20:00 | 2024-12-07 07:45:15 | 00:25:15   |
| 1           | Laundry      | 2024-12-05 06:50:25 | 2024-12-05 07:10:35 | 00:20:10   |
| 1           | Gardening    | 2024-12-04 08:30:25 | 2024-12-04 09:00:40 | 00:30:15   |
| 1           | Laundry      | 2024-12-08 09:40:30 | 2024-12-08 10:05:40 | 00:25:10   |
| 1           | Gardening    | 2024-12-03 07:05:00 | 2024-12-03 07:25:40 | 00:20:40   |
| 1           | Dishwashing  | 2024-12-02 07:30:15 | 2024-12-02 07:45:50 | 00:15:35   |
| 2           | Gardening    | 2024-12-08 10:20:15 | 2024-12-08 10:50:00 | 00:29:45   |
| 2           | Vacuuming    | 2024-12-02 08:15:30 | 2024-12-02 08:35:45 | 00:20:15   |
| 2           | Dishwashing  | 2024-12-03 07:40:15 | 2024-12-03 07:55:25 | 00:15:10   |
| 2           | Dishwashing  | 2024-12-06 07:10:15 | 2024-12-06 07:25:30 | 00:15:15   |
| 2           | Vacuuming    | 2024-12-07 08:05:30 | 2024-12-07 08:30:40 | 00:25:10   |
| 3           | Cooking      | 2024-12-04 07:15:50 | 2024-12-04 07:45:30 | 00:29:40   |
| 3           | Cooking      | 2024-12-05 08:15:50 | 2024-12-05 08:35:20 | 00:19:30   |
| 3           | Vacuuming    | 2024-12-06 08:15:50 | 2024-12-06 08:35:40 | 00:19:50   |
| 3           | Vacuuming    | 2024-12-03 08:30:50 | 2024-12-03 08:50:30 | 00:19:40   |
| 3           | Laundry      | 2024-12-02 09:00:12 | 2024-12-02 09:40:10 | 00:39:58   |
| 3           | Gardening    | 2024-12-07 09:30:25 | 2024-12-07 09:55:50 | 00:25:25   |
| 3           | Cooking      | 2024-12-08 07:00:15 | 2024-12-08 07:25:35 | 00:25:20   |
| 4           | Dishwashing  | 2024-12-08 08:15:20 | 2024-12-08 08:45:10 | 00:29:50   |
| 4           | Cooking      | 2024-12-02 10:35:30 | 2024-12-02 11:00:45 | 00:25:15   |
| 4           | Dishwashing  | 2024-12-04 07:55:00 | 2024-12-04 08:15:15 | 00:20:15   |
| 4           | Laundry      | 2024-12-06 09:45:25 | 2024-12-06 10:15:50 | 00:30:25   |
| 5           | Gardening    | 2024-12-05 10:40:15 | 2024-12-05 11:10:10 | 00:29:55   |
| 5           | Laundry      | 2024-12-03 12:45:20 | 2024-12-03 13:10:10 | 00:24:50   |
| 5           | Vacuuming    | 2024-12-04 09:15:50 | 2024-12-04 09:45:15 | 00:29:25   |
| 5           | Dishwashing  | 2024-12-07 11:35:15 | 2024-12-07 12:00:30 | 00:25:15   |


Then, the average of duration of chore tasks done by each resident will be calculated

```sql
avg_duration_chore AS (
    SELECT
        resident_id,
        chore_type,
        AVG(duration) AS avg_duration
    FROM chore_records_durations
    GROUP BY resident_id, chore_type
)
SELECT * FROM avg_duration_chore
GROUP BY resident_id, chore_type, avg_duration;
```
| Resident ID | Chore Type  | Average Duration |
|-------------|-------------|------------------|
| 1           | Cooking     | 00:25:15         |
| 1           | Dishwashing | 00:15:35         |
| 1           | Gardening   | 00:25:27.5       |
| 1           | Laundry     | 00:22:40         |
| 2           | Dishwashing | 00:15:12.5       |
| 2           | Gardening   | 00:29:45         |
| 2           | Vacuuming   | 00:22:42.5       |
| 3           | Cooking     | 00:24:50         |
| 3           | Gardening   | 00:25:25         |
| 3           | Laundry     | 00:39:58         |
| 3           | Vacuuming   | 00:19:45         |
| 4           | Cooking     | 00:25:15         |
| 4           | Dishwashing | 00:25:02.5       |
| 4           | Laundry     | 00:30:25         |
| 5           | Dishwashing | 00:25:15         |
| 5           | Gardening   | 00:29:55         |
| 5           | Laundry     | 00:24:50         |
| 5           | Vacuuming   | 00:29:25         |


-to be confirmed-
=======
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
|      0      | 2024-12-03 15:29:36 | 2024-12-03 17:15:12   |
|      0      | 2024-12-06 07:43:47 | 2024-12-06 08:52:15   |
(2 rows)

- Second SQL query output (Occupancy = 1) where only one resident is present in the household. (This output has yet to be inserted)

| resident_id |   start_timestamp    |     end_timestamp    |
|-------------|----------------------|----------------------|
|      1      | 2024-12-02 07:12:34 | 2024-12-02 07:58:19   |
|      2      | 2024-12-03 13:09:51 | 2024-12-03 15:29:36   |
|      4      | 2024-12-03 17:15:12 | 2024-12-04 07:13:25   |
|      4      | 2024-12-04 07:49:39 | 2024-12-04 08:22:57   |
|      2      | 2024-12-06 07:03:38 | 2024-12-06 07:43:47   |
|      1      | 2024-12-06 08:52:15 | 2024-12-06 09:37:26   |
(6 rows)
