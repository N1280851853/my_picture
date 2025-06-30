-- 创建数据库的语句
create database if not exists whc_picture;

-- 切换数据库
use whc_picture;


-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    gmt_create   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (user_account),
    INDEX idx_userName (user_name)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    pic_size   bigint            null comment '图片体积',
    pic_width  int               null comment '图片宽度',
    pic_height int               null comment '图片高度',
    pic_scale  double            null comment '图片宽高比例',
    pic_format varchar(32)       null comment '图片格式',
    user_id    bigint            not null comment '创建用户 id',
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete  tinyint default 0 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_userId (user_id)             -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

create table if not exists tag
(
    id           bigint unsigned auto_increment comment 'id' primary key,
    tag_name     varchar(128)                       not null comment '标签名称',
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    INDEX idx_name (tag_name)
) comment '标签' collate = utf8mb4_unicode_ci;


create table if not exists picture_tag
(
    id           bigint unsigned auto_increment comment 'id' primary key,
    picture_id   bigint unsigned                    NOT NULL comment '图片ID',
    picture_name varchar(128)                       not null comment '图片名称',
    tag_id       bigint unsigned                    not null comment '标签Id',
    tag_name     varchar(128)                       not null comment '标签名称',
    gmt_create   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    gmt_modified datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_pictureId (picture_id), -- 基于图片id提高查询性能
    INDEX idx_tagId (tag_id)          -- 基于标签id提高查询性能
) comment '图片、标签关联关系表' collate = utf8mb4_unicode_ci;


ALTER TABLE picture
    -- 添加新列
    ADD COLUMN `review_status` TINYINT unsigned DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝' after `user_id`,
    ADD COLUMN `review_message` VARCHAR(512) NULL COMMENT '审核信息' after `review_status`,
    ADD COLUMN `reviewer_id` BIGINT NULL COMMENT '审核人 ID' after `review_message`,
    ADD COLUMN `review_time` DATETIME NULL COMMENT '审核时间' after `reviewer_id`;

-- 创建基于 review_status 列的索引
CREATE INDEX idx_reviewStatus ON picture (review_status);

ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnail_url varchar(512) NULL COMMENT '缩略图 url' after `url`;

