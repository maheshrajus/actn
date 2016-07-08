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
package org.onosproject.pce.cli;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pce.pceservice.LspType;
import org.onosproject.pce.pceservice.api.PceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure PCE LSP type.
 */
@Command(scope = "onos", name = "pce-set", description = "PCE LSP type & mode")
public class PceLspTypeAndModeCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(PceLspTypeAndModeCommand.class);

    @Argument(index = 0, name = "lspType",
            description = "PCE LSP type RSVP-TE/SR/CR",
            required = false, multiValued = false)
    String lspTypeName = null;

    @Option(name = "-m", aliases = "--mode", description = "The mode of PCE PNC or MDSC",
            required = true, multiValued = false)
    String mode = "PNC";

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
       LspType lspType = null;

       PceService service = get(PceService.class);
       if (lspTypeName != null) {
           if (lspTypeName.equals("SR")) {
               lspType =  LspType.SR_WITHOUT_SIGNALLING;
           } else if (lspTypeName.equals("CR")) {
               lspType =  LspType.WITHOUT_SIGNALLING_AND_WITHOUT_SR;
           } else if (lspTypeName.equals("RSVP-TE")) {
               lspType =  LspType.WITH_SIGNALLING;
           }
       }

       service.setdefaultLspType(lspType);
       if (mode.equals("PNC") || mode.equals("MDSC")) {
           service.setPceMode(mode);
       } else {
           error("PCE mode should be PNC or MDSC.");
       }

    }
}
