package org.onosproject.pcc.pccmgr.ctl;

import org.onlab.packet.Ip4Address;
import org.onosproject.pcc.pccmgr.api.PcepConnectPeer;
import org.onosproject.pcc.pccmgr.api.PcepPeerCfg;

/**
 * Created by root1 on 3/6/16.
 */
public class PcepPeerConfig implements PcepPeerCfg {
    private int asNumber;
    private short holdTime;
    private Ip4Address pceIp = null;
    private State state;
    private boolean selfInitiated;
    private PcepConnectPeer connectPeer;
    private byte sessionId;

    /**
     * Constructor to initialize the values.
     */
    PcepPeerConfig() {
        state = State.IDLE;
        selfInitiated = false;
    }

    @Override
    public int getAsNumber() {
        return this.asNumber;
    }

    @Override
    public void setAsNumber(int asNumber) {
        this.asNumber = asNumber;
    }

    @Override
    public short getHoldtime() {
        return this.holdTime;
    }

    @Override
    public void setHoldtime(short holdTime) {
        this.holdTime = holdTime;
    }

    @Override
    public void setPeerPceIp(String pceIp, int asNumber) {
        this.pceIp = Ip4Address.valueOf(pceIp);
        this.asNumber = asNumber;
    }

    @Override
    public String getPeerPceIp() {
        if (this.pceIp != null) {
            return this.pceIp.toString();
        } else {
            return null;
        }
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean getSelfInnitConnection() {
        return this.selfInitiated;
    }

    @Override
    public void setSelfInnitConnection(boolean selfInit) {
        this.selfInitiated = selfInit;
    }

    @Override
    public PcepConnectPeer connectPeer() {
        return this.connectPeer;
    }

    @Override
    public void setConnectPeer(PcepConnectPeer connectPeer) {
        this.connectPeer = connectPeer;
    }

    public void setSessionId(byte id) {
        this.sessionId = id;
    }

    public byte getSessionId() {
        return this.sessionId;
    }
}