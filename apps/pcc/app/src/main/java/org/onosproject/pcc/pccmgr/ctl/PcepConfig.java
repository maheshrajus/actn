package org.onosproject.pcc.pccmgr.ctl;

import org.onosproject.pcc.pccmgr.api.PcepCfg;
import org.onosproject.pcc.pccmgr.api.PcepConnectPeer;
import org.onosproject.pcc.pccmgr.api.PcepPeerCfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * Created by root1 on 3/6/16.
 */
public final class PcepConfig implements PcepCfg {
    private static PcepConfig pccConfig;
    protected static final Logger log = LoggerFactory.getLogger(PcepConfig.class);

    private static final short DEFAULT_HOLD_TIMER = 120;

    private State state = State.INIT;
    private int localAs;
    private short holdTime;
    private TreeMap<String, PcepPeerCfg> pcepPeerTree = new TreeMap<>();
    private Controller controller;
    private byte sessionId;

    private PcepConfig() {

    }

    /* Static 'instance' method */
    public static PcepConfig getInstance() {
        if (pccConfig == null) {
            // Thread Safe. Might be costly operation in some case
            synchronized (PcepConfig.class) {
                if (pccConfig == null) {
                    pccConfig = new PcepConfig();
                }
            }
        }

        return pccConfig;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return this.controller;
    }

    public static byte getSessionId() {
        PcepConfig pccConfig = getInstance();
        if (pccConfig != null) {
            pccConfig.sessionId = (byte) (pccConfig.sessionId + 1);
            return pccConfig.sessionId;
        }

        return 0;
    }

    @Override
    public boolean isPeerConfigured(String peerAddr) {
        PcepPeerCfg pcepPeer = this.pcepPeerTree.get(peerAddr);
        return (pcepPeer != null);
    }

    @Override
    public void setPeerConnState(String peerAddr, PcepPeerCfg.State state) {
        PcepPeerCfg pcepPeer = this.pcepPeerTree.get(peerAddr);

        if (pcepPeer != null) {
            pcepPeer.setState(state);
            log.debug("Peer : " + peerAddr + " is not available");

        } else {
            log.debug("Did not find : " + peerAddr);
        }
    }

    @Override
    public PcepPeerCfg.State getPeerConnState(String peerAddr) {
        PcepPeerCfg pcePeer = this.getPeer(peerAddr);

        if (pcePeer != null) {
            return pcePeer.getState();
        } else {
            return PcepPeerCfg.State.INVALID; //No instance
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void setAsNumber(int localAs) {
        State localState = getState();
        this.localAs = localAs;

        /* Set configuration state */
        if (localState == State.IP_CONFIGURED) {
            setState(State.IP_AS_CONFIGURED);
        } else {
            setState(State.AS_CONFIGURED);
        }
    }

    @Override
    public int getAsNumber() {
        return this.localAs;
    }

    @Override
    public boolean addPeer(String peerAddr, int as) {
        return addPeer(peerAddr, as, DEFAULT_HOLD_TIMER);
    }

    @Override
    public boolean addPeer(String peerAddr, int as, short holdTime) {
        PcepPeerConfig pcepPeer = new PcepPeerConfig();
        if (this.pcepPeerTree.get(peerAddr) == null) {

            pcepPeer.setPeerPceIp(peerAddr, as);
            pcepPeer.setAsNumber(as);
            pcepPeer.setHoldtime(holdTime);
            pcepPeer.setState(PcepPeerCfg.State.IDLE);
            pcepPeer.setSelfInnitConnection(false);
            pcepPeer.setSessionId(PcepConfig.getSessionId());

            this.pcepPeerTree.put(peerAddr, pcepPeer);
            log.info("pcep peer cfg added successfully");
            return true;
        } else {
            log.info("pcep peer cfg already exists");
            return false;
        }
    }

    @Override
    public boolean removePeer(String peerAddr) {
        PcepPeerCfg pcepPeer = this.pcepPeerTree.get(peerAddr);

        if (pcepPeer != null) {

            disconnectPeer(peerAddr);
            pcepPeer.setSelfInnitConnection(false);
            this.pcepPeerTree.remove(peerAddr);
            log.info("Deleted : " + peerAddr + " successfully");

            return true;
        } else {
            log.info("Did not find : " + peerAddr);
            return false;
        }
    }

    @Override
    public boolean connectPeer(String peerAddr) {
        PcepPeerCfg pcepPeer = this.pcepPeerTree.get(peerAddr);

        if (pcepPeer != null) {
            pcepPeer.setSelfInnitConnection(true);

            if (pcepPeer.connectPeer() == null) {
                PcepConnectPeer connectPeer = new PcepConnectPeerImpl(controller, peerAddr, Controller.PCEP_PORT_NUM);
                pcepPeer.setConnectPeer(connectPeer);
                connectPeer.connectPeer();
            }
            return true;
        }

        return false;
    }

    public PcepPeerCfg getPeer(String peerAddr) {
        return this.pcepPeerTree.get(peerAddr);
    }

    @Override
    public boolean disconnectPeer(String peerAddr) {
        PcepPeerCfg pcepPeer = this.pcepPeerTree.get(peerAddr);

        if (pcepPeer != null) {

            if (pcepPeer.connectPeer() != null) {
                log.info("disconnectPeer : " + peerAddr + " successfully");
                pcepPeer.connectPeer().disconnectPeer();
            }

            pcepPeer.setState(PcepPeerCfg.State.IDLE);
            pcepPeer.setSelfInnitConnection(false);
            log.info("Disconnected : " + peerAddr + " successfully");

            return true;
        } else {
            log.info("Did not find : " + peerAddr);
            return false;
        }
    }
}
