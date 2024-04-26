package com.lenz.moldovanotifier;

import com.lenz.moldovanotifier.manager.ScheduledTaskManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RunApplication {


  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(RunApplication.class, args);
    ScheduledTaskManager scheduledTaskManager = context.getBean(ScheduledTaskManager.class);
    if (args.length > 0) {
      scheduledTaskManager.startScheduleCallApiJob(args);
    } else {
      scheduledTaskManager.startScheduleCallApiJob();
    }
  }

}
