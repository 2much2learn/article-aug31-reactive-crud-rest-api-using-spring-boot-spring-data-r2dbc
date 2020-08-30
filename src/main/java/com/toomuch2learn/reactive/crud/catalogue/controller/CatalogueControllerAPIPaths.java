package com.toomuch2learn.reactive.crud.catalogue.controller;

/**
 * Class to hold API Paths used across the controller classes
 *
 * @author Madan Narra
 */
public class CatalogueControllerAPIPaths {

    public static final String BASE_PATH = "/api/v1";

    public static final String CREATE = "/";
    public static final String GET_ITEMS = "/";
    public static final String GET_ITEMS_STREAM = "/stream";
    public static final String GET_ITEM = "/{sku}";
    public static final String UPDATE = "/{sku}";
    public static final String DELETE = "/{sku}";
    public static final String UPLOAD_IMAGE = "/{sku}/image";

    public static final String GET_ITEMS_SSE_EVENTS = "/sse/events";

    public static final String GET_ITEMS_WS_EVENTS = BASE_PATH+"/ws/events";
}
