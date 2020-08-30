package com.toomuch2learn.reactive.crud.catalogue.controller;

import com.toomuch2learn.reactive.crud.catalogue.CatalogueItemGenerator;
import com.toomuch2learn.reactive.crud.catalogue.SpringReactiveCrudCatalogueApplication;
import com.toomuch2learn.reactive.crud.catalogue.exception.FileStorageException;
import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;
import com.toomuch2learn.reactive.crud.catalogue.service.CatalogueCrudService;
import com.toomuch2learn.reactive.crud.catalogue.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.toomuch2learn.reactive.crud.catalogue.controller.CatalogueControllerAPIPaths.*;

@Slf4j
@SpringBootTest(
    classes = SpringReactiveCrudCatalogueApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CatalogueControllerTest {

    private static WebTestClient client;
    private static CatalogueItem catalogueItem = CatalogueItemGenerator.generateCatalogueItem();

    @LocalServerPort
    int port;

    @MockBean
    private FileStorageService fileStorageService;

    @Autowired
    private CatalogueCrudService catalogueCrudService;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        this.client
            = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl(BASE_PATH)
                .build();
    }

    @Test
    @Order(10)
    public void testGetAllCatalogueItems() {

        this.client
            .get()
            .uri(GET_ITEMS)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[0].id").isNotEmpty()
            .jsonPath("$.[0].sku").isNotEmpty()
            .jsonPath("$.[0].name").isNotEmpty()
            .jsonPath("$.[0].description").isNotEmpty();
    }

    @Test
    @Order(20)
    public void testGetCatalogueItem() throws Exception {

        createCatalogueItem();

        this.client
            .get()
            .uri(replaceSKU(GET_ITEM))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.sku").isNotEmpty()
            .jsonPath("$.name").isNotEmpty()
            .jsonPath("$.description").isNotEmpty();
    }

    @Test
    @Order(30)
    public void testGetCatalogueItemsStream() throws Exception {

        FluxExchangeResult<CatalogueItem> result
            = this.client
                .get()
                .uri(GET_ITEMS_STREAM)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CatalogueItem.class);

        Flux<CatalogueItem> events = result.getResponseBody();
        StepVerifier
            .create(events)
            .expectNextMatches(catalogueItem -> catalogueItem.getId() == 1l)
            .expectNextMatches(catalogueItem -> catalogueItem.getId() == 2l)
            .expectNextMatches(catalogueItem -> catalogueItem.getId() == 3l)
            .thenCancel()
            .verify();
    }

    @Test
    @Order(40)
    public void testCreateCatalogueItem() {
        CatalogueItem item = CatalogueItemGenerator.generateCatalogueItem();
        item.setId(null);

        this.client
            .post()
            .uri(CREATE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(item), CatalogueItem.class)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(40)
    public void testUpdateCatalogueItem() throws Exception {
        createCatalogueItem();

        this.client
            .put()
            .uri(replaceSKU(UPDATE))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(CatalogueItemGenerator.generateCatalogueItem()), CatalogueItem.class)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @Order(50)
    public void testDeleteCatalogueItem() throws Exception {
        createCatalogueItem();

        this.client
            .delete()
            .uri(replaceSKU(DELETE))
            .exchange()
            .expectStatus().isNoContent();
    }

    /**
     * Test method to validate create catalogue item if Invalid Category is passed in request
     */
    @Test
    @Order(60)
    public void testCreateCatalogueItemWithInvalidCategory() {

        CatalogueItem catalogueItem = CatalogueItemGenerator.generateCatalogueItem();
        catalogueItem.setCategory("INVALID");

        this.client
            .post()
            .uri(CREATE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(catalogueItem), CatalogueItem.class)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Test method to validate Resource not found exception
     */
    @Test
    @Order(70)
    public void testResourceNotFoundException() throws Exception{

        this.client
            .get()
            .uri(GET_ITEM.replaceAll("\\{sku\\}", "INVALID"))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Test CatalogueItem Image file upload
     * @throws Exception
     */
    @Test
    @Order(80)
    public void testCatalogueItemImageUpload() throws Exception {

        when(fileStorageService.storeFile(any())).thenReturn(Mono.just("FILE_NAME"));

        createCatalogueItem();

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder
            .part("file", new ClassPathResource("application.yml"));

        this.client
            .post()
            .uri(replaceSKU(UPLOAD_IMAGE))
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus().isCreated();
    }

    private void createCatalogueItem() {
        CatalogueItem item = CatalogueItemGenerator.generateCatalogueItem();
        item.setId(null);

        this.client
            .post()
            .uri(CREATE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(item), CatalogueItem.class)
            .exchange()
            .expectStatus().isCreated();
    }

    private String replaceSKU(String path) {
        return path.replaceAll("\\{sku\\}", catalogueItem.getSku());
    }
}
