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
        r1.timestamp AS start_timestamp, 
        r2.timestamp AS end_timestamp
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
SELECT 0 AS resident_id, start_timestamp, end_timestamp
FROM zero_to_one_transitions
WHERE NOT EXISTS (
    SELECT 1 
    FROM total_hour_suggestions 
    WHERE total_hour_suggestions.start_timestamp = zero_to_one_transitions.start_timestamp
      AND total_hour_suggestions.end_timestamp = zero_to_one_transitions.end_timestamp
      AND total_hour_suggestions.resident_id = 0
)
ORDER BY start_timestamp;
--------------------
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
  AND NOT EXISTS (
      SELECT 1 
      FROM total_hour_suggestions 
      WHERE total_hour_suggestions.resident_id = filtered_periods.resident_id
        AND total_hour_suggestions.start_timestamp = filtered_periods.start_timestamp
        AND total_hour_suggestions.end_timestamp = filtered_periods.end_timestamp
)
ORDER BY start_timestamp;
