package com.javahelps.wisdom.core.pattern;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.processor.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by gobinath on 6/29/17.
 */
class FollowingPattern extends CustomPattern {


    private Pattern first;
    private Pattern next;

    FollowingPattern(String patternId, Pattern first, Pattern next) {
        super(patternId);
        this.first = first;
        this.next = next;
        this.eventDistributor.add(first);
        this.eventDistributor.add(next);

        this.first.setProcessConditionMet(event -> true);
        this.next.setProcessConditionMet(event -> !this.first.isWaiting());

        this.first.setEmitConditionMet(event -> false);
        this.next.setEmitConditionMet(event -> true);

        this.next.setMergePreviousEvents(event -> {
            for(Event e : this.first.getEvents()) {
                event.getData().putAll(e.getData());
            }
        });

        this.first.setPreProcess(event -> this.next.onPreviousPreProcess(event));
        this.first.setPostProcess(event -> this.next.onPreviousPostProcess(event));

        this.next.setPreProcess(event -> this.first.onNextPreProcess(event));
        this.next.setPostProcess(event -> this.first.onNextPostProcess(event));


        // Add th streams to this pattern
        this.streamIds.addAll(this.first.streamIds);
        this.streamIds.addAll(this.next.streamIds);
    }

    @Override
    public void onNextPreProcess(Event event) {
        this.next.onNextPreProcess(event);
    }

    @Override
    public void onNextPostProcess(Event event) {
        this.reset();
    }

    @Override
    public void onPreviousPreProcess(Event event) {
        this.first.onPreviousPreProcess(event);
    }

    @Override
    public void onPreviousPostProcess(Event event) {
        this.first.onPreviousPostProcess(event);
    }

    @Override
    public void reset() {
        this.first.reset();
        this.next.reset();
    }

    @Override
    public void init(WisdomApp wisdomApp) {

        this.first.init(wisdomApp);
        this.next.init(wisdomApp);
        this.first.streamIds.forEach(streamId -> {
            wisdomApp.getStream(streamId).removeProcessor(this.first);
            wisdomApp.getStream(streamId).addProcessor(this);
        });
        this.next.streamIds.forEach(streamId -> {
            wisdomApp.getStream(streamId).removeProcessor(this.next);
            wisdomApp.getStream(streamId).addProcessor(this);
        });
    }

    @Override
    public Event event() {

        return next.event();
    }

    @Override
    public void setProcessConditionMet(Predicate<Event> processConditionMet) {
        this.first.setProcessConditionMet(processConditionMet);
    }

    @Override
    public void setEmitConditionMet(Predicate<Event> emitConditionMet) {
        this.next.setEmitConditionMet(emitConditionMet);
    }

    @Override
    public void setNextProcessor(Processor nextProcessor) {
        super.setNextProcessor(nextProcessor);
        this.first.setNextProcessor(nextProcessor);
        this.next.setNextProcessor(nextProcessor);
    }

    @Override
    public void process(Event event) {
        this.eventDistributor.process(event);
    }

    @Override
    public boolean isWaiting() {
        return this.first.isWaiting() || this.next.isWaiting();
    }

    @Override
    public void setMergePreviousEvents(Consumer<Event> mergePreviousEvents) {
        this.first.setMergePreviousEvents(mergePreviousEvents);
    }

    @Override
    public List<Event> getEvents() {

        List<Event> events = new ArrayList<>();
        events.addAll(this.first.getEvents());
        events.addAll(this.next.getEvents());
        return events;
    }

    @Override
    public void setPostProcess(Consumer<Event> postProcess) {
        this.next.setPostProcess(postProcess);
    }
}
