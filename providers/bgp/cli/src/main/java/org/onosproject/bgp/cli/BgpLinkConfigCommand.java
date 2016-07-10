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
package org.onosproject.bgp.cli;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.bgp.controller.BgpCfg;
import org.onosproject.bgp.controller.BgpController;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure BGP link.
 */
@Command(scope = "onos", name = "Bgp-link", description = "BGP link configuration command")
public class BgpLinkConfigCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(BgpLinkConfigCommand.class);
    protected BgpController bgpController;

    @Argument(index = 0, name = "sourceDeviceId", description = "Source device ID", required = true,
                                                                                    multiValued = false)
    String srcDeviceId = null;
    @Argument(index = 1, name = "sourceInterface", description = "Source interface address",
                                                                    required = true, multiValued = false)
    String srcInterface = null;
    @Argument(index = 2, name = "destinationDeviceId", description = "Destination device ID", required = true,
                                                                                     multiValued = false)
    String dstDeviceId = null;
    @Argument(index = 3, name = "destinationInterface", description = "Destination interface",
                                                                    required = true, multiValued = false)
    String dstInterface = null;
    @Argument(index = 4, name = "cost", description = "cost", required = true,
            multiValued = false)
    Integer cost = null;

    @Argument(index = 5, name = "teCost", description = "TE cost", required = true,
                                                                                     multiValued = false)
    Integer teCost = null;
    @Argument(index = 6, name = "maxReservablebandwidth", description = "Max reservable Link bandwidth",
                                                          required = false, multiValued = false)
    Double maxReservableBandWidth = null;

    @Argument(index = 7, name = "maxBandwidth", description = "max Link bandwidth", required = false,
                                                           multiValued = false)
    Double maxBandWidth = null;

    @Argument(index = 8, name = "unReservedBandwidth", description = "Unreserved Link bandwidth", required = false,
                                                           multiValued = false)
    Double unReservedBandWidth = null;

    @Activate
    public void activate() {
        log.debug("Activated...!!!");
    }

    @Deactivate
    public void deactivate() {
        log.debug("Deactivated...!!!");
    }

   @Override
    protected void execute() {
        log.info("Configure BGP link");
        this.bgpController = get(BgpController.class);
        BgpCfg bgpCfg = bgpController.getConfig();
        bgpCfg.addLink(DeviceId.deviceId(srcDeviceId), IpAddress.valueOf(srcInterface), cost,
                       DeviceId.deviceId(dstDeviceId), IpAddress.valueOf(dstInterface), teCost,
                       maxReservableBandWidth, maxBandWidth, unReservedBandWidth);
    }
}
