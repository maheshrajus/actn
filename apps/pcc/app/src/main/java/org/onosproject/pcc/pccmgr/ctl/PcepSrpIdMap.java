package org.onosproject.pcc.pccmgr.ctl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by swapna on 6/12/16.
 */
public enum PcepSrpIdMap {

    INSTANCE;

    private static Map<byte[], Integer> srpIdMap = new HashMap<byte[], Integer>();

    public static void add(byte[] symbolicPathName, Integer srpId) {
        srpIdMap.put(symbolicPathName, srpId);
    }


    public static Integer getSrpId(byte[] symbolicPathName) {
        if (srpIdMap.containsKey(symbolicPathName)) {
            return srpIdMap.get(symbolicPathName);
        } else {
            return 0;
        }
    }

    public static void remove(byte[] symbolicPathName) {
        srpIdMap.remove(symbolicPathName);
    }

}
