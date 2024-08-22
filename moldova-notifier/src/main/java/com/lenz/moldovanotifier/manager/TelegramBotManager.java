package com.lenz.moldovanotifier.manager;

import com.lenz.moldovanotifier.config.TelegramBotConfig;
import com.lenz.moldovanotifier.model.EmbassyServiceType;
import com.lenz.moldovanotifier.model.bot.BotCommandType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TelegramBotManager extends TelegramLongPollingBot {

  private final TelegramBotConfig botConfig;
  private final ConcurrentHashMap<Long, Set<EmbassyServiceType>> subscribers; // chatId -> command


  @Autowired
  public TelegramBotManager(TelegramBotConfig botConfig) {
    this.botConfig = botConfig;
    this.subscribers = new ConcurrentHashMap<>();
  }

  private void handleCommand(String message, Update update) {
    long chatId = update.getMessage().getChatId();
    BotCommandType command = BotCommandType.fromMessage(message);

    if (command == null) {
      log.info("Message: {} is not a command", message);
      return;
    }

    switch (command) {
      case START -> start(chatId);
      case RE_REGISTRATION -> subscribeToService(chatId, EmbassyServiceType.REREGISTRATION);
      case PASSPORT -> subscribeToService(chatId, EmbassyServiceType.PASSPORT);
      case NOTARY -> subscribeToService(chatId, EmbassyServiceType.NOTARY);
      case CITIZENSHIP -> subscribeToService(chatId, EmbassyServiceType.CITIZENSHIP);
      case UNSUBSCRIBE_ALL -> unsubscribeAllForChat(chatId);
      default -> System.out.println("Command is not supported");
    }
  }

  private void start(long chatId) {
    StringBuilder text = new StringBuilder();
    text.append("Available commands:\n");
    for (BotCommandType command : BotCommandType.values()) {
      text.append(command.getMessage());
      text.append("\n");
    }

    SendMessage message = createMessage(chatId, text.toString());
    sendMessage(message);
  }

  private void subscribeToService(long chatId, EmbassyServiceType service) {
    log.info("User with chatId: {} is subscribed on service: {}", chatId, service);
    updateSubscribers(chatId, service);
    SendMessage message = createMessage(chatId, "Successfully subscribed to " + service);
    sendMessage(message);
  }

  private void unsubscribeAllForChat(long chatId) {
    log.info("User with chatId: {} is unsubscribed for all services", chatId);
    subscribers.remove(chatId);
    SendMessage message = createMessage(chatId, "Successfully unsubscribed from all services");
    sendMessage(message);
  }

  private void updateSubscribers(long chatId, EmbassyServiceType service) {
    Set<EmbassyServiceType> services = subscribers.get(chatId);
    if (services != null) {
      services.add(service);
    } else {
      services = new HashSet<>();
      services.add(service);
      subscribers.put(chatId, services);
    }
  }

  private SendMessage createMessage(long chatId, String message) {
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(String.valueOf(chatId));
    sendMessage.setText(message);
    return sendMessage;
  }

  @Override
  public void onUpdateReceived(Update update) {
    boolean messageReceived = update.hasMessage() && update.getMessage().hasText();
    if (messageReceived) {
      String message = update.getMessage().getText();
      handleCommand(message, update);
    }
  }

  public void sendMessage(SendMessage message) {
    try {
      execute(message);
    } catch (Exception ex) {
      log.error("Error when sending a message to bot chat with ID: {}. Message: {}. Error: {}", message.getChatId(), message.getText(), ex.getMessage());
      throw new RuntimeException(ex);
    }
  }

  public void notifySubscribers(String message, EmbassyServiceType service) {
    subscribers.entrySet().stream()
      .filter(subscribers -> subscribers.getValue().contains(service))
      .forEach(subscribers -> sendMessage(createMessage(subscribers.getKey(), message)));
  }

  @Override
  public String getBotUsername() {
    return botConfig.getBotName();
  }

  @Override
  public String getBotToken() {
    return botConfig.getToken();
  }
}
