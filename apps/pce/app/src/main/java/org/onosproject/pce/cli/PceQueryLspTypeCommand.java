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
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pce.pceservice.LspType;
import org.onosproject.pce.pceservice.api.PceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configure PCE LSP type.
 */
@Command(scope = "onos", name = "Pce-PathType", description = "PCE LSP type")
public class PceQueryLspTypeCommand extends AbstractShellCommand {
    private static final Logger log = LoggerFactory.getLogger(PceQueryLspTypeCommand.class);

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
       String lspType = "";

       PceService service = get(PceService.class);

       if (service.defaultLspType().equals(LspType.SR_WITHOUT_SIGNALLING)) {
           lspType = "SR";
       } else if (service.defaultLspType().equals(LspType.WITHOUT_SIGNALLING_AND_WITHOUT_SR)) {
           lspType = "CR";
       } else if (service.defaultLspType().equals(LspType.WITH_SIGNALLING)) {
           lspType = "RSVP-TE";
       }

       print("LSP Type : %s", lspType);
    }
}
