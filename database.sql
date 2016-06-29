CREATE DATABASE `zhihu` /*!40100 DEFAULT CHARACTER SET gbk */;
CREATE TABLE `user` (
  `user_id` varchar(64) NOT NULL COMMENT '短链接',
  `name` varchar(45) DEFAULT NULL,
  `bio` varchar(256) DEFAULT NULL COMMENT '一句话介绍',
  `location` varchar(16) DEFAULT NULL COMMENT '城市',
  `business` varchar(16) DEFAULT NULL COMMENT '行业',
  `gender` varchar(2) DEFAULT NULL COMMENT '性别',
  `education` varchar(64) DEFAULT NULL COMMENT '教育经历',
  `education_extra` varchar(45) DEFAULT NULL COMMENT '学习方向',
  `description` varchar(1024) DEFAULT NULL COMMENT '个人简介',
  `agree` bigint(20) DEFAULT NULL COMMENT '赞同数',
  `thanks` bigint(20) DEFAULT NULL COMMENT '感谢数',
  `asks` bigint(20) DEFAULT NULL COMMENT '提问数',
  `answers` bigint(20) DEFAULT NULL COMMENT '回答数',
  `posts` bigint(20) DEFAULT NULL COMMENT '文章数',
  `collections` bigint(20) DEFAULT NULL COMMENT '收藏夹数',
  `logs` bigint(20) DEFAULT NULL COMMENT '公关编辑数',
  `following` bigint(20) DEFAULT NULL COMMENT '关注数',
  `followers` bigint(20) DEFAULT NULL COMMENT '粉丝数',
  `hash_id` varchar(45) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=gbk;
CREATE TABLE `relation` (
  `user_id` varchar(64) DEFAULT NULL,
  `user_name` varchar(64) DEFAULT NULL,
  `followee_id` varchar(64) DEFAULT NULL,
  `followee_name` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=gbk;
