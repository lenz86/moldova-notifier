package com.lenz.moldovanotifier.manager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ScheduledTaskManager {

  private ScheduledThreadPoolExecutor executor;
  private final String threadNamePrefix = ScheduledTaskManager.class.getSimpleName();
  private final ApiManager apiManager = new ApiManager();
  private final Object stopSignal = new Object();
  private AtomicBoolean shutdownFlag = new AtomicBoolean();

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
    executor.scheduleWithFixedDelay(apiManager::callApi, 1, 10, TimeUnit.SECONDS);
  }

  ;

  public void startScheduleCallApiJob(String[] args) {
    executor.scheduleWithFixedDelay(() -> {
      apiManager.callApi(args);
    }, 1, 10, TimeUnit.SECONDS);
  }

  ;
}
