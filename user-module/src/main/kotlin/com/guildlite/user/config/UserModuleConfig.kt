package com.guildlite.user.config

import com.guildlite.security.config.SecurityConfig
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@ComponentScan(basePackages = ["com.guildlite.user"])
@EntityScan(basePackages = ["com.guildlite.user.entity"])
@EnableTransactionManagement
@Import(SecurityConfig::class)
class UserModuleConfig {

}