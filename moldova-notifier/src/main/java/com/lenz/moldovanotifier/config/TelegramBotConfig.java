package com.lenz.moldovanotifier.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class TelegramBotConfig {

  private @Value("${bot.name}") String botName;
  private @Value("${bot.token}") String token;
}
