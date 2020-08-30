package com.toomuch2learn.reactive.crud.catalogue.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toomuch2learn.reactive.crud.catalogue.event.CatalogueItemEvent;
import com.toomuch2learn.reactive.crud.catalogue.event.CatalogueItemEventPublisher;
import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to initialize WebSocketHandler class to establish websocket connection and publish messages when Catalogue Item
 * is added or updated.
 *
 * @author Madan Narra
 */
@Slf4j
@Configuration
public class CatalogueWSController {

    @Bean
    HandlerMapping handlerMapping(WebSocketHandler wsh) {
        return new SimpleUrlHandlerMapping() {{
            setUrlMap(Collections.singletonMap(CatalogueControllerAPIPaths.GET_ITEMS_WS_EVENTS, wsh));
            setOrder(10);
        }};
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler(CatalogueItemEventPublisher eventPublisher, ObjectMapper objectMapper) {

        Flux<CatalogueItemEvent> publish = Flux.create(eventPublisher).share();

        // Push events that are captured when catalogue item is added or updated
        return session -> {
            Flux<WebSocketMessage> messageFlux = publish.map(evt -> {
                try {

                    // Get source from event and set the type of event in map when pushing the message
                    CatalogueItem item = (CatalogueItem) evt.getSource();
                    Map<String, CatalogueItem> data = new HashMap<>();
                    data.put(evt.getEventType(), item);

                    return objectMapper.writeValueAsString(data);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).map(str -> {
                log.debug("Publishing message to Websocket :: " + str);
                return session.textMessage(str);
            });

            return session.send(messageFlux);
        };
    }
}
