package cn.rongcloud.mic.user.dao;

import cn.rongcloud.mic.user.model.TUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by sunyinglong on 2020/05/25.
 */
@Repository
public interface UserDao extends JpaRepository<TUser, Long> {

    TUser findTUserByMobileEquals(String mobile);

    TUser findTUserByDeviceIdEqualsAndTypeEquals(String deviceId, Integer type);

    TUser findTUserByUidEquals(String uid);

    @Transactional
    @Modifying
    @Query(value = "update TUser t  set t.name=?2, t.portrait=?3 where t.uid=?1")
    int updateUser(String userId, String name, String portrait);

}