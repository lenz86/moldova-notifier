package com.lenz.moldovanotifier.manager;

import com.lenz.moldovanotifier.model.EmbassyServiceType;
import com.lenz.moldovanotifier.model.booking.BookingData;
import com.lenz.moldovanotifier.model.booking.BookingResponse;
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
public class EmbassyBookingService {

  private @Autowired ReservioIntegrationManager integrationManager;

  public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private @Value("${Embassy.BaseUrl}") String BASE_URL;
  private @Value("${Embassy.CheckingPeriodInMonth}") int                     CHECKING_PERIOD_IN_MONTH;
  private @Value("${Embassy.IgnoreBooking}") byte                    IGNORE_BOOKING;

  private @Value("${Embassy.EmbassyApiId}")             String EMBASSY_API_ID;
  private @Value("${Embassy.CitizenshipResourceId}")    String CITIZENSHIP_RESOURCE_ID;
  private @Value("${Embassy.CitizenshipServiceId}")     String CITIZENSHIP_SERVICE_ID;
  private @Value("${Embassy.NotaryResourceId}")         String NOTARY_RESOURCE_ID;
  private @Value("${Embassy.NotaryServiceId}")          String NOTARY_SERVICE_ID;
  private @Value("${Embassy.ReregistrationResourceId}") String REREGISTRATION_RESOURCE_ID;
  private @Value("${Embassy.ReregistrationServiceId}")  String REREGISTRATION_SERVICE_ID;
  private @Value("${Embassy.PassportResourceId}")       String PASSPORT_RESOURCE_ID;
  private @Value("${Embassy.PassportServiceId}")        String PASSPORT_SERVICE_ID;



  public void checkAllServices() {
    checkReRegistrationAvailableDates();
    checkCitizenshipAvailableDates();
    checkNotaryAvailableDates();
    checkPassportAvailableDates();
  }

  public void checkCitizenshipAvailableDates() {
    String citizenshipUrl = buildUrl(CITIZENSHIP_RESOURCE_ID, CITIZENSHIP_SERVICE_ID);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(citizenshipUrl, EmbassyServiceType.CITIZENSHIP, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.",
        EmbassyServiceType.CITIZENSHIP);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.",
        EmbassyServiceType.CITIZENSHIP, availablePeriods.size());
    }
  }

  public void checkNotaryAvailableDates() {
    String notaryUrl = buildUrl(NOTARY_RESOURCE_ID, NOTARY_SERVICE_ID);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(notaryUrl, EmbassyServiceType.NOTARY, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.}",
        EmbassyServiceType.NOTARY);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.",
        EmbassyServiceType.NOTARY, availablePeriods.size());
    }
  }

  public void checkReRegistrationAvailableDates() {
    String reRegistrationUrl = buildUrl(REREGISTRATION_RESOURCE_ID, REREGISTRATION_SERVICE_ID);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(reRegistrationUrl, EmbassyServiceType.REREGISTRATION, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.",
        EmbassyServiceType.REREGISTRATION);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.",
        EmbassyServiceType.REREGISTRATION, availablePeriods.size());
    }
  }

  public void checkPassportAvailableDates() {
    String reRegistrationUrl = buildUrl(PASSPORT_RESOURCE_ID, PASSPORT_SERVICE_ID);
    ResponseEntity<BookingResponse> response = integrationManager.callApi(reRegistrationUrl, EmbassyServiceType.PASSPORT, BookingResponse.class);
    List<BookingData> availablePeriods = filterAvailablePeriods(response);
    if (availablePeriods.isEmpty()) {
      log.info("There are no available periods for service: {}.",
        EmbassyServiceType.PASSPORT);
    } else {
      log.warn("There are available booking periods for service: {}. Total available: {}.",
        EmbassyServiceType.PASSPORT, availablePeriods.size());
    }
  }

  private String buildUrl(String resourceId, String serviceId) {
    LocalDateTime from = LocalDateTime.now();
    LocalDateTime to = from.plusMonths(CHECKING_PERIOD_IN_MONTH);
    String filterFrom = buildFilterFrom(from);
    String filterTo = buildFilterTo(to);
    String apiPath = buildApiPath();
    String filterResourceId = buildResourceId(resourceId);
    String filterServiceId = buildServiceId(serviceId);
    String filterIgnoreBooking = buildIgnoreBooking();

    StringBuilder builder = new StringBuilder();
    return builder.append(BASE_URL)
      .append(apiPath)
      .append(filterFrom)
      .append(filterResourceId)
      .append(filterServiceId)
      .append(filterTo)
      .append(filterIgnoreBooking)
      .toString();
  }

  private String buildApiPath() {
    return "/api/v2/businesses/" + EMBASSY_API_ID + "/availability/booking-days";
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
    return "&ignoreBookingBoundaries=" + IGNORE_BOOKING;
  }

  private List<BookingData> filterAvailablePeriods(ResponseEntity<BookingResponse> response) {
    List<BookingData> periods = response.getBody().getBookingData();
    return periods.stream()
      .filter(day -> day.getAttributes().getIsAvailable())
      .toList();
  }
}
