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
import org.onosproject.net.intent.Constraint;
import org.onosproject.vn.api.VnEndPoints;

import java.util.List;
import java.util.Objects;

/**
 * Input path information to compute CSPF path.
 * This path information will be stored in vn store and will be used later to recalculate the path.
 */
public final class VirtualNetworkInfo {

    private String vnName;
    private List<Constraint> constraints;
    private VnEndPoints endPoint;


    public void setVnName(String vnName) {
        this.vnName = vnName;
    }

    /**
     * Initialization of member variables.
     *
     * @param vnName virtual network name
     * @param constraints  constraints
     * @param endPoint end point
     */
    public VirtualNetworkInfo(String vnName, List<Constraint> constraints, VnEndPoints endPoint) {
       this.vnName = vnName;
       this.constraints = constraints;
       this.endPoint = endPoint;
    }

    /**
     * Initialization of member variables.
     */
    public VirtualNetworkInfo() {

    }

    /**
     * Returns end points.
     *
     * @return end points
     */
    public VnEndPoints endPoint() {
       return endPoint;
    }

    public void setEndPoint(VnEndPoints endPoint) {
        this.endPoint = endPoint;
    }
    /**
     * Returns constraints.
     *
     * @return constraints
     */
    public List<Constraint> constraints() {
       return constraints;
    }

    public void setConstraint(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    /**
     * Returns virtual network name.
     *
     * @return virtual network name
     */
    public String vnName() {
       return vnName;
    }
    @Override
    public int hashCode() {
        return Objects.hash(vnName, constraints, endPoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VirtualNetworkInfo) {
            final VirtualNetworkInfo other = (VirtualNetworkInfo) obj;
            return Objects.equals(this.vnName, other.vnName) &&
                    Objects.equals(this.constraints, other.constraints) &&
                    Objects.equals(this.endPoint, other.endPoint);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .omitNullValues()
                .add("vnName", vnName.toString())
                .add("constraints", constraints)
                .add("endPoint", endPoint)
                .toString();
    }
}
