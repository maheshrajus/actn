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
import org.apache.karaf.shell.commands.Command;
import org.onosproject.bgp.controller.BgpCfg;
import org.onosproject.bgp.controller.BgpController;
import org.onosproject.bgp.controller.BgpLinkCfg;
import org.onosproject.cli.AbstractShellCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Lists ISIS neighbors, database and interfaces details.
 */
@Command(scope = "onos", name = "BGP-InterDomainLinks", description = "Query BGP links")
public class BgpLinkQueryCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(BgpLinkQueryCommand.class);

    protected BgpController bgpController;

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
        log.info("executing BGP-query");

        BgpController bgpController = get(BgpController.class);
        BgpCfg bgpCfg = bgpController.getConfig();
        TreeMap<String, BgpLinkCfg> bgpLinks;
        BgpLinkCfg bgpLinkCfg;

        /* link configuration */
        bgpLinks = bgpCfg.getLinks();
        if (bgpLinks.isEmpty()) {
            log.info("There are no BGP links to iterate");
        } else {
            Set set = bgpLinks.entrySet();
            Iterator i = set.iterator();

            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                bgpLinkCfg = (BgpLinkCfg) me.getValue();
                display(bgpLinkCfg);
            }
        }
    }

/**
     * Display BGP inter domain link information on the terminal.
     *
     * @param cfg BGP link configuration data
     */
    void display(BgpLinkCfg cfg) {
        print("SourceDevice : %s, SourceInterface : %s, DestinationDevice : %s, Destination" +
                      "Interface : %s, Cost : %d, TE_Cost : %d, maxReserveableBandWidth : %f, maxBandWidth : %f," +
                      "unReservedBandWidth : %f ", cfg.srcDeviceId()
                .toString(), cfg
                .srcInterface().toString(), cfg.dstDeviceId(), cfg.dstInterface().toString(),
              cfg.cost(), cfg.teCost(),
              cfg.maxReservedBandwidth(), cfg.maxBandwidth(), cfg.unReservedBandwidth());
    }
}
