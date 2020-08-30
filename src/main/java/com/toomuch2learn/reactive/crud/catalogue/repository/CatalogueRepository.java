package com.toomuch2learn.reactive.crud.catalogue.repository;

import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Mono;

public interface CatalogueRepository extends ReactiveSortingRepository<CatalogueItem, Long> {

    Mono<CatalogueItem> findBySku(String sku);
}
