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
package org.onosproject.bgp.controller.impl;

import org.onlab.packet.IpAddress;
import org.onosproject.bgp.controller.BgpLinkCfg;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides BGP link configuration.
 */
public class BgpLinkConfig implements BgpLinkCfg {

    protected static final Logger log = LoggerFactory.getLogger(BgpLinkConfig.class);
    private DeviceId srcDeviceId;
    private IpAddress srcInterface;
    private Integer srcPort;
    private DeviceId dstDeviceId;
    private IpAddress dstInterface;
    private Integer dstPort;
    private Double maxReservedBandwidth;

    /*
     * Constructor to initialize the values.
     */
    public BgpLinkConfig(DeviceId srcDeviceId, IpAddress srcInterface, Integer srcPort, DeviceId dstDeviceId,
                         IpAddress dstInterface,
                         Integer dstPort, Double maxReservedBandwidth) {
    this.srcDeviceId = srcDeviceId;
    this.srcInterface = srcInterface;
    this.srcPort = srcPort;
    this.dstDeviceId = dstDeviceId;
    this.dstInterface = dstInterface;
    this.dstPort = dstPort;
    this.maxReservedBandwidth = maxReservedBandwidth;
    }

    @Override
    public DeviceId srcDeviceId() {
        return srcDeviceId;
    }

    @Override
    public Integer srcPort() {
        return srcPort;
    }

    @Override
    public DeviceId dstDeviceId() {
        return dstDeviceId;
    }
    @Override
    public IpAddress srcInterface() {
        return srcInterface;
    }

    @Override
    public IpAddress dstInterface() {
        return dstInterface;
    }

    @Override
    public Integer dstPort() {
        return dstPort;
    }

    @Override
    public Double maxReservedBandwidth() {
        return maxReservedBandwidth;
    }
}
