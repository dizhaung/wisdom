package com.javahelps.wisdom.extensions.ml.onehot;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.exception.WisdomAppValidationException;
import com.javahelps.wisdom.core.extension.WisdomExtension;
import com.javahelps.wisdom.core.map.Mapper;
import com.javahelps.wisdom.core.operand.WisdomArray;
import com.javahelps.wisdom.core.util.Commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.javahelps.wisdom.core.util.WisdomConstants.ATTR;

@WisdomExtension("toOneHot")
public class ToOneHotMapper extends Mapper {

    private final String currentName;
    private final List<Comparable> items;
    private final int size;

    public ToOneHotMapper(String attrName, Map<String, ?> properties) {
        super(attrName, properties);
        this.currentName = Commons.getProperty(properties, ATTR, 0);
        Object items = Commons.getProperty(properties, "items", 1);
        if (this.currentName == null) {
            throw new WisdomAppValidationException("Required property %s for ToOneHot mapper not found", ATTR);
        }
        if (items instanceof List) {
            this.items = new ArrayList<>((List<Comparable>) items);
        } else if (items instanceof WisdomArray) {
            WisdomArray array = (WisdomArray) items;
            this.items = new ArrayList<>(array.size());
            for (Object item : array) {
                if (item instanceof Comparable) {
                    this.items.add((Comparable) item);
                } else {
                    throw new WisdomAppValidationException("Every item in ToOneHot items must be a java.lang.Comparable object");
                }
            }
        } else {
            throw new WisdomAppValidationException("items must be either java.util.List or WisdomArray");
        }
        if (this.items == null) {
            throw new WisdomAppValidationException("Required property items for ToOneHot mapper not found");
        }
        this.size = this.items.size();
        Collections.sort(this.items);
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
        int[] oneHot = new int[size];
        oneHot[items.indexOf(event.get(this.currentName))] = 1;
        event.set(this.attrName, oneHot);
        return event;
    }
}
