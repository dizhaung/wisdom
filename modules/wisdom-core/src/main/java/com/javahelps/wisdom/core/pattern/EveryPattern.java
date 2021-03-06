/*
 * Copyright (c) 2018, Gobinath Loganathan (http://github.com/slgobinath) All Rights Reserved.
 *
 * Gobinath licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. In addition, if you are using
 * this file in your research work, you are required to cite
 * WISDOM as mentioned at https://github.com/slgobinath/wisdom.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.javahelps.wisdom.core.pattern;

import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.processor.Processor;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by gobinath on 6/29/17.
 */
class EveryPattern extends WrappingPattern {

    private Pattern pattern;

    EveryPattern(String patternId, Pattern pattern) {

        super(patternId, pattern);

        this.pattern = pattern;
        this.pattern.setBatchPattern(true);
        this.setBatchPattern(true);
        this.pattern.setProcessConditionMet(event -> true);
        this.attributeCache.setMap(pattern.attributeCache.getMap());
    }

    @Override
    public void setNextProcessor(Processor nextProcessor) {

        super.setNextProcessor(nextProcessor);
        this.pattern.setNextProcessor(nextProcessor);
    }

    @Override
    protected void globalReset() {
        this.reset();
    }

    @Override
    public void process(Event event) {

        try {
            this.lock.lock();
            this.pattern.process(event);
            this.pattern.setAccepting(true);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public boolean isConsumed() {
        return this.pattern.isConsumed();
    }

    @Override
    public void setConsumed(boolean consumed) {
        this.pattern.setConsumed(false);
    }

    @Override
    public boolean isComplete() {
        return this.pattern.isComplete();
    }

    @Override
    public boolean isAccepting() {
        return true;
    }

    @Override
    public List<Event> getEvents() {
        return this.pattern.getEvents();
    }

    @Override
    public void setProcessConditionMet(Predicate<Event> processConditionMet) {

//        processConditionMet = processConditionMet.or(event -> !this.getEvents().isEmpty());
//        this.definePattern.setProcessConditionMet(this.definePattern.getProcessConditionMet().and(processConditionMet));
        this.pattern.setProcessConditionMet(processConditionMet);
    }

    @Override
    public void setEmitConditionMet(Predicate<Event> emitConditionMet) {

//        Predicate<Event> filter = this.filter.and(emitConditionMet);
        this.pattern.setEmitConditionMet(emitConditionMet);
    }

//    @Override
//    public void setMergePreviousEvents(Consumer<Event> mergePreviousEvents) {
//
//        super.setMergePreviousEvents(mergePreviousEvents);
//        this.definePattern.setMergePreviousEvents(this.definePattern.getMergePreviousEvents().andThen(mergePreviousEvents));
//    }

    @Override
    public void setPreviousEvents(Supplier<List<Event>> previousEvents) {
        this.pattern.setPreviousEvents(previousEvents);
    }

    @Override
    public void onNextPreProcess(Event event) {
        super.onNextPreProcess(event);
    }

    @Override
    public void clear() {
        try {
            this.lock.lock();
            this.pattern.clear();
        } finally {
            this.lock.unlock();
        }
    }
}
