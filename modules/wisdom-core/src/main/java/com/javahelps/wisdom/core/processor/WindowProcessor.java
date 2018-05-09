package com.javahelps.wisdom.core.processor;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.window.Window;

import java.util.List;

/**
 * The runtime {@link StreamProcessor} of {@link Window}s.
 */
public class WindowProcessor extends StreamProcessor implements Stateful {


    private Window window;
    private WisdomApp wisdomApp;

    public WindowProcessor(String id, Window window) {
        super(id);
        this.window = window;
    }

    @Override
    public void init(WisdomApp wisdomApp) {
        this.window.init(wisdomApp);
        this.wisdomApp = wisdomApp;
    }

    @Override
    public void start() {

    }

    @Override
    public void process(Event event) {

        this.window.process(event, getNextProcessor());
    }

    @Override
    public void process(List<Event> events) {

    }

    @Override
    public Processor copy() {

        WindowProcessor windowProcessor = new WindowProcessor(this.id, this.window.copy());
        windowProcessor.setNextProcessor(this.getNextProcessor().copy());
        windowProcessor.init(this.wisdomApp);
        return windowProcessor;
    }

    @Override
    public void destroy() {
        this.window.destroy();
        this.window = null;
    }

    @Override
    public void clear() {
        this.window.clear();
    }
}
