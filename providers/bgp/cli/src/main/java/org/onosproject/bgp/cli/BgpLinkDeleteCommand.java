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
 * Lists ISIS neighbors, database and interfaces details.
 */
@Command(scope = "onos", name = "BGP-DeleteLink", description = "Delete BGP link")
public class BgpLinkDeleteCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(BgpLinkDeleteCommand.class);

    protected BgpController bgpController;
    @Argument(index = 0, name = "sourceDeviceId", description = "Source device ID", required = true,
                                                                                    multiValued = false)
    String srcDeviceId = null;
    @Argument(index = 1, name = "sourceInterface", description = "Source interface address", required = true,
                                                                                    multiValued = false)
    String srcInterface = null;
    @Argument(index = 2, name = "sourcePort", description = "Source port", required = true,
                                                                                     multiValued = false)
    Integer srcPort = null;
    @Argument(index = 3, name = "destinationDeviceId", description = "Destination device ID",
                                                                    required = true, multiValued = false)
    String dstDeviceId = null;
    @Argument(index = 4, name = "destinationInterface", description = "Destination interface",
                                                                    required = true, multiValued = false)
    String dstInterface = null;
    @Argument(index = 5, name = "destinationPort", description = "Destination port", required = true,
                                                                                     multiValued = false)
    Integer dstPort = null;

    @Activate
    public void activate() {
        log.debug("Activated");
    }

    @Deactivate
    public void deactivate() {
        log.debug("Deactivated");
    }

    @Override
    protected void execute() {
        log.info("executing BGP delete link");

        this.bgpController = get(BgpController.class);
        BgpCfg bgpCfg = bgpController.getConfig();
        bgpCfg.deleteLink(DeviceId.deviceId(srcDeviceId), IpAddress.valueOf(srcInterface), (Integer) srcPort,
                       DeviceId.deviceId(dstDeviceId), IpAddress.valueOf(dstInterface), (Integer) dstPort);
    }
}
