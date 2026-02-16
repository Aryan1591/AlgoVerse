// src/main/java/com/algoverse/monolith/oauth/jackson/RegisteredClientMixin.java
package com.algoverse.platform.security_config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class RegisteredClientMixin {}
