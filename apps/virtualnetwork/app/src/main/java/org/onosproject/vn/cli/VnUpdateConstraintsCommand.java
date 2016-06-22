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
import org.onosproject.vn.vnservice.api.VnService;
import org.onosproject.vn.vnservice.constraint.VnBandwidth;
import org.onosproject.vn.vnservice.constraint.VnConstraint;
import org.onosproject.vn.vnservice.constraint.VnCost;
import org.onosproject.vn.store.VirtualNetworkInfo;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Supports creating the virtual network.
 */
@Command(scope = "onos", name = "vn-updateConstraints", description = "Supports updating  virtual network constraints.")
public class VnUpdateConstraintsCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Argument(index = 0, name = "vnName", description = "virtual network name.", required = true, multiValued = false)
    String vnName = null;

    @Option(name = "-c", aliases = "--costType", description = "The cost attribute IGP cost (1) or TE cost (2).",
            required = false, multiValued = false)
    Integer costType = null;

    @Option(name = "-b", aliases = "--bandwidth", description = "The bandwidth attribute of path. "
            + "Data rate unit is in bps.", required = false, multiValued = false)
    Double bandWidth = null;

    @Override
    protected void execute() {
        log.info("executing vn-updateConstraints");

        VnService service = get(VnService.class);
        List<VnConstraint> constraint = new LinkedList<>();

        if (bandWidth == null && costType == null) {
            error("constraints cannot be null.");
            return;
        }

        VirtualNetworkInfo vnData = service.queryVn(vnName);
        if (vnData == null) {
            error("Virtual network does not exist.");
            return;
        }
        VnBandwidth bandWidthConstraints;
        if (bandWidth != null) {
            bandWidthConstraints = new VnBandwidth(Bandwidth.bps(bandWidth));
            //vnData.setBandWidth(bandWidthConstraints);
            constraint.add(bandWidthConstraints);
        }

        if (costType != null) {
            if ((costType.intValue() < 1) || (costType.intValue() > 2)) {
                error("The cost attribute value either IGP cost(1) or TE cost(2).");
                return;
            }

            VnCost.Type enCostType = VnCost.Type.values()[costType - 1];
            VnCost costConstraint = VnCost.of(enCostType);
            constraint.add(costConstraint);
        }

        if (!service.updateVn(vnName, constraint)) {
            error("Virtual network update constraints failed.");
        }

    }
}
