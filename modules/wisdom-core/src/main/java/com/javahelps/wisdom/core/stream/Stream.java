package com.javahelps.wisdom.core.stream;

import com.javahelps.wisdom.core.ThreadBarrier;
import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.exception.WisdomAppRuntimeException;
import com.javahelps.wisdom.core.exception.WisdomAppValidationException;
import com.javahelps.wisdom.core.processor.Processor;
import com.javahelps.wisdom.core.statistics.StatisticsManager;
import com.javahelps.wisdom.core.statistics.StreamTracker;
import com.javahelps.wisdom.core.stream.async.EventHolder;
import com.javahelps.wisdom.core.time.EventBasedTimestampGenerator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.javahelps.wisdom.core.util.WisdomConfig.EMPTY_PROPERTIES;
import static com.javahelps.wisdom.core.util.WisdomConstants.*;

/**
 * {@link Stream} is the fundamental data-structure of the event processor. At the runtime, it acts attrName the entry point
 * and manager of all the queries starting with this attrName the input stream.
 */
public class Stream implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Stream.class);

    protected String id;
    protected WisdomApp wisdomApp;
    private List<Processor> processorList = new ArrayList<>();
    private Processor[] processors;
    private Disruptor<EventHolder> disruptor;
    private RingBuffer<EventHolder> ringBuffer;
    private boolean disabled = false;
    private StreamTracker tracker;
    private ThreadBarrier threadBarrier;
    private EventBasedTimestampGenerator timestampGenerator;
    private String playbackTimestamp;


    public Stream(WisdomApp wisdomApp, String id) {
        this(wisdomApp, id, EMPTY_PROPERTIES);
    }

    public Stream(WisdomApp wisdomApp, String id, Properties properties) {
        this.id = id;
        this.wisdomApp = wisdomApp;
        this.threadBarrier = wisdomApp.getContext().getThreadBarrier();
        final boolean async = ((Boolean) properties.getOrDefault(ASYNC, wisdomApp.getContext().isAsync()));
        final int bufferSize = ((Number) properties.getOrDefault(BUFFER, wisdomApp.getBufferSize())).intValue();

        // Create disruptor if async mode is enables
        if (async) {
            this.disruptor = new Disruptor<>(EventHolder::new, bufferSize,
                    wisdomApp.getContext().getThreadFactory(),
                    ProducerType.MULTI, new YieldingWaitStrategy());

            // Connect the handler
            disruptor.handleEventsWith((eventHolder, sequence, endOfBatch) -> this.sendToProcessors(eventHolder.get()));

            // Get the ring buffer from the Disruptor to be used for publishing.
            this.ringBuffer = disruptor.getRingBuffer();
        }
        if ((Boolean) properties.getOrDefault(STATISTICS, false)) {
            StatisticsManager statisticsManager = wisdomApp.getStatisticsManager();
            if (statisticsManager == null) {
                throw new WisdomAppValidationException("App level statistic stream must be defined before enabling stream stats");
            }
            this.setTracker(statisticsManager.createStreamTracker(id));
        }
        if (wisdomApp.getContext().isPlaybackEnabled()) {
            this.timestampGenerator = (EventBasedTimestampGenerator) wisdomApp.getContext().getTimestampGenerator();
            this.playbackTimestamp = wisdomApp.getContext().getPlaybackAttribute();
        }
    }

    @Override
    public void start() {
        this.processors = this.processorList.toArray(new Processor[0]);

        if (this.disruptor != null) {
            // Start the Disruptor, starts all threads running
            disruptor.start();
        }
    }

    @Override
    public void stop() {
        if (this.disruptor != null) {
            // Start the Disruptor, starts all threads running
            disruptor.shutdown();
        }
    }

    @Override
    public void process(Event event) {

        if (this.disabled) {
            return;
        }
        if (this.tracker != null) {
            this.tracker.inEvent();
        }
        if (this.disruptor == null) {
            this.sendToProcessors(event);
        } else {
            // Async enabled
            this.ringBuffer.publishEvent((eventHolder, sequence, buffer) -> eventHolder.set(event));
        }
    }

    @Override
    public void process(List<Event> events) {

        if (this.disabled) {
            return;
        }
        if (this.tracker != null) {
            this.tracker.inEvent(events.size());
        }
        if (this.disruptor == null) {
            if (this.timestampGenerator != null) {
                events.stream().map(event -> event.get(this.playbackTimestamp))
                        .filter(x -> x instanceof Long)
                        .mapToLong(x -> ((Long) x).longValue())
                        .forEach(this.timestampGenerator::setCurrentTimestamp);
            }
            for (Processor processor : this.processors) {
                List<Event> newEvents = this.convertEvent(events);
                try {
                    this.threadBarrier.pass();
                    processor.process(newEvents);
                } catch (WisdomAppRuntimeException ex) {
                    this.wisdomApp.handleException(ex);
                }
            }
        } else {
            // Async enabled
            for (Event event : events) {
                this.ringBuffer.publishEvent((eventHolder, sequence, buffer) -> eventHolder.set(event));
            }
        }
    }

    private void sendToProcessors(Event event) {
        if (this.timestampGenerator != null) {
            Object timestamp = event.get(this.playbackTimestamp);
            if (timestamp != null && timestamp instanceof Long) {
                this.timestampGenerator.setCurrentTimestamp(((Long) timestamp).longValue());
            }
        }
        for (Processor processor : this.processors) {
            Event newEvent = this.convertEvent(event);
            try {
                this.threadBarrier.pass();
                processor.process(newEvent);
            } catch (WisdomAppRuntimeException ex) {
                this.wisdomApp.handleException(ex);
            }
        }
    }

    public String getId() {
        return id;
    }

    private Event convertEvent(Event from) {

        Event newEvent = from.copyEvent();
        newEvent.setStream(this);
        return newEvent;
    }

    private List<Event> convertEvent(List<Event> from) {

        List<Event> newEvents = new ArrayList<>(from.size());
        for (Event event : from) {
            newEvents.add(convertEvent(event));
        }
        return newEvents;
    }

    public void addProcessor(Processor processor) {
        if (!this.processorList.contains(processor)) {
            this.processorList.add(processor);
        }
    }

    public void addProcessor(Processor processor, int index) {
        if (!this.processorList.contains(processor)) {
            this.processorList.add(index, processor);
        }
    }

    public void removeProcessor(Processor processor) {
        this.processorList.remove(processor);
    }

    @Override
    public Processor copy() {

        return this;
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    public void enable() {
        this.disabled = false;
    }

    public void disable() {
        this.disabled = true;
    }

    public void setTracker(StreamTracker tracker) {
        this.tracker = tracker;
    }
}
