package com.javahelps.wisdom.core.processor;

import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.partition.Partitionable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The abstract representation of all kind of {@link Event} processors.
 */
public interface Processor extends Cloneable {

    void start();

    void process(Event event);

    void process(List<Event> events);

    Object clone();
}
