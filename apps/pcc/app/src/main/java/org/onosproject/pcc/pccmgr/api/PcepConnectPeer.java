package org.onosproject.pcc.pccmgr.api;

/**
 * Abstraction of an PCEP connect peer, initiate remote connection to PCE on configuration.
 */
public interface PcepConnectPeer {
    /**
     * Initiate bgp peer connection.
     */
    void connectPeer();

    /**
     * End bgp peer connection.
     */
    void disconnectPeer();
}
