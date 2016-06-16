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
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pcep.controller.PcepClientController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure PCEP LSP type.
 */
@Command(scope = "onos", name = "Pcep-DomainMap", description = "PCEP domain mapping")
public class PcepDomainMapCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(PcepDomainMapCommand.class);
    protected PcepClientController pcepClientController;

    @Argument(index = 0, name = "ipAddress",
            description = "PCEP IP address",
            required = true, multiValued = false)
    String ipAddress = null;

    @Argument(index = 1, name = "asNumber",
            description = "As Number",
            required = true, multiValued = false)
    int asNumber = 0;

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
        log.info("Configure domain map");
        this.pcepClientController = get(PcepClientController.class);
        pcepClientController.getConfig().addConfig(IpAddress.valueOf(ipAddress), asNumber);
    }
}
