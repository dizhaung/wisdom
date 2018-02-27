package com.javahelps.wisdom.core.operand;

public class WisdomDouble {

    private transient double value;

    public WisdomDouble() {
        this(0);
    }

    public WisdomDouble(double initialValue) {
        this.value = initialValue;
    }

    public double addAndGet(double increment) {
        this.value += increment;
        return this.value;
    }

    public double set(double val) {
        this.value = val;
        return this.value;
    }
}