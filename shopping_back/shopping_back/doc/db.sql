-- ==========================================
-- 二手交易平台（shopping_back）数据库初始化脚本
-- ==========================================

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `shop_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 2. 切换到该数据库
USE `shop_db`;

-- 3. 创建用户表（使用 username 作为主键）
CREATE TABLE IF NOT EXISTS `users` (
    `user_id` INT(11) NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '加密后的密码哈希',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `credit` INT(5) NOT NULL DEFAULT 100 COMMENT '信用',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='用户基础信息表';

CREATE TABLE IF NOT EXISTS `goods` (
    `goods_id` INT(11) NOT NULL AUTO_INCREMENT,
    `seller_id` INT(11) NOT NULL COMMENT '卖家ID',
    `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称/标题(对应前端title)',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '商品分类(数码影音/家居生活等)',
    `goods_desc` TEXT COMMENT '商品描述/规格/购买时间',
    `goods_condition` VARCHAR(100) DEFAULT NULL COMMENT '成色/状态(全新/9成新/有瑕疵)',
    `story` TEXT COMMENT '二手故事或新品卖点',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '当前售价',
    `floor_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '最低可接受价(用于AI议价)',
    `scene` VARCHAR(20) NOT NULL COMMENT 'new: 新品 used:闲置',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '所在地区/发货地址(对应前端location)',
    `image` TEXT DEFAULT NULL COMMENT '商品图片(建议存 JSON 字符串或逗号拼接的图片列表)',
    `status` VARCHAR(2) DEFAULT '0' COMMENT '0:在售 1:下架 2:审核中(对应前端提交审核)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`goods_id`),
    CONSTRAINT `fk_goods_user` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='商品基础信息表';


