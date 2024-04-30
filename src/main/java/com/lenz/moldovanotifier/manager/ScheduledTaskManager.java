package com.lenz.moldovanotifier.manager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class ScheduledTaskManager {

  private @Autowired EmbassyBookingService bookingService;

  private ScheduledThreadPoolExecutor executor;
  private final String threadNamePrefix = ScheduledTaskManager.class.getSimpleName();
  private final Object stopSignal = new Object();
  private AtomicBoolean shutdownFlag = new AtomicBoolean();
  private @Value(("${ReservioApi.CallMsDelay}")) Integer CALL_API_DELAY;

  @PostConstruct
  public void init() {
    executor = new ScheduledThreadPoolExecutor(9, r -> {
      Thread thread = new Thread(r);
      thread.setName(threadNamePrefix);
      return thread;
    });
  }

  @PreDestroy
  public void shutdown() {
    try {
      shutdownFlag.set(true);
      synchronized (stopSignal) {
        stopSignal.notifyAll();
      }
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  public void startScheduleCallApiJob() {
    // TODO: 26.04.2024 set api url
    // TODO: 26.04.2024 move delays to constants
    executor.scheduleWithFixedDelay(() -> {
        try {
          bookingService.checkAllServices();
        } catch (Exception ex) {
          log.error("An error has occurred while do scheduled job. Error: {}", ex.toString());
        }
      },
      1, CALL_API_DELAY, TimeUnit.MILLISECONDS);
  }
}
