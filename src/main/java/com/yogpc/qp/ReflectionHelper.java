package com.yogpc.qp;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReflectionHelper {
    public static List<Method> getMethods(final Class<?> t, final Class<? extends Annotation> a) {
        final List<Method> ms = new ArrayList<>();
        Class<?> c = t;
        while (c != null && c != Object.class) {
            for (final Method m : c.getDeclaredMethods())
                if (m.isAnnotationPresent(a)) {
                    m.setAccessible(true);
                    ms.add(m);
                }
            c = c.getSuperclass();
        }
        return ms;
    }

    public static Method getMethod(final Class<?> t, final String[] sv, final Class<?>[]... av) {
        final Collection<Exception> ec = new ArrayList<>();
        Class<?> c = t;
        while (c != null && c != Object.class) {
            for (final String element : sv)
                for (final Class<?>[] element2 : av)
                    try {
                        final Method tmp = c.getDeclaredMethod(element, element2);
                        tmp.setAccessible(true);
                        return tmp;
                    } catch (final Exception e) {
                        ec.add(e);
                    }
            c = c.getSuperclass();
        }
        for (final Exception e : ec)
            e.printStackTrace();
        return null;
    }

    public static Field getField(final Class<?> t, final String... sv) {
        final Collection<Exception> ec = new ArrayList<>();
        Class<?> c = t;
        while (c != null && c != Object.class) {
            for (final String s : sv)
                try {
                    final Field tmp = c.getDeclaredField(s);
                    tmp.setAccessible(true);
                    return tmp;
                } catch (final Exception e) {
                    ec.add(e);
                }
            c = c.getSuperclass();
        }
        for (final Exception e : ec)
            e.printStackTrace();
        return null;
    }

    public static Class<?> getClass(final String... sv) {
        final Collection<Exception> ec = new ArrayList<>();
        for (final String s : sv)
            try {
                return Class.forName(s, false, ReflectionHelper.class.getClassLoader());
            } catch (final Exception e) {
                ec.add(e);
            }
        for (final Exception e : ec)
            e.printStackTrace();
        return null;
    }

    public static Object invoke(final Method m, final Object obj, final Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (final Exception | Error e) {
            e.printStackTrace();
        }
        return null;
    }
}
