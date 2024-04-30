package com.lenz.moldovanotifier.model.bot;

public enum BotCommandType {
  START("/start"),
  PASSPORT("/passport"),
  NOTARY("/notary"),
  CITIZENSHIP("/citizenship"),
  RE_REGISTRATION ("/reregistration"),
  UNSUBSCRIBE_ALL ("/unsubscribe_all");

  private final String message;

  BotCommandType(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public static BotCommandType fromMessage(String message) {
    for (BotCommandType type : BotCommandType.values()) {
      if (type.getMessage().equalsIgnoreCase(message)) {
        return type;
      }
    }
    return null;
  }
}
