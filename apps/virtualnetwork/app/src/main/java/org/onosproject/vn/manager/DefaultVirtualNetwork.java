/*
 * Copyright 2016 Open Networking Laboratory
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

package org.onosproject.vn.manager;

import org.onlab.util.Bandwidth;
import org.onosproject.net.DeviceId;
import org.onosproject.vn.manager.constraint.VnBandwidth;
import org.onosproject.vn.manager.constraint.VnConstraint;
import org.onosproject.vn.manager.constraint.VnCost;
import org.onosproject.vn.store.EndPoint;
import org.onosproject.vn.store.VirtualNetworkInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Implementation of an entity which provides functionalities of virtual network.
 */
public final class DefaultVirtualNetwork implements VirtualNetwork {

    private String vnName;
    private VnConstraint cost; // cost constraint
    private VnConstraint bandwidth; // bandwidth constraint
    private List<String> source; // Ingress
    private List<String> destination; // Egress
    private EndPoint endPoint;

    /**
     * Initializes virtual network attributes.
     *
     * @param vnName virtual network name
     * @param cost cost constraint
     * @param bandwidth bandwidth constraint
     * @param endPoint end point
     */
    private DefaultVirtualNetwork(String vnName, VnConstraint cost, VnConstraint bandwidth, EndPoint endPoint) {

        this.vnName = vnName;
        this.cost = cost;
        this.bandwidth = bandwidth;
        this.endPoint = endPoint;
    }

    @Override
    public String vnName() {
        return vnName;
    }

    @Override
    public VnConstraint cost() {
        return cost;
    }

    @Override
    public VnConstraint bandwidth() {
        return bandwidth;
    }

    @Override
    public List<String> source() {
        return source;
    }

    @Override
    public void source(List<String> src) {
        this.source = src;
    }

    @Override
    public EndPoint endPoint() {
        return endPoint;
    }

    @Override
    public List<String> destination() {
        return destination;
    }

    @Override
    public void destination(List<String> dst) {
        this.destination = dst;
    }

    @Override
    public VirtualNetwork copy(VirtualNetwork vn) {
        if (null != vn.vnName()) {
            this.vnName = vn.vnName();
        }

        if (null != vn.cost()) {
            this.cost = vn.cost();
        }

        if (null != vn.bandwidth()) {
            this.bandwidth = vn.bandwidth();
        }

        if (null != vn.source()) {
            this.source = vn.source();
        }
        if (null != vn.destination()) {
            this.destination = vn.destination();
        }

        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vnName, cost, bandwidth, source, destination);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DefaultVirtualNetwork) {
            DefaultVirtualNetwork that = (DefaultVirtualNetwork) obj;
            return Objects.equals(vnName, that.vnName)
                    && Objects.equals(cost, that.cost)
                    && Objects.equals(bandwidth, that.bandwidth)
                    && Objects.equals(source, that.source)
                    && Objects.equals(destination, that.destination);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("vnName", vnName)
                .add("cost", cost)
                .add("bandwidth", bandwidth)
                .add("source", source)
                .add("destination", destination)
                .toString();
    }

    /**
     * Creates an instance of the virtual network builder.
     *
     * @return instance of builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for virtual network.
     */
    public static final class Builder implements VirtualNetwork.Builder {
        private String vnName;
        //private List<VnConstraint> constraints;
        private VnConstraint cost; // cost constraint
        private VnConstraint bandwidth; // bandwidth constraint
        private List<String> source;
        private List<String> destination;

        @Override
        public Builder vnName(String vnName) {
            this.vnName = vnName;
            return this;
        }

        /*@Override
        public Builder cost(String cost) {
            if (null != cost) {
                this.cost = VnCost.of(VnCost.Type.values()[(Integer.valueOf(cost) - 1)], 1.0);
            }
            return this;
        }*/

        @Override
        public Builder bandwidth(String bandwidth) {
            if (null != cost) {
                this.bandwidth = VnBandwidth.of(Bandwidth.bps(Double.valueOf(bandwidth)));
            }
            return this;
        }

        @Override
        public Builder source(List<String> source) {
            this.source = source;
            return this;
        }

        @Override
        public Builder destination(List<String> destination) {
            this.destination = destination;
            return this;
        }

        @Override
        public VirtualNetwork.Builder cost(String costType) {
            if (null != costType) {
                this.cost = VnCost.of(VnCost.Type.values()[(Integer.valueOf(costType) - 1)]);
            }
            return this;
        }

        @Override
        public Builder of(VirtualNetworkInfo vn) {
            this.source = new LinkedList<>();
            this.destination = new LinkedList<>();

            this.vnName = vn.vnName();

            source.addAll(vn.endPoint().src().stream().map(DeviceId::toString).collect(Collectors.toList()));

            destination.addAll(vn.endPoint().dst().stream().map(DeviceId::toString).collect(Collectors.toList()));

            for (VnConstraint c : vn.constraints()) {
                if (c.getType() == VnBandwidth.TYPE) {
                    VnBandwidth vnBandwidth = (VnBandwidth) c;
                    this.bandwidth = vnBandwidth;
                } else if (c.getType() == VnCost.TYPE) {
                    VnCost vnCost = (VnCost) c;
                    this.cost = vnCost;
                }
            }
            return this;
        }

        @Override
        public VirtualNetwork build() {
            List<DeviceId> srcDeviceId = new LinkedList<>();
            List<DeviceId> dstDeviceId = new LinkedList<>();

            if (source != null) {
                srcDeviceId.addAll(source.stream().map(DeviceId::deviceId).collect(Collectors.toList()));
            }

            if (destination != null) {
                dstDeviceId.addAll(destination.stream().map(DeviceId::deviceId).collect(Collectors.toList()));
            }

            return new DefaultVirtualNetwork(vnName, cost, bandwidth,
                                             new EndPoint(srcDeviceId, dstDeviceId));
        }
    }
}
