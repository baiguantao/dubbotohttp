package com.baigt;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

/**
 * 类功能描述:
 *
 * @author baigt
 * @version V1.0
 * @since 2019-01-17 11:33
 */
@SpringBootApplication(scanBasePackages = {"com.baigt"})
@ImportResource(locations = { "classpath:/spring/*.xml" })
@EnableDubbo
public class MainStarter {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MainStarter.class);
        //关闭日志banner日志打印
        springApplication.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = springApplication.run(args);
    }
}
