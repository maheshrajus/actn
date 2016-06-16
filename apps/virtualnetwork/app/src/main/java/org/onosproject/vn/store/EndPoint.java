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
package org.onosproject.vn.store;

import com.google.common.base.MoreObjects;
import org.onosproject.net.DeviceId;

import java.util.List;
import java.util.Objects;

//import org.onosproject.net.DeviceId;

/**
 * Input path information to compute CSPF path.
 * This path information will be stored in pce store and will be used later to recalculate the path.
 */
public final class EndPoint {

    private List<DeviceId> src;
    private List<DeviceId> dst;

    /**
     * Initialization of member variables.
     *
     * @param src soure end point
     * @param dst destination end point
     */
    public EndPoint(List<DeviceId> src, List<DeviceId> dst) {
       this.src = src;
       this.dst = dst;
    }

    /**
     * Returns source device id.
     *
     * @return source device id
     */
    public List<DeviceId> src() {
       return src;
    }

    /**
     * Returns destination device id.
     *
     * @return destination device id
     */
    public List<DeviceId> dst() {
       return dst;
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof EndPoint) {
            final EndPoint other = (EndPoint) obj;
            return Objects.equals(this.src, other.src) &&
                    Objects.equals(this.dst, other.dst);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .omitNullValues()
                .add("src", src.toString())
                .add("dst", dst.toString())
                .toString();
    }
}
