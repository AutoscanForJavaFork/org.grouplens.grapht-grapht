/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.*;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.grapht.util.Preconditions;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Satisfaction implementation wrapping an instance. It has no dependencies, and
 * the resulting providers just return the instance.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InstanceSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("squid:S1948") // serializable warning; satisfaction is serializable iff instance is
    private final Object instance;

    /**
     * Create a new instance node wrapping an instance.
     * 
     * @param obj The object to return.
     * @throws NullPointerException if obj is null
     */
    public InstanceSatisfaction(Object obj) {
        Preconditions.notNull("instance", obj);
        instance = obj;
    }
    
    /**
     * @return The instance that satisfies this satisfaction
     */
    public Object getInstance() {
        return instance;
    }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }

    @Override
    public List<Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return instance.getClass();
    }

    @Override
    public Class<?> getErasedType() {
        return instance.getClass();
    }

    @Override
    public boolean hasInstance() {
        return true;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitInstance(instance);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Instantiator makeInstantiator(Map<Desire,Instantiator> dependencies,
                                         LifecycleManager injectionContainer) {
        return Instantiators.ofInstance(instance);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InstanceSatisfaction)) {
            return false;
        }
        return ((InstanceSatisfaction) o).instance.equals(instance);
    }
    
    @Override
    public int hashCode() {
        return instance.hashCode();
    }
    
    @Override
    public String toString() {
        return "Instance(" + instance + ")";
    }
}
