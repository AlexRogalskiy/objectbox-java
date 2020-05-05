/*
 * Copyright 2017-2019 ObjectBox Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.objectbox;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import io.objectbox.annotation.apihint.Internal;
import io.objectbox.converter.PropertyConverter;
import io.objectbox.exception.DbException;
import io.objectbox.query.PropertyQueryCondition;
import io.objectbox.query.PropertyQueryConditionImpl.ByteArrayCondition;
import io.objectbox.query.PropertyQueryConditionImpl.DoubleCondition;
import io.objectbox.query.PropertyQueryConditionImpl.DoubleDoubleCondition;
import io.objectbox.query.PropertyQueryConditionImpl.IntArrayCondition;
import io.objectbox.query.PropertyQueryConditionImpl.LongArrayCondition;
import io.objectbox.query.PropertyQueryConditionImpl.LongCondition;
import io.objectbox.query.PropertyQueryConditionImpl.LongLongCondition;
import io.objectbox.query.PropertyQueryConditionImpl.NullCondition;
import io.objectbox.query.PropertyQueryConditionImpl.StringArrayCondition;
import io.objectbox.query.PropertyQueryConditionImpl.StringCondition;
import io.objectbox.query.PropertyQueryConditionImpl.StringCondition.Operation;
import io.objectbox.query.QueryBuilder.StringOrder;

/**
 * Meta data describing a Property of an ObjectBox Entity.
 * Properties are typically used when defining {@link io.objectbox.query.Query Query} conditions
 * using {@link io.objectbox.query.QueryBuilder QueryBuilder}.
 * Access properties using the generated underscore class of an entity (e.g. {@code Example_.id}).
 */
@SuppressWarnings("WeakerAccess,UnusedReturnValue, unused")
public class Property<ENTITY> implements Serializable {
    private static final long serialVersionUID = 8613291105982758093L;

    public final EntityInfo<ENTITY> entity;
    public final int ordinal;
    public final int id;

    /** One of the supported types to be mapped to the DB. */
    public final Class<?> type;

    public final String name;
    public final boolean isId;
    public final boolean isVirtual;
    public final String dbName;
    public final Class<? extends PropertyConverter<?, ?>> converterClass;

    /** Type, which is converted to a type supported by the DB. */
    public final Class<?> customType;

    // TODO verified state should be per DB -> move to BoxStore/Box.
    // Also, this should make the Property class truly @Immutable.
    private boolean idVerified;

    public Property(EntityInfo<ENTITY> entity, int ordinal, int id, Class<?> type, String name) {
        this(entity, ordinal, id, type, name, false, name, null, null);
    }

    public Property(EntityInfo<ENTITY> entity, int ordinal, int id, Class<?> type, String name, boolean isVirtual) {
        this(entity, ordinal, id, type, name, false, isVirtual, name, null, null);
    }

    public Property(EntityInfo<ENTITY> entity, int ordinal, int id, Class<?> type, String name, boolean isId,
                    @Nullable String dbName) {
        this(entity, ordinal, id, type, name, isId, dbName, null, null);
    }

    // Note: types of PropertyConverter might not exactly match type and customtype, e.g. if using generics like List.class.
    public Property(EntityInfo<ENTITY> entity, int ordinal, int id, Class<?> type, String name, boolean isId,
                    @Nullable String dbName, @Nullable Class<? extends PropertyConverter<?, ?>> converterClass,
                    @Nullable Class<?> customType) {
        this(entity, ordinal, id, type, name, isId, false, dbName, converterClass, customType);
    }

    public Property(EntityInfo<ENTITY> entity, int ordinal, int id, Class<?> type, String name, boolean isId,
                    boolean isVirtual, @Nullable String dbName,
                    @Nullable Class<? extends PropertyConverter<?, ?>> converterClass, @Nullable Class<?> customType) {
        this.entity = entity;
        this.ordinal = ordinal;
        this.id = id;
        this.type = type;
        this.name = name;
        this.isId = isId;
        this.isVirtual = isVirtual;
        this.dbName = dbName;
        this.converterClass = converterClass;
        this.customType = customType;
    }

    /** Creates an "IS NULL" condition for this property. */
    public PropertyQueryCondition<ENTITY> isNull() {
        return new NullCondition<>(this, NullCondition.Operation.IS_NULL);
    }

    /** Creates an "IS NOT NULL" condition for this property. */
    public PropertyQueryCondition<ENTITY> notNull() {
        return new NullCondition<>(this, NullCondition.Operation.NOT_NULL);
    }

    /** Creates an "equal ('=')" condition for this property. */
    public PropertyQueryCondition<ENTITY> equal(boolean value) {
        return new LongCondition<>(this, LongCondition.Operation.EQUAL, value);
    }

    /** Creates a "not equal ('&lt;&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> notEqual(boolean value) {
        return new LongCondition<>(this, LongCondition.Operation.NOT_EQUAL, value);
    }

    /** Creates an "equal ('=')" condition for this property. */
    public PropertyQueryCondition<ENTITY> equal(short value) {
        return equal((long) value);
    }

    /** Creates a "not equal ('&lt;&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> notEqual(short value) {
        return notEqual((long) value);
    }

    /** Creates a "greater than ('&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> greater(short value) {
        return greater((long) value);
    }

    /** Creates a "less than ('&lt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> less(short value) {
        return less((long) value);
    }

    /** Creates an "BETWEEN ... AND ..." condition for this property. */
    public PropertyQueryCondition<ENTITY> between(short lowerBoundary, short upperBoundary) {
        return between((long) lowerBoundary, upperBoundary);
    }

    /** Creates an "equal ('=')" condition for this property. */
    public PropertyQueryCondition<ENTITY> equal(int value) {
        return equal((long) value);
    }

    /** Creates a "not equal ('&lt;&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> notEqual(int value) {
        return notEqual((long) value);
    }

    /** Creates a "greater than ('&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> greater(int value) {
        return greater((long) value);
    }

    /** Creates a "less than ('&lt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> less(int value) {
        return less((long) value);
    }

    /** Creates an "BETWEEN ... AND ..." condition for this property. */
    public PropertyQueryCondition<ENTITY> between(int lowerBoundary, int upperBoundary) {
        return between((long) lowerBoundary, upperBoundary);
    }

    /** Creates an "IN (..., ..., ...)" condition for this property. */
    public PropertyQueryCondition<ENTITY> oneOf(int[] values) {
        return new IntArrayCondition<>(this, IntArrayCondition.Operation.IN, values);
    }

    /** Creates a "NOT IN (..., ..., ...)" condition for this property. */
    public PropertyQueryCondition<ENTITY> notOneOf(int[] values) {
        return new IntArrayCondition<>(this, IntArrayCondition.Operation.NOT_IN, values);
    }

    /** Creates an "equal ('=')" condition for this property. */
    public PropertyQueryCondition<ENTITY> equal(long value) {
        return new LongCondition<>(this, LongCondition.Operation.EQUAL, value);
    }

    /** Creates a "not equal ('&lt;&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> notEqual(long value) {
        return new LongCondition<>(this, LongCondition.Operation.NOT_EQUAL, value);
    }

    /** Creates a "greater than ('&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> greater(long value) {
        return new LongCondition<>(this, LongCondition.Operation.GREATER, value);
    }

    /** Creates a "less than ('&lt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> less(long value) {
        return new LongCondition<>(this, LongCondition.Operation.LESS, value);
    }

    /** Creates an "BETWEEN ... AND ..." condition for this property. */
    public PropertyQueryCondition<ENTITY> between(long lowerBoundary, long upperBoundary) {
        return new LongLongCondition<>(this, LongLongCondition.Operation.BETWEEN, lowerBoundary, upperBoundary);
    }

    /** Creates an "IN (..., ..., ...)" condition for this property. */
    public PropertyQueryCondition<ENTITY> oneOf(long[] values) {
        return new LongArrayCondition<>(this, LongArrayCondition.Operation.IN, values);
    }

    /** Creates a "NOT IN (..., ..., ...)" condition for this property. */
    public PropertyQueryCondition<ENTITY> notOneOf(long[] values) {
        return new LongArrayCondition<>(this, LongArrayCondition.Operation.NOT_IN, values);
    }

    /**
     * Calls {@link #between(double, double)} with {@code value - tolerance} as lower bound and
     * {@code value + tolerance} as upper bound.
     */
    public PropertyQueryCondition<ENTITY> equal(double value, double tolerance) {
        return new DoubleDoubleCondition<>(this, DoubleDoubleCondition.Operation.BETWEEN,
                value - tolerance, value + tolerance);
    }

    /** Creates a "greater than ('&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> greater(double value) {
        return new DoubleCondition<>(this, DoubleCondition.Operation.GREATER, value);
    }

    /** Creates a "less than ('&lt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> less(double value) {
        return new DoubleCondition<>(this, DoubleCondition.Operation.LESS, value);
    }

    /** Creates an "BETWEEN ... AND ..." condition for this property. */
    public PropertyQueryCondition<ENTITY> between(double lowerBoundary, double upperBoundary) {
        return new DoubleDoubleCondition<>(this, DoubleDoubleCondition.Operation.BETWEEN,
                lowerBoundary, upperBoundary);
    }

    /** Creates an "equal ('=')" condition for this property. */
    public PropertyQueryCondition<ENTITY> equal(Date value) {
        return new LongCondition<>(this, LongCondition.Operation.EQUAL, value);
    }

    /** Creates a "not equal ('&lt;&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> notEqual(Date value) {
        return new LongCondition<>(this, LongCondition.Operation.NOT_EQUAL, value);
    }

    /** Creates a "greater than ('&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> greater(Date value) {
        return new LongCondition<>(this, LongCondition.Operation.GREATER, value);
    }

    /** Creates a "less than ('&lt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> less(Date value) {
        return new LongCondition<>(this, LongCondition.Operation.LESS, value);
    }

    /** Creates an "BETWEEN ... AND ..." condition for this property. */
    public PropertyQueryCondition<ENTITY> between(Date lowerBoundary, Date upperBoundary) {
        return new LongLongCondition<>(this, LongLongCondition.Operation.BETWEEN, lowerBoundary, upperBoundary);
    }

    /**
     * Creates an "equal ('=')" condition for this property.
     * <p>
     * Ignores case when matching results, e.g. {@code equal("example")} matches both "Example" and "example".
     * <p>
     * Use {@link #equal(String, StringOrder) equal(value, StringOrder.CASE_SENSITIVE)} to only match if case is equal.
     * <p>
     * Note: Use a case sensitive condition to utilize an {@link io.objectbox.annotation.Index @Index}
     * on {@code property}, dramatically speeding up look-up of results.
     *
     * @see #equal(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> equal(String value) {
        return new StringCondition<>(this, StringCondition.Operation.EQUAL, value);
    }

    /**
     * Creates an "equal ('=')" condition for this property.
     * <p>
     * Set {@code order} to {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to only match
     * if case is equal. E.g. {@code equal("example", StringOrder.CASE_SENSITIVE)} only matches "example",
     * but not "Example".
     * <p>
     * Note: Use a case sensitive condition to utilize an {@link io.objectbox.annotation.Index @Index}
     * on {@code property}, dramatically speeding up look-up of results.
     */
    public PropertyQueryCondition<ENTITY> equal(String value, StringOrder order) {
        return new StringCondition<>(this, StringCondition.Operation.EQUAL, value, order);
    }

    /**
     * Creates a "not equal ('&lt;&gt;')" condition for this property.
     * <p>
     * Ignores case when matching results, e.g. {@code notEqual("example")} excludes both "Example" and "example".
     * <p>
     * Use {@link #notEqual(String, StringOrder) notEqual(value, StringOrder.CASE_SENSITIVE)} to only exclude
     * if case is equal.
     * <p>
     * Note: Use a case sensitive condition to utilize an {@link io.objectbox.annotation.Index @Index}
     * on {@code property}, dramatically speeding up look-up of results.
     *
     * @see #notEqual(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> notEqual(String value) {
        return new StringCondition<>(this, StringCondition.Operation.NOT_EQUAL, value);
    }

    /**
     * Creates a "not equal ('&lt;&gt;')" condition for this property.
     * <p>
     * Set {@code order} to {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to only exclude
     * if case is equal. E.g. {@code notEqual("example", StringOrder.CASE_SENSITIVE)} only excludes "example",
     * but not "Example".
     * <p>
     * Note: Use a case sensitive condition to utilize an {@link io.objectbox.annotation.Index @Index}
     * on {@code property}, dramatically speeding up look-up of results.
     */
    public PropertyQueryCondition<ENTITY> notEqual(String value, StringOrder order) {
        return new StringCondition<>(this, StringCondition.Operation.NOT_EQUAL, value, order);
    }

    /**
     * Creates a "greater than ('&gt;')" condition for this property.
     * <p>
     * Ignores case when matching results. Use the overload and pass
     * {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to specify that case should not be ignored.
     *
     * @see #greater(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> greater(String value) {
        return new StringCondition<>(this, StringCondition.Operation.GREATER, value);
    }

    /**
     * Creates a "greater than ('&gt;')" condition for this property.
     */
    public PropertyQueryCondition<ENTITY> greater(String value, StringOrder order) {
        return new StringCondition<>(this, StringCondition.Operation.GREATER, value, order);
    }

    /**
     * Creates a "less than ('&lt;')" condition for this property.
     * <p>
     * Ignores case when matching results. Use the overload and pass
     * {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to specify that case should not be ignored.
     *
     * @see #less(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> less(String value) {
        return new StringCondition<>(this, StringCondition.Operation.LESS, value);
    }

    /**
     * Creates a "less than ('&lt;')" condition for this property.
     */
    public PropertyQueryCondition<ENTITY> less(String value, StringOrder order) {
        return new StringCondition<>(this, StringCondition.Operation.LESS, value, order);
    }

    /**
     * Ignores case when matching results. Use the overload and pass
     * {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to specify that case should not be ignored.
     *
     * @see #contains(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> contains(String value) {
        return new StringCondition<>(this, StringCondition.Operation.CONTAINS, value);
    }

    public PropertyQueryCondition<ENTITY> contains(String value, StringOrder order) {
        return new StringCondition<>(this, StringCondition.Operation.CONTAINS, value, order);
    }

    /**
     * Ignores case when matching results. Use the overload and pass
     * {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to specify that case should not be ignored.
     *
     * @see #startsWith(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> startsWith(String value) {
        return new StringCondition<>(this, Operation.STARTS_WITH, value);
    }

    public PropertyQueryCondition<ENTITY> startsWith(String value, StringOrder order) {
        return new StringCondition<>(this, Operation.STARTS_WITH, value, order);
    }

    /**
     * Ignores case when matching results. Use the overload and pass
     * {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to specify that case should not be ignored.
     *
     * @see #endsWith(String, StringOrder)
     */
    public PropertyQueryCondition<ENTITY> endsWith(String value) {
        return new StringCondition<>(this, Operation.ENDS_WITH, value);
    }

    public PropertyQueryCondition<ENTITY> endsWith(String value, StringOrder order) {
        return new StringCondition<>(this, Operation.ENDS_WITH, value, order);
    }

    /**
     * Creates an "IN (..., ..., ...)" condition for this property.
     * <p>
     * Ignores case when matching results. Use the overload and pass
     * {@link StringOrder#CASE_SENSITIVE StringOrder.CASE_SENSITIVE} to specify that case should not be ignored.
     *
     * @see #oneOf(String[], StringOrder)
     */
    public PropertyQueryCondition<ENTITY> oneOf(String[] values) {
        return new StringArrayCondition<>(this, StringArrayCondition.Operation.IN, values);
    }

    /**
     * Creates an "IN (..., ..., ...)" condition for this property.
     */
    public PropertyQueryCondition<ENTITY> oneOf(String[] values, StringOrder order) {
        return new StringArrayCondition<>(this, StringArrayCondition.Operation.IN, values, order);
    }

    /** Creates an "equal ('=')" condition for this property. */
    public PropertyQueryCondition<ENTITY> equal(byte[] value) {
        return new ByteArrayCondition<>(this, ByteArrayCondition.Operation.EQUAL, value);
    }

    /** Creates a "greater than ('&gt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> greater(byte[] value) {
        return new ByteArrayCondition<>(this, ByteArrayCondition.Operation.GREATER, value);
    }

    /** Creates a "less than ('&lt;')" condition for this property. */
    public PropertyQueryCondition<ENTITY> less(byte[] value) {
        return new ByteArrayCondition<>(this, ByteArrayCondition.Operation.LESS, value);
    }

    @Internal
    public int getEntityId() {
        return entity.getEntityId();
    }

    @Internal
    public int getId() {
        if (this.id <= 0) {
            throw new IllegalStateException("Illegal property ID " + id + " for " + toString());
        }
        return id;
    }

    boolean isIdVerified() {
        return idVerified;
    }

    void verifyId(int idInDb) {
        if (this.id <= 0) {
            throw new IllegalStateException("Illegal property ID " + id + " for " + toString());
        }
        if (this.id != idInDb) {
            throw new DbException(toString() + " does not match ID in DB: " + idInDb);
        }
        idVerified = true;
    }

    @Override
    public String toString() {
        return "Property \"" + name + "\" (ID: " + id + ")";
    }
}
