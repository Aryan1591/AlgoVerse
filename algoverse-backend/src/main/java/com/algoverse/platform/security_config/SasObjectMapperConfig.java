// src/main/java/com/algoverse/monolith/config/SasObjectMapperConfig.java
package com.algoverse.platform.security_config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

@Configuration
public class SasObjectMapperConfig {

@Bean("sasObjectMapper")
public ObjectMapper sasObjectMapper() {
  ObjectMapper om = new ObjectMapper();
  om.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
  om.registerModule(new OAuth2AuthorizationServerJackson2Module());
  om.addMixIn(
      RegisteredClient.class,
      RegisteredClientMixin.class
  );
  return om;
}

}
