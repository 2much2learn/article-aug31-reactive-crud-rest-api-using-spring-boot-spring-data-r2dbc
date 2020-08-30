package com.toomuch2learn.reactive.crud.catalogue.event;

import com.toomuch2learn.reactive.crud.catalogue.model.CatalogueItem;
import org.springframework.context.ApplicationEvent;

/**
 * Event thrown when CatalogueItem is created or updated
 */
public class CatalogueItemEvent extends ApplicationEvent {

    public static final String CATALOGUEITEM_CREATED = "CREATED";
    public static final String CATALOGUEITEM_UPDATED = "UPDATED";

    private String eventType;

    public CatalogueItemEvent(String eventType, CatalogueItem catalogueItem) {
        super(catalogueItem);
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }
}
