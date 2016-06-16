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
import org.apache.karaf.shell.commands.Option;
import org.onlab.util.Bandwidth;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.vn.manager.api.VnService;
import org.onosproject.vn.manager.constraint.VnBandwidth;
import org.onosproject.vn.manager.constraint.VnConstraint;
import org.onosproject.vn.manager.constraint.VnCost;
import org.onosproject.vn.store.EndPoint;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Supports creating the virtual network.
 */
@Command(scope = "onos", name = "vn-setup", description = "Supports creating virtual network.")
public class VnSetupCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Option(name = "-ct", aliases = "--costType", description = "The cost attribute IGP cost (1) or TE cost (2).",
            required = false, multiValued = false)
    Integer costType = null;

    @Option(name = "-cv", aliases = "--costValue", description = "The cost value.",
            required = false, multiValued = false)
    Double costValue = null;

    @Option(name = "-b", aliases = "--bandwidth", description = "The bandwidth attribute of path. "
            + "Data rate unit is in bps.", required = false, multiValued = false)
    Double bandWidth = null;

    @Argument(index = 0, name = "vnName", description = "virtual network name.", required = true, multiValued = false)
    String vnName = null;

    @Argument(index = 1, name = "endPoint", description = "End Point source and destination " +
            "should be split with '/' delimiter (e.g., L3:device1 L3:device2 / L3:device3 L3:device4).", required = true, multiValued = true)
    List<String> endPoint = null;

    @Override
    protected void execute() {
        log.info("executing vn-setup");
        boolean updateSource = true;
        
        List<DeviceId> src = new LinkedList<>();
        List<DeviceId> dst = new LinkedList<>();
        List<VnConstraint> constraints = new LinkedList<>();

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
        if (bandWidth == null && costType == null) {
            if (!service.setupVn(vnName, endPoint)) {
                error("Virtual network creation failed.");
            }
            return;
        }

        if (costType != null) {
            if ((costType.intValue() < 1) || (costType.intValue() > 2)) {
                error("The cost attribute value either IGP cost(1) or TE cost(2).");
                return;
            }
            if (costValue == null) {
                error("The cost value cannot be null.");
                return;
            }
            VnCost.Type enCostType = VnCost.Type.values()[costType - 1];
            VnCost cost = VnCost.of(enCostType, costValue);

            //cost.setCost(cost);
            constraints.add(cost);
        }

        if (bandWidth != null) {
            VnBandwidth vnBandWidth = new VnBandwidth(Bandwidth.bps(bandWidth));
            constraints.add(vnBandWidth);
        }

        //System.out.print(cost);
        //System.out.print(bandwidth);
        if (!service.setupVn(vnName, constraints, endPoint)) {
            error("Virtual network creation failed.");
        }
        return;
    }
}
