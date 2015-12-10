SELECT price, timestamp, product_id, sale_id, title
FROM sale
WHERE product_id = ?
ORDER BY timestamp;