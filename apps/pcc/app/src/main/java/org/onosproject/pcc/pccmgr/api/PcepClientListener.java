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
 * Allows for providers interested in PCC client events to be notified.
 */
public interface PcepClientListener {

    /**
     * Notify that the PCC was connected.
     *
     * @param pceId the id of the server that connected
     */
    void clientConnected(PceId pceId);

    /**
     * Notify that the PCC was disconnected.
     *
     * @param pceId the id of the server that disconnected.
     */
    void clientDisconnected(PceId pceId);
}
