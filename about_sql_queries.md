## Following are the queries planned to be used

The following query compile a temporary result set of the duration of each chore shift done, in chore_type and resident_id, to be used for further computation

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