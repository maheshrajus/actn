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
package org.onosproject.pce.pcestore;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.util.List;

import org.onosproject.pce.pcestore.api.LspLocalLabelInfo;

/**
 * PCECC tunnel information is used to store
 * list of links label information of a path containing IN, OUT label and destination port of a link
 * to release label allocation in devices.
 * Also storing resource consumer id to release bandwdith of a tunnel.
 * The first entry is created with TunnelId and resource consumer id,
 * later this entry may be updated to store label information on basic PCECC case.
 */
public final class PceccTunnelInfo {

    private List<LspLocalLabelInfo> lspLocalLabelInfoList;

    /**
     * Initialization of member variables.
     *
     * @param lspLocalLabelInfoList list of devices local label info
     */
    public PceccTunnelInfo(List<LspLocalLabelInfo> lspLocalLabelInfoList) {
        this.lspLocalLabelInfoList = lspLocalLabelInfoList;
    }

    /**
     * Initialization for serialization.
     */
    public PceccTunnelInfo() {
        this.lspLocalLabelInfoList = null;
    }

    /**
     * Retrieves list of devices local label info.
     *
     * @return list of devices local label info
     */
    public List<LspLocalLabelInfo> lspLocalLabelInfoList() {
        return this.lspLocalLabelInfoList;
    }

    /**
     * Sets list of local label info of a path.
     *
     * @param lspLocalLabelInfoList list of devices local label info
     */
    public void lspLocalLabelInfoList(List<LspLocalLabelInfo> lspLocalLabelInfoList) {
       this.lspLocalLabelInfoList = lspLocalLabelInfoList;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lspLocalLabelInfoList);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PceccTunnelInfo) {
            final PceccTunnelInfo other = (PceccTunnelInfo) obj;
            return Objects.equals(this.lspLocalLabelInfoList, other.lspLocalLabelInfoList);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .omitNullValues()
                .add("DeviceLabelInfoList", lspLocalLabelInfoList)
                .toString();
    }
}
