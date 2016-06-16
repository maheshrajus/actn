/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.pcep.controller.impl;

import org.onlab.packet.IpAddress;
import org.onosproject.pcep.controller.PcepCfg;
import org.onosproject.pcep.controller.PcepCfgData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * Provides PCEP configuration.
 */
public class PcepConfig implements PcepCfg {

    protected static final Logger log = LoggerFactory.getLogger(PcepConfig.class);

    private int lspType;
    private TreeMap<Integer, PcepCfgData> pcepDomainMap = new TreeMap<>();

    /*
     * Constructor to initialize the values.
     */
    public PcepConfig() {
    }

    @Override
    public TreeMap<Integer, PcepCfgData> pcepDomainMap() {
        return this.pcepDomainMap;
    }

    @Override
    public boolean isIpConfigured(IpAddress ipAddress) {
        PcepCfgData cfgData = this.pcepDomainMap.get(ipAddress.toString());
        return !(cfgData == null);
        //return (cfgData != null) ? true : false;
    }

    @Override
    public PcepCfgData pcepCfgData(Integer asNumber) {
        PcepCfgData cfgData = this.pcepDomainMap.get(asNumber);
        return cfgData;
    }

    @Override
    public void setPcepCfgData(Integer asNumber, PcepCfgData pcepCfgdata) {
        pcepDomainMap.replace(asNumber, pcepCfgdata);
    }

    @Override
    public void setLspType(int lspType) {
        this.lspType = lspType;
    }

    public int lspType() {
        return lspType;
    }

    @Override
    public void addConfig(IpAddress ipAddress, int asNumber) {
        PcepCfgData cfgData = new PcepConfigData(ipAddress, asNumber);
        pcepDomainMap.put(asNumber, cfgData);
        return;
    }

    @Override
    public void deleteConfig(Integer asNumber) {
        pcepDomainMap.remove(asNumber);
        return;
    }
}
