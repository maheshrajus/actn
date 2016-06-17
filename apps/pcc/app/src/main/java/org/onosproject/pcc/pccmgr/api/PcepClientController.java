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

import java.util.Collection;

import org.onosproject.pcep.pcepio.protocol.PcepMessage;

/**
 * Abstraction of an Pcep client controller. Serves as a one stop
 * shop for obtaining Pcep devices and (un)register listeners
 * on pcep events
 */
public interface PcepClientController {

    /**
     * Returns list of pcc clients connected to this Pcep controller.
     *
     * @return list of PcepClient elements
     */
    Collection<PcepClient> getClients();

    /**
     * Returns the actual pcc client for the given ip address.
     *
     * @param pceId the id of the pce to fetch
     * @return the interface to this pcc client
     */
    PcepClient getClient(PceId pceId);

    /**
     * Register a listener for meta events that occur to pcep
     * devices.
     *
     * @param listener the listener to notify
     */
    void addListener(PcepClientListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    void removeListener(PcepClientListener listener);

    /**
     * Register a listener for PCEP msg events.
     *
     * @param listener the listener to notify
     */
    void addEventListener(PcepEventListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to unregister
     */
    void removeEventListener(PcepEventListener listener);

    /**
     * Register a listener for PCEP msg events[carrying node descriptor details].
     *
     * @param listener the listener to notify
     */
    void addNodeListener(PcepNodeListener listener);

    /**
     * Unregister a listener.
     *
     * @param listener the listener to be unregistered
     */
    void removeNodeListener(PcepNodeListener listener);

    /**
     * Register a listener for packet events.
     *
     * @param listener the listener to notify
     */
    void addPacketListener(PcepPacketListener listener);

    /**
     * Unregister a packet listener.
     *
     * @param listener the listener to unregister
     */
    void removePacketListener(PcepPacketListener listener);

    /**
     * Send a message to a particular pcc client.
     *
     * @param pceId the id of the server to send message.
     * @param msg the message to send
     */
    void writeMessage(PceId pceId, PcepMessage msg);

    /**
     * Process a message and notify the appropriate listeners.
     *
     * @param pceId id of the server the message arrived on
     * @param msg the message to process.
     */
    void processClientMessage(PceId pceId, PcepMessage msg);

    /**
     * Close all connected PCC clients.
     */
    void closeConnectedClients();
}
