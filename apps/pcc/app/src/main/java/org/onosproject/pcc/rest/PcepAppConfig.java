package org.onosproject.pcc.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.config.Config;
import org.onosproject.pcc.pccmgr.api.PcepCfg;
import org.onosproject.pcc.pccmgr.ctl.PcepConfig;

import java.util.ArrayList;
import java.util.List;

import static org.onosproject.net.config.Config.FieldPresence.MANDATORY;
import static org.onosproject.net.config.Config.FieldPresence.OPTIONAL;

/**
 * Created by root1 on 9/6/16.
 */
public class PcepAppConfig  extends Config<ApplicationId> {

    PcepCfg pccConfig = null;

    public static final String PCEP_PEER = "pcepPeer";
    public static final String PEER_IP = "peerIp";
    public static final String AS_NUMBER = "asNumber";

    @Override
    public boolean isValid() {

        boolean fields = false;

        pccConfig = PcepConfig.getInstance();

        fields = hasOnlyFields(PEER_IP, AS_NUMBER) && isIpAddress(PEER_IP, MANDATORY)
                && isNumber(AS_NUMBER, OPTIONAL);

        if (!fields) {
            return false;
        }

        return true;
    }

    public boolean validatePcepConfiguration() {
        if (!validatePcepPeers()) {
            return false;
        }

        return true;
    }

    public boolean validatePcepPeers() {
        List<PcepPeerConfig> nodes;

        nodes = pcepPeer();

        /* can do specific validation for each field */

        return true;
    }

    public List<PcepPeerConfig> pcepPeer() {
        List<PcepPeerConfig> nodes = new ArrayList<PcepPeerConfig>();

        JsonNode jsonNodes = object.get(PCEP_PEER);
        if (jsonNodes == null) {
            return null;
        }

        jsonNodes.forEach(jsonNode -> nodes.add(new PcepPeerConfig(
                jsonNode.path(PEER_IP).asText(),
                jsonNode.path(AS_NUMBER).asInt())));

        return nodes;
    }

    /**
     * Configuration for Pcep peer nodes.
     */
    public static class PcepPeerConfig {

        private final String peerIp;
        private final int asNumber;


        public PcepPeerConfig(String peerIp, int asNumber) {
            this.peerIp = peerIp;
            this.asNumber = asNumber;
        }

        /**
         * Returns hostname of the peer node.
         *
         * @return hostname
         */
        public String peerIp() {
            return this.peerIp;
        }

        /**
         * Returns asNumber if peer.
         *
         * @return asNumber
         */
        public int asNumber() {
            return this.asNumber;
        }
    }
}
