package com.javahelps.wisdom.core.pattern;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Attribute;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.processor.Stateful;
import com.javahelps.wisdom.core.processor.StreamProcessor;
import com.javahelps.wisdom.core.util.FunctionalUtility;
import com.javahelps.wisdom.core.time.TimestampGenerator;
import com.javahelps.wisdom.core.util.WisdomConstants;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The most basic component of a Wisdom pattern. A pattern must have a name and optionally a filter.
 */
public class Pattern extends StreamProcessor implements Stateful {

    /**
     * User defined unique name to refer this pattern.
     */
    protected String name;
    protected Duration duration;
    protected List<String> streamIds = new ArrayList<>();
    protected Predicate<Event> predicate = FunctionalUtility.truePredicator();
    protected EventDistributor eventDistributor = new EventDistributor();
    protected Lock lock = new ReentrantLock();
    private boolean consumed = false;
    private boolean accepting = true;
    private TimestampGenerator timestampGenerator;
    private boolean batchPattern = false;
    private List<Event> events = new ArrayList<>();
    private Map<Event, Event> eventMap = new HashMap<>();
    private Consumer<Event> postProcess = FunctionalUtility.silentConsumer();
    private Consumer<Event> preProcess = FunctionalUtility.silentConsumer();

    private Predicate<Event> emitConditionMet = event -> consumed;
    private Predicate<Event> processConditionMet = event -> accepting;
    private BiFunction<Event, Event, Boolean> expiredCondition = (currentEvent, oldEvent) -> false;
    private Supplier<List<Event>> previousEvents = () -> {
        ArrayList<Event> arrayList = new ArrayList<>();
        arrayList.add(new Event(timestampGenerator.currentTimestamp()));
        return arrayList;
    };
    private CopyEventAttributes copyEventAttributes = (pattern, src, destination) -> {
        for (Map.Entry<String, Object> entry : src.getData().entrySet()) {
            destination.set(this.name + "." + entry.getKey(), entry.getValue());
        }
    };


    public Pattern(String patternId) {
        super(patternId);
    }

    public Pattern(String patternId, String name, String streamId) {
        this(patternId);
        this.name = name;
        this.streamIds.add(streamId);
    }

    public static Pattern pattern(String patternId, String name, String streamId) {
        Pattern pattern = new Pattern(patternId, name, streamId);
        return pattern;
    }

    public static Pattern followedBy(Pattern first, Pattern following) {

        return new FollowingPattern(first.id + WisdomConstants.PATTERN_FOLLOWED_BY_INFIX + following.id, first,
                following);
    }

    public static Pattern followedBy(Pattern first, Pattern following, long withinTimestamp) {

        FollowingPattern followingPattern = new FollowingPattern(first.id + WisdomConstants.PATTERN_FOLLOWED_BY_INFIX + following.id, first,
                following);
        followingPattern.setWithin(withinTimestamp);
        return followingPattern;
    }

    public static Pattern and(Pattern first, Pattern second) {

        LogicalPattern logicalPattern = new LogicalPattern(first.id + WisdomConstants.PATTERN_AND_INFIX + second.id,
                LogicalPattern.Type.AND, first, second);
        return logicalPattern;
    }

    public static Pattern or(Pattern first, Pattern second) {

        LogicalPattern logicalPattern = new LogicalPattern(first.id + WisdomConstants.PATTERN_OR_INFIX + second.id,
                LogicalPattern.Type.OR, first, second);
        return logicalPattern;
    }

    public static Pattern not(Pattern pattern) {

        NotPattern notPattern = new NotPattern(WisdomConstants.PATTERN_NOT_PREFIX + pattern.id, pattern);
        return notPattern;
    }

    public static Pattern every(Pattern pattern) {

        EveryPattern everyPattern = new EveryPattern(WisdomConstants.PATTERN_EVERY_PREFIX + pattern.id, pattern);
        return everyPattern;
    }

    public void init(WisdomApp wisdomApp) {

        this.timestampGenerator = wisdomApp.getContext().getTimestampGenerator();
        this.streamIds.forEach(streamId -> wisdomApp.getStream(streamId).addProcessor(this));
    }

    public void setPostProcess(Consumer<Event> postProcess) {
        this.postProcess = postProcess;
    }

    public void setPreProcess(Consumer<Event> preProcess) {
        this.preProcess = preProcess;
    }

    public boolean isBatchPattern() {
        return batchPattern;
    }

    public void setBatchPattern(boolean batchPattern) {
        this.batchPattern = batchPattern;
    }

    public boolean isComplete() {
        return !this.events.isEmpty();
    }

    public Map<Event, Event> getEventMap() {
        return eventMap;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public void setEmitConditionMet(Predicate<Event> emitConditionMet) {
        this.emitConditionMet = emitConditionMet;
    }

    public Predicate<Event> getProcessConditionMet() {
        return processConditionMet;
    }

    public void setProcessConditionMet(Predicate<Event> processConditionMet) {
        this.processConditionMet = processConditionMet;
    }

    public void setCopyEventAttributes(CopyEventAttributes copyEventAttributes) {
        this.copyEventAttributes = copyEventAttributes;
    }

    public Pattern filter(Predicate<Event> predicate) {
        this.predicate = predicate;
        return this;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public Pattern times(int minCount, int maxCount) {

        CountPattern countPattern = new CountPattern(this.id, this, minCount, maxCount);
        return countPattern;
    }

    public Pattern times(int count) {

        return this.times(count, count);
    }

    public Pattern maxTimes(int maxCount) {

        return this.times(0, maxCount);
    }

    public Pattern minTimes(int minCount) {

        return this.times(minCount, Integer.MAX_VALUE);
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public Supplier<List<Event>> getPreviousEvents() {
        return previousEvents;
    }

    public void setPreviousEvents(Supplier<List<Event>> previousEvents) {
        this.previousEvents = previousEvents;
    }

    public Event event() {
        return this.event(0);
    }

    public Attribute attribute(String attribute) {
        return new Attribute(this::event, attribute);
    }

    public Event last() {
        return this.events.get(this.events.size() - 1);
    }

    public void setExpiredCondition(BiFunction<Event, Event, Boolean> expiredCondition) {
        this.expiredCondition = expiredCondition;
    }

    public Event event(int index) {
        Event event = null;
        if (index < this.events.size()) {
            event = this.events.get(index);
        }
        return event;
    }

    public void reset() {
        this.events.clear();
        this.eventMap.clear();
        this.accepting = true;
        this.consumed = false;
    }

    @Override
    public void start() {

    }

    @Override
    public void process(Event event) {

        try {
            this.lock.lock();

            consumed = false;
            if (this.processConditionMet.test(event) && this.predicate.test(event)) {

                this.preProcess.accept(event);

                Iterator<Event> events = this.previousEvents.get().iterator();
                Event newEvent = null;
                while (events.hasNext()) {

                    Event preEvent = events.next();

                    // Remove the expired events
                    if (this.expiredCondition.apply(event, preEvent)) {
                        events.remove();
                        continue;
                    }

                    newEvent = new Event(event.getStream(), event.getTimestamp());
                    newEvent.setOriginal(event);
                    newEvent.setName(this.name);
                    this.copyEventAttributes.copy(this, event, newEvent);

                    newEvent.getData().putAll(preEvent.getData());

                    this.events.add(newEvent);
                    this.eventMap.put(event.getOriginal(), newEvent);
                }

                if (newEvent != null) {

                    this.accepting = false;
                    this.consumed = true;
                    this.postProcess.accept(newEvent);

                    if (this.emitConditionMet.test(newEvent)) {

                        if (batchPattern) {
                            List<Event> eventsToEmit = new ArrayList<>();
                            eventsToEmit.addAll(this.events);
                            this.reset();
                            this.getNextProcessor().process(eventsToEmit);
                        } else {
                            for (Event e : this.events) {
                                if (e != newEvent) {
                                    newEvent.getData().putAll(e.getData());
                                }
                            }
                            this.reset();
                            this.getNextProcessor().process(newEvent);
                        }
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void process(List<Event> events) {

    }

    public void onPreviousPostProcess(Event event) {

    }

    public void onPreviousPreProcess(Event event) {

    }

    public void onNextPostProcess(Event event) {
        this.reset();
    }

    public void onNextPreProcess(Event event) {

    }

    public Pattern within(Duration duration) {

        this.duration = duration;
        return this;
    }

    @Override
    public Pattern copy() {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void clear() {
        try {
            this.lock.lock();
            this.events.clear();
            this.eventMap.clear();
        } finally {
            this.lock.unlock();
        }
    }

    @FunctionalInterface
    protected interface CopyEventAttributes {
        void copy(Pattern pattern, Event src, Event destination);
    }
}
