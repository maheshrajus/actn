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
import org.onosproject.incubator.net.tunnel.Tunnel;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.vn.manager.api.VnService;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Supports quering PCE path.
 */
@Command(scope = "onos", name = "vn-query",
        description = "Supports querying virtual network.")
public class VnQueryCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());
    public static final String COST_TYPE = "costType";
    @Option(name = "-n", aliases = "--name", description = "vnName", required = false,
            multiValued = false)
    String vnName = null;

    @Override
    protected void execute() {
        log.info("executing vn-query");

        VnService service = get(VnService.class);
        if (null == vnName) {
            Iterable<Tunnel> vnTunnels = service.queryAllVnTunnels();
            if (vnTunnels != null) {
                /*for (final VirtualNetworkInfo vn : virtualNetworks) {
                    display(vn);
                }*/
                vnTunnels.forEach(this::display);
            } else {
                print("No virtual network found.");
                return;
            }
        } else {
            Iterable<Tunnel> vnTunnels = service.queryVnTunnels(vnName);
            if (vnTunnels == null) {
                print("Virtual network doesnot exists.");
                return;
            }
            vnTunnels.forEach(this::display);
        }
    }

    /**
     * Display tunnel information on the terminal.
     *
     * @param tunnel pce tunnel
     */
    void display(Tunnel tunnel) {
        print("\npath-id            : %s \n" +
                      "source             : %s \n" +
                      "destination        : %s \n" +
                      "path-type          : %s \n" +
                      "symbolic-path-name : %s \n" +
                      "constraints:            \n" +
                      "   cost            : %s \n" +
                      "   bandwidth       : %s",
              tunnel.tunnelId().id(), tunnel.src().toString(), tunnel.dst().toString(),
              tunnel.type().name(), tunnel.tunnelName(), tunnel.annotations().value(COST_TYPE),
              tunnel.annotations().value(AnnotationKeys.BANDWIDTH));
    }
}
