SELECT price, timestamp, sale_id, title
FROM sale
WHERE product_id = ?
ORDER BY timestamp;