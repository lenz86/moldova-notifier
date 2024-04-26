package com.lenz.moldovanotifier.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiManager {

  public static final LocalDateTime now = LocalDateTime.now();
  public static final LocalDateTime fourMonth = LocalDateTime.now().plusMonths(4);
  public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  public static final String baseUrl = "https://ambasada-r-moldova-in-f-rusa.reservio.com";
  public static final String apiPath = "/api/v2/businesses/09250556-2450-437f-aede-82e78712f114/availability/booking-days";
  public static final String filterFrom = "?filter[from]=" + now + "Z";
  public static final String filterTo = "&filter[to]=" + fourMonth + "Z";
  public static final String filterResourceIdPass = "&filter[resourceId]=b6eae6ed-86ac-4ed7-9fe5-2482f0d0bcb9";
  public static final String filterServiceIdPass = "&filter[serviceId]=f8c23fdc-e908-47a5-80f7-a1b43732cf26";
  public static final String filterResourceIdCitizenship = "&filter[resourceId]=f36c0d4f-71ec-4b39-8660-2337857176ed";
  public static final String filterServiceIdCitizenship = "&filter[serviceId]=63fe0e8c-b127-43e3-874a-bac9c660045b";
  public static final String filterResourceIdTest = "&filter[resourceId]=0a840ab9-a324-4187-a502-d08ec0f5721e";
  public static final String filterServiceIdTest = "&filter[serviceId]=8e13743d-076d-4aa0-b0c2-c8d3c2b64de2";
  public static final String ignoreBooking = "&ignoreBookingBoundaries=0";
  private final String citizenshipUrl;
  private final String passportUrl;
  private final String testUrl;
  private int availableCnt = 0;

  public ApiManager() {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    citizenshipUrl = urlBuilder
      .append(apiPath)
      .append(filterFrom)
      .append(filterResourceIdCitizenship)
      .append(filterServiceIdCitizenship)
      .append(ignoreBooking)
      .append(filterTo)
      .toString();

    urlBuilder = new StringBuilder(baseUrl);

    passportUrl = urlBuilder
      .append(apiPath)
      .append(filterFrom)
      .append(filterResourceIdPass)
      .append(filterServiceIdPass)
      .append(ignoreBooking)
      .append(filterTo)
      .toString();

    urlBuilder = new StringBuilder(baseUrl);

    testUrl = urlBuilder
      .append(apiPath)
      .append(filterFrom)
      .append(filterResourceIdTest)
      .append(filterServiceIdTest)
      .append(ignoreBooking)
      .append(filterTo)
      .toString();
  }

  public void callApi() {
    callApiImpl(citizenshipUrl, "CITIZENSHIP");
    callApiImpl(passportUrl, "PASSPORT");
  }

  public void callApi(String[] args) {
    for (String arg : args) {
      if (arg.equals("-test")) {
        callApiImpl(testUrl, "TEST");
      }
    }
    callApiImpl(citizenshipUrl, "CITIZENSHIP");
    callApiImpl(passportUrl, "PASSPORT");
  }

  private void callApiImpl(String url, String type) {
    RestTemplate restTemplate = new RestTemplate();
    try {
      ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
      String jsonResponse = response.getBody();
      Map<LocalDate, String> date2available = parseValue(jsonResponse);
      logResult(type, response.getStatusCode(), date2available);
    } catch (RestClientException e) {
      e.printStackTrace();
    }
  }

  private Map<LocalDate, String> parseValue(String jsonResponse) {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<LocalDate, String> date2available = new HashMap<>();
    try {
      JsonNode jsonNode = objectMapper.readTree(jsonResponse);
      JsonNode data = jsonNode.get("data");
      if (data != null && data.isArray()) {
        for (JsonNode itemNode : data) {
          JsonNode attributesNode = itemNode.get("attributes");
          if (attributesNode != null) {
            JsonNode date = attributesNode.get("date");
            JsonNode isAvailable = attributesNode.get("isAvailable");
            if (date != null && isAvailable != null) {
              date2available.put(LocalDate.parse(date.asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd")), isAvailable.asText());
            }
          }
        }
      }

    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return date2available;
  }

  private void logResult(String type, HttpStatusCode status, Map<LocalDate, String> date2available) {
    Map<LocalDate, String> availableDates = date2available.entrySet().stream()
      .filter(entry -> entry.getValue().equals("true"))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<LocalDate, String> sortedMap = availableDates.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (oldValue, newValue) -> oldValue,
        LinkedHashMap::new
      ));

    String base = type + " " + status + " Period: " + now + " - " + fourMonth + " ==> ";

    if (sortedMap.isEmpty()) {
      System.out.println(base + " No available dates. Check time:  " + new Date() + " Total available: " + availableCnt);
    } else {
      availableCnt++;
      System.out.println(base + " HAS AVAILABLE!!! Check time: " + new Date() + " Total available: " + availableCnt);
      sortedMap.forEach((k, v) -> {
        System.out.println("====================================================================");
        System.out.println("====================================================================");
        System.out.println();
        System.out.println("Available date: " + k + " -------> " + v);
        System.out.println();
        System.out.println("====================================================================");
        System.out.println("====================================================================");
      });
    }
  }
}
