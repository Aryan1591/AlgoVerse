package com.algoverse.platform.config;

import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // ---- PROD (Upstash) ----
    @Value("${spring.data.redis.url:}")
    private String redisUrl;

    // ---- DEV (Local) ----
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSslEnabled;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        // ✅ PROD: Use Upstash REDIS_URL (includes TLS + auth)
        if (redisUrl != null && !redisUrl.isBlank()) {
            RedisURI uri = RedisURI.create(redisUrl);

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
                    uri.getHost(),
                    uri.getPort());

            // Note: getPassword() is deprecated but still functional
            // Use it for simplicity until Lettuce provides a cleaner alternative
            @SuppressWarnings("deprecation")
            char[] password = uri.getPassword();
            if (password != null && password.length > 0) {
                config.setPassword(new String(password));
            }

            config.setDatabase(uri.getDatabase());

            LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder = LettuceClientConfiguration
                    .builder()
                    .commandTimeout(Duration.ofSeconds(5));

            if (uri.isSsl()) {
                clientConfigBuilder.useSsl();
            }

            return new LettuceConnectionFactory(config, clientConfigBuilder.build());
        }

        // ✅ DEV: Local Redis
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);

        config.setDatabase(redisDatabase);

        if (redisPassword != null && !redisPassword.isBlank()) {
            config.setPassword(redisPassword);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder = LettuceClientConfiguration
                .builder()
                .commandTimeout(Duration.ofSeconds(5));

        if (redisSslEnabled) {
            clientConfigBuilder.useSsl();
        }

        LettuceClientConfiguration clientConfig = clientConfigBuilder.build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * RedisTemplate for Object values (JSON)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisTemplate for String values
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
