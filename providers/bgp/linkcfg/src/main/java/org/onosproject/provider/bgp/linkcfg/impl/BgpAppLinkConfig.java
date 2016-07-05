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
package org.onosproject.provider.bgp.linkcfg.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onlab.packet.IpAddress;
import org.onosproject.bgp.controller.BgpCfg;
import org.onosproject.bgp.controller.BgpController;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configuration object for BGP.
 */
public class BgpAppLinkConfig extends Config<ApplicationId> {
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    BgpController bgpController;

    BgpCfg bgpConfig = null;

    public static final String SRC_DEVICEID = "sourceDeviceId";
    public static final String SRC_INTERFACE = "sourceInterface";
    public static final String SRC_PORT = "sourcePort";
    public static final String DST_DEVICEID = "destinationDeviceId";
    public static final String DST_INTERFACE = "destinationInterface";
    public static final String DST_PORT = "destinationPort";
    public static final String MAX_RESERVED_BANDWIDTH = "maxReservableBandwidth";
    public static final String MAX_BANDWIDTH = "maxBandwidth";
    public static final String UN_RESERVED_BANDWIDTH = "unReservedBandwidth";

    public static final String BGP_LINK = "bgpLink";

    @Override
    public boolean isValid() {
        boolean fields = false;

        this.bgpController = DefaultServiceDirectory.getService(BgpController.class);
        bgpConfig = bgpController.getConfig();

        fields = hasOnlyFields(BGP_LINK);

        if (!fields) {
            return fields;
        }

        return validateBgpConfiguration();
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public String sourceDeviceId() {
        return get(SRC_DEVICEID, null);
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public IpAddress sourceInterface() {
        return IpAddress.valueOf(get(SRC_INTERFACE, null));
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public Short sourcePort() {
        return Short.parseShort(get(SRC_PORT, null));
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public String destinationDeviceId() {
        return get(DST_DEVICEID, null);
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public IpAddress destinationInterface() {
        return IpAddress.valueOf(get(DST_INTERFACE, null));
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public Short destinationPort() {
        return Short.parseShort(get(DST_PORT, null));
    }

    /**
     * Returns max reserved bandwidth.
     *
     * @return amxx reserved bandwidth
     */
    public Double maxReservedBandWidth() {
        return Double.parseDouble(get(MAX_RESERVED_BANDWIDTH, null));
    }

    /**
     * Returns reserved bandwidth.
     *
     * @return max reserved bandwidth
     */
    public Double maxBandWidth() {
        return Double.parseDouble(get(MAX_BANDWIDTH, null));
    }


    /**
     * Returns unreserved bandwidth.
     *
     * @return unreserved bandwidth
     */
    public Double unReservedBandWidth() {
        return Double.parseDouble(get(UN_RESERVED_BANDWIDTH, null));
    }

    /**
     * Validates the Bgp local and peer configuration.
     *
     * @return true if valid else false
     */
    public boolean validateBgpConfiguration() {

        if (!validateBgpLinks()) {
            return false;
        }

        return true;
    }

    /**
     * Validates the Bgp link configuration.
     *
     * @return true if valid else false
     */
    public boolean validateBgpLinks() {
        List<BgpLinkConfig> nodes;
        String connectMode;

        nodes = bgpLinks();
        for (int i = 0; i < nodes.size(); i++) {
            if ((nodes.get(i).srcDeviceId == null) || (nodes.get(i).srcInterface == null)
              || (nodes.get(i).srcPort == null)
              || (nodes.get(i).dstDeviceId == null) || (nodes.get(i).dstInterface == null)
              || (nodes.get(i).dstPort == null)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the set of nodes read from network config.
     *
     * @return list of BgpLinkConfig or null
     */
    public List<BgpLinkConfig> bgpLinks() {
        List<BgpLinkConfig> nodes = new ArrayList<BgpLinkConfig>();

        JsonNode jsonNodes = object.get(BGP_LINK);
        if (jsonNodes == null) {
            return null;
        }

        jsonNodes.forEach(jsonNode -> nodes.add(new BgpLinkConfig(
                jsonNode.path(SRC_DEVICEID).asText(),
                IpAddress.valueOf(jsonNode.path(SRC_INTERFACE).asText()),
                jsonNode.path(SRC_PORT).asInt(),
                jsonNode.path(DST_DEVICEID).asText(),
                IpAddress.valueOf(jsonNode.path(DST_INTERFACE).asText()),
                jsonNode.path(DST_PORT).asInt(),
                jsonNode.path(MAX_RESERVED_BANDWIDTH).asDouble(),
                jsonNode.path(MAX_BANDWIDTH).asDouble(),
                jsonNode.path(UN_RESERVED_BANDWIDTH).asDouble())));

        return nodes;
    }

    /**
     * Configuration for Bgp link nodes.
     */
    public static class BgpLinkConfig {

        private final String srcDeviceId;
        private final IpAddress srcInterface;
        private final Integer srcPort;
        private final String dstDeviceId;
        private final IpAddress dstInterface;
        private final Integer dstPort;
        private final Double maxReservedBandwidth;
        private final Double maxBandwidth;
        private final Double unReservedBandwidth;

        public BgpLinkConfig(String srcDeviceId, IpAddress srcInterface, Integer srcPort, String dstDeviceId,
                             IpAddress dstInterface, Integer dstPort,
                             Double maxReservedBandwidth,
                             Double maxBandwidth,
                             Double unReservedBandwidth) {
            this.srcDeviceId = checkNotNull(srcDeviceId);
            this.srcInterface = srcInterface;
            this.srcPort = srcPort;
            this.dstDeviceId = checkNotNull(dstDeviceId);
            this.dstInterface = dstInterface;
            this.dstPort = dstPort;
            this.maxReservedBandwidth = maxReservedBandwidth;
            this.maxBandwidth = maxBandwidth;
            this.unReservedBandwidth = unReservedBandwidth;
        }

        public String srcDeviceId() {
            return srcDeviceId;
        }

        public IpAddress srcInterface() {
            return srcInterface;
        }

        public Integer srcPort() {
            return srcPort;
        }

        public String dstDeviceId() {
            return dstDeviceId;
        }

        public IpAddress dstInterface() {
            return dstInterface;
        }

        public Integer dstPort() {
            return dstPort;
        }

        public Double maxReservedBandwidth() {
            return maxReservedBandwidth;
        }

        public Double maxBandwidth() {
            return maxBandwidth;
        }

        public Double unReservedBandwidth() {
            return unReservedBandwidth;
        }
    }
}
