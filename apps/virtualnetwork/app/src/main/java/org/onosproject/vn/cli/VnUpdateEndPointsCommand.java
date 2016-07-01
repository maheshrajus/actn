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
package org.onosproject.vn.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.vn.vnservice.api.VnService;
import org.onosproject.vn.store.EndPoint;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Supports creating the virtual network.
 */
@Command(scope = "onos", name = "vn-updatEndPoints", description = "Supports updating virtual network end points.")
public class VnUpdateEndPointsCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Argument(index = 0, name = "vnName", description = "virtual network name.", required = true,
                                                                                 multiValued = false)
    String vnName = null;

    @Argument(index = 1, name = "endPoint", description = "End Point source and destination " +
            "should be split with '/' delimiter (e.g., L3:device1 L3:device2 / L3:device3 L3:device4).",
            required = true, multiValued = true)
    List<String> endPoint = null;

    @Override
    protected void execute() {
        log.info("executing vn-updateEndPoints");
        boolean updateSource = true;
        List<DeviceId> src = new LinkedList<>();
        List<DeviceId> dst = new LinkedList<>();

        VnService service = get(VnService.class);

        for (String endPointElement: endPoint) {
                if (endPointElement.equals("/")) {
                    updateSource = false;
                    continue;
                }

                if (updateSource) {
                   src.add(DeviceId.deviceId(endPointElement));
                } else {
                    dst.add(DeviceId.deviceId(endPointElement));
                }
        }
        EndPoint endPoint = new EndPoint(src, dst);

        //System.out.print(vnData.toString());
        if (!service.updateVn(vnName, endPoint)) {
            error("Virtual network updation failed.");
        }

    }
}
