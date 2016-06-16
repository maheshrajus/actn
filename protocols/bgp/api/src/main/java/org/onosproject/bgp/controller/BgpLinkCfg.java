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
package org.onosproject.bgp.controller;

import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;

/**
 * Abstraction of an BGP configuration. Manages the BGP configuration from CLI to the BGP controller.
 */
public interface BgpLinkCfg {
    /**
     * Returns source device ID.
     *
     * @return
     */
    DeviceId srcDeviceId();

    /**
     * Returns source interface.
     *
     * @return
     */
    IpAddress srcInterface();

    /**
     * Returns source port.
     *
     * @return
     */
    Integer srcPort();

    /**
     * Returns destination port.
     *
     * @return
     */
    DeviceId dstDeviceId();

    /**
     * Returns destination interface.
     *
     * @return
     */
    IpAddress dstInterface();

    /**
     * Retursn destination port.
     *
     * @return
     */
    Integer dstPort();

    /**
     * Returns max reserved bandwidth.
     *
     * @return
     */
    Double maxReservedBandwidth();
}
