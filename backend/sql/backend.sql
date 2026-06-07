-- 创建数据库
DROP DATABASE IF EXISTS campus_share_order;
CREATE DATABASE campus_share_order CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_share_order;

-- 1. 用户账户表
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(11) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    student_no VARCHAR(10) UNIQUE,
    is_verified BOOLEAN NOT NULL DEFAULT 0,
    credit_score INTEGER NOT NULL DEFAULT 80,
    status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    contact_info VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户表索引
CREATE INDEX idx_user_account_status_created_at ON user_account(status, created_at);

-- 2. 管理员账户表
CREATE TABLE IF NOT EXISTS admin_account (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at DATETIME
);

-- 管理员表索引
CREATE UNIQUE INDEX uk_admin_account_username ON admin_account(username);

-- 3. 拼单订单表
CREATE TABLE IF NOT EXISTS group_order (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    creator_user_id BIGINT UNSIGNED NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_desc TEXT,
    total_member_count INTEGER NOT NULL,
    current_member_count INTEGER NOT NULL DEFAULT 1,
    estimated_total_amount DECIMAL(10,2) NOT NULL,
    estimated_per_amount DECIMAL(10,2) NOT NULL,
    actual_total_amount DECIMAL(10,2),
    actual_per_amount DECIMAL(10,2),
    pickup_point VARCHAR(100) NOT NULL,
    deadline_at DATETIME NOT NULL,
    receipt_upload_deadline_at DATETIME,
    expected_delivery_start_at DATETIME,
    expected_delivery_end_at DATETIME,
    delivered_at DATETIME,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    cancel_reason VARCHAR(255),
    complaint_opened BOOLEAN NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_user_id) REFERENCES user_account(id)
);

-- 订单表索引
CREATE UNIQUE INDEX uk_group_order_order_no ON group_order(order_no);
CREATE INDEX idx_group_order_creator_created_at ON group_order(creator_user_id, created_at);
CREATE INDEX idx_group_order_status_deadline_at ON group_order(status, deadline_at);
CREATE INDEX idx_group_order_status_receipt_deadline_at ON group_order(status, receipt_upload_deadline_at);
CREATE INDEX idx_group_order_status_expected_delivery_end_at ON group_order(status, expected_delivery_end_at);

-- 4. 订单成员表
CREATE TABLE IF NOT EXISTS group_order_member (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    group_order_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    is_creator BOOLEAN NOT NULL DEFAULT 0,
    remark VARCHAR(255),
    join_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    pay_status VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
    pay_amount DECIMAL(10,2),
    paid_at DATETIME,
    refund_amount_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    receive_status VARCHAR(32) NOT NULL DEFAULT 'NOT_READY',
    received_at DATETIME,
    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_order_id) REFERENCES group_order(id),
    FOREIGN KEY (user_id) REFERENCES user_account(id),
    UNIQUE KEY uk_group_order_member_order_user (group_order_id, user_id)
);

-- 成员表索引
CREATE INDEX idx_group_order_member_order_join ON group_order_member(group_order_id, join_status);
CREATE INDEX idx_group_order_member_order_pay ON group_order_member(group_order_id, pay_status);
CREATE INDEX idx_group_order_member_order_receive ON group_order_member(group_order_id, receive_status);
CREATE INDEX idx_group_order_member_user_created_at ON group_order_member(user_id, created_at);

-- 5. 订单凭证表
CREATE TABLE IF NOT EXISTS order_receipt (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    group_order_id BIGINT UNSIGNED UNIQUE NOT NULL,
    uploader_user_id BIGINT UNSIGNED NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    actual_total_amount DECIMAL(10,2) NOT NULL,
    expected_delivery_start_at DATETIME NOT NULL,
    expected_delivery_end_at DATETIME NOT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_order_id) REFERENCES group_order(id),
    FOREIGN KEY (uploader_user_id) REFERENCES user_account(id)
);

-- 凭证表索引
CREATE UNIQUE INDEX uk_order_receipt_group_order_id ON order_receipt(group_order_id);

-- 6. 投诉表
CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    complaint_no VARCHAR(32) UNIQUE NOT NULL,
    group_order_id BIGINT UNSIGNED NOT NULL,
    complainant_user_id BIGINT UNSIGNED NOT NULL,
    accused_user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    handle_result TEXT,
    handled_by_admin_id BIGINT UNSIGNED,
    handled_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (group_order_id) REFERENCES group_order(id),
    FOREIGN KEY (complainant_user_id) REFERENCES user_account(id),
    FOREIGN KEY (accused_user_id) REFERENCES user_account(id),
    FOREIGN KEY (handled_by_admin_id) REFERENCES admin_account(id),
    UNIQUE KEY uk_complaint_order_complainant (group_order_id, complainant_user_id)
);

-- 投诉表索引
CREATE INDEX idx_complaint_status_created_at ON complaint(status, created_at);
CREATE INDEX idx_complaint_order_created_at ON complaint(group_order_id, created_at);

-- 7. 资金流水表
CREATE TABLE IF NOT EXISTS capital_record (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    biz_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    group_order_id BIGINT UNSIGNED,
    member_id BIGINT UNSIGNED,
    type VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    remark VARCHAR(255),
    operator_type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    operator_id BIGINT UNSIGNED,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_account(id),
    FOREIGN KEY (group_order_id) REFERENCES group_order(id),
    FOREIGN KEY (member_id) REFERENCES group_order_member(id)
);

-- 资金表索引
CREATE UNIQUE INDEX uk_capital_record_biz_no ON capital_record(biz_no);
CREATE INDEX idx_capital_record_user_created_at ON capital_record(user_id, created_at);
CREATE INDEX idx_capital_record_order_type_created_at ON capital_record(group_order_id, type, created_at);
CREATE INDEX idx_capital_record_member_created_at ON capital_record(member_id, created_at);

-- 8. 信用分变更表
CREATE TABLE IF NOT EXISTS credit_change_record (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    change_value INTEGER NOT NULL,
    reason_type VARCHAR(32) NOT NULL,
    related_order_id BIGINT UNSIGNED,
    related_complaint_id BIGINT UNSIGNED,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_account(id),
    FOREIGN KEY (related_order_id) REFERENCES group_order(id),
    FOREIGN KEY (related_complaint_id) REFERENCES complaint(id)
);

-- 信用分表索引
CREATE INDEX idx_credit_change_record_user_created_at ON credit_change_record(user_id, created_at);
CREATE INDEX idx_credit_change_record_reason_created_at ON credit_change_record(reason_type, created_at);

-- 9. 通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(255) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT 0,
    related_order_id BIGINT UNSIGNED,
    related_complaint_id BIGINT UNSIGNED,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES user_account(id),
    FOREIGN KEY (related_order_id) REFERENCES group_order(id),
    FOREIGN KEY (related_complaint_id) REFERENCES complaint(id)
);

-- 通知表索引
CREATE INDEX idx_notification_user_read_created_at ON notification(user_id, is_read, created_at);
CREATE INDEX idx_notification_user_type_created_at ON notification(user_id, type, created_at);

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    operator_type VARCHAR(32) NOT NULL,
    operator_id BIGINT UNSIGNED,
    biz_type VARCHAR(32) NOT NULL,
    biz_id BIGINT UNSIGNED,
    action VARCHAR(64) NOT NULL,
    detail_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 日志表索引
CREATE INDEX idx_operation_log_operator_created_at ON operation_log(operator_type, operator_id, created_at);
CREATE INDEX idx_operation_log_biz_created_at ON operation_log(biz_type, biz_id, created_at);
CREATE INDEX idx_operation_log_action_created_at ON operation_log(action, created_at);

-- 插入初始管理员账户（密码：123456）
INSERT INTO admin_account (username, password_hash, status) 
VALUES ('admin', '$2a$10$PfhUK8XWBKL8Bms/OwbAHO8Kqub8UbigAJVnjQVFOh871KZCHAxA2', 'NORMAL')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 插入初始用户（密码：123456）
-- BCrypt: $2a$10$PfhUK8XWBKL8Bms/OwbAHO8Kqub8UbigAJVnjQVFOh871KZCHAxA2
INSERT INTO user_account (id, phone, password_hash, nickname, student_no, is_verified, credit_score, status, contact_info) 
VALUES 
(1, '13800000001', '$2a$10$PfhUK8XWBKL8Bms/OwbAHO8Kqub8UbigAJVnjQVFOh871KZCHAxA2', '张三', '20240001', 1, 100, 'NORMAL', '微信 zhangsan_01'),
(2, '13800000002', '$2a$10$PfhUK8XWBKL8Bms/OwbAHO8Kqub8UbigAJVnjQVFOh871KZCHAxA2', '李四', '20240002', 1, 96, 'NORMAL', '微信 lisi_02'),
(3, '13800000003', '$2a$10$PfhUK8XWBKL8Bms/OwbAHO8Kqub8UbigAJVnjQVFOh871KZCHAxA2', '王五', NULL, 0, 88, 'NORMAL', '微信 wangwu_03')
ON DUPLICATE KEY UPDATE phone = VALUES(phone);

-- 插入初始拼单订单
INSERT INTO group_order (id, order_no, creator_user_id, product_name, product_desc, total_member_count, current_member_count, estimated_total_amount, estimated_per_amount, pickup_point, deadline_at, status)
VALUES 
(1, 'GO20260417001', 1, '晚饭拼单', '三个人一起点晚饭，均摊配送费。', 3, 2, 48.00, 16.00, '东区宿舍门口', DATE_ADD(NOW(), INTERVAL 6 HOUR), 'OPEN'),
(2, 'GO20260417002', 2, '奶茶拼单', '奶茶加小料拼单。', 3, 3, 60.00, 20.00, '图书馆南门', DATE_ADD(NOW(), INTERVAL 2 HOUR), 'WAIT_DELIVERY')
ON DUPLICATE KEY UPDATE order_no = VALUES(order_no);

-- 插入订单成员
INSERT INTO group_order_member (group_order_id, user_id, is_creator, pay_status, pay_amount, paid_at, receive_status, role)
VALUES 
(1, 1, 1, 'PAID', 16.00, DATE_SUB(NOW(), INTERVAL 90 MINUTE), 'NOT_READY', 'INITIATOR'),
(1, 2, 0, 'UNPAID', 16.00, NULL, 'NOT_READY', 'MEMBER'),
(2, 2, 1, 'PAID', 20.00, DATE_SUB(NOW(), INTERVAL 60 MINUTE), 'NOT_READY', 'INITIATOR'),
(2, 1, 0, 'PAID', 20.00, DATE_SUB(NOW(), INTERVAL 55 MINUTE), 'NOT_READY', 'MEMBER'),
(2, 3, 0, 'PAID', 20.00, DATE_SUB(NOW(), INTERVAL 50 MINUTE), 'NOT_READY', 'MEMBER')
ON DUPLICATE KEY UPDATE group_order_id = VALUES(group_order_id), user_id = VALUES(user_id);

-- 插入信用分记录
INSERT INTO credit_change_record (user_id, change_value, reason_type, remark)
VALUES 
(1, 100, 'INITIAL', '初始信用分'),
(2, 96, 'INITIAL', '初始信用分'),
(3, 88, 'INITIAL', '初始信用分')
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
