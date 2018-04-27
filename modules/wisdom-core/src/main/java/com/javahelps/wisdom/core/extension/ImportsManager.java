package com.javahelps.wisdom.core.extension;

import com.javahelps.wisdom.core.exception.WisdomAppValidationException;
import com.javahelps.wisdom.core.map.Mapper;
import com.javahelps.wisdom.core.stream.input.Source;
import com.javahelps.wisdom.core.stream.output.Sink;
import com.javahelps.wisdom.core.window.Window;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum ImportsManager {

    INSTANCE;

    private final Map<String, Constructor> windows = new HashMap();
    private final Map<String, Constructor> sinks = new HashMap();
    private final Map<String, Constructor> sources = new HashMap();
    private final Map<String, Constructor> mappers = new HashMap();

    public void use(Class<?> clazz) {

        WisdomExtension annotation = clazz.getAnnotation(WisdomExtension.class);
        if (annotation == null) {
            throw new WisdomAppValidationException("Class %s is not annotated by @WisdomExtension", clazz.getCanonicalName());
        }
        String namespace = annotation.value();

        if (Window.class.isAssignableFrom(clazz)) {
            try {
                this.windows.put(namespace, clazz.getConstructor(Map.class));
            } catch (NoSuchMethodException e) {
                throw new WisdomAppValidationException("<init>(java.util.Map<String, ?>) not found in %s", clazz.getCanonicalName());
            }
        } else if (Sink.class.isAssignableFrom(clazz)) {
            try {
                this.sinks.put(namespace, clazz.getConstructor(Map.class));
            } catch (NoSuchMethodException e) {
                throw new WisdomAppValidationException("<init>(java.util.Map<String, ?>) not found in %s", clazz.getCanonicalName());
            }
        } else if (Source.class.isAssignableFrom(clazz)) {
            try {
                this.sources.put(namespace, clazz.getConstructor(Map.class));
            } catch (NoSuchMethodException e) {
                throw new WisdomAppValidationException("<init>(java.util.Map<String, ?>) not found in %s", clazz.getCanonicalName());
            }
        } else if (Mapper.class.isAssignableFrom(clazz)) {
            try {
                this.mappers.put(namespace, clazz.getConstructor(String.class, String.class, Map.class));
            } catch (NoSuchMethodException e) {
                throw new WisdomAppValidationException("<init>(java.lang.String, java.lang.String, java.util.Map<String, ?>) not found in %s", clazz.getCanonicalName());
            }
        }
    }

    public void use(String packageName) {

        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Object>> extensionClasses = reflections.getTypesAnnotatedWith(WisdomExtension.class);
        for (Class<?> clazz : extensionClasses) {
            this.use(clazz);
        }
    }

    public Window createWindow(String namespace, Map<String, ?> properties) {
        Constructor constructor = this.windows.get(namespace);
        if (constructor == null) {
            throw new WisdomAppValidationException("Class to create %s window was not imported", namespace);
        }
        try {
            return (Window) constructor.newInstance(properties);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new WisdomAppValidationException(e.getCause(), "Failed to create %s window instance", namespace);
        }
    }

    public Sink createSink(String namespace, Map<String, ?> properties) {
        Constructor constructor = this.sinks.get(namespace);
        if (constructor == null) {
            throw new WisdomAppValidationException("Class to create %s sink was not imported", namespace);
        }
        try {
            return (Sink) constructor.newInstance(properties);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new WisdomAppValidationException(e.getCause(), "Failed to create %s sink instance", namespace);
        }
    }

    public Source createSource(String namespace, Map<String, ?> properties) {
        Constructor constructor = this.sources.get(namespace);
        if (constructor == null) {
            throw new WisdomAppValidationException("Class to create %s source was not imported", namespace);
        }
        try {
            return (Source) constructor.newInstance(properties);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new WisdomAppValidationException(e.getCause(), "Failed to create %s source instance", namespace);
        }
    }

    public Mapper createMapper(String namespace, String currentName, String newName, Map<String, ?> properties) {
        Constructor constructor = this.mappers.get(namespace);
        if (constructor == null) {
            throw new WisdomAppValidationException("Class to create %s mapper was not imported", namespace);
        }
        try {
            return (Mapper) constructor.newInstance(currentName, newName, properties);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new WisdomAppValidationException(e.getCause(), "Failed to create %s mapper instance", namespace);
        }
    }

    public void scanClassPath() {
        ConfigurationBuilder builder = new ConfigurationBuilder()
                .addUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
        Reflections reflections = new Reflections(builder);

        Set<Class<?>> extensions = reflections.getTypesAnnotatedWith(WisdomExtension.class);
        for (Class<?> clazz : extensions) {
            this.use(clazz);
        }
    }
}
