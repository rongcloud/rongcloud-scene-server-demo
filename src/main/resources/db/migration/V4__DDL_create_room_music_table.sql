CREATE TABLE `t_room_music` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '音乐名称',
  `author` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '作者',
  `room_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间 id',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '音乐类型：0 官方音乐，1用户上传,3 从官方添加',
  `url` varchar(64) NOT NULL DEFAULT '1' COMMENT '音乐地址',
  `size` varchar(10) NOT NULL DEFAULT '0' COMMENT '大小',
  `create_dt` datetime DEFAULT NULL,
  `update_dt` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `room_id_index` (`room_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;