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
package org.grouplens.grapht.spi.reflect;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.QualifierMatcher;

/**
 * Utilities related to Qualifier implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class Qualifiers {
    private Qualifiers() { }
    
    /**
     * Return the Qualifier representing the {@link Qualifier} contained in the
     * parameter annotations given. If the annotations do not have any
     * annotation that is a {@link Qualifier}, then null is returned. If
     * {@link Named} is encountered, a NamedQualifier is used, otherwise a
     * {@link AnnotationQualifier} is used.
     * 
     * @param parameterAnnots The parameter annotations on the setter or
     *            constructor
     * @return The Qualifier for the injection point, or null if there
     *         is no {@link Qualifier}
     */
    public static AnnotationQualifier getQualifier(Annotation[] parameterAnnots) {
        for (int i = 0; i < parameterAnnots.length; i++) {
            if (Qualifiers.isQualifier(parameterAnnots[i].annotationType())) {
                return new AnnotationQualifier(parameterAnnots[i]);
            }
        }
        return null;
    }

    /**
     * Return true or false whether or not the annotation type represents a
     * {@link Qualifier}
     * 
     * @param type The annotation type
     * @return True if the annotation is a {@link Qualifier} or parameter
     * @throws NullPointerException if the type is null
     */
    public static boolean isQualifier(Class<? extends Annotation> type) {
        return type.getAnnotation(javax.inject.Qualifier.class) != null;
    }
    
    /**
     * @return A QualifierMatcher that matches any qualifier
     */
    public static QualifierMatcher matchAny() {
        return new AnyMatcher();
    }
    
    /**
     * @return A QualifierMatcher that matches only the null qualifier
     */
    public static QualifierMatcher matchNone() {
        return new NullMatcher();
    }
    
    /**
     * @param annotType Annotation type class to match
     * @return A QualifierMatcher that matches any annotation of the given class
     *         type
     */
    public static QualifierMatcher match(Class<? extends Annotation> annotType) {
        return new AnnotationClassMatcher(annotType);
    }
    
    /**
     * @param annot Annotation instance to match
     * @return A QualifierMatcher that matches annotations equaling annot
     */
    public static QualifierMatcher match(Annotation annot) {
        return new AnnotationMatcher(annot);
    }
    
    // These priorities specify that:
    // AnyMatcher < AnnotationClassMatcher < NullMatcher == AnnotationMatcher
    private static final Map<Class<? extends QualifierMatcher>, Integer> TYPE_PRIORITIES;
    static {
        Map<Class<? extends QualifierMatcher>, Integer> tp = new HashMap<Class<? extends QualifierMatcher>, Integer>();
        tp.put(AnyMatcher.class, 0);
        tp.put(AnnotationClassMatcher.class, 1);
        tp.put(NullMatcher.class, 2);
        tp.put(AnnotationMatcher.class, 2);
        
        TYPE_PRIORITIES = Collections.unmodifiableMap(tp);
    }
    
    private static abstract class AbstractMatcher implements QualifierMatcher {
        @Override
        public int compareTo(QualifierMatcher o) {
            Integer p1 = TYPE_PRIORITIES.get(getClass());
            Integer p2 = TYPE_PRIORITIES.get(o.getClass());
            
            if (p2 == null) {
                // other type is unknown, so push it to the front
                return 1;
            }
            
            // otherwise compare based on priorities, where a higher priority
            // puts the matcher near the front
            return p2 - p1;
        }
    }
    
    private static class AnyMatcher extends AbstractMatcher {
        @Override
        public boolean matches(Qualifier q) {
            return true;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnyMatcher)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return AnyMatcher.class.hashCode();
        }
    }
    
    private static class NullMatcher extends AbstractMatcher {
        @Override
        public boolean matches(Qualifier q) {
            return q == null;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NullMatcher)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return NullMatcher.class.hashCode();
        }
    }
    
    private static class AnnotationClassMatcher extends AbstractMatcher {
        private final Class<? extends Annotation> type;
        
        public AnnotationClassMatcher(Class<? extends Annotation> type) {
            if (type == null) {
                throw new NullPointerException("Annotation type cannot be null");
            }
            if (!Qualifiers.isQualifier(type)) {
                throw new IllegalArgumentException("Annotation is not a Qualifier annotation");
            }
            this.type = type;
        }
        
        @Override
        public boolean matches(Qualifier q) {
            Class<? extends Annotation> qtype = (q == null ? null : ((AnnotationQualifier) q).getAnnotation().annotationType());
            return type.equals(qtype);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationClassMatcher)) {
                return false;
            }
            return ((AnnotationClassMatcher) o).type.equals(type);
        }
        
        @Override
        public int hashCode() {
            return type.hashCode();
        }
    }
    
    private static class AnnotationMatcher extends AbstractMatcher {
        private final Annotation annot;
        
        public AnnotationMatcher(Annotation annot) {
            if (annot == null) {
                throw new NullPointerException("Annotationcannot be null");
            }
            if (!Qualifiers.isQualifier(annot.annotationType())) {
                throw new IllegalArgumentException("Annotation is not a Qualifier annotation");
            }
            this.annot = annot;
        }
        
        @Override
        public boolean matches(Qualifier q) {
            Annotation qa = (q == null ? null : ((AnnotationQualifier) q).getAnnotation());
            return annot.equals(qa);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationMatcher)) {
                return false;
            }
            return ((AnnotationMatcher) o).annot.equals(annot);
        }
        
        @Override
        public int hashCode() {
            return annot.hashCode();
        }
    }
}
