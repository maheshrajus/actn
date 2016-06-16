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
package org.onosproject.provider.pcep.cfg.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;
import org.onosproject.pcep.controller.PcepCfg;
import org.onosproject.pcep.controller.PcepClientController;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configuration object for PCEP.
 */
public class PcepAppConfig extends Config<ApplicationId> {
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    PcepClientController pcepController;

    PcepCfg pcepConfig = null;
    public static final String LSP_TYPE = "lspType";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String AS_NUMBER = "asNumber";
    public static final String PCEP_DOMAIN_MAP = "pcepDomainMap";
    static final long MAX_LONG_AS_NUMBER = 4294967295L;

    @Override
    public boolean isValid() {
        boolean fields = false;

        this.pcepController = DefaultServiceDirectory.getService(PcepClientController.class);
        pcepConfig = pcepController.getConfig();

        fields = hasOnlyFields(LSP_TYPE, PCEP_DOMAIN_MAP);

        if (!fields) {
            return fields;
        }

        return validatePcepConfiguration();
    }

    /**
     * Returns routerId from the configuration.
     *
     * @return routerId
     */
    public String ipAddress() {
        return get(IP_ADDRESS, null);
    }

    /**
     * Returns localAs number from the configuration.
     *
     * @return local As number
     */
    public int asNumber() {
        return Integer.parseInt(get(AS_NUMBER, null));
    }

    /**
     * Returns LSP type.
     *
     * @return LSP type
     */
    public String lspType() {
        return get(LSP_TYPE, null);
    }

    /**
     * Validates the PCEP and peer configuration.
     *
     * @return true if valid else false
     */
    public boolean validatePcepConfiguration() {

        if (!validatePcepDomain()) {
            return false;
        }
        if (!validateLspType()) {
            return false;
        }
        return true;
    }

    /**
     * Validates the PCEP peer configuration.
     *
     * @return true if valid else false
     */
    public boolean validatePcepDomain() {
        List<PcepConfig> nodes;

        nodes = pcepDomainMap();
        for (int i = 0; i < nodes.size(); i++) {
            if ((IpAddress.valueOf(nodes.get(i).ipAddress()) == null) ||
                    !validateAsNumber(nodes.get(i).asNumber())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the Bgp As number.
     *
     * @return true if valid else false
     */
    public boolean validateAsNumber(int asNumber) {

        if (asNumber == 0 || asNumber >= MAX_LONG_AS_NUMBER) {
            return false;
        }

        return true;
    }

    /**
     * Validates the LSP type.
     *
     * @return true if valid else false
     */
    public boolean validateLspType() {
        String lspType;
        lspType = lspType();

        if (lspType == null) {
            return true;
        }

        if (!lspType.equals("SR") && !lspType.equals("CR") && !lspType.equals("RSVPTE")) {
            return false;
        }

        return true;
    }


    /**
     * Returns the set of nodes read from network config.
     *
     * @return list of BgpPeerConfig or null
     */
    public List<PcepConfig> pcepDomainMap() {
        List<PcepConfig> nodes = new ArrayList<PcepConfig>();

        JsonNode jsonNodes = object.get(PCEP_DOMAIN_MAP);
        if (jsonNodes == null) {
            return null;
        }

        jsonNodes.forEach(jsonNode -> nodes.add(new PcepConfig(
                jsonNode.path(IP_ADDRESS).asText(),
                jsonNode.path(AS_NUMBER).asInt())));

        return nodes;
    }

    /**
     * Configuration for PCEP nodes.
     */
    public static class PcepConfig {

        private final String ipAddress;
        private final int asNumber;

        public PcepConfig(String ipAddress, int asNumber) {
            this.ipAddress = checkNotNull(ipAddress);
            this.asNumber = asNumber;
        }

        /**
         * Returns IP address of the peer node.
         *
         * @return IP address
         */
        public String ipAddress() {
            return this.ipAddress;
        }

        /**
         * Returns asNumber.
         *
         * @return asNumber
         */
        public int asNumber() {
            return this.asNumber;
        }
    }
}
