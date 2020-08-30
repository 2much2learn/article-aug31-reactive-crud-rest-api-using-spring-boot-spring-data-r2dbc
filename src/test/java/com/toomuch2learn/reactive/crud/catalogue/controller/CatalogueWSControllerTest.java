package com.toomuch2learn.reactive.crud.catalogue.controller;

import com.toomuch2learn.reactive.crud.catalogue.CatalogueItemGenerator;
import com.toomuch2learn.reactive.crud.catalogue.SpringReactiveCrudCatalogueApplication;
import com.toomuch2learn.reactive.crud.catalogue.event.CatalogueItemEvent;
import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to validate websocket handler that is registered in the application
 *
 * @author Madan Narra
 */
@Slf4j
@SpringBootTest(
    classes = SpringReactiveCrudCatalogueApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CatalogueWSControllerTest {

    private final WebSocketClient socketClient = new ReactorNettyWebSocketClient();

    private final WebClient webClient = WebClient.builder().build();

    @LocalServerPort
    int port;

    @Test
    public void testCatalogueItemEvent() throws Exception {

        // Prepare sample event which will be published and received
        final CatalogueItemEvent catalogueItemEvent
            = new CatalogueItemEvent(CatalogueItemEvent.CATALOGUEITEM_CREATED, CatalogueItemGenerator.generateCatalogueItem());

        int count = 1;
        AtomicLong counter = new AtomicLong();

        /*
         * Create WebSocket client connecting to handler mapped to the uri endpoint and receive messages sent by application
         *
         * Increment AtomicLong counter to ensure we receive as many messages that were actually published by the application
         */
        URI uri = URI.create("ws://localhost:"+port+ CatalogueControllerAPIPaths.GET_ITEMS_WS_EVENTS);
        socketClient.execute(uri, (WebSocketSession session) -> {
            return session
                .receive()
                .map(WebSocketMessage::getPayloadAsText).log()
                .doOnNext(str -> counter.incrementAndGet())
                .then();
        }).subscribe();

        /*
         * Invoke Create API Endpoint as many times as specified in 'count'. This will create the CatalogueItem and then publish
         * message to websocket which will be received by the client used in this test
         */
        Flux
            .<CatalogueItemEvent>generate(sink -> sink.next(catalogueItemEvent))
            .take(count)
            .flatMap(this::write)
            .blockLast();

        // Sleep for a second to ensure messages published by the application is received by websocket client used here
        Thread.sleep(1000);

        // Validate if we received as many messages that are supposed to be received based on the number of catalogueItems created.
        assertThat(counter.get()).isEqualTo(count);
    }

    /**
     * Method to publish Create API request which will publish events to websocket and received in this test class
     *
     * @param catalogueItemEvent
     * @return CatalogueItemEvent publisher
     */
    private Publisher<CatalogueItemEvent> write(CatalogueItemEvent catalogueItemEvent) {
        CatalogueItem catalogueItem = (CatalogueItem) catalogueItemEvent.getSource();
        catalogueItem.setId(null);

        Mono<String> response =
            this.webClient
                .post()
                .uri("http://localhost:"+port+CatalogueControllerAPIPaths.BASE_PATH+CatalogueControllerAPIPaths.CREATE)
                .body(BodyInserters.fromValue(catalogueItemEvent.getSource()))
                .retrieve()
                .bodyToMono(String.class).log();

        // Validate Create API response is not empty
        assertThat(response.log().block()).isNotEmpty();

        // Return back same catalogueItemEvent
        return Mono.just(catalogueItemEvent);
    }
}