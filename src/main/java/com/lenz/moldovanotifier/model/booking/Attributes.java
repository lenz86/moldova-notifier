package com.lenz.moldovanotifier.model.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Attributes {

  @JsonProperty("createdAt")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private LocalDateTime createdAt;

  private String date;
  private Boolean isAvailable;
}
