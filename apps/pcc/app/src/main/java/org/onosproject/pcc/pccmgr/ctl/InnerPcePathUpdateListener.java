package org.onosproject.pcc.pccmgr.ctl;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pce.pceservice.api.PcePathReport;
import org.onosproject.pce.pceservice.api.PcePathUpdateListener;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcepAttribute;
import org.onosproject.pcep.pcepio.protocol.PcepEroObject;
import org.onosproject.pcep.pcepio.protocol.PcepFactory;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepMsgPath;
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
        PcepReportMsg pcRptMsg = buildPCRptMsg(pc, reportInfo);

        pc.sendMessage(Collections.singletonList(pcRptMsg));
    }

    public static PcepReportMsg buildPCRptMsg(PcepClientDriver pc, PcePathReport reportInfo) {
        PcepSrpObject srpObj = null;
        PcepValueType tlv;
        LinkedList<PcepValueType> llSubObjects;
        byte operState = 0;

        LinkedList<PcepStateReport> llPcRptList = new LinkedList<>();
        PcepStateReport.Builder stateRptBldr = pc.factory().buildPcepStateReport();
        LinkedList<PcepValueType> llOptionalTlv = new LinkedList<PcepValueType>();

        // build srp object if srpid exists
        Integer srpId = 0;
        if ((srpId = PcepSrpIdMap.getSrpId(reportInfo.pathName().getBytes())) != 0) {
            // build SRP object
            try {
                srpObj = pc.factory().buildSrpObject().setSrpID(srpId).setRFlag(reportInfo.isRemoved()).build();
            } catch (PcepParseException e) {
                e.printStackTrace();
            }
        }

        //build LSP object
        // TODO : need to fill extended tunnel id
        tlv = new StatefulIPv4LspIdentifiersTlv(reportInfo.ingress().getIp4Address().toInt(),
                                                Short.parseShort(reportInfo.localLspId()),
                                                Short.parseShort(reportInfo.pceTunnelId()),
                                                0, reportInfo.egress().getIp4Address().toInt());
        llOptionalTlv.add(tlv);

        //set SymbolicPathNameTlv of LSP object
        tlv = new SymbolicPathNameTlv(reportInfo.pathName().getBytes());
        llOptionalTlv.add(tlv);

        PcepLspObject.Builder lspObjBldr = pc.factory().buildLspObject();
        lspObjBldr.setAFlag((reportInfo.adminState() == PcePathReport.State.UP));
        lspObjBldr.setPlspId(Integer.parseInt(reportInfo.plspId()))
                .setRFlag(reportInfo.isRemoved()).setDFlag(reportInfo.isDelegate())
                .setSFlag(reportInfo.isSync())
                .setOptionalTlv(llOptionalTlv);
        if (reportInfo.state() == PcePathReport.State.UP) {
            operState = 1;
        }
        lspObjBldr.setOFlag(operState);


        //build ERO object
        llSubObjects = createPcepPath(reportInfo.eroPath());
        PcepEroObject eroObj = pc.factory().buildEroObject().setSubObjects(llSubObjects).build();

        //build Attribute
        PcepAttribute pcepAttr = null;

        if (reportInfo.xroPath() != null) {
            llSubObjects = createPcepPath(reportInfo.xroPath());

            //build XRO object
            PcepXroObject xroObj = pc.factory().buildXroObject().setSubObjects(llSubObjects).build();

            pcepAttr = pc.factory().buildPcepAttribute().setXroObject(xroObj).build();
        }

        //build RRO object
        PcepRroObject rroObj = null;

        if (reportInfo.rroPath() != null) {
            llSubObjects = createPcepPath(reportInfo.rroPath());
            rroObj = pc.factory().buildRroObject().setSubObjects(llSubObjects).build();
        }

        // set all the objects in state report
        if (srpObj != null) {
            stateRptBldr.setSrpObject(srpObj);
        }

        stateRptBldr.setLspObject(lspObjBldr.build());
        stateRptBldr.setEroObject(eroObj);

        if (pcepAttr != null) {
            stateRptBldr.setPcepAttribute(pcepAttr);
        }

        if (rroObj != null) {
            stateRptBldr.setRroObject(rroObj);
        }

        try {
            llPcRptList.add(stateRptBldr.build());
        } catch (PcepParseException e) {
            e.printStackTrace();
        }

        //build PCRpt message
        return pc.factory().buildReportMsg()
                .setStateReportList(llPcRptList).build();
    }

    public static LinkedList<PcepValueType> createPcepPath(Path path) {
        LinkedList<PcepValueType> llSubObjects = new LinkedList<PcepValueType>();
        List<Link> listLink = path.links();
        ConnectPoint source = null;
        ConnectPoint destination = null;
        IpAddress ipDstAddress = null;
        IpAddress ipSrcAddress = null;
        PcepValueType subObj = null;
        long portNo;

        for (Link link : listLink) {
            source = link.src();
            if (!(source.equals(destination))) {
                //set IPv4SubObject for ERO object
                portNo = source.port().toLong();
                portNo = ((portNo & IDENTIFIER_SET) == IDENTIFIER_SET) ? portNo & SET : portNo;
                ipSrcAddress = Ip4Address.valueOf((int) portNo);
                subObj = new IPv4SubObject(ipSrcAddress.getIp4Address().toInt());
                llSubObjects.add(subObj);
            }

            destination = link.dst();
            portNo = destination.port().toLong();
            portNo = ((portNo & IDENTIFIER_SET) == IDENTIFIER_SET) ? portNo & SET : portNo;
            ipDstAddress = Ip4Address.valueOf((int) portNo);
            subObj = new IPv4SubObject(ipDstAddress.getIp4Address().toInt());
            llSubObjects.add(subObj);
        }

        return llSubObjects;
    }
}
