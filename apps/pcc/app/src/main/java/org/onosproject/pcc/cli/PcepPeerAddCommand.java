package org.onosproject.pcc.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pcc.pccmgr.ctl.PcepConfig;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by root1 on 6/6/16.
 */
@Command(scope = "onos", name = "pcep-peer-add", description = "Supports creating pcep peer.")
public class PcepPeerAddCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Argument(index = 0, name = "peerIp", description = "pce server ip.", required = true, multiValued = false)
    String peerIp = null;

    @Argument(index = 1, name = "asNumber", description = "as number", required = true, multiValued = false)
    int asNumber = 0;

    @Override
    protected void execute() {
        log.info("executing pcep-peer-add");

        // LSP type validation
        if (asNumber <= 0) {
            error("As Number cannot be zero.");
            return;
        }

        PcepConfig pcepConfig = PcepConfig.getInstance();

        if (pcepConfig != null) {
            pcepConfig.addPeer(peerIp, asNumber);
            pcepConfig.connectPeer(peerIp);
        }
    }
}

