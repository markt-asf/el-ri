/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package jakarta.el;

import static jakarta.el.ELUtil.getExceptionMessageString;

import java.util.Objects;
import java.util.Optional;

/**
 * Defines property resolution, method invocation and type conversion behaviour on {@link Optional}s.
 * <p>
 * This resolver handles base objects that are instances of {@link Optional}.
 * <p>
 * This resolver is always a read-only resolver since {@link Optional} instances are immutable.
 * 
 * @since Jakarta Expression Language 6.0
 */
public class OptionalELResolver extends ELResolver {

    /**
     * {@inheritDoc}
     *
     * @return If the base object is an {@link Optional} and {@link Optional#isEmpty()} returns {@code true} then the
     *             resulting value is {@code null}.
     *             <p>
     *             If the base object is an {@link Optional}, {@link Optional#isPresent()} returns {@code true} and the
     *             property is {@code null} then the resulting value is the result of calling {@link Optional#get()} on
     *             the base object.
     *             <p>
     *             If the base object is an {@link Optional}, {@link Optional#isPresent()} returns {@code true} and the
     *             property is not {@code null} then the resulting value is the result of calling
     *             {@link ELResolver#getValue(ELContext, Object, Object)} using the {@link ELResolver} obtained from
     *             {@link ELContext#getELResolver()} with the following parameters:
     *             <ul>
     *             <li>The {@link ELContext} is the current context</li>
     *             <li>The base object is the result of calling {@link Optional#get()} on the current base object</li>
     *             <li>The property object is the current property object</li>
     *             </ul>
     *             <p>
     *             If the base object is not an {@link Optional} then the return value is undefined.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);

        if (base instanceof Optional) {
            context.setPropertyResolved(base, property);
            if (((Optional<?>) base).isEmpty()) {
                if (property == null) {
                    return null;
                }
            } else {
                if (property == null) {
                    return ((Optional<?>) base).get();
                } else {
                    Object resolvedBase = ((Optional<?>) base).get();
                    return context.getELResolver().getValue(context, resolvedBase, property);
                }
            }
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @return If the base object is an {@link Optional} this method always returns {@code null} since instances of this
     *             resolver are always read-only.
     *             <p>
     *             If the base object is not an {@link Optional} then the return value is undefined.
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);

        if (base instanceof Optional) {
            context.setPropertyResolved(base, property);
        }

        return null;
    }


    /**
     * {@inheritDoc}
     * <p>
     * If the base object is an {@link Optional} this method always throws a {@link PropertyNotWritableException} since
     * instances of this resolver are always read-only.
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        Objects.requireNonNull(context);

        if (base instanceof Optional) {
            throw new PropertyNotWritableException(getExceptionMessageString(
                    context, "resolverNotwritable", new Object[] { base.getClass().getName() }));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @return If the base object is an {@link Optional} this method always returns {@code true} since instances of this
     *             resolver are always read-only.
     *             <p>
     *             If the base object is not an {@link Optional} then the return value is undefined.
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        Objects.requireNonNull(context);

        if (base instanceof Optional) {
            context.setPropertyResolved(base, property);
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @return If the base object is an {@link Optional} this method always returns {@code Object.class}.
     *             <p>
     *             If the base object is not an {@link Optional} then the return value is undefined.
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base instanceof Optional) {
            return Object.class;
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @return If the base object is an {@link Optional} and {@link Optional#isEmpty()} returns {@code true} then this
     *             method returns the result of coercing {@code null} to the requested {@code type}.
     *             <p>
     *             If the base object is an {@link Optional} and {@link Optional#isPresent()} returns {@code true} then
     *             this method returns the result of coercing {@code Optional#get()} to the requested {@code type}.
     *             <p>
     *             If the base object is not an {@link Optional} then the return value is undefined.
     */
    @Override
    public <T> T convertToType(ELContext context, Object obj, Class<T> type) {
        Objects.requireNonNull(context);
        if (obj instanceof Optional) {
            Object value = null;
            if (((Optional<?>) obj).isPresent()) {
                value = ((Optional<?>) obj).get();
                // If the value is assignable to the required type, do so.
                if (type.isAssignableFrom(value.getClass())) {
                    context.setPropertyResolved(true);
                    @SuppressWarnings("unchecked")
                    T result = (T) value;
                    return result;
                }
            }

            try {
                Object convertedValue = context.convertToType(value, type);
                context.setPropertyResolved(true);
                @SuppressWarnings("unchecked")
                T result = (T) convertedValue;
                return result;
            } catch (ELException e) {
                /*
                 * TODO: This isn't pretty but it works. Significant refactoring would be required to avoid the
                 * exception. See also Util.isCoercibleFrom().
                 */
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @return If the base object is an {@link Optional} and {@link Optional#isEmpty()} returns {@code true} then this
     *             method returns {@code null}.
     *             <p>
     *             If the base object is an {@link Optional} and {@link Optional#isPresent()} returns {@code true} then
     *             this method returns the result of invoking the specified method on the object obtained by calling
     *             {@link Optional#get()} with the specified parameters.
     *             <p>
     *             If the base object is not an {@link Optional} then the return value is undefined.
     */
    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        Objects.requireNonNull(context);

        if (base instanceof Optional && method != null) {
            context.setPropertyResolved(base, method);
            if (((Optional<?>) base).isEmpty()) {
                return null;
            } else {
                Object resolvedBase = ((Optional<?>) base).get();
                return context.getELResolver().invoke(context, resolvedBase, method, paramTypes, params);
            }
        }

        return null;
    }
}
