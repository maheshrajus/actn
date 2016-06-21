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
package org.onosproject.pcep.cli;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pcep.controller.PcepCfg;
import org.onosproject.pcep.controller.PcepCfgData;
import org.onosproject.pcep.controller.PcepClientController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Lists ISIS neighbors, database and interfaces details.
 */
@Command(scope = "onos", name = "Pcep-DomainMap-Query", description = "Query PCEP Domain map info")
public class PcepQueryCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(PcepQueryCommand.class);

    protected PcepClientController pcepClientController;

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
        log.info("executing Pcep-query");
        String lspType = null;
        PcepClientController pcepClientController = get(PcepClientController.class);
        PcepCfg pcepCfg = pcepClientController.getConfig();

        TreeMap<Integer, PcepCfgData> domainMap =  pcepCfg.pcepDomainMap();

        if (domainMap != null) {
            // Get a set of the entries
            Set set = domainMap.entrySet();

            // Get an iterator
            Iterator it = set.iterator();

            if (it.hasNext()) {
                print("Domain Map        :");
            }

            // Display elements
            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                PcepCfgData cfg = (PcepCfgData) me.getValue();
                display(cfg);
            }
        } else {
            print("No domain map configured.");
            return;
        }
    }

/**
     * Display tunnel information on the terminal.
     *
     * @param cfg PCEP configuration data
     */
    void display(PcepCfgData cfg) {
        print("    IP address    : %s    As number      : %d", cfg.ipAddress().toString(), cfg.asNumber());
    }
}
