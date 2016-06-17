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

package org.onosproject.pcep.pcepio.protocol.ver1;

import org.onosproject.pcep.pcepio.protocol.PcInitiatedLspRequest;
import org.onosproject.pcep.pcepio.protocol.PcepAssociationObject;
import org.onosproject.pcep.pcepio.protocol.PcepAttribute;
import org.onosproject.pcep.pcepio.protocol.PcepBandwidthObject;
import org.onosproject.pcep.pcepio.protocol.PcepCloseMsg;
import org.onosproject.pcep.pcepio.protocol.PcepEndPointsObject;
import org.onosproject.pcep.pcepio.protocol.PcepEroObject;
import org.onosproject.pcep.pcepio.protocol.PcepErrorInfo;
import org.onosproject.pcep.pcepio.protocol.PcepError;
import org.onosproject.pcep.pcepio.protocol.PcepErrorMsg;
import org.onosproject.pcep.pcepio.protocol.PcepErrorObject;
import org.onosproject.pcep.pcepio.protocol.PcepFactory;
import org.onosproject.pcep.pcepio.protocol.PcepFecObjectIPv4.Builder;
import org.onosproject.pcep.pcepio.protocol.PcepFecObjectIPv4Adjacency;
import org.onosproject.pcep.pcepio.protocol.PcepInitiateMsg;
import org.onosproject.pcep.pcepio.protocol.PcepIroObject;
import org.onosproject.pcep.pcepio.protocol.PcepKeepaliveMsg;
import org.onosproject.pcep.pcepio.protocol.PcepLabelObject;
import org.onosproject.pcep.pcepio.protocol.PcepLabelRangeObject;
import org.onosproject.pcep.pcepio.protocol.PcepLabelRangeResvMsg;
import org.onosproject.pcep.pcepio.protocol.PcepLabelUpdate;
import org.onosproject.pcep.pcepio.protocol.PcepLabelUpdateMsg;
import org.onosproject.pcep.pcepio.protocol.PcepLspObject;
import org.onosproject.pcep.pcepio.protocol.PcepLspaObject;
import org.onosproject.pcep.pcepio.protocol.PcepMessage;
import org.onosproject.pcep.pcepio.protocol.PcepMessageReader;
import org.onosproject.pcep.pcepio.protocol.PcepMetricObject;
import org.onosproject.pcep.pcepio.protocol.PcepMsgPath;
import org.onosproject.pcep.pcepio.protocol.PcepOpenMsg;
import org.onosproject.pcep.pcepio.protocol.PcepOpenObject;
import org.onosproject.pcep.pcepio.protocol.PcepReportMsg;
import org.onosproject.pcep.pcepio.protocol.PcepRroObject;
import org.onosproject.pcep.pcepio.protocol.PcepSrpObject;
import org.onosproject.pcep.pcepio.protocol.PcepStateReport;
import org.onosproject.pcep.pcepio.protocol.PcepUpdateMsg;
import org.onosproject.pcep.pcepio.protocol.PcepUpdateRequest;
import org.onosproject.pcep.pcepio.protocol.PcepVersion;
import org.onosproject.pcep.pcepio.protocol.PcepXroObject;

/**
 * Provides PCEP Factory and returns builder classes for all objects and messages.
 */
public class PcepFactoryVer1 implements PcepFactory {

    public static final PcepFactoryVer1 INSTANCE = new PcepFactoryVer1();

    @Override
    public PcepOpenMsg.Builder buildOpenMsg() {
        return new PcepOpenMsgVer1.Builder();
    }

    @Override
    public PcepOpenObject.Builder buildOpenObject() {
        return new PcepOpenObjectVer1.Builder();
    }

    @Override
    public PcepKeepaliveMsg.Builder buildKeepaliveMsg() {
        return new PcepKeepaliveMsgVer1.Builder();
    }

    @Override
    public PcepCloseMsg.Builder buildCloseMsg() {
        return new PcepCloseMsgVer1.Builder();
    }

    @Override
    public PcepUpdateMsg.Builder buildUpdateMsg() {
        return new PcepUpdateMsgVer1.Builder();
    }

    @Override
    public PcepReportMsg.Builder buildReportMsg() {
        return new PcepReportMsgVer1.Builder();
    }

    @Override
    public PcepInitiateMsg.Builder buildPcepInitiateMsg() {
        return new PcepInitiateMsgVer1.Builder();
    }

    @Override
    public PcepLspObject.Builder buildLspObject() {
        return new PcepLspObjectVer1.Builder();
    }

    @Override
    public PcepMessageReader<PcepMessage> getReader() {
        return PcepMessageVer1.READER;
    }

    @Override
    public PcepVersion getVersion() {
        return PcepVersion.PCEP_1;
    }

    @Override
    public PcepSrpObject.Builder buildSrpObject() {
        return new PcepSrpObjectVer1.Builder();
    }

    @Override
    public PcepEndPointsObject.Builder buildEndPointsObject() {
        return new PcepEndPointsObjectVer1.Builder();
    }

    @Override
    public PcepEroObject.Builder buildEroObject() {
        return new PcepEroObjectVer1.Builder();
    }

    @Override
    public PcepAssociationObject.Builder buildAssociationObject() {
        return new PcepAssociationObjectVer1.Builder();
    }

    @Override
    public PcepXroObject.Builder buildXroObject() {
        return new PcepXroObjectVer1.Builder();
    }

    @Override
    public PcepRroObject.Builder buildRroObject() {
        return new PcepRroObjectVer1.Builder();
    }

    @Override
    public PcepLspaObject.Builder buildLspaObject() {
        return new PcepLspaObjectVer1.Builder();
    }

    @Override
    public PcepIroObject.Builder buildIroObject() {
        return new PcepIroObjectVer1.Builder();
    }

    @Override
    public PcepMetricObject.Builder buildMetricObject() {
        return new PcepMetricObjectVer1.Builder();
    }

    @Override
    public PcepBandwidthObject.Builder buildBandwidthObject() {
        return new PcepBandwidthObjectVer1.Builder();
    }

    @Override
    public PcepMsgPath.Builder buildPcepMsgPath() {
        return new PcepMsgPathVer1.Builder();
    }

    @Override
    public PcepStateReport.Builder buildPcepStateReport() {
        return new PcepStateReportVer1.Builder();
    }

    @Override
    public PcepUpdateRequest.Builder buildPcepUpdateRequest() {
        return new PcepUpdateRequestVer1.Builder();
    }

    @Override
    public PcInitiatedLspRequest.Builder buildPcInitiatedLspRequest() {
        return new PcInitiatedLspRequestVer1.Builder();
    }

    @Override
    public PcepAttribute.Builder buildPcepAttribute() {
        return new PcepAttributeVer1.Builder();
    }

    @Override
    public PcepLabelUpdateMsg.Builder buildPcepLabelUpdateMsg() {
        return new PcepLabelUpdateMsgVer1.Builder();
    }

    @Override
    public PcepLabelUpdate.Builder buildPcepLabelUpdateObject() {
        return new PcepLabelUpdateVer1.Builder();
    }

    @Override
    public PcepLabelObject.Builder buildLabelObject() {
        return new PcepLabelObjectVer1.Builder();
    }

    @Override
    public PcepErrorMsg.Builder buildPcepErrorMsg() {
        return new PcepErrorMsgVer1.Builder();
    }

    @Override
    public PcepErrorObject.Builder buildPcepErrorObject() {
        return new PcepErrorObjectVer1.Builder();
    }

    @Override
    public PcepFecObjectIPv4Adjacency.Builder buildFecIpv4Adjacency() {
        return new PcepFecObjectIPv4AdjacencyVer1.Builder();
    }

    @Override
    public PcepErrorInfo.Builder buildPcepErrorInfo() {
        return new PcepErrorInfoVer1.Builder();
    }

    @Override
    public PcepError.Builder buildPcepError() {
        return new PcepErrorVer1.Builder();
    }

    @Override
    public PcepLabelRangeObject.Builder buildPcepLabelRangeObject() {
        return new PcepLabelRangeObjectVer1.Builder();
    }

    @Override
    public PcepLabelRangeResvMsg.Builder buildPcepLabelRangeResvMsg() {
        return new PcepLabelRangeResvMsgVer1.Builder();
    }

    @Override
    public Builder buildFecObjectIpv4() {
        return new PcepFecObjectIPv4Ver1.Builder();
    }
}
