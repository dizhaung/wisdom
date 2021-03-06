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

package com.javahelps.wisdom.core.statistics;


import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.WisdomContext;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.stream.InputHandler;
import com.javahelps.wisdom.core.util.EventGenerator;
import com.javahelps.wisdom.core.time.Scheduler;
import com.javahelps.wisdom.core.time.TimestampGenerator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.javahelps.wisdom.core.util.Commons.map;


public class StatisticsManager {

    private final String statisticStream;
    private final long reportFrequency;
    private final Comparable[] environmentVariables;
    private final List<StreamTracker> streamTrackers = new ArrayList<>();
    private boolean active;
    private WisdomApp app;
    private WisdomContext context;
    private InputHandler inputHandler;
    private Scheduler scheduler;
    private TimestampGenerator timestampGenerator;

    public StatisticsManager(String statisticStream, long reportFrequency, Comparable... env) {
        this.statisticStream = statisticStream;
        this.reportFrequency = reportFrequency;
        this.environmentVariables = env;
    }

    public void init(WisdomApp app) {
        this.app = app;
        this.context = app.getContext();
        this.scheduler = app.getContext().getScheduler();
        this.timestampGenerator = app.getContext().getTimestampGenerator();
        this.inputHandler = app.getInputHandler(this.statisticStream);
    }

    public StreamTracker createStreamTracker(String streamId) {
        if (this.statisticStream.equals(streamId)) {
            return null;
        }
        StreamTracker streamTracker = new StreamTracker(streamId);
        this.streamTrackers.add(streamTracker);
        return streamTracker;
    }

    public void start() {
        if (this.inputHandler != null) {
            this.active = true;
            long currentTime = this.timestampGenerator.currentTimestamp();
            for (StreamTracker tracker : this.streamTrackers) {
                tracker.setStartTime(currentTime);
            }
            this.scheduler.schedule(Duration.ofMillis(this.reportFrequency), this::send);
        }
    }

    public void stop() {
        if (this.inputHandler != null) {
            this.active = false;
        }
    }

    private void send(long currentTime) {
        if (!this.active) {
            synchronized (this) {
                if (!this.active) {
                    return;
                }
            }
        }
        for (StreamTracker tracker : this.streamTrackers) {
            double duration = (currentTime - tracker.getStartTime()) / 1000;
            double throughput = tracker.getCount() / duration;
            Map<String, Object> data = map("app", this.app.getName(), "name", tracker.getStreamId(), "throughput", throughput, "timestamp", currentTime);
            for (Comparable variable : this.environmentVariables) {
                data.put(variable.toString(), this.context.getProperty(variable));
            }
            Event event = EventGenerator.generate(data);
            tracker.reset();
            tracker.setStartTime(currentTime);
            this.inputHandler.send(event);
        }
        if (!this.active) {
            synchronized (this) {
                if (!this.active) {
                    return;
                }
            }
        }
        this.scheduler.schedule(Duration.ofMillis(this.reportFrequency), this::send);
    }
}
