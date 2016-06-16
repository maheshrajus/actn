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
package org.onosproject.pcep.controller;

import org.onlab.packet.IpAddress;

import java.util.TreeMap;

/**
 * Abstraction of an PCEP configuration. Manages the BGP configuration from CLI to the BGP controller.
 */
public interface PcepCfg {

    /**
     * Set configuration data.
     *
     * @param ipAddress IP address
     * @param pcepCfgdata configuration data
     */
    void setPcepCfgData(Integer asNumber, PcepCfgData pcepCfgdata);

    /**
     * Get the configuration data.
     *
     * @return configuration data
     */
    PcepCfgData pcepCfgData(Integer asNumber);

    /**
     * Set LSP type.
     *
     * @param lspType LSP type
     */
    void setLspType(int lspType);

    /**
     * Get the LSP type.
     *
     * @return LSP type
     */
    int lspType();

    boolean isIpConfigured(IpAddress ipAddress);

    /**
     * Returns the configurations.
     *
     * @return configurations
     */
    TreeMap<Integer, PcepCfgData> pcepDomainMap();

    /**
     * Adds entry in to local data store.
     *
     * @param ipAddress IP address
     * @param asNumber AS number
     */
    void addConfig(IpAddress ipAddress, int asNumber);

    /**
     * Removes entry from local data store.
     *
     * @param asNumber As number
     */
    void deleteConfig(Integer asNumber);
}
