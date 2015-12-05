SELECT price, timestamp, product_id, sale_id
FROM sale
WHERE product_id = ?
ORDER BY timestamp;