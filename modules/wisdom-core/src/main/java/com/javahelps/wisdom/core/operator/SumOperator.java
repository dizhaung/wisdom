package com.javahelps.wisdom.core.operator;

import com.javahelps.wisdom.core.event.Event;
import com.javahelps.wisdom.core.exception.WisdomAppValidationException;
import com.javahelps.wisdom.core.extension.WisdomExtension;
import com.javahelps.wisdom.core.partition.Partitionable;
import com.javahelps.wisdom.core.util.Commons;

import java.util.Map;

import static com.javahelps.wisdom.core.util.WisdomConstants.ATTR;

@WisdomExtension("sum")
public class SumOperator extends AggregateOperator {

    private String attribute;
    private double sum;

    public SumOperator(String as, Map<String, ?> properties) {
        super(as, properties);
        this.attribute = Commons.getProperty(properties, ATTR, 0);
        if (this.attribute == null) {
            throw new WisdomAppValidationException("Required property %s of Sum operator not found", ATTR);
        }
    }

    @Override
    public Object apply(Event event) {
        double value;
        synchronized (this) {
            if (event.isReset()) {
                this.sum = 0.0;
            } else {
                this.sum += event.getAsDouble(this.attribute);
            }
            value = this.sum;
        }
        return value;
    }

    @Override
    public void clear() {
        synchronized (this) {
            this.sum = 0.0;
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public Partitionable copy() {
        return new SumOperator(this.newName, Map.of(ATTR, this.attribute));
    }
}
