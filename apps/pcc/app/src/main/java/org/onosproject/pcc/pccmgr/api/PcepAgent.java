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

import org.onosproject.pcep.pcepio.protocol.PcepMessage;

/**
 * Responsible for keeping track of the current set Pcep clients
 * connected to the system.
 *
 */
public interface PcepAgent {

    /**
     * Add a pce that has just connected to the system.
     *
     * @param pceId the id of pce  to add
     * @param pc the actual pce client object.
     * @return true if added, false otherwise.
     */
    boolean addConnectedClient(PceId pceId, PcepClient pc);


    PcepClientDriver getConnectedClient(PceId pceId);
    /**
     * Checks if the activation for this pce is valid.
     *
     * @param pceId the id of pce to check
     * @return true if valid, false otherwise
     */
    boolean validActivation(PceId pceId);

    /**
     * Clear all state in controller client maps for a pce that has
     * disconnected from the local controller. Also release control for
     * that pceIds from the global repository. Notify client listeners.
     *
     * @param pceIds the id of pce client to remove.
     */
    void removeConnectedClient(PceId pceIds);

    /**
     * Process a message coming from a pce.
     *
     * @param pceId the id of pce client the message was received.
     * @param m the message to process
     */
    void processPcepMessage(PceId pceId, PcepMessage m);

    /**
     * Adds PCEP device when session is successfully established.
     *
     * @param pc PCEP client details
     */
    void addNode(PcepClient pc);

    /**
     * Removes PCEP device when session is disconnected.
     *
     * @param pceId PCEP client ID
     */
    void deleteNode(PceId pceId);

    /**
     * Analyzes report messages received during LSP DB sync again tunnel store and takes necessary actions.
     *
     * @param pceId the id of pce
     * @return success or failure
     */
    boolean analyzeSyncMsgList(PceId pceId);
}
