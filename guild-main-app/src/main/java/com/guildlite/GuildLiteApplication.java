package com.guildlite;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.guildlite.security",
        "com.guildlite.user",
        "com.guildlite"
})
@EntityScan(basePackages = {
        "com.guildlite.user.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.guildlite.user.repository"
})
public class GuildLiteApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(GuildLiteApplication.class, args);

        // TODO - Delete
        System.out.println("=== SCANNING CONTROLLERS ===");
        Map<String, Object> controllers = context.getBeansWithAnnotation(RestController.class);
        if (controllers.isEmpty()) {
            System.out.println("❌ NO CONTROLLERS FOUND!");
        } else {
            controllers.forEach((name, bean) -> {
                System.out.println("✅ Controller: " + name + " -> " + bean.getClass().getName());
            });
        }
        System.out.println("============================");
    }

}