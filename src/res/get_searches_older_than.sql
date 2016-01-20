SELECT product_id, timestamp, query
FROM search
WHERE timestamp < ?
ORDER BY timestamp;