package com.toomuch2learn.reactive.crud.catalogue.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Class to publish CatalogueItemEvent in single executor thread with events added to BlockingQueue when ever happens
 * and be emitted by FluxSink
 *
 * @author Madan Narra
 */
@Component
public class CatalogueItemEventPublisher implements
    ApplicationListener<CatalogueItemEvent>,
    Consumer<FluxSink<CatalogueItemEvent>> {

    private final Executor executor;
    private final BlockingQueue<CatalogueItemEvent> queue;

    CatalogueItemEventPublisher() {
        this.executor = Executors.newSingleThreadExecutor();
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onApplicationEvent(CatalogueItemEvent event) {
        this.queue.offer(event);
    }

    @Override
    public void accept(FluxSink<CatalogueItemEvent> sink) {
        this.executor.execute(() -> {
            while (true) {
                try {
                    CatalogueItemEvent event = queue.take();
                    sink.next(event);
                } catch (InterruptedException e) {
                    ReflectionUtils.rethrowRuntimeException(e);
                }
            }
        });
    }
}
