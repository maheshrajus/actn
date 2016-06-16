/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.vn.manager.constraint;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Constraint that evaluates whether cost for a link is available, if yes return cost for that link.
 */
public final class VnCost implements VnConstraint {

    /**
     * Represents about cost types.
     */
    public enum Type {
        /**
         * Signifies that cost is IGP cost.
         */
        COST(1),

        /**
         * Signifies that cost is TE cost.
         */
        TE_COST(2);

        int value;

        /**
         * Assign val with the value as the Cost type.
         *
         * @param val Cost type
         */
        Type(int val) {
            value = val;
        }

        /**
         * Returns value of Cost type.
         *
         * @return Cost type
         */
        public byte type() {
            return (byte) value;
        }
    }

    private Type type;
    public static final String TE_COST = "teCost";
    public static final String COST = "cost";
    private Double cost;
    public static final byte TYPE = 1;

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double cost() {
        return cost;
    }

    // Constructor for serialization
    private VnCost() {
        this.type = null;
        this.cost = null;
    }

    /**
     * Creates a new cost constraint.
     *
     * @param type of a link
     * @param cost of a link
     */
    public VnCost(Type type, Double cost) {
        this.type = checkNotNull(type, "Type cannot be null");
        this.cost = checkNotNull(cost, "Cost cannot be null");
    }

    /**
     * Creates new CostConstraint with specified cost type.
     *
     * @param type of cost
     * @param cost of a link
     * @return instance of CostConstraint
     */
    public static VnCost of(Type type, Double cost) {
        return new VnCost(type, cost);
    }

    /**
     * Returns the type of a cost specified in a constraint.
     *
     * @return required cost type
     */
    public Type type() {
        return type;
    }

    @Override
    public short getType() {
        return TYPE;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, cost);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof VnCost) {
            VnCost other = (VnCost) obj;
            return Objects.equals(this.type, other.type) && 
                   Objects.equals(this.cost, other.cost);
        }

        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("type", type)
                .add("cost", cost)
                .toString();
    }
}
