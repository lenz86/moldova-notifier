package com.lenz.moldovanotifier.model.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lenz.moldovanotifier.model.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class BookingResponse extends BaseResponse {

  private Meta meta;
  private Links links;

  @JsonProperty("data")
  private List<BookingData> bookingData;
}
