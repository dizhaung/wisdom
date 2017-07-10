package com.javahelps.wisdom.core.pattern;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.exception.WisdomAppValidationException;
import com.javahelps.wisdom.core.processor.Processor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by gobinath on 6/29/17.
 */
public class FollowingPattern extends Pattern {


    private Pattern first;
    private Pattern next;

    public FollowingPattern(String patternId, Pattern first, Pattern next) {
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
    }


    @Override
    public void init(WisdomApp wisdomApp) {

        this.first.init(wisdomApp);
        this.next.init(wisdomApp);
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
    public Pattern filter(Predicate<Event> predicate) {

        throw new WisdomAppValidationException("FollowingPattern cannot have filter");
    }

    @Override
    public boolean isWaiting() {
        return this.next.isWaiting();
    }

    @Override
    public void setMergePreviousEvents(Consumer<Event> mergePreviousEvents) {
        this.first.setMergePreviousEvents(mergePreviousEvents);
    }

    @Override
    public List<Event> getEvents() {
        return this.next.getEvents();
    }
}
