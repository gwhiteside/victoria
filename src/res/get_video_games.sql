SELECT product_id, title, product.year, product.region, system.system_id, system.name
FROM product
INNER JOIN system ON product.system_id = system.system_id;