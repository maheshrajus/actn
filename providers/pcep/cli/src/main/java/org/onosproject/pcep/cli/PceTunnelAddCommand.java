package org.onosproject.pcep.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.pcep.controller.PccId;
import org.onosproject.pcep.controller.PcepClient;
import org.onosproject.pcep.controller.PcepClientController;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcInitiatedLspRequest;
import org.onosproject.pcep.pcepio.protocol.PcepAssociationObject;
import org.onosproject.pcep.pcepio.protocol.PcepAttribute;
import org.onosproject.pcep.pcepio.protocol.PcepBandwidthObject;
import org.onosproject.pcep.pcepio.protocol.PcepEndPointsObject;
import org.onosproject.pcep.pcepio.protocol.PcepEroObject;
import org.onosproject.pcep.pcepio.protocol.PcepInitiateMsg;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepSrpObject;
import org.onosproject.pcep.pcepio.types.IPv4SubObject;
import org.onosproject.pcep.pcepio.types.PathSetupTypeTlv;
import org.onosproject.pcep.pcepio.types.PcepValueType;
import org.onosproject.pcep.pcepio.types.StatefulIPv4LspIdentifiersTlv;
import org.onosproject.pcep.pcepio.types.SymbolicPathNameTlv;
import org.onosproject.pcep.pcepio.types.VirtualNetworkTlv;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.LinkedList;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by root1 on 13/6/16.
 */
@Command(scope = "onos", name = "pcep-tunnel-add", description = "Test CLI : Supports creating pce init tunnel.")
public class PceTunnelAddCommand extends AbstractShellCommand {

    private final Logger log = getLogger(getClass());

    @Argument(index = 0, name = "ingressIp", description = "Ingress Lsr Id", required = true, multiValued = false)
    String ingressIp = null;

    @Argument(index = 1, name = "egressIp", description = "Engress Lsr Id", required = true, multiValued = false)
    String egressIp = null;

    @Argument(index = 2, name = "symName", description = "Symbolic Path Name", required = true, multiValued = false)
    String symName = null;


    @Override
    protected void execute() {
        log.info("Execute PCInit Tunnel Add --- Test");
        pcepSetupTunnelTest(ingressIp, egressIp, symName);
    }

    /**
     * To send initiate tunnel message to pcc.
     *
     */
    private void pcepSetupTunnelTest(String src, String dst, String symbPath) {
        try {
            int srpId = 1;
            IpAddress srcIp = IpAddress.valueOf(src);
            IpAddress dstIp = IpAddress.valueOf(dst);

            PcepClientController service = get(PcepClientController.class);

            PcepClient pc = service.getClient(PccId.pccId(srcIp));
            if (null == pc) {
                log.info("Session not exist with ingress -- PCInit Add");
                return;
            }

            PcepValueType tlv;

            LinkedList<PcepValueType> llOptionalTlv = new LinkedList<PcepValueType>();

            // set PathSetupTypeTlv of SRP object
            tlv = new PathSetupTypeTlv(0);
            llOptionalTlv.add(tlv);

            // build SRP object
            PcepSrpObject srpobj = pc.factory().buildSrpObject().setSrpID(srpId).setRFlag(false)
                    .setOptionalTlv(llOptionalTlv).build();

            llOptionalTlv = new LinkedList<PcepValueType>();
            LinkedList<PcInitiatedLspRequest> llPcInitiatedLspRequestList = new LinkedList<PcInitiatedLspRequest>();

            // set LSP identifiers TLV
            short localLspId = 0;

            tlv = new StatefulIPv4LspIdentifiersTlv(srcIp.getIp4Address().toInt(),
                                                    localLspId, (short) 0, 0,
                                                    dstIp.getIp4Address().toInt());
            llOptionalTlv.add(tlv);
            //set SymbolicPathNameTlv of LSP object
            tlv = new SymbolicPathNameTlv(symbPath.getBytes());
            llOptionalTlv.add(tlv);

            //build LSP object
            PcepLspObject lspobj = pc.factory().buildLspObject().setAFlag(true).setOFlag((byte) 0).setPlspId(0)
                    .setOptionalTlv(llOptionalTlv).build();

            //build ENDPOINTS object
            PcepEndPointsObject endpointsobj = pc.factory().buildEndPointsObject()
                    .setSourceIpAddress(srcIp.getIp4Address().toInt())
                    .setDestIpAddress(dstIp.getIp4Address().toInt())
                    .setPFlag(true).build();

            //build ERO object
            LinkedList<PcepValueType> llSubObjects = new LinkedList<PcepValueType>();
            PcepValueType subObj = null;
            subObj = new IPv4SubObject(0x0a010101);
            llSubObjects.add(subObj);
            subObj = new IPv4SubObject(0x0a010102);
            llSubObjects.add(subObj);

            PcepEroObject eroobj = pc.factory().buildEroObject().setSubObjects(llSubObjects).build();

            LinkedList<PcepAssociationObject> llAssociationObj = new LinkedList<PcepAssociationObject>();
            llOptionalTlv = new LinkedList<PcepValueType>();
            String vn = "virtual-network";
            VirtualNetworkTlv vnTlv = new VirtualNetworkTlv(vn.getBytes());
            llOptionalTlv.add(vnTlv);
            PcepAssociationObject associationObj = pc.factory().buildAssociationObject().setAssociationID((short) 1)
                    .setAssociationSource(0x01010101).setAssociationType((short) 1)
                    .setOptionalTlv(llOptionalTlv).build();
            llAssociationObj.add(associationObj);

            float iBandwidth = 1000;
            // build bandwidth object
            PcepBandwidthObject bandwidthObject = pc.factory().buildBandwidthObject().setBandwidth(iBandwidth).build();
            // build pcep attribute
            PcepAttribute pcepAttribute = pc.factory().buildPcepAttribute().setBandwidthObject(bandwidthObject).build();

            PcInitiatedLspRequest initiateLspRequest = pc.factory().buildPcInitiatedLspRequest().setSrpObject(srpobj)
                    .setLspObject(lspobj).setEndPointsObject(endpointsobj).setEroObject(eroobj)
                    .setAssociationObjectList(llAssociationObj)
                    .setPcepAttribute(pcepAttribute).build();
            llPcInitiatedLspRequestList.add(initiateLspRequest);

            if (llPcInitiatedLspRequestList.size() == 0) {
                log.error("Failed to create PcInitiatedLspRequestList");
                return;
            }

            //build PCInitiate message
            PcepInitiateMsg pcInitiateMsg = pc.factory().buildPcepInitiateMsg()
                    .setPcInitiatedLspRequestList(llPcInitiatedLspRequestList)
                    .build();

            pc.sendMessage(Collections.singletonList(pcInitiateMsg));

        } catch (PcepParseException e) {
            log.error("PcepParseException occurred while processing setup tunnel {}", e.getMessage());
        }
    }
}
