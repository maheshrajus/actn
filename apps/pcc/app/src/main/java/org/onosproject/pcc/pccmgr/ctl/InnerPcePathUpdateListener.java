package org.onosproject.pcc.pccmgr.ctl;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pcc.pccmgr.api.PcepSyncStatus;
import org.onosproject.pce.pceservice.api.PcePathReport;
import org.onosproject.pce.pceservice.api.PcePathUpdateListener;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcepAttribute;
import org.onosproject.pcep.pcepio.protocol.PcepEroObject;
import org.onosproject.pcep.pcepio.protocol.PcepFactory;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepReportMsg;
import org.onosproject.pcep.pcepio.protocol.PcepRroObject;
import org.onosproject.pcep.pcepio.protocol.PcepSrpObject;
import org.onosproject.pcep.pcepio.protocol.PcepStateReport;
import org.onosproject.pcep.pcepio.protocol.PcepXroObject;
import org.onosproject.pcep.pcepio.types.IPv4SubObject;
import org.onosproject.pcep.pcepio.types.PcepValueType;
import org.onosproject.pcep.pcepio.types.StatefulIPv4LspIdentifiersTlv;
import org.onosproject.pcep.pcepio.types.SymbolicPathNameTlv;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by root1 on 17/6/16.
 */
public class InnerPcePathUpdateListener implements PcePathUpdateListener {

    protected PcepFactory factory = PcepConfig.getInstance().getController().getPcepMessageFactory1();
    public static final long IDENTIFIER_SET = 0x100000000L;
    public static final long SET = 0xFFFFFFFFL;

    @Override
    public void updatePath(PcePathReport reportInfo) {
        PcepConfig pcepConfig = PcepConfig.getInstance();

        if (pcepConfig == null) {
            return;
        }

        PcepClientDriver pc = pcepConfig.getController().getAgent().getConnectedClient(null);
        if (pc == null) {
            return;
        }

         //build PCRpt message
        boolean syncState = (pc.lspDbSyncStatus() == PcepSyncStatus.IN_SYNC);
        PcepReportMsg pcRptMsg = pc.buildPCRptMsg(reportInfo, syncState);

        pc.sendMessage(Collections.singletonList(pcRptMsg));
    }
}
