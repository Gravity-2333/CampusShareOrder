# ER 图绘制说明

## 1. 绘图目标

ER 图用于展示校园拼单平台中**核心数据实体之间的结构关系**，重点说明系统“有哪些主要数据对象、这些对象之间如何关联、主外键如何建立、关系基数如何确定”。
ER 图不用于描述业务先后顺序，不用于描述页面跳转，也不用于描述类的继承与实现关系。

本图需要表达以下核心内容：

1.  系统有哪些核心实体；
2.  各实体的主键与关键外键是什么；
3.  哪些实体之间是一对多关系；
4.  哪些关系通过中间实体实现；
5.  数据模型如何支撑拼单、投诉、资金流水、信用分记录、通知和日志等核心业务。

ER 图必须与以下设计内容保持一致：

-   概要设计中的数据库结构说明
-   数据库表概要一览表
-   后续类图中对应的实体对象
-   后续接口设计中出现的数据对象
-   后续程序中的数据库表结构和实体类
    如果 ER 图中的表名、关系名、字段名与文档正文不一致，后续验收时很容易被判定为设计不严谨。

------

## 2. 绘图风格要求

### 2.1 总体风格

ER 图采用**标准数据库实体关系图风格**绘制，不使用老师给的类图、顺序图、活动图样式。
推荐使用以下表达方式：

-   **矩形框**表示实体（数据表）
-   在实体框内部列出表名和少量关键字段
-   使用**连线**表示实体关系
-   在线两端标注关系基数，如 `1`、`N`、`0..1`
-   通过字段名体现主键（PK）和外键（FK）

如果使用 PowerDesigner、draw.io、Visio、ProcessOn 等工具，均可，只要最终样式清晰规范即可。

### 2.2 不要画成什么样

ER 图**不要画成**：

-   业务流程图
-   UML 类图
-   模块架构图
-   泳道图
-   纯字段清单表

ER 图的重点不是流程，而是“数据对象之间的静态关系”。

------

## 3. 绘图范围

本系统 ER 图建议绘制 **10 个核心实体**，与数据库概要设计保持一致：

1.  `user_account`
2.  `admin_account`
3.  `group_order`
4.  `group_order_member`
5.  `order_receipt`
6.  `complaint`
7.  `capital_record`
8.  `credit_change_record`
9.  `notification`
10.  `operation_log`
     这些表均已在当前数据库设计方案中被确定为系统的核心数据表。

如果图面空间有限，可以在主图中重点展示前 8 个实体，把 `notification` 和 `operation_log` 放在边缘位置，但**不要省略**，因为这两张表在系统设计中仍然属于正式数据实体。

------

## 4. 每个实体应如何绘制

### 4.1 实体框的基本结构

每个实体建议画成一个矩形框，分为两部分：

#### 第一行：表名

例如：

-   `user_account`
-   `group_order`
-   `complaint`

#### 第二部分：关键字段

每张表只保留 4～7 个关键字段，不要把所有字段全塞进去，否则图会过于拥挤。
字段前建议标识：

-   `PK`：主键
-   `FK`：外键

例如：

**group_order**

-   PK `id`
-   FK `creator_user_id`
-   `product_name`
-   `total_member_count`
-   `estimated_total_amount`
-   `status`

### 4.2 字段保留原则

ER 图中的字段只保留：

-   主键
-   外键
-   能体现业务含义的关键字段

不要在 ER 图中放入大量时间戳、备注、创建时间、更新时间等通用字段，除非它们在关系判断中非常关键。

------

## 5. 各实体建议保留的字段

下面是建议放进 ER 图中的字段，不是完整建表字段，而是**用于画图展示的关键字段集**。

------

### 5.1 `user_account`（用户表）

建议保留字段：

-   PK `id`
-   `phone`
-   `student_no`
-   `nickname`
-   `credit_score`
-   `status`

说明：
该实体表示普通用户，是系统大多数业务关系的起点，包括发起拼单、参与拼单、发起投诉、被投诉、接收通知、信用分变化等。

------

### 5.2 `admin_account`（管理员表）

建议保留字段：

-   PK `id`
-   `username`
-   `status`

说明：
管理员表主要用于后台登录与投诉处理，不直接参与拼单主业务关系，但与投诉处理、后台管理和操作日志有关。

------

### 5.3 `group_order`（拼单主表）

建议保留字段：

-   PK `id`
-   FK `creator_user_id`
-   `product_name`
-   `total_member_count`
-   `estimated_total_amount`
-   `status`

说明：
这是系统核心业务主表，表示一条拼单记录。所有成员参与关系、订单凭证、投诉、资金流水等都围绕该实体展开。

------

### 5.4 `group_order_member`（拼单成员表）

建议保留字段：

-   PK `id`
-   FK `group_order_id`
-   FK `user_id`
-   `is_creator`
-   `pay_status`
-   `receive_status`

说明：
该表是系统中最关键的中间关系表，用于连接用户和拼单，表达“某用户在某拼单中的参与情况”。
它本质上是 `user_account` 和 `group_order` 之间多对多关系的拆解结果。

------

### 5.5 `order_receipt`（订单凭证表）

建议保留字段：

-   PK `id`
-   FK `group_order_id`
-   FK `uploader_user_id`
-   `image_url`
-   `actual_total_amount`

说明：
该表用于记录发起人上传的订单凭证。一个拼单最多对应一条凭证记录，因此这是 `group_order` 的一对零或一关系实体。

------

### 5.6 `complaint`（投诉表）

建议保留字段：

-   PK `id`
-   FK `group_order_id`
-   FK `complainant_user_id`
-   FK `accused_user_id`
-   `type`
-   `status`

说明：
投诉表与拼单、用户都有关联。一个投诉记录既关联发起投诉的用户，也关联被投诉的用户，同时关联具体的拼单。

------

### 5.7 `capital_record`（资金流水表）

建议保留字段：

-   PK `id`
-   FK `user_id`
-   FK `group_order_id`
-   FK `member_id`
-   `type`
-   `amount`

说明：
该表用于记录伪支付、差额退款、取消退款、结算给发起人等资金动作，是系统资金追踪的核心表。

------

### 5.8 `credit_change_record`（信用分变更表）

建议保留字段：

-   PK `id`
-   FK `user_id`
-   FK `related_order_id`
-   FK `related_complaint_id`
-   `change_value`
-   `reason_type`

说明：
该表用于记录用户信用分的加减及其原因，用于支持用户个人主页中的信用分展示和后台追溯。

------

### 5.9 `notification`（通知表）

建议保留字段：

-   PK `id`
-   FK `user_id`
-   `type`
-   `is_read`
-   FK `related_order_id`
-   FK `related_complaint_id`

说明：
该表用于记录发给用户的系统通知，如成团通知、退款通知、投诉结果通知等。

------

### 5.10 `operation_log`（操作日志表）

建议保留字段：

-   PK `id`
-   `operator_type`
-   `operator_id`
-   `biz_type`
-   `biz_id`
-   `action`

说明：
该表用于记录用户、管理员、系统的关键操作，更多体现“审计记录”性质，在 ER 图中通常放在边缘位置。

------

## 6. 实体之间的关系及画法

这一部分是 ER 图绘制的核心。组员画图时必须严格按下面的关系连接，不要自行增删关系。

------

### 6.1 用户 与 拼单主表

#### 关系

```
user_account (1) —— (N) group_order
```

#### 含义

一个用户可以发起多个拼单；每个拼单只有一个发起人。

#### 连接字段

```
group_order.creator_user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连一条线到 `group_order`
-   在 `user_account` 一侧标 `1`
-   在 `group_order` 一侧标 `N`
-   在线旁边可标注关系名：**创建 / 发起**

------

### 6.2 用户 与 拼单成员表

#### 关系

```
user_account (1) —— (N) group_order_member
```

#### 含义

一个用户可以参与多个拼单成员记录；每条成员记录只对应一个用户。

#### 连接字段

```
group_order_member.user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连到 `group_order_member`
-   用户侧标 `1`
-   成员表侧标 `N`
-   关系名可标注：**参与**

------

### 6.3 拼单主表 与 拼单成员表

#### 关系

```
group_order (1) —— (N) group_order_member
```

#### 含义

一个拼单可以包含多个成员；每条成员记录只属于一个拼单。

#### 连接字段

```
group_order_member.group_order_id -> group_order.id
```

#### 画法

-   从 `group_order` 连到 `group_order_member`
-   拼单侧标 `1`
-   成员表侧标 `N`

------

### 6.4 用户 与 拼单 的多对多关系说明

这里不要直接画 `user_account` 与 `group_order` 的多对多连线。
正确方式是通过 `group_order_member` 中间实体体现：

-   `user_account (1) —— (N) group_order_member`
-   `group_order (1) —— (N) group_order_member`

#### 说明

因为用户既可以发起拼单，也可以参与拼单，所以用户与拼单存在多对多关系；但这种关系在数据库中是通过成员关系表实现的。
因此 ER 图里必须突出 `group_order_member` 作为中间实体的重要性。

------

### 6.5 拼单主表 与 订单凭证表

#### 关系

```
group_order (1) —— (0..1) order_receipt
```

#### 含义

一个拼单最多有一条订单凭证记录；有的拼单在未成团或未上传凭证阶段可能还没有对应凭证，因此是“零或一”。

#### 连接字段

```
order_receipt.group_order_id -> group_order.id
```

#### 画法

-   从 `group_order` 连到 `order_receipt`
-   拼单侧标 `1`
-   凭证侧标 `0..1`
-   关系名可标注：**上传凭证**

------

### 6.6 用户 与 订单凭证表

#### 关系

```
user_account (1) —— (N) order_receipt
```

#### 含义

一个用户理论上可以在多个拼单中上传凭证；每条凭证记录只由一个上传者上传。由于上传者必须是该拼单发起人，所以这是业务约束，不必在 ER 图中特别强调“必须为发起人”。

#### 连接字段

```
order_receipt.uploader_user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连到 `order_receipt`
-   用户侧标 `1`
-   凭证侧标 `N`

------

### 6.7 拼单主表 与 投诉表

#### 关系

```
group_order (1) —— (N) complaint
```

#### 含义

一个拼单可以关联多条投诉；每条投诉只对应一个具体拼单。

#### 连接字段

```
complaint.group_order_id -> group_order.id
```

#### 画法

-   从 `group_order` 连到 `complaint`
-   拼单侧标 `1`
-   投诉侧标 `N`

------

### 6.8 用户 与 投诉表（投诉人）

#### 关系

```
user_account (1) —— (N) complaint
```

#### 含义

一个用户可以发起多条投诉；每条投诉只有一个投诉人。

#### 连接字段

```
complaint.complainant_user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连到 `complaint`
-   在线旁边标注角色名：**complainant** 或 **投诉人**
-   用户侧标 `1`
-   投诉侧标 `N`

------

### 6.9 用户 与 投诉表（被投诉人）

#### 关系

```
user_account (1) —— (N) complaint
```

#### 含义

一个用户可以在多条投诉中作为被投诉对象出现；每条投诉只有一个被投诉人。

#### 连接字段

```
complaint.accused_user_id -> user_account.id
```

#### 画法

-   再从 `user_account` 连一条线到 `complaint`
-   在线旁边标注角色名：**accused** 或 **被投诉人**
-   不要和上一条线混成一条，必须明确区分两个角色

------

### 6.10 用户 与 信用分变更表

#### 关系

```
user_account (1) —— (N) credit_change_record
```

#### 含义

一个用户可以对应多条信用分变更记录；每条信用分变更记录只属于一个用户。

#### 连接字段

```
credit_change_record.user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连到 `credit_change_record`
-   用户侧标 `1`
-   信用分记录侧标 `N`

------

### 6.11 拼单主表 与 信用分变更表

#### 关系

```
group_order (1) —— (N) credit_change_record
```

#### 含义

一个拼单可能触发多条信用分变化，例如拼单完成加分、投诉成立扣分等。

#### 连接字段

```
credit_change_record.related_order_id -> group_order.id
```

#### 画法

-   从 `group_order` 连到 `credit_change_record`
-   拼单侧标 `1`
-   信用分记录侧标 `N`

------

### 6.12 投诉表 与 信用分变更表

#### 关系

```
complaint (1) —— (N) credit_change_record
```

#### 含义

某些信用分变更是由投诉处理触发的，因此可以通过投诉记录关联信用分变化。

#### 连接字段

```
credit_change_record.related_complaint_id -> complaint.id
```

#### 画法

-   从 `complaint` 连到 `credit_change_record`
-   投诉侧标 `1`
-   信用分记录侧标 `N`

------

### 6.13 拼单主表 与 资金流水表

#### 关系

```
group_order (1) —— (N) capital_record
```

#### 含义

一个拼单可以产生多条资金流水，如发起人支付、参与者支付、差额退款、整单退款、完成后结算等。

#### 连接字段

```
capital_record.group_order_id -> group_order.id
```

#### 画法

-   从 `group_order` 连到 `capital_record`
-   拼单侧标 `1`
-   流水侧标 `N`

------

### 6.14 用户 与 资金流水表

#### 关系

```
user_account (1) —— (N) capital_record
```

#### 含义

一个用户可以关联多条资金流水；每条资金流水记录对应一个用户。

#### 连接字段

```
capital_record.user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连到 `capital_record`
-   用户侧标 `1`
-   流水侧标 `N`

------

### 6.15 拼单成员表 与 资金流水表

#### 关系

```
group_order_member (1) —— (N) capital_record
```

#### 含义

一个成员关系可能关联多条资金流水，用于精确定位某笔支付或退款动作对应的是哪一个拼单成员。

#### 连接字段

```
capital_record.member_id -> group_order_member.id
```

#### 画法

-   从 `group_order_member` 连到 `capital_record`
-   成员侧标 `1`
-   流水侧标 `N`

------

### 6.16 用户 与 通知表

#### 关系

```
user_account (1) —— (N) notification
```

#### 含义

一个用户可以收到多条通知；每条通知只属于一个用户。

#### 连接字段

```
notification.user_id -> user_account.id
```

#### 画法

-   从 `user_account` 连到 `notification`
-   用户侧标 `1`
-   通知侧标 `N`

------

### 6.17 拼单主表 与 通知表

#### 关系

```
group_order (1) —— (N) notification
```

#### 含义

某些通知与拼单有关，如成团通知、退款通知、可取货通知等。

#### 连接字段

```
notification.related_order_id -> group_order.id
```

#### 画法

-   从 `group_order` 连到 `notification`
-   拼单侧标 `1`
-   通知侧标 `N`

------

### 6.18 投诉表 与 通知表

#### 关系

```
complaint (1) —— (N) notification
```

#### 含义

某些通知与投诉有关，如投诉结果通知。

#### 连接字段

```
notification.related_complaint_id -> complaint.id
```

#### 画法

-   从 `complaint` 连到 `notification`
-   投诉侧标 `1`
-   通知侧标 `N`

------

### 6.19 管理员表 与 投诉表

#### 关系

```
admin_account (1) —— (N) complaint
```

#### 含义

一个管理员可以处理多条投诉；每条投诉在处理后由一个管理员完成处理。

#### 连接字段

```
complaint.handled_by_admin_id -> admin_account.id
```

#### 画法

-   从 `admin_account` 连到 `complaint`
-   管理员侧标 `1`
-   投诉侧标 `N`
-   关系名可标注：**处理**

------

### 6.20 操作日志表的画法说明

`operation_log` 是审计型记录表，不宜和太多实体强耦合，否则 ER 图会过于复杂。
因此建议这样处理：

#### 推荐画法

只单独画出 `operation_log` 实体框，不强行把它与 `user_account`、`admin_account`、`group_order` 等全部拉满连接线。
在实体框中通过字段体现：

-   `operator_type`
-   `operator_id`
-   `biz_type`
-   `biz_id`

#### 原因

因为操作日志本质上是“多态记录”，既可能记录用户操作，也可能记录管理员操作，还可能记录系统定时任务动作。如果强行在 ER 图里全连，会严重影响图的可读性。

#### 可选画法

如果组员坚持要画关系，最多只画两条虚线辅助关系：

-   `user_account` —— `operation_log`
-   `admin_account` —— `operation_log`

并在图注中说明：日志通过 `operator_type + operator_id` 记录操作者来源。

------

## 7. 推荐布局方式

为了让整张图结构清晰，建议按“中心实体 + 关系实体 + 边缘记录实体”的方式布局。

### 7.1 中心区域

放两个最核心实体：

-   `group_order`
-   `group_order_member`

这两个放在图中间。

### 7.2 左侧区域

放用户相关实体：

-   `user_account`
-   `admin_account`

其中 `user_account` 靠中间，`admin_account` 靠上或靠下都可以。

### 7.3 右侧区域

放业务附属实体：

-   `order_receipt`
-   `complaint`
-   `capital_record`

### 7.4 下方区域

放记录型实体：

-   `credit_change_record`
-   `notification`
-   `operation_log`

### 7.5 目的

这样布局能突出业务核心关系：

-   用户 ↔ 拼单 ↔ 成员
-   拼单 ↔ 凭证 / 投诉 / 流水
-   用户 ↔ 信用分 / 通知

整张图会比较平衡，不容易交叉过多。

------

## 8. 命名统一要求

为了保证与文档正文、数据库表概要和后续程序实现一致，图中表名与字段名必须严格统一。

### 8.1 表名统一为

-   `user_account`
-   `admin_account`
-   `group_order`
-   `group_order_member`
-   `order_receipt`
-   `complaint`
-   `capital_record`
-   `credit_change_record`
-   `notification`
-   `operation_log`

### 8.2 关键字段统一为

-   `creator_user_id`
-   `group_order_id`
-   `user_id`
-   `uploader_user_id`
-   `complainant_user_id`
-   `accused_user_id`
-   `handled_by_admin_id`
-   `related_order_id`
-   `related_complaint_id`

不要在 ER 图里一会儿写 `creatorId`，一会儿又写 `creator_user_id`。
数据库命名必须统一使用**下划线风格**。

------

## 9. 图名与图下注释建议

### 图名建议

**图 X-X 校园拼单平台数据库 ER 图**

### 图下注释建议

本图展示校园拼单平台核心数据实体之间的关系结构。系统以用户、拼单主表和拼单成员表为中心，通过订单凭证、投诉、资金流水、信用分变更、通知和操作日志等实体支撑拼单流程、异常处理和后台审计。ER 图中的实体关系与系统数据库设计、业务流程和后续程序实现保持一致。

------

## 10. 组员绘图时的注意事项

1.  **不要把 ER 图画成字段大全**
    每张表只保留关键字段，图的重点是关系，不是完整建表语句。
2.  **不要漏掉 `group_order_member`**
    这是整张 ER 图里最重要的中间实体，不能省略。
3.  **投诉表与用户表必须画两条关系线**
    一条代表投诉人，一条代表被投诉人，不能合并成一条。
4.  **`order_receipt` 与 `group_order` 的关系必须画成 1 对 0..1**
    因为不是所有拼单一开始就有凭证。
5.  **`operation_log` 不要拉太多线**
    否则整张图会变得很乱。
6.  **图中实体名必须是表名，不要写中文模块名代替**
    例如必须写 `group_order`，不能只写“拼单表”。
7.  **关系线尽量减少交叉**
    优先通过布局调整解决，不要让整张图缠在一起。
8.  **管理员表不要画得太核心**
    它只服务后台处理，不是拼单主流程的主实体。

------

## 11. 最终交付要求

负责绘制 ER 图的组员最终需要提交：

1.  一张完整的数据库 ER 图；
2.  图中包含 10 个核心实体；
3.  图中体现主键、关键外键和关系基数；
4.  图名、表名、字段名全部统一；
5.  图能够直接插入 Word，清晰可读；
6.  图与概要设计数据库部分内容完全一致。