/*
 * Copyright 2015-present Open Networking Laboratory
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

package org.onosproject.pcc.pccmgr.ctl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.jboss.netty.channel.Channel;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.pcc.pccmgr.api.ClientCapability;
import org.onosproject.pcc.pccmgr.api.LspKey;
import org.onosproject.pcc.pccmgr.api.PceId;
import org.onosproject.pcc.pccmgr.api.PcepClient;
import org.onosproject.pcc.pccmgr.api.PcepPacketStats;
import org.onosproject.pcc.pccmgr.api.PcepSyncStatus;
import org.onosproject.pcc.pccmgr.api.PcepAgent;
import org.onosproject.pcc.pccmgr.api.PcepClientDriver;
import org.onosproject.pce.pceservice.api.PcePathReport;
import org.onosproject.pce.pceservice.api.PceService;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcepAttribute;
import org.onosproject.pcep.pcepio.protocol.PcepEroObject;
import org.onosproject.pcep.pcepio.protocol.PcepFactories;
import org.onosproject.pcep.pcepio.protocol.PcepFactory;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepMessage;
import org.onosproject.pcep.pcepio.protocol.PcepReportMsg;
import org.onosproject.pcep.pcepio.protocol.PcepRroObject;
import org.onosproject.pcep.pcepio.protocol.PcepSrpObject;
import org.onosproject.pcep.pcepio.protocol.PcepStateReport;
import org.onosproject.pcep.pcepio.protocol.PcepVersion;
import org.onosproject.pcep.pcepio.protocol.PcepXroObject;
import org.onosproject.pcep.pcepio.types.IPv4SubObject;
import org.onosproject.pcep.pcepio.types.PcepValueType;
import org.onosproject.pcep.pcepio.types.StatefulIPv4LspIdentifiersTlv;
import org.onosproject.pcep.pcepio.types.SymbolicPathNameTlv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * An abstract representation of an OpenFlow switch. Can be extended by others
 * to serve as a base for their vendor specific representation of a switch.
 */
public class PcepClientImpl implements PcepClientDriver {

    protected final Logger log = LoggerFactory.getLogger(PcepClientImpl.class);

    private static final String SHUTDOWN_MSG = "Worker has already been shutdown";

    private Channel channel;
    protected String channelId;

    private boolean connected;
    protected boolean startDriverHandshakeCalled;
    protected boolean isHandShakeComplete;
    private PcepSyncStatus lspDbSyncStatus;
    private PcepSyncStatus labelDbSyncStatus;
    private PceId pceId;
    private PcepAgent agent;

    private ClientCapability capability;
    private PcepVersion pcepVersion;
    private byte keepAliveTime;
    private byte deadTime;
    private byte sessionId;
    private PcepPacketStatsImpl pktStats;
    private Map<LspKey, Boolean> lspDelegationInfo;
    private Map<PceId, List<PcepStateReport>> sycRptCache = new HashMap<>();
    public static final long IDENTIFIER_SET = 0x100000000L;
    public static final long SET = 0xFFFFFFFFL;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PceService pceService;

    @Override
    public void init(PceId pceId, PcepVersion pcepVersion, PcepPacketStats pktStats) {
        this.pceId = pceId;
        this.pcepVersion = pcepVersion;
        this.pktStats = (PcepPacketStatsImpl) pktStats;
    }

    @Override
    public final void disconnectClient() {
        log.info("Disconnect peer {}", channel.getRemoteAddress());
        this.channel.close();
    }

    @Override
    public void setCapability(ClientCapability capability) {
        this.capability = capability;
    }

    @Override
    public ClientCapability capability() {
        return capability;
    }

    @Override
    public final void sendMessage(PcepMessage m) {
        log.info("Sending message to {}", channel.getRemoteAddress());
        try {
            channel.write(Collections.singletonList(m));
            this.pktStats.addOutPacket();
        } catch (RejectedExecutionException e) {
            log.warn(e.getMessage());
            if (!e.getMessage().contains(SHUTDOWN_MSG)) {
                throw e;
            }
        }
    }

    @Override
    public final void sendMessage(List<PcepMessage> msgs) {
        try {
            channel.write(msgs);
            this.pktStats.addOutPacket(msgs.size());
        } catch (RejectedExecutionException e) {
            log.warn(e.getMessage());
            if (!e.getMessage().contains(SHUTDOWN_MSG)) {
                throw e;
            }
        }
    }

    @Override
    public final boolean isConnected() {
        return this.connected;
    }

    @Override
    public final void setConnected(boolean connected) {
        this.connected = connected;
    };

    @Override
    public final void setChannel(Channel channel) {
        this.channel = channel;
        final SocketAddress address = channel.getRemoteAddress();
        log.info("Set  channel for: {}", address);
        if (address instanceof InetSocketAddress) {
            final InetSocketAddress inetAddress = (InetSocketAddress) address;
            final IpAddress ipAddress = IpAddress.valueOf(inetAddress.getAddress());
            if (ipAddress.isIp4()) {
                channelId = ipAddress.toString() + ':' + inetAddress.getPort();
            } else {
                channelId = '[' + ipAddress.toString() + "]:" + inetAddress.getPort();
            }
        }
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public PceService getPceService() {
        return this.pceService;
    }

    @Override
    public String channelId() {
        return channelId;
    }

    @Override
    public final PceId getPceId() {
        return this.pceId;
    }

    @Override
    public final String getStringId() {
        return this.pceId.toString();
    }

    @Override
    public final void setPcVersion(PcepVersion pcepVersion) {
        this.pcepVersion = pcepVersion;
    }

    @Override
    public void setPcKeepAliveTime(byte keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    @Override
    public void setPcDeadTime(byte deadTime) {
        this.deadTime = deadTime;
    }

    @Override
    public void setPcSessionId(byte sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void setLspDbSyncStatus(PcepSyncStatus syncStatus) {
        this.lspDbSyncStatus = syncStatus;
    }

    @Override
    public PcepSyncStatus lspDbSyncStatus() {
        return lspDbSyncStatus;
    }

    @Override
    public void setLabelDbSyncStatus(PcepSyncStatus syncStatus) {

        PcepSyncStatus syncOldStatus = labelDbSyncStatus();
        this.labelDbSyncStatus = syncStatus;

        if ((syncOldStatus == PcepSyncStatus.IN_SYNC) && (syncStatus == PcepSyncStatus.SYNCED)) {
            // Perform end of LSP DB sync actions.
            this.agent.analyzeSyncMsgList(pceId);
        }
    }

    @Override
    public PcepSyncStatus labelDbSyncStatus() {
        return labelDbSyncStatus;
    }

    @Override
    public final void handleMessage(PcepMessage m) {
        this.pktStats.addInPacket();
        this.agent.processPcepMessage(pceId, m);
    }

    @Override
    public void addNode(PcepClient pc) {
        this.agent.addNode(pc);
    }

    @Override
    public void deleteNode(PceId pceId) {
        this.agent.deleteNode(pceId);
    }

    @Override
    public final boolean connectClient() {
        return this.agent.addConnectedClient(pceId, this);
    }

    @Override
    public final void removeConnectedClient() {
        this.agent.removeConnectedClient(pceId);
    }

    @Override
    public PcepFactory factory() {
        return PcepFactories.getFactory(pcepVersion);
    }

    @Override
    public boolean isHandshakeComplete() {
        return isHandShakeComplete;
    }

    @Override
    public final void setAgent(PcepAgent ag) {
        if (this.agent == null) {
            this.agent = ag;
        }
    }

    @Override
    public void setLspAndDelegationInfo(LspKey lspKey, boolean dFlag) {
        lspDelegationInfo.put(lspKey, dFlag);
    }

    @Override
    public Boolean delegationInfo(LspKey lspKey) {
        return lspDelegationInfo.get(lspKey);
    }

    @Override
    public void initializeSyncMsgList(PceId pceId) {
        List<PcepStateReport> rptMsgList = new LinkedList<>();
        sycRptCache.put(pceId, rptMsgList);
    }

    @Override
    public List<PcepStateReport> getSyncMsgList(PceId pceId) {
        return sycRptCache.get(pceId);
    }

    @Override
    public void removeSyncMsgList(PceId pceId) {
        sycRptCache.remove(pceId);
    }

    @Override
    public void addSyncMsgToList(PceId pceId, PcepStateReport rptMsg) {
        List<PcepStateReport> rptMsgList = sycRptCache.get(pceId);
        rptMsgList.add(rptMsg);
        sycRptCache.put(pceId, rptMsgList);
    }

    @Override
    public PcepReportMsg buildPCRptMsg(PcePathReport reportInfo, boolean synchFlag) {
        PcepSrpObject srpObj = null;
        PcepValueType tlv;
        LinkedList<PcepValueType> llSubObjects;
        byte operState = 0;

        LinkedList<PcepStateReport> llPcRptList = new LinkedList<>();
        PcepStateReport.Builder stateRptBldr = this.factory().buildPcepStateReport();
        LinkedList<PcepValueType> llOptionalTlv = new LinkedList<PcepValueType>();

        // build srp object if srpid exists
        Integer srpId = PcepSrpIdMap.getSrpId(reportInfo.pathName().getBytes());
        if (srpId  != 0) {
            // build SRP object
            try {
                srpObj = this.factory().buildSrpObject().setSrpID(srpId).setRFlag(reportInfo.isRemoved()).build();
            } catch (PcepParseException e) {
                e.printStackTrace();
            }
        }

        //build LSP object
        tlv = new StatefulIPv4LspIdentifiersTlv(reportInfo.ingress().getIp4Address().toInt(),
                                                Short.parseShort(reportInfo.localLspId()),
                                                Short.parseShort(reportInfo.pceTunnelId()),
                                                reportInfo.ingress().getIp4Address().toInt(),
                                                reportInfo.egress().getIp4Address().toInt());
        llOptionalTlv.add(tlv);

        //set SymbolicPathNameTlv of LSP object
        tlv = new SymbolicPathNameTlv(reportInfo.pathName().getBytes());
        llOptionalTlv.add(tlv);

        PcepLspObject.Builder lspObjBldr = this.factory().buildLspObject();
        lspObjBldr.setAFlag((reportInfo.adminState() == PcePathReport.State.UP));
        lspObjBldr.setPlspId(Integer.parseInt(reportInfo.plspId()))
                .setRFlag(reportInfo.isRemoved()).setDFlag(reportInfo.isDelegate())
                .setSFlag(synchFlag)
                .setOptionalTlv(llOptionalTlv);
        if (reportInfo.state() == PcePathReport.State.UP) {
            operState = 1;
        }
        lspObjBldr.setOFlag(operState);


        //build ERO object
        llSubObjects = createPcepPath(reportInfo.eroPath());
        PcepEroObject eroObj = this.factory().buildEroObject().setSubObjects(llSubObjects).build();

        //build Attribute
        PcepAttribute pcepAttr = null;

        if (reportInfo.xroPath() != null) {
            llSubObjects = createPcepPath(reportInfo.xroPath());

            //build XRO object
            PcepXroObject xroObj = this.factory().buildXroObject().setSubObjects(llSubObjects).build();

            pcepAttr = this.factory().buildPcepAttribute().setXroObject(xroObj).build();
        }

        //build RRO object
        PcepRroObject rroObj = null;

        if (reportInfo.rroPath() != null) {
            llSubObjects = createPcepPath(reportInfo.rroPath());
            rroObj = this.factory().buildRroObject().setSubObjects(llSubObjects).build();
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
        return this.factory().buildReportMsg().setStateReportList(llPcRptList).build();
    }

    @Override
    public PcepReportMsg buildLspSyncEndMsg() {
        LinkedList<PcepStateReport> llPcRptList = new LinkedList<>();
        PcepStateReport.Builder stateRptBldr = this.factory().buildPcepStateReport();

        //build LSP object
        PcepLspObject lspObj = this.factory().buildLspObject().setPlspId(0).setSFlag(false).build();

        //build empty ERO object
        PcepEroObject eroObj = this.factory().buildEroObject().build();

        stateRptBldr.setLspObject(lspObj);
        stateRptBldr.setEroObject(eroObj);

        try {
            llPcRptList.add(stateRptBldr.build());
        } catch (PcepParseException e) {
            e.printStackTrace();
        }

        //build PCRpt message
        return this.factory().buildReportMsg().setStateReportList(llPcRptList).build();
    }

    public LinkedList<PcepValueType> createPcepPath(Path path) {
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

    @Override
    public boolean isOptical() {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("channel", channelId())
                .add("pceId", getPceId())
                .toString();
    }
}
