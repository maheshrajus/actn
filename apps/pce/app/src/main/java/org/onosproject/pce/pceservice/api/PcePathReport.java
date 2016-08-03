/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.pce.pceservice.api;

import org.onlab.packet.IpAddress;
import org.onosproject.net.Path;

/**
 * Abstraction of an entity which provides functionality of pce path report.
 */
public interface PcePathReport {

    /**
     * Path state.
     */
    enum State {
        /**
         * Signifies that the path is up.
         */
        UP,
        /**
         * Signifies that the path is down.
         */
        DOWN
    }

    /**
     * Returns the path name.
     *
     * @return path name
     */
    String pathName();

    /**
     * Returns the SRP id.
     *
     * @return SRP id
     */
    String srpId();

    /**
     * Returns the plsp id.
     *
     * @return path plsp id
     */
    String plspId();

    /**
     * Returns the local lsp id.
     *
     * @return path local lsp id
     */
    String localLspId();

    /**
     * Returns the pcc tunnel id.
     *
     * @return path pcc tunnel id
     */
    String pceTunnelId();

    /**
     * Returns the delegation status.
     *
     * @return path delegation status
     */
    boolean isDelegate();

    /**
     * Returns the sync status.
     *
     * @return path sync status
     */
    boolean isSync();

    /**
     * Returns the removed status.
     *
     * @return path removed status
     */
    boolean isRemoved();

    /**
     * Returns the path administrative status.
     *
     * @return path administrative status
     */
    State adminState();

    /**
     * Returns the path operation status.
     *
     * @return path operation status
     */
    State state();

    /**
     * Returns the path ingress address.
     *
     * @return path ingress address
     */
    IpAddress ingress();

    /**
     * Returns the path egress address.
     *
     * @return path egress address
     */
    IpAddress egress();

    /**
     * Returns the path error information.
     *
     * @return path error information
     */
    String errorInfo();

    /**
     * Returns the ERO path information.
     *
     * @return ERO path information
     */
    Path eroPath();

    /**
     * Returns the RRO path information.
     *
     * @return RRO path information
     */
    Path rroPath();

    /**
     * Returns the XRO path information.
     *
     * @return XRO path information
     */
    Path xroPath();

    String vnName();

    /**
     * Builder for pce path report.
     */
    interface Builder {

        /**
         * Returns the builder object of path name.
         *
         * @param name path name
         * @return builder object of path name
         */
        Builder pathName(String name);

        /**
         * Returns the builder object of report SRP id.
         *
         * @param srpId SRP id
         * @return builder object
         */
        Builder srpId(String srpId);

        /**
         * Returns the builder object of path plsp id.
         *
         * @param plspId path plsp id
         * @return builder object of path plspid
         */
        Builder plspId(String plspId);

        /**
         * Returns the builder object of path localLsp id.
         *
         * @param localLspId path localLsp id
         * @return builder object of path localLspId
         */
        Builder localLspId(String localLspId);

        /**
         * Returns the builder object of path tunnel id.
         *
         * @param tunnelId path tunnel id
         * @return builder object of path tunnelId
         */
        Builder pceTunnelId(String tunnelId);

        /**
         * Returns the builder object of path delegate state.
         *
         * @param delegate path delegate state
         * @return builder object of path delegate state
         */
        Builder isDelegate(boolean delegate);

        /**
         * Returns the builder object of path sync state.
         *
         * @param sync path sync state
         * @return builder object of path sync state
         */
        Builder isSync(boolean sync);

        /**
         * Returns the builder object of path removed state.
         *
         * @param removed path removed state
         * @return builder object of path removed state
         */
        Builder isRemoved(boolean removed);

        /**
         * Returns the builder object of path administrative state.
         *
         * @param adminState path removed administrative state
         * @return builder object of path administrative state
         */
        Builder adminState(State adminState);

        /**
         * Returns the builder object of path operation state.
         *
         * @param state path operation state
         * @return builder object of path operation state
         */
        Builder state(State state);

        /**
         * Returns the builder object of path ingress.
         *
         * @param ingress path ingress
         * @return builder object of path ingress
         */
        Builder ingress(IpAddress ingress);

        /**
         * Returns the builder object of path egress.
         *
         * @param egress path egress
         * @return builder object of path egress
         */
        Builder egress(IpAddress egress);

        /**
         * Returns the builder object of path error information.
         *
         * @param error path error information
         * @return builder object of path error information
         */
        Builder errorInfo(String error);

        /**
         * Returns the builder object of path ERO information.
         *
         * @param path ERO path information
         * @return builder object of path ERO information
         */
        Builder eroPath(Path path);

        /**
         * Returns the builder object of path RRO information.
         *
         * @param path RRO path information
         * @return builder object of path RRO information
         */
        Builder rroPath(Path path);

        /**
         * Returns the builder object of path XRO information.
         *
         * @param path XRO path information
         * @return builder object of path XRO information
         */
        Builder xroPath(Path path);

        Builder vnName(String vnName);
        /**
         * Builds object of pce path report.
         *
         * @return object of pce path report
         */
        PcePathReport build();
    }
}