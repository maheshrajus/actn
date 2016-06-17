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
package org.onosproject.pcc.pccmgr.api;

/**
 * Created by root1 on 3/6/16.
 */
public interface PcepCfg {

    boolean isPeerConfigured(String peerAddr);

    enum State {
        /**
         * Signifies that its just created.
         */
        INIT,

        /**
         * Signifies that only IP Address is configured.
         */
        IP_CONFIGURED,

        /**
         * Signifies that only Autonomous System is configured.
         */
        AS_CONFIGURED,

        /**
         * Signifies that both IP and Autonomous System is configured.
         */
        IP_AS_CONFIGURED
    }

    /**
     * Returns the status of the configuration based on this state certain operations like connection is handled.
     *
     * @return State of the configuration
     */
    State getState();

    /**
     * To set the current state of the configuration.
     *
     * @param state Configuration State enum
     */
    void setState(State state);

    /**
     * Set the AS number to which this BGP speaker belongs.
     *
     * @param localAs 16 or 32 bit AS number, length is dependent on the capability
     */
    void setAsNumber(int localAs);

    /**
     * Get the AS number to which this BGP speaker belongs.
     *
     * @return 16 or 32 bit AS number, length is dependent on the capability
     */
    int getAsNumber();

    /**
    /**
     * Add the BGP peer IP address and the AS number to which it belongs.
     *
     * @param peerAddr IP address in string format
     * @param as AS number to which it belongs
     *
     * @return true if added successfully else false
     */
    boolean addPeer(String peerAddr, int as);

    boolean addPeer(String peerAddr, int as, short holdTime);

    /**
     * Remove the BGP peer with this IP address.
     *
     * @param peerAddr router IP address
     *
     * @return true if removed successfully else false
     */
    boolean removePeer(String peerAddr);

    /**
     * Set the current connection state information.
     *
     * @param peerAddr router IP address in string format
     * @param state state information
     */
    void setPeerConnState(String peerAddr, PcepPeerCfg.State state);

    PcepPeerCfg.State getPeerConnState(String peerAddr);

    /**
     * Connect to BGP peer with this IP address.
     *
     * @param peerAddr router IP address
     *
     * @return true of the configuration is found and able to connect else false
     */
    boolean connectPeer(String peerAddr);

    /**
     * Disconnect this BGP peer with this IP address.
     *
     * @param peerAddr router IP address in string format
     *
     * @return true if the configuration is found and able to disconnect else false
     */
    boolean disconnectPeer(String peerAddr);
}
