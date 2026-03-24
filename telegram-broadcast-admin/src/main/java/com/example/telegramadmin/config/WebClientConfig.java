package com.example.telegramadmin.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

//    @Bean
//    public WebClient telegramWebClient(@Value("${bot.token}") String botToken) {
//        return WebClient.builder()
//                .baseUrl("https://api.telegram.org/bot" + botToken)
//                .build();
//    }
@Bean
public WebClient telegramWebClient(@Value("${bot.token}") String botToken) {
    HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(480)) // ✅ Таймаут ответа (включает SSL handshake)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000000); // ✅ TCP-соединение (1000 сек)

    return WebClient.builder()
            .baseUrl("https://api.telegram.org/bot" + botToken)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
}
}