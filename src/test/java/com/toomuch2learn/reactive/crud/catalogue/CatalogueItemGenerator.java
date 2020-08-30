package com.toomuch2learn.reactive.crud.catalogue;

import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class CatalogueItemGenerator {

    private static Instant now = Instant.now();

    public static CatalogueItem generateCatalogueItem() {
        return generateCatalogueItem(1000l);
    }

    /**
     * Generate sample Catalogue Item which will be used in test classes
     *
     * @return catalogueItem
     */
    private static CatalogueItem generateCatalogueItem(Long id) {
        CatalogueItem item = new CatalogueItem();
        item.setId(id);
        item.setSku("SKU-1234");
        item.setName("Item Name");
        item.setDescription("Item Desc");
        item.setCategory("Books");
        item.setInventory(10);
        item.setPrice(100.0);
        item.setCreatedOn(now);

        return item;
    }

    public static List<CatalogueItem> generateCatalogueItemsList() {
        return
            LongStream.range(1, 100).mapToObj(value -> {
                return generateCatalogueItem(value);
            }).collect(Collectors.toList());
    }
}
