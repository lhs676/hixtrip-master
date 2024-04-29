#todo
你的建表语句,包含索引


CREATE TABLE `order` (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         order_id VARCHAR(255) NOT NULL,
                         user_id VARCHAR(255),
                         sku_id VARCHAR(255),
                         amount INT,
                         money DECIMAL(10, 2),
                         pay_time DATETIME,
                         pay_status VARCHAR(255),
                         del_flag BIGINT DEFAULT 0,
                         create_by VARCHAR(255),
                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                         update_by VARCHAR(255),
                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_order_id ON `order` (order_id);
