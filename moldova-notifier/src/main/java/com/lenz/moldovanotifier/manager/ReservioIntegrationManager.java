package com.lenz.moldovanotifier.manager;

import com.lenz.moldovanotifier.model.BaseResponse;
import com.lenz.moldovanotifier.model.EmbassyServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ReservioIntegrationManager {

  public <T extends BaseResponse> ResponseEntity<T> callApi(String targetUrl, EmbassyServiceType serviceType, Class<T> responseType) {
    return makeRequest(targetUrl, serviceType, responseType);
  }

  private <T extends BaseResponse> ResponseEntity<T> makeRequest(String url, EmbassyServiceType serviceType, Class<T> responseType) {
    RestTemplate restTemplate = new RestTemplate();
    try {
      ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
      return response;
    } catch (Exception ex) {
      // TODO: 26.04.2024 create an exception
      log.error("Request to service: {} failed with error: {}", serviceType, ex);
      throw new RuntimeException("Error when sending request to URL: " + url + ". Error: " + ex);
    }
  }
}
