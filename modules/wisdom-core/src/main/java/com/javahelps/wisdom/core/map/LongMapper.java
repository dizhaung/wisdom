package com.javahelps.wisdom.core.map;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.exception.AttributeNotFoundException;
import com.javahelps.wisdom.core.exception.WisdomAppValidationException;
import com.javahelps.wisdom.core.extension.WisdomExtension;
import com.javahelps.wisdom.core.operand.WisdomArray;
import com.javahelps.wisdom.core.util.Commons;

import java.util.Map;

import static com.javahelps.wisdom.core.util.WisdomConstants.ATTR;

@WisdomExtension("long")
public class LongMapper extends Mapper {

    private final String currentName;

    public LongMapper(String attrName, Map<String, ?> properties) {
        super(attrName, properties);
        this.currentName = Commons.getProperty(properties, ATTR, 0);
        if (this.currentName == null) {
            throw new WisdomAppValidationException("Required property %s for Long mapper not found", ATTR);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void init(WisdomApp wisdomApp) {

    }

    @Override
    public void stop() {

    }

    @Override
    public Event map(Event event) {
        Object value = event.get(currentName);
        if (value == null) {
            throw new AttributeNotFoundException(String.format("Attribute %s not found in event %s", currentName,
                    this.toString()));
        }
        if (value instanceof Number) {
            event.set(attrName, ((Number) value).longValue());
        } else if (value instanceof WisdomArray) {
            event.set(attrName, ((WisdomArray) value).toLongArray());
        }
        return event;
    }
}
