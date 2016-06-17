package org.onosproject.pcc.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pcc.pccmgr.api.PceId;
import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pcc.pccmgr.api.PcepPeerCfg;
import org.onosproject.pcc.pccmgr.ctl.PcepConfig;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by root1 on 7/6/16.
 */
@Command(scope = "onos", name = "pcep-peer-remove", description = "Supports removing pcep peer.")
public class PcepPeerRemoveCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Argument(index = 0, name = "peerIp", description = "pce server ip.", required = true, multiValued = false)
    String peerIp = null;

    @Override
    protected void execute() {
        log.info("executing pcep-peer-remove");

        PcepConfig pcepConfig = PcepConfig.getInstance();

        if (pcepConfig != null) {
            PceId pceId = PceId.pceId(IpAddress.valueOf(peerIp));
            PcepClientDriver pc = pcepConfig.getController().getAgent().getConnectedClient(pceId);
            if (pc != null) {
                log.info("Close channel for remove peer: {}", peerIp);
                pcepConfig.setPeerConnState(peerIp, PcepPeerCfg.State.INVALID);
                pc.getChannel().close();
            } else {
                pcepConfig.disconnectPeer(peerIp);
            }
            pcepConfig.removePeer(peerIp);
        }
    }
}
