package com.toomuch2learn.reactive.crud.catalogue.service;

import com.toomuch2learn.reactive.crud.catalogue.exception.ResourceNotFoundException;
import com.toomuch2learn.reactive.crud.catalogue.event.CatalogueItemEvent;
import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;
import com.toomuch2learn.reactive.crud.catalogue.repository.CatalogueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Service class to handle Catalogue Item CRUD Operations. Upon Creating/Updating CatalogueItem, CatalogueItemEvent will
 * be published to applicationEventPublisher
 *
 * @author Madan Narra
 */
@Service
@Slf4j
public class CatalogueCrudService {

    private final ApplicationEventPublisher publisher;
    private final CatalogueRepository catalogueRepository;

    CatalogueCrudService(ApplicationEventPublisher publisher, CatalogueRepository catalogueRepository) {
        this.publisher = publisher;
        this.catalogueRepository = catalogueRepository;
    }

    public Flux<CatalogueItem> getCatalogueItems() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        return catalogueRepository.findAll(sort);
    }

    public Mono<CatalogueItem> getCatalogueItem( String skuNumber) throws ResourceNotFoundException {
        return getCatalogueItemBySku(skuNumber);
    }

    public Mono<Long> addCatalogItem(CatalogueItem catalogueItem) {
        catalogueItem.setCreatedOn(Instant.now());

        return
                catalogueRepository
                        .save(catalogueItem)
                        .doOnSuccess(item -> publishCatalogueItemEvent(CatalogueItemEvent.CATALOGUEITEM_CREATED, item))
                        .flatMap(item -> Mono.just(item.getId()));
    }

    public void updateCatalogueItem(CatalogueItem catalogueItem) throws ResourceNotFoundException{

        Mono<CatalogueItem> catalogueItemfromDB = getCatalogueItemBySku(catalogueItem.getSku());

        catalogueItemfromDB.subscribe(
                value -> {
                    value.setName(catalogueItem.getName());
                    value.setDescription(catalogueItem.getDescription());
                    value.setPrice(catalogueItem.getPrice());
                    value.setInventory(catalogueItem.getInventory());
                    value.setUpdatedOn(Instant.now());

                    catalogueRepository
                            .save(value)
                            .doOnSuccess(item -> publishCatalogueItemEvent(CatalogueItemEvent.CATALOGUEITEM_UPDATED, item))
                            .subscribe();
                });
    }

    public void deleteCatalogueItem(CatalogueItem catalogueItem) {

        // For delete to work as expected, we need to subscribe() for the flow to complete
        catalogueRepository.delete(catalogueItem).subscribe();
    }

    private Mono<CatalogueItem> getCatalogueItemBySku(String skuNumber) throws ResourceNotFoundException {
        return catalogueRepository.findBySku(skuNumber)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ResourceNotFoundException(
                        String.format("Catalogue Item not found for the provided SKU :: %s" , skuNumber)))));
    }

    private final void publishCatalogueItemEvent(String eventType, CatalogueItem item) {
        this.publisher.publishEvent(new CatalogueItemEvent(eventType, item));
    }
}