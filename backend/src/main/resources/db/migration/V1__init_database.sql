-- CampusShareOrder 数据库建表脚本
-- 创建日期: 2026-04-18
-- 描述: 校园拼单平台数据库表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_share_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_share_order;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `student_id` VARCHAR(20) NOT NULL COMMENT '学号',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `credit_score` INT DEFAULT 100 COMMENT '信用分（默认100）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    KEY `idx_credit_score` (`credit_score`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 拼单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '拼单ID',
    `title` VARCHAR(200) NOT NULL COMMENT '拼单标题',
    `description` TEXT COMMENT '拼单描述',
    `target_count` INT NOT NULL COMMENT '目标人数',
    `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前人数',
    `deadline` DATETIME NOT NULL COMMENT '截止时间',
    `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `amount_per_person` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '每人金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待成团，1-已成团，2-已取消，3-已完成',
    `creator_id` BIGINT NOT NULL COMMENT '发起人ID',
    `delivery_address` VARCHAR(255) COMMENT '收货地址',
    `delivery_time` DATETIME COMMENT '送达时间',
    `proof_image` VARCHAR(255) COMMENT '凭证图片URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deadline` (`deadline`),
    CONSTRAINT `fk_order_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼单表';

-- 拼单参与者表
CREATE TABLE IF NOT EXISTS `order_participant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
    `order_id` BIGINT NOT NULL COMMENT '拼单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-已退款',
    `receipt_status` TINYINT NOT NULL DEFAULT 0 COMMENT '收货状态：0-未收货，1-已收货',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_user` (`order_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payment_status` (`payment_status`),
    CONSTRAINT `fk_participant_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_participant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼单参与者表';

-- 投诉表
CREATE TABLE IF NOT EXISTS `complaint` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
    `order_id` BIGINT NOT NULL COMMENT '拼单ID',
    `complainant_id` BIGINT NOT NULL COMMENT '投诉人ID',
    `defendant_id` BIGINT NOT NULL COMMENT '被投诉人ID',
    `reason` VARCHAR(200) NOT NULL COMMENT '投诉原因',
    `description` TEXT NOT NULL COMMENT '投诉描述',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已处理，3-已驳回',
    `admin_id` BIGINT COMMENT '处理管理员ID',
    `admin_comment` TEXT COMMENT '管理员处理意见',
    `refund_amount` DECIMAL(10, 2) COMMENT '退款金额',
    `credit_deduction` INT COMMENT '扣分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_complainant_id` (`complainant_id`),
    KEY `idx_defendant_id` (`defendant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_admin_id` (`admin_id`),
    CONSTRAINT `fk_complaint_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_complaint_complainant` FOREIGN KEY (`complainant_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_complaint_defendant` FOREIGN KEY (`defendant_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `order_id` BIGINT NOT NULL COMMENT '拼单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(50) COMMENT '支付方式（伪支付：wechat、alipay、balance）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付，1-支付成功，2-支付失败',
    `transaction_id` VARCHAR(100) COMMENT '交易ID（伪支付生成）',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_transaction_id` (`transaction_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS `refund_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退款记录ID',
    `order_id` BIGINT NOT NULL COMMENT '拼单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额',
    `reason` VARCHAR(255) COMMENT '退款原因',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待退款，1-退款成功，2-退款失败',
    `transaction_id` VARCHAR(100) COMMENT '退款交易ID',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_refund_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_refund_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `admin_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
    `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) NOT NULL COMMENT '目标类型（user、order、complaint等）',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `description` TEXT COMMENT '操作描述',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_action` (`action`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入管理员账户（密码：admin123，需要加密）
INSERT INTO `user` (`username`, `password`, `student_id`, `real_name`, `credit_score`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN001', '系统管理员', 100, 1);

-- 创建索引优化查询性能
ALTER TABLE `order` ADD INDEX `idx_create_time` (`create_time`);
ALTER TABLE `order_participant` ADD INDEX `idx_create_time` (`create_time`);
ALTER TABLE `complaint` ADD INDEX `idx_create_time` (`create_time`);
ALTER TABLE `payment_record` ADD INDEX `idx_create_time` (`create_time`);
ALTER TABLE `refund_record` ADD INDEX `idx_create_time` (`create_time`);
