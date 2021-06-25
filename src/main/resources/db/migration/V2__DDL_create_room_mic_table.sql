CREATE TABLE `t_room_mic` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `uid` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自定义唯一标识',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间名称',
  `theme_picture_url` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间主题图片',
  `background_url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '房间背景图片',
  `allowed_join_room` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许观众加入房间 0否1是',
  `allowed_free_join_mic` tinyint(1) NOT NULL,
  `create_dt` datetime DEFAULT NULL,
  `update_dt` datetime DEFAULT NULL,
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '房间类型：0 官方房间，1自建房间',
  `is_private` tinyint(1) NOT NULL DEFAULT '0' COMMENT '房间类型：0 不是，1 是',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '私密房间密码 md5',
  `user_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '房间创建人 uid',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid_unique` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

