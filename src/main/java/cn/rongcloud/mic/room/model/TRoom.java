package cn.rongcloud.mic.room.model;

import java.util.Date;
import javax.persistence.*;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.Data;

/**
 * Created by sunyinglong on 2020/5/25
 */
@Entity
@Table(name = "t_room_mic")
@Data
public class TRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uid;
    private String name;
    @Column(name = "theme_picture_url")
    private String themePictureUrl;
    @Column(name = "background_url")
    private String backgroundUrl;
    @Column(name = "allowed_join_room")
    private boolean allowedJoinRoom;
    @Column(name = "allowed_free_join_mic")
    private boolean allowedFreeJoinMic;
    private Integer type;
    @Column(name = "is_private")
    private Integer isPrivate;
    private String password;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "create_dt")
    private Date createDt;
    @Column(name = "update_dt")
    private Date updateDt;

    //以下是新加房间设置字段，数据库不存储
    @Transient
    private Boolean applyOnMic;
    @Transient
    private Boolean applyAllLockMic;
    @Transient
    private Boolean applyAllLockSeat;
    @Transient
    private Boolean setMute;
    @Transient
    private Integer setSeatNumber;

}
