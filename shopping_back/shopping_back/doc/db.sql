CREATE DATABASE IF NOT EXISTS `shop_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `shop_db`;

CREATE TABLE IF NOT EXISTS `users` (
    `user_id` INT(11) NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '加密后的密码哈希',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `credit` INT(5) NOT NULL DEFAULT 100 COMMENT '信用分',
    `role` VARCHAR(20) NOT NULL DEFAULT 'buyer' COMMENT 'buyer/seller/admin',
    `status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT 'normal/disabled',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='用户基础信息表';

CREATE TABLE IF NOT EXISTS `goods` (
    `goods_id` INT(11) NOT NULL AUTO_INCREMENT,
    `seller_id` INT(11) NOT NULL COMMENT '卖家ID',
    `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称/标题',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '商品分类',
    `goods_desc` TEXT COMMENT '商品描述',
    `goods_condition` VARCHAR(100) DEFAULT NULL COMMENT '成色/状态',
    `story` TEXT COMMENT '二手故事或新品卖点',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '当前售价',
    `floor_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '最低可接受价',
    `scene` VARCHAR(20) NOT NULL COMMENT 'new: 新品 used: 闲置',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '所在地/发货地址',
    `image` TEXT DEFAULT NULL COMMENT '商品图片',
    `status` VARCHAR(2) DEFAULT '0' COMMENT '0:在售 1:下架 2:审核中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`goods_id`),
    CONSTRAINT `fk_goods_user` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='商品基础信息表';

CREATE TABLE IF NOT EXISTS `user_realname_auth` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `real_name` VARCHAR(50) NOT NULL,
    `id_card_masked` VARCHAR(30) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
    `reject_reason` VARCHAR(255) DEFAULT NULL,
    `reviewed_by` INT DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `reviewed_at` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='实名模拟审核';

CREATE TABLE IF NOT EXISTS `favorite_goods` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `goods_id` INT NOT NULL,
    `item_title` VARCHAR(255) DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='买家收藏';

CREATE TABLE IF NOT EXISTS `browse_history` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `goods_id` INT NOT NULL,
    `item_title` VARCHAR(255) DEFAULT NULL,
    `viewed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='买家足迹';

CREATE TABLE IF NOT EXISTS `follow_store` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `store_id` INT NOT NULL,
    `store_name` VARCHAR(255) DEFAULT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='关注店铺';

CREATE TABLE IF NOT EXISTS `store` (
    `store_id` INT NOT NULL AUTO_INCREMENT,
    `seller_id` INT NOT NULL,
    `store_name` VARCHAR(100) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'normal',
    `score` DECIMAL(3,1) NOT NULL DEFAULT 4.8,
    `credit_score` INT NOT NULL DEFAULT 100,
    `violation_count` INT NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`store_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='卖家店铺';

CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` INT NOT NULL AUTO_INCREMENT,
    `buyer_id` INT NOT NULL,
    `seller_id` INT NOT NULL,
    `goods_id` INT DEFAULT NULL,
    `status` VARCHAR(30) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='订单概览';

CREATE TABLE IF NOT EXISTS `credit_record` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `change_value` INT NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COMMENT='信用分记录';
