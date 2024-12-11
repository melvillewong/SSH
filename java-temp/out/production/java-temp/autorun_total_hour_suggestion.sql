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