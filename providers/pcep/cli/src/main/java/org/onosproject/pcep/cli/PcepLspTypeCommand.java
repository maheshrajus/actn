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
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pcep.controller.PcepClientController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure PCEP LSP type.
 */
@Command(scope = "onos", name = "Pcep-LspType", description = "PCEP LSP type")
public class PcepLspTypeCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(PcepLspTypeCommand.class);
    private static final String INTERFACE = "interface";
    protected PcepClientController pcepClientController;

    @Argument(index = 0, name = "lspType",
            description = "PCEP LSP type RSVP-TE/SR/CR",
            required = true, multiValued = false)
    String lspTypeName = null;

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
        log.info("Configure LSP Type");
       int lspType = 0;

        if (lspTypeName.equals("SR")) {
            lspType = 1;
        } else if (lspTypeName.equals("CR")) {
            lspType = 2;
        } else if (lspTypeName.equals("RSVP-TE")) {
            lspType = 3;
        }

       this.pcepClientController = get(PcepClientController.class);
       pcepClientController.getConfig().setLspType(lspType);
    }
}
