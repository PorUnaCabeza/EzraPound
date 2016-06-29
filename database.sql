CREATE DATABASE `zhihu` /*!40100 DEFAULT CHARACTER SET gbk */;
CREATE TABLE `user` (
  `user_id` varchar(64) NOT NULL COMMENT '������',
  `name` varchar(45) DEFAULT NULL,
  `bio` varchar(256) DEFAULT NULL COMMENT 'һ�仰����',
  `location` varchar(16) DEFAULT NULL COMMENT '����',
  `business` varchar(16) DEFAULT NULL COMMENT '��ҵ',
  `gender` varchar(2) DEFAULT NULL COMMENT '�Ա�',
  `education` varchar(64) DEFAULT NULL COMMENT '��������',
  `education_extra` varchar(45) DEFAULT NULL COMMENT 'ѧϰ����',
  `description` varchar(1024) DEFAULT NULL COMMENT '���˼��',
  `agree` bigint(20) DEFAULT NULL COMMENT '��ͬ��',
  `thanks` bigint(20) DEFAULT NULL COMMENT '��л��',
  `asks` bigint(20) DEFAULT NULL COMMENT '������',
  `answers` bigint(20) DEFAULT NULL COMMENT '�ش���',
  `posts` bigint(20) DEFAULT NULL COMMENT '������',
  `collections` bigint(20) DEFAULT NULL COMMENT '�ղؼ���',
  `logs` bigint(20) DEFAULT NULL COMMENT '���ر༭��',
  `following` bigint(20) DEFAULT NULL COMMENT '��ע��',
  `followers` bigint(20) DEFAULT NULL COMMENT '��˿��',
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
