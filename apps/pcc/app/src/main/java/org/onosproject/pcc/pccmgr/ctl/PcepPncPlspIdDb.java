package org.onosproject.pcc.pccmgr.ctl;

import org.onlab.packet.Ip4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root1 on 29/7/16.
 */
public class PcepPncPlspIdDb {
    private static int pncPlspId;

    private static Map<Ip4Address, Map<Integer, Integer>> plspIdMap = new HashMap<Ip4Address, Map<Integer, Integer>>();
    protected static final Logger log = LoggerFactory.getLogger(PcepPncPlspIdDb.class);

    public static int add(Ip4Address ingress, int plspId) {
        Map<Integer, Integer> innerMap = null;

        if (plspIdMap.containsKey(ingress)) {
            innerMap = plspIdMap.get(ingress);
        } else {
            innerMap = new HashMap<>();
        }

        pncPlspId++;
        innerMap.put(plspId, pncPlspId);

        plspIdMap.put(ingress, innerMap);

        log.info("adding entry for Ingress: " + ingress.toString() + " PlspId: " + plspId + " PncPlspId: " + pncPlspId);
        return pncPlspId;
    }

    public static int getPncPlspId(Ip4Address ingress, int plspId) {
        if (plspIdMap.containsKey(ingress)) {
            Map<Integer, Integer> innerMap = plspIdMap.get(ingress);
            if (innerMap.containsKey(plspId)) {
                return innerMap.get(plspId);
            }
        }

        return 0;
    }

    public static int remove(Ip4Address ingress, int plspId) {
        int currPncPlspId = 0;
        if (0 != getPncPlspId(ingress, plspId)) {

            Map<Integer, Integer> innerMap = plspIdMap.get(ingress);
            currPncPlspId = innerMap.get(plspId);
            innerMap.remove(plspId);

            if (innerMap.isEmpty()) {
                plspIdMap.remove(ingress);
            } else {
                plspIdMap.put(ingress, innerMap);
            }
        }

        log.info("removing entry for Ingress: " + ingress.toString() + " PlspId: " + plspId + " PncPlspId: " + currPncPlspId);

        return currPncPlspId;
    }

    public static int getPlspIdByPncPlspId(int pncPlspId) {

        for (Map.Entry<Ip4Address, Map<Integer, Integer>> entry : plspIdMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerMapEntry : entry.getValue().entrySet()) {
                if (innerMapEntry.getValue() == pncPlspId) {
                    log.info("PncPlspId: " + pncPlspId + " PlspId: " + innerMapEntry.getKey());
                    return innerMapEntry.getKey();
                }
            }
        }

        log.info("PncPlspId: " + pncPlspId + " PlspId: not found");

        return 0;
    }

    public static Ip4Address getIngressByPncPlspId(int pncPlspId) {

        for (Map.Entry<Ip4Address, Map<Integer, Integer>> entry : plspIdMap.entrySet()) {
            for (Map.Entry<Integer, Integer> innerMapEntry : entry.getValue().entrySet()) {
                if (innerMapEntry.getValue() == pncPlspId) {
                    log.info("PncPlspId: " + pncPlspId + " Ingress: " + entry.getKey().toString());
                    return entry.getKey();
                }
            }
        }

        log.info("PncPlspId: " + pncPlspId + " Ingress: not found");
        return null;
    }
}
