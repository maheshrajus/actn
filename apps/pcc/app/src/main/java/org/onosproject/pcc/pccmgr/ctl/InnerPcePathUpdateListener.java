package org.onosproject.pcc.pccmgr.ctl;


import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pcc.pccmgr.api.PcepSyncStatus;
import org.onosproject.pce.pceservice.api.PcePathReport;
import org.onosproject.pce.pceservice.api.PcePathUpdateListener;

import org.onosproject.pcep.pcepio.protocol.PcepFactory;

import org.onosproject.pcep.pcepio.protocol.PcepReportMsg;


import java.util.Collections;


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
