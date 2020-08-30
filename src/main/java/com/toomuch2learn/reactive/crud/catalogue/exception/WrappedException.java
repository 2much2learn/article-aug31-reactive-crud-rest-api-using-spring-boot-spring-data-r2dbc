package com.toomuch2learn.reactive.crud.catalogue.exception;

public class WrappedException extends RuntimeException{

    public WrappedException(Throwable e) {
        super(e);
    }
}
