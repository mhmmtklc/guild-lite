package com.guildlite;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.guildlite.security",
        "com.guildlite.user",
        "com.guildlite.team",
        "com.guildlite.coin",
        "com.guildlite.chat",
        "com.guildlite"
})
@EntityScan(basePackages = {
        "com.guildlite.user.entity",
        "com.guildlite.team.entity",
        "com.guildlite.coin.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.guildlite.user.repository",
        "com.guildlite.team.repository",
        "com.guildlite.coin.repository",
})
public class GuildLiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuildLiteApplication.class, args);
    }

}