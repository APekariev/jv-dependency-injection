package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzComponentInstance = getComponentInstance(clazz);
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzComponentInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Clazz: "
                            + interfaceClazz.getName()
                            + "; Field: " + field.getName());
                }
            }
        }
        if (clazzComponentInstance == null) {
            clazzComponentInstance = getComponentInstance(clazz);
        }
        return clazzComponentInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object getComponentInstance(Class<?> clazz) {
        Object componentClassInstance = null;
        if (clazz.isAnnotationPresent(Component.class)) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                componentClassInstance = constructor.newInstance();
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Can't create a new instance of " + clazz.getName());
            }
        } else {
            throw new RuntimeException("Not component class: " + clazz);
        }
        return componentClassInstance;
    }
}
