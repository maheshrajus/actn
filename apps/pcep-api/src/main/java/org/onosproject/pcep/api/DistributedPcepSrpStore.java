package org.onosproject.pcep.api;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Versioned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@Service
public class DistributedPcepSrpStore implements PcepSrpStore {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    // Store the information to correlate parent and child PCE SRP ids.
    private ConsistentMap<String, SrpIdMapping> srpIdCorrelation;

    @Activate
    protected void activate() {
        srpIdCorrelation = storageService.<String, SrpIdMapping>consistentMapBuilder()
                .withName("onos-pce-srpIdCorrelation")
                .withSerializer(Serializer.using(
                        new KryoNamespace.Builder()
                                .register(KryoNamespaces.API)
                                .register(SrpIdMapping.class)
                                .build()))
                .build();
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    @Override
    public SrpIdMapping getSrpIdMapping(String pathName) {
        checkNotNull(pathName);
        Versioned<SrpIdMapping> srpIdMapping = srpIdCorrelation.get(pathName);
        return srpIdMapping == null ? null : srpIdMapping.value();
    }

    @Override
    public void addSrpIdMapping(String pathName, SrpIdMapping srpIdMapping) {
        checkNotNull(srpIdMapping);
        checkNotNull(pathName);
        srpIdCorrelation.put(pathName, srpIdMapping);
    }

    @Override
    public void updateSrpIdMapping(String pathName, SrpIdMapping srpIdMapping) {
        checkNotNull(pathName);
        checkNotNull(srpIdMapping);
        srpIdCorrelation.replace(pathName, srpIdMapping);
    }

    @Override
    public void removeSrpIdMapping(String pathName) {
        checkNotNull(pathName);
        srpIdCorrelation.remove(pathName);
    }
}
