package cn.rongcloud.mic.room.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_room_music")
@Data
public class TRoomMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String author;
    @Column(name = "room_id")
    private String roomId;
    private Integer type;
    private String url;
    private String size;
    @Column(name = "create_dt")
    private Date createDt;
    @Column(name = "update_dt")
    private Date updateDt;
    private Integer sort;

}
