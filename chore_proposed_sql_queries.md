# Proposed SQL Queries for the Chore Shift Time Slots Recommendation System

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

avg_duration_chore AS (
    SELECT
        resident_id,
        chore_type,
        AVG(duration) AS avg_duration
    FROM chore_records_durations
    GROUP BY resident_id, chore_type
),

resident_presence AS (
    SELECT
        resident_id, 
        timestamp, 
        action
    FROM access_log
),
transitions AS (
    SELECT 
        resident_id, 
        timestamp AS start_timestamp, 
        LEAD(timestamp) OVER (PARTITION BY resident_id ORDER BY timestamp) AS end_timestamp
    FROM resident_presence
),
filtered_periods_mec AS (
    SELECT 
        resident_id, 
        start_timestamp, 
        end_timestamp
    FROM transitions
    WHERE end_timestamp IS NOT NULL
),

chore_hours_suggestions AS (
    SELECT
        presence.resident_id,
        chore.chore_type,
        presence.start_timestamp,
        presence.start_timestamp + INTERVAL '1 second' * EXTRACT(EPOCH FROM chore.avg_duration) AS suggested_end_timestamp
    FROM filtered_periods_mec presence
    JOIN avg_duration_chore chore
        ON presence.resident_id = chore.resident_id
)

SELECT * FROM chore_hours_suggestions;
```

## Chore Shift Duration Calculation

This step calculates the duration of chore shifts by averaging time spent each resident spent on a certain chore in the past


### Stage 1: Compile durations of completed chore shifts
This step extract chore durations from chores_log by making use of start_timestamp & end_timestamp. Also, conditions are in place to avoid erroneous start actions between a pair of start and finish timestamps.


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
)
```


Sample Output:
```sql
SELECT * FROM chore_records_durations ORDER BY resident_id;
```

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

### Stage 2: Calculate the average of chore shifts durations

```sql
avg_duration_chore AS (
    SELECT
        resident_id,
        chore_type,
        AVG(duration) AS avg_duration
    FROM chore_records_durations
    GROUP BY resident_id, chore_type
)
```

```sql
SELECT * FROM avg_duration_chore;
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

## Chore Shift Time Recommendation
This step determines appropriate time slots by making use of the  duration data obtained in the previous step and resident availability obtained from access_log.

### Stage 1: Resident Availability (Similar approach in getting resident net presence record in total_hour_suggestions, written by Chapman Leung)

```sql
resident_presence AS (
    SELECT
        resident_id, 
        timestamp, 
        action
    FROM access_log
),
transitions AS (
    SELECT 
        resident_id, 
        timestamp AS start_timestamp, 
        LEAD(timestamp) OVER (PARTITION BY resident_id ORDER BY timestamp) AS end_timestamp
    FROM resident_presence
),
filtered_periods_mec AS (
    SELECT 
        resident_id, 
        start_timestamp, 
        end_timestamp
    FROM transitions
    WHERE end_timestamp IS NOT NULL
)
```
### Stage 2: Chore Shift Time Slot Suggestions

```sql
chore_hours_suggestions AS (
    SELECT
        presence.resident_id,
        chore.chore_type,
        presence.start_timestamp,
        presence.start_timestamp + INTERVAL '1 second' * EXTRACT(EPOCH FROM chore.avg_duration) AS suggested_end_timestamp
    FROM filtered_periods_mec presence
    JOIN avg_duration_chore chore
        ON presence.resident_id = chore.resident_id
)
```

Sample Output: 
```sql
SELECT * FROM chore_hours_suggestions;
```

| Resident ID | Chore Type   | Start Timestamp       | Suggested End Timestamp  |
|-------------|--------------|-----------------------|---------------------------|
| 1           | Gardening    | 2024-12-02 07:12:34  | 2024-12-02 07:38:01.5    |
| 1           | Laundry      | 2024-12-02 07:12:34  | 2024-12-02 07:35:14      |
| 1           | Dishwashing  | 2024-12-02 07:12:34  | 2024-12-02 07:28:09      |
| 1           | Cooking      | 2024-12-02 07:12:34  | 2024-12-02 07:37:49      |
| 1           | Gardening    | 2024-12-02 09:41:12  | 2024-12-02 10:06:39.5    |
| 1           | Laundry      | 2024-12-02 09:41:12  | 2024-12-02 10:03:52      |
| 1           | Dishwashing  | 2024-12-02 09:41:12  | 2024-12-02 09:56:47      |
| 1           | Cooking      | 2024-12-02 09:41:12  | 2024-12-02 10:06:27      |
| ...         | ...          | ...                   | ...                      |
|           5 | Dishwashing  | 2024-12-02 11:29:21 | 2024-12-02 11:54:36     |
|           5 | Laundry      | 2024-12-02 11:29:21 | 2024-12-02 11:54:11     |
|           5 | Gardening    | 2024-12-02 11:29:21 | 2024-12-02 11:59:16     |
|           5 | Vacuuming    | 2024-12-02 11:29:21 | 2024-12-02 11:58:46     |
|           5 | Dishwashing  | 2024-12-03 12:43:18 | 2024-12-03 13:08:33     |
|           5 | Laundry      | 2024-12-03 12:43:18 | 2024-12-03 13:08:08     |
|           5 | Gardening    | 2024-12-03 12:43:18 | 2024-12-03 13:13:13     |
|           5 | Vacuuming    | 2024-12-03 12:43:18 | 2024-12-03 13:12:43     |
|           5 | Dishwashing  | 2024-12-04 09:12:45 | 2024-12-04 09:38:00     |
|           5 | Laundry      | 2024-12-04 09:12:45 | 2024-12-04 09:37:35     |
|           5 | Gardening    | 2024-12-04 09:12:45 | 2024-12-04 09:42:40     |
|           5 | Vacuuming    | 2024-12-04 09:12:45 | 2024-12-04 09:42:10     |
|           5 | Dishwashing  | 2024-12-05 10:34:14 | 2024-12-05 10:59:29     |
|           5 | Laundry      | 2024-12-05 10:34:14 | 2024-12-05 10:59:04     |
|           5 | Gardening    | 2024-12-05 10:34:14 | 2024-12-05 11:04:09     |
|           5 | Vacuuming    | 2024-12-05 10:34:14 | 2024-12-05 11:03:39     |
|           5 | Dishwashing  | 2024-12-06 11:45:52 | 2024-12-06 12:11:07     |
|           5 | Laundry      | 2024-12-06 11:45:52 | 2024-12-06 12:10:42     |
|           5 | Gardening    | 2024-12-06 11:45:52 | 2024-12-06 12:15:47     |
|           5 | Vacuuming    | 2024-12-06 11:45:52 | 2024-12-06 12:15:17     |
|           5 | Dishwashing  | 2024-12-07 11:30:58 | 2024-12-07 11:56:13     |
|           5 | Laundry      | 2024-12-07 11:30:58 | 2024-12-07 11:55:48     |
|           5 | Gardening    | 2024-12-07 11:30:58 | 2024-12-07 12:00:53     |
|           5 | Vacuuming    | 2024-12-07 11:30:58 | 2024-12-07 12:00:23     |
