USE campus_share_order;

-- Existing databases should run this migration once before deploying the
-- iteration-two capital record changes.
ALTER TABLE capital_record
    ADD COLUMN operator_type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM' AFTER remark,
    ADD COLUMN operator_id BIGINT UNSIGNED NULL AFTER operator_type;

UPDATE capital_record
SET operator_type = 'USER',
    operator_id = user_id
WHERE type IN ('PAY', 'REFUND_EXIT');
