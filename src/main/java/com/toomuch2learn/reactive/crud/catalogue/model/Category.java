package com.toomuch2learn.reactive.crud.catalogue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Category {

    BOOKS("Books"),
    MUSIC("Music"),
    MOVIES("Movies"),
    GAMES("Games"),
    ELECTRONICS("Electronics"),
    COMPUTERS("Computers"),
    OFFICE("Office");

    @Getter private String value;
}
