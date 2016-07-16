package org.onosproject.vn.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onlab.util.DataRateUnit;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.pce.pceservice.constraint.CostConstraint;
import org.onosproject.pce.pceservice.constraint.PceBandwidthConstraint;
import org.onosproject.vn.vnservice.api.VnService;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Supports compute optimal path.
 */
@Command(scope = "onos", name = "vn-compute-optimalpath", description = "Supports compute optimal path.")
public class VnComputeOptimalPathCommand extends AbstractShellCommand {
    private final Logger log = getLogger(getClass());

    @Option(name = "-ct", aliases = "--costType", description = "The cost attribute IGP cost (1) or TE cost (2).",
            required = false, multiValued = false)
    int costType = 2;

    @Option(name = "-b", aliases = "--bandwidth", description = "The bandwidth attribute of path. "
            + "Data rate unit is in BPS.", required = false, multiValued = false)
    double bandwidth = 0.0;

    @Argument(index = 0, name = "vnName", description = "virtual network name.", required = true, multiValued = false)
    String vnName = null;

    @Argument(index = 1, name = "srcPoint", description = "Source Point (e.g., L1:device5).",
            required = true, multiValued = false)
    String srcPoint = null;

    @Argument(index = 2, name = "dstPoints", description = "Destination Points " +
            " (e.g., L3:device1 L3:device2 L3:device3 L3:device4).",
            required = true, multiValued = true)
    List<String> dstPoints = null;

    @Override
    protected void execute() {
        log.info("executing vn-Compute-MultiDestination");

        DeviceId src = null;
        List<DeviceId> dst = new LinkedList<>();
        List<Constraint> listConstrnt = new LinkedList<>();

        VnService service = get(VnService.class);

        if (vnName == null) {
            error("Should specify Vn Name!!.");
        }

        if (srcPoint == null || dstPoints == null) {
            error("Should specify Source and Destination values!!.");
        }

        src = DeviceId.deviceId(srcPoint);

        for (String endPointElement: dstPoints) {
            dst.add(DeviceId.deviceId(endPointElement));
        }

        // Add bandwidth
        // bandwidth default data rate unit is in BPS
        if (bandwidth != 0.0) {
            listConstrnt.add(PceBandwidthConstraint.of(bandwidth, DataRateUnit.valueOf("BPS")));
        }

        // Add cost
        // Cost validation
        if ((costType < 1) || (costType > 2)) {
            error("The cost attribute value either IGP cost(1) or TE cost(2).");
            return;
        }
        // Here 'cost - 1' indicates the index of enum
        CostConstraint.Type costConstType = CostConstraint.Type.values()[costType - 1];
        listConstrnt.add(CostConstraint.of(costConstType));

        if (!service.computeOptimalPath(vnName, listConstrnt, src, dst)) {
            error("Setup optimal path failed. please check source and destinations !!.");
        }
    }
}
