package cn.rongcloud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.MultipartConfigElement;
import java.util.StringJoiner;

@SpringBootApplication(scanBasePackages = {"cn.rongcloud"})
@ServletComponentScan
@EnableAsync
@Slf4j
@RequestMapping("test")
@EnableScheduling
public class RongRTCApplication {

    public static void main(String[] args) {
        SpringApplication.run(RongRTCApplication.class, args);
        log.info("RongRTCApplication started");
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        factory.setMaxRequestSize(DataSize.ofMegabytes(500));
        return factory.createMultipartConfig();
    }

    @GetMapping("")
    public @ResponseBody
    String test() {
        StringJoiner stringJoiner = new StringJoiner("</br>");
        stringJoiner.add("RongRTC_Server发布成功！");
        stringJoiner.add("</br >");
        stringJoiner.add("版本：");
        stringJoiner.add("2021.06.07 上传文件大小50MB");
        stringJoiner.add("2021.06.08 添加音乐增加size字段");
        stringJoiner.add("2021.06.08 点击用户列表，增加人员校正");
        stringJoiner.add("2021.06.09 房间列表中image增加路径显示");
        stringJoiner.add("2021.06.10 增加清空所有房间接口 delete/all");
        stringJoiner.add("2021.06.22 创建房间，检查用户是否存在;房间列表如果用户为空，删除房间");
        stringJoiner.add("2021.06.23 增加自动检测房间大于3小时后人数为0自动删除任务");
        return stringJoiner.toString();
    }
}
