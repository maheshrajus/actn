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

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.vn.manager.api.VnService;
import org.onosproject.vn.manager.constraint.VnBandwidth;
import org.onosproject.vn.manager.constraint.VnConstraint;
import org.onosproject.vn.manager.constraint.VnCost;
import org.onosproject.vn.store.EndPoint;
import org.onosproject.vn.store.VirtualNetworkInfo;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Supports quering PCE path.
 */
@Command(scope = "onos", name = "vn-query",
        description = "Supports querying virtual network.")
public class VnQueryCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Option(name = "-n", aliases = "--name", description = "vnName", required = false,
            multiValued = false)
    String vnName = null;

    @Override
    protected void execute() {
        log.info("executing vn-query");

        VnService service = get(VnService.class);
        if (null == vnName) {
            List<VirtualNetworkInfo> virtualNetworks = service.queryAllVn();
            if (virtualNetworks != null) {
                /*for (final VirtualNetworkInfo vn : virtualNetworks) {
                    display(vn);
                }*/
                virtualNetworks.forEach(this::display);
            } else {
                print("No virtual network found.");
                return;
            }
        } else {
            VirtualNetworkInfo vn = service.queryVn(vnName);
            if (vn == null) {
                print("Virtual network doesnot exists.");
                return;
            }
            display(vn);
        }
    }

    /**
     * Display tunnel information on the terminal.
     *
     * @param vn virtual network
     */
    void display(VirtualNetworkInfo vn) {
        /*print("\nvnName            : %d \n" +
                "constraints:            \n" +
                "   cost            : %d \n" +
                "   bandwidth       : %.2f" +
                "Endpoints         : %s \n",
                vn.vnName(), vn.constraints());*/

        print("vnName            : %s", vn.vnName());
        print("constraints       :");

        for (VnConstraint c : vn.constraints()) {
            if (c.getType() == VnBandwidth.TYPE) {
                VnBandwidth vnBandwidth = (VnBandwidth) c;
                print("        bandwidth : %f", vnBandwidth.bandWidthValue().bps());
            } else if (c.getType() == VnCost.TYPE) {
                VnCost vnCost = (VnCost) c;
                print("        costType  : %d", vnCost.type().type());
                print("        cost      : %f", vnCost.cost());
            }
        }
        EndPoint endPoint = vn.endPoint();
        print("endPoint          :\n      source      : ");
        for (DeviceId deviceId : endPoint.src()) {
            print("                    %s ", deviceId.toString());
        }

        print("      destination : ");
        for (DeviceId deviceId : endPoint.dst()) {
            print("                    %s ", deviceId.toString());
        }
        print("----------------------------------------");
    }
}
