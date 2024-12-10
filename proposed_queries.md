## Following are the queries planned to be used

- The following query compile a temporary result set of the duration of each chore shift done, in chore_type and resident_id, to be used for further computation
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
    AND end_log.action = 'end'
    AND start_log.timestamp < end_log.timestamp

    WHERE 
        NOT EXISTS 
            SELECT 1 FROM chores_log AS any
            WHERE any.resident_id = start_log.resident_id
            AND any.chore_type = start_log.chore_type
            AND any.action = 'start'
            AND any.timestamp > start_log.timestamp
            AND any.timestamp < finish_log.timestamp
)
```

- Description B
```sql
```
