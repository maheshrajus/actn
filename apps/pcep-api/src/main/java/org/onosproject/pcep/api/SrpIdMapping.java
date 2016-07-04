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

package org.onosproject.pcep.api;

public class SrpIdMapping {
    private int mdscSrpId;
    private int pncSrpId;
    private int rptSrpId;

    /**
     * Initializes object with SRP ids used in report msg.
     *
     * @param mdscSrpId SRP id used in report msg from MDSC to PNC
     * @param pncSrpId SRP id used in report msg from PNC to device for the above mdscSrpId
     * @param rptSrpId SRP id received in the current report msg from device to PNC
     */
    public SrpIdMapping(int mdscSrpId, int pncSrpId, int rptSrpId) {
        this.mdscSrpId = mdscSrpId;
        this.pncSrpId = pncSrpId;
        this.rptSrpId = rptSrpId;
    }

    /**
     * Sets SRP id used in report msg from MDSC to PNC.
     *
     * @param mdscSrpId SRP id used in report msg from MDSC to PNC
     */
    public void setMdscSrpId(int mdscSrpId) {
        this.mdscSrpId = mdscSrpId;
    }

    /**
     * Sets SRP id used in report msg from PNC to device for a corresponding SRP id from MDSC.
     *
     * @param pncSrpId SRP id used in report msg from PNC to device
     */
    public void setPncSrpId(int pncSrpId) {
        this.pncSrpId = pncSrpId;
    }

    /**
     * Sets the SRP id received in the current report msg from device to PNC.
     *
     * @param rptSrpId SRP id received in the current report msg from device to PNC
     */
    public void setRptSrpId(int rptSrpId) {
        this.rptSrpId = rptSrpId;
    }

    /**
     * Returns the SRP id used in report msg from MDSC to PNC.
     *
     * @return SRP id used in report msg from MDSC to PNC
     */
    public int mdscSrpId() {
        return mdscSrpId;
    }

    /**
     * Returns SRP id used in report msg from PNC to device for a corresponding SRP id from MDSC.
     *
     * @return SRP id used in report msg from PNC to device for a corresponding SRP id from MDSCs
     */
    public int pncSrpId() {
        return pncSrpId;
    }

    /**
     * Returns the SRP id received in the current report msg from device to PNC.
     *
     * @return SRP id received in the current report msg from device to PNC
     */
    public int rptSrpId() {
        return rptSrpId;
    }
}
