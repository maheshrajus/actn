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

import org.onlab.util.Bandwidth;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * virtual network bandwidth constraint.
 */
public final class VnBandwidth implements VnConstraint {

    public static final byte TYPE = 2;
    private final Bandwidth bandWidthValue;

    // Constructor for serialization
    private VnBandwidth() {
        bandWidthValue = null;
    }
    /**
     * Creates a new bandwidth constraint.
     *
     * @param bandWidthValue shared bandwidth of the links
     */
    public VnBandwidth(Bandwidth bandWidthValue) {
        this.bandWidthValue = bandWidthValue;
    }

    /**
     * Creates a new SharedBandwidth constraint.
     *
     * @param bandWidthValue shared bandwidth of the links
     * @return SharedBandwidth instance
     */
    public static VnBandwidth of(Bandwidth bandWidthValue) {
        return new VnBandwidth(bandWidthValue);
    }

    /**
     * Obtains bandwidth of the links.
     *
     * @return bandwidth
     */
    public Bandwidth bandWidthValue() {
        return bandWidthValue;
    }

    @Override
    public short getType() {
        return TYPE;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bandWidthValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof VnBandwidth) {
            VnBandwidth other = (VnBandwidth) obj;
            return Objects.equals(this.bandWidthValue, other.bandWidthValue);
        }

        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("bandWidthValue", bandWidthValue)
                .toString();
    }
}
