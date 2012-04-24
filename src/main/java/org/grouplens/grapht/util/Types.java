/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.util;

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.grapht.annotation.Transient;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Static helper methods for working with types.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class Types {
    private Types() {}

    /**
     * Return the boxed version of the given type if the type is primitive.
     * Otherwise, if the type is not a primitive the original type is returned.
     * As an example, int.class is converted to Integer.class.
     * 
     * @param type The possibly unboxed type
     * @return The boxed type
     */
    public static Class<?> box(Class<?> type) {
        if (int.class.equals(type)) {
            return Integer.class;
        } else if (short.class.equals(type)) {
            return Short.class;
        } else if (byte.class.equals(type)) {
            return Byte.class;
        } else if (long.class.equals(type)) {
            return Long.class;
        } else if (boolean.class.equals(type)) {
            return Boolean.class;
        } else if (char.class.equals(type)) {
            return Character.class;
        } else if (float.class.equals(type)) {
            return Float.class;
        } else if (double.class.equals(type)) {
            return Double.class;
        } else {
            return type;
        }
    }

    /**
     * Compute the erasure of a type.
     * 
     * @param type The type to erase.
     * @return The class representing the erasure of the type.
     * @throws IllegalArgumentException if <var>type</var> is unerasable (e.g.
     *             it is a type variable or a wildcard).
     */
    public static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type raw = pt.getRawType();
            try {
                return (Class<?>) raw;
            } catch (ClassCastException e) {
                throw new RuntimeException("raw type not a Class", e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return the type distance between the child and parent types. If the child
     * does not extend from parent, then a negative value is returned.
     * Otherwise, the number of steps between child and parent is returned. As
     * an example, if child is an immediate subclass of parent, then 1 is
     * returned. If child and parent are equal than 0 is returned.
     * 
     * @param child The child type
     * @param parent The parent type
     * @return The type distance
     * @throws NullPointerException if child or parent are null
     */
    public static int getTypeDistance(Class<?> child, Class<?> parent) {
        if (!parent.isAssignableFrom(child)) {
            // if child does not extend from the parent, return -1
            return -1;
        }
        
        // at this point we can assume at some point a superclass of child
        // will equal parent
        int distance = 0;
        while(!child.equals(parent)) {
            distance++;
            child = child.getSuperclass();
        }
        return distance;
    }
    
    /**
     * Get the type that is provided by a given implementation of
     * {@link Provider}.
     * 
     * @param providerClass The provider's class
     * @return The provided class type
     * @throws IllegalArgumentException if the class doesn't actually implement
     *             Provider
     */
    public static Class<?> getProvidedType(Class<? extends Provider<?>> providerClass) {
        try {
            return Types.box(providerClass.getMethod("get").getReturnType());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement get()");
        }
    }

    /**
     * Get the type that is provided by the Provider instance.
     * 
     * @param provider The provider instance queried
     * @return The provided class type
     * @see #getProvidedType(Class)
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getProvidedType(Provider<?> provider) {
        return getProvidedType((Class<? extends Provider<?>>) provider.getClass());
    }
    
    /**
     * Return true if the type is not abstract and not an interface, and has
     * a constructor annotated with {@link Inject}, or its only constructor
     * is the default constructor.
     * 
     * @param type A class type
     * @return True if the class type is instantiable
     */
    public static boolean isInstantiable(Class<?> type) {
        if (!Modifier.isAbstract(type.getModifiers()) && !type.isInterface()) {
            // first check for a constructor annotated with @Inject, 
            //  - this doesn't care how many we'll let the injector complain
            //    if there are more than one
            for (Constructor<?> c: type.getConstructors()) {
                if (c.getAnnotation(Inject.class) != null) {
                    return true;
                }
            }
            
            // check if we only have the default constructor
            if (type.getConstructors().length == 1 
                && type.getConstructors()[0].getParameterTypes().length == 0) {
                return true;
            }
        }
        
        // no constructor available
        return false;
    }
    
    /**
     * Return true if the array of Annotations contains an Annotation with a
     * simple name of 'Nullable'. It does not matter which actual Nullable
     * annotation is present.
     * 
     * @param annotations Array of annotations, e.g. from a setter or
     *            constructor
     * @return True if there exists a Nullable annotation in the array
     */
    public static boolean hasNullableAnnotation(Annotation[] annotations) {
        for (Annotation a: annotations) {
            if (a.annotationType().getSimpleName().equals("Nullable")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return true if the array of Annotations contains a {@link Transient}
     * annotation.
     * 
     * @param annotations Array of annotations, e.g. from a setter or
     *            constructor
     * @return True if there exists a Transient annotation in the array
     */
    public static boolean hasTransientAnnotation(Annotation[] annotations) {
        for (Annotation a: annotations) {
            if (a instanceof Transient) {
                return true;
            }
        }
        return false;
    }
}
