package com.lenz.moldovanotifier.manager;

import com.lenz.moldovanotifier.model.EmbassyServiceType;
import com.lenz.moldovanotifier.model.booking.BookingData;
import com.lenz.moldovanotifier.model.booking.BookingResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@Getter
public class EmbassyBookingService {

  private @Autowired ReservioIntegrationManager integrationManager;
  private @Autowired TelegramBotManager telegramBotManager;

  public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private @Value("${Embassy.BaseUrl}") String baseUrl;
  private @Value("${Embassy.CheckingPeriodInMonth}") int checkingPeriodInMonth;
  private @Value("${Embassy.IgnoreBooking}") byte ignoreBooking;

  private @Value("${Embassy.EmbassyApiId}")             String embassyApiId;
  private @Value("${Embassy.CitizenshipResourceId}")    String citizenshipResourceId;
  private @Value("${Embassy.CitizenshipServiceId}")     String citizenshipServiceId;
  private @Value("${Embassy.NotaryResourceId}")         String notaryResourceId;
  private @Value("${Embassy.NotaryServiceId}")          String notaryServiceId;
  private @Value("${Embassy.ReregistrationResourceId}") String reregistrationResourceId;
  private @Value("${Embassy.ReregistrationServiceId}")  String reregistrationServiceId;
  private @Value("${Embassy.PassportResourceId}")       String passportResourceId;
  private @Value("${Embassy.PassportServiceId}")        String passportServiceId;



  public void checkAllServices() {
    checkReRegistrationAvailableDates();
    checkCitizenshipAvailableDates();
    checkNotaryAvailableDates();
    checkPassportAvailableDates();
  }

  public void checkCitizenshipAvailableDates() {
    String citizenshipUrl = buildApiUrl(citizenshipResourceId, citizenshipServiceId);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(citizenshipUrl, EmbassyServiceType.CITIZENSHIP, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.", EmbassyServiceType.CITIZENSHIP);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.", EmbassyServiceType.CITIZENSHIP, availablePeriods.size());
      String urlForBooking = buildBookingUrl(citizenshipServiceId);
      String message = """
        There are available dates for service: %s. Link for booking: %s
        """.formatted(EmbassyServiceType.CITIZENSHIP, urlForBooking);
      telegramBotManager.notifySubscribers(message, EmbassyServiceType.CITIZENSHIP);
    }
  }

  public void checkNotaryAvailableDates() {
    String notaryUrl = buildApiUrl(notaryResourceId, notaryServiceId);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(notaryUrl, EmbassyServiceType.NOTARY, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.}", EmbassyServiceType.NOTARY);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.", EmbassyServiceType.NOTARY, availablePeriods.size());
      String urlForBooking = buildBookingUrl(notaryServiceId);
      String message = """
        There are available dates for service: %s. Link for booking: %s
        """.formatted(EmbassyServiceType.NOTARY, urlForBooking);
      telegramBotManager.notifySubscribers(message, EmbassyServiceType.NOTARY);
    }
  }

  public void checkReRegistrationAvailableDates() {
    String reRegistrationUrl = buildApiUrl(reregistrationResourceId, reregistrationServiceId);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(reRegistrationUrl, EmbassyServiceType.REREGISTRATION, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.", EmbassyServiceType.REREGISTRATION);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.", EmbassyServiceType.REREGISTRATION, availablePeriods.size());
      String urlForBooking = buildBookingUrl(reregistrationServiceId);
      String message = """
        There are available dates for service: %s. Link for booking: %s
        """.formatted(EmbassyServiceType.REREGISTRATION, urlForBooking);
      telegramBotManager.notifySubscribers(message, EmbassyServiceType.REREGISTRATION);
    }
  }

  public void checkPassportAvailableDates() {
    String reRegistrationUrl = buildApiUrl(passportResourceId, passportServiceId);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(reRegistrationUrl, EmbassyServiceType.PASSPORT, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.", EmbassyServiceType.PASSPORT);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.", EmbassyServiceType.PASSPORT, availablePeriods.size());
      String urlForBooking = buildBookingUrl(passportServiceId);
      String message = """
        There are available dates for service: %s. Link for booking: %s
        """.formatted(EmbassyServiceType.PASSPORT, urlForBooking);
      telegramBotManager.notifySubscribers(message, EmbassyServiceType.PASSPORT);
    }
  }

  public String buildApiUrl(String resourceId, String serviceId) {
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime to = from.plusMonths(checkingPeriodInMonth);
    String filterFrom = buildFilterFrom(from);
    String filterTo = buildFilterTo(to);
    String apiPath = buildApiPath();
    String filterResourceId = buildResourceId(resourceId);
    String filterServiceId = buildServiceId(serviceId);
    String filterIgnoreBooking = buildIgnoreBooking();

    StringBuilder builder = new StringBuilder();
    return builder.append(baseUrl)
      .append(apiPath)
      .append(filterFrom)
      .append(filterResourceId)
      .append(filterServiceId)
      .append(filterTo)
      .append(filterIgnoreBooking)
      .toString();
  }

  private String buildBookingUrl(String serviceId) {
    String bookingPrefixPath = "/booking?step=1&";
    StringBuilder builder = new StringBuilder();
    return builder.append(baseUrl)
      .append(bookingPrefixPath)
      .append("serviceId=")
      .append(serviceId)
      .toString();
  }

  private String buildApiPath() {
    return "/api/v2/businesses/" + embassyApiId + "/availability/booking-days";
  }

  private String buildFilterFrom(LocalDateTime from) {
    return "?filter[from]=" + from.format(formatter);
  }

  private String buildFilterTo(LocalDateTime to) {
    return "&filter[to]=" + to.format(formatter);
  }

  private String buildServiceId(String serviceId) {
    return "&filter[serviceId]=" + serviceId;
  }

  private String buildResourceId(String resourceId) {
    return "&filter[resourceId]=" + resourceId;
  }

  private String buildIgnoreBooking() {
    return "&ignoreBookingBoundaries=" + ignoreBooking;
  }

  private List<BookingData> filterAvailablePeriods(ResponseEntity<BookingResponse> response) {
    List<BookingData> periods = response.getBody().getBookingData();
    return periods.stream()
      .filter(day -> day.getAttributes().getIsAvailable())
      .toList();
  }
}
