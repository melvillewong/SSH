## Here are lists of SQL queries that shall be used

- This is a SQL query that returns the list of number of residents present in the house for every change in the 'enter' and 'leave' state
```sql
SELECT timestamp, action, SUM(CASE WHEN action = 'enter' THEN 1 ELSE -1 END) OVER (ORDER BY timestamp) AS net_presence FROM access_log;
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
