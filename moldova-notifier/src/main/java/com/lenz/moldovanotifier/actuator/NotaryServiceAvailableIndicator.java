package com.lenz.moldovanotifier.actuator;

import com.lenz.moldovanotifier.manager.EmbassyBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("notaryServiceCheck")
@Slf4j
public class NotaryServiceAvailableIndicator implements HealthIndicator {

  private final RestTemplate restTemplate = new RestTemplate();
  private final EmbassyBookingService embassyBookingService;

  @Autowired
  public NotaryServiceAvailableIndicator(EmbassyBookingService embassyBookingService) {
    this.embassyBookingService = embassyBookingService;
  }

  @Override
  public Health health() {
    return checkHealth() ?
      Health.up().build() :
      Health.down().withDetail("Error", "Custom health check failed").build();
  }

  private boolean checkHealth() {
    String notaryServiceUrl = embassyBookingService.buildApiUrl(embassyBookingService.getNotaryResourceId(), embassyBookingService.getNotaryServiceId());
    try {
      String response = restTemplate.getForObject(notaryServiceUrl, String.class);
      return response != null && response.contains("OK");
    } catch (Exception e) {
      // Логируем ошибку или обрабатываем ее
      return false;
    }
  }
}
