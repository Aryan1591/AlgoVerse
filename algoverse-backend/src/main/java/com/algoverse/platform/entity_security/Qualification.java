package com.algoverse.platform.entity_security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Qualification {
  HIGH_SCHOOL,
  BACHELORS,
  MASTERS,
  DOCTORATE;

  @JsonCreator
  public static Qualification from(String value) {
    if (value == null) return null;
    String v = value.trim().toUpperCase();

    // normalize common inputs
    v = v.replace(" ", "_");
    v = v.replace("-", "_");

    return Qualification.valueOf(v);
  }

  @JsonValue
  public String toJson() {
    return name();
  }
}
