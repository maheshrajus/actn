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
package org.onosproject.provider.bgp.linkcfg.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.bgp.controller.BgpCfg;
import org.onosproject.bgp.controller.BgpController;
import org.onosproject.bgp.controller.BgpLinkCfg;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * BGP config provider to validate and populate the configuration.
 */
@Component(immediate = true)
public class BgpLinkCfgProvider extends AbstractProvider {

    private static final Logger log = getLogger(BgpLinkCfgProvider.class);

    static final String PROVIDER_ID = "org.onosproject.provider.bgp.linkcfg";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected BgpController bgpController;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry configRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService configService;

    private final ConfigFactory configFactory =
            new ConfigFactory(SubjectFactories.APP_SUBJECT_FACTORY, BgpAppLinkConfig.class, "bgpLinkapp") {
                @Override
                public BgpAppLinkConfig createConfig() {
                    return new BgpAppLinkConfig();
                }
            };

    private final NetworkConfigListener configListener = new InternalConfigListener();

    private ApplicationId appId;

    /**
     * Creates a Bgp config provider.
     */
    public BgpLinkCfgProvider() {
        super(new ProviderId("bgp", PROVIDER_ID));
    }

    @Activate
    public void activate(ComponentContext context) {
        appId = coreService.registerApplication(PROVIDER_ID);
        configService.addListener(configListener);
        configRegistry.registerConfigFactory(configFactory);
        readConfiguration();
        log.info("Activated");
    }

    @Deactivate
    public void deactivate(ComponentContext context) {
        configRegistry.unregisterConfigFactory(configFactory);
        configService.removeListener(configListener);
        log.info("DeActivated");
    }

    void setBgpController(BgpController bgpController) {
        this.bgpController = bgpController;
    }

    /**
     * Reads the configuration and set it to the BGP-LS south bound protocol.
     */
    private void readConfiguration() {
        BgpCfg bgpConfig = null;
        List<BgpAppLinkConfig.BgpLinkConfig> nodes;
        bgpConfig = bgpController.getConfig();
        BgpAppLinkConfig config = configRegistry.getConfig(appId, BgpAppLinkConfig.class);

        if (config == null) {
            log.warn("No configuration found");
            return;
        }

        nodes = config.bgpLinks();
        for (int i = 0; i < nodes.size(); i++) {
            bgpConfig.addLink(DeviceId.deviceId(nodes.get(i).srcDeviceId()),
                              nodes.get(i).srcInterface(), nodes.get(i).cost(),
                              DeviceId.deviceId(nodes.get(i).dstDeviceId()),
                              nodes.get(i).dstInterface(), nodes.get(i).teCost(),
                              nodes.get(i).maxReservedBandwidth(),
                              nodes.get(i).maxBandwidth(),
                              nodes.get(i).unReservedBandwidth());
        }
    }

    /**
     * Read the configuration and update it to the BGP-LS south bound protocol.
     */
    private void updateConfiguration() {
        BgpCfg bgpConfig = null;
        List<BgpAppLinkConfig.BgpLinkConfig> nodes;
        TreeMap<String, BgpLinkCfg> bgpLinks;
        bgpConfig = bgpController.getConfig();
        BgpLinkCfg peer = null;
        BgpAppLinkConfig config = configRegistry.getConfig(appId, BgpAppLinkConfig.class);

        if (config == null) {
            log.warn("No configuration found");
            return;
        }

        /* update the peer configuration */
        bgpLinks = bgpConfig.getLinks();
        if (bgpLinks.isEmpty()) {
            log.info("There are no BGP links to iterate");
        } else {
            Set set = bgpLinks.entrySet();
            Iterator i = set.iterator();
            List<BgpLinkCfg> absPeerList = new ArrayList<BgpLinkCfg>();

            boolean exists = false;

            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                peer = (BgpLinkCfg) me.getValue();

                nodes = config.bgpLinks();
                for (int j = 0; j < nodes.size(); j++) {
                    String peerIp = nodes.get(j).srcDeviceId();
                    if (peerIp.equals(peer.srcDeviceId().toString())) {
                        nodes.remove(j);
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    absPeerList.add(peer);
                    exists = false;
                }
            }

            /* Remove the absent nodes. */
            for (int j = 0; j < absPeerList.size(); j++) {
                BgpLinkCfg link = absPeerList.get(j);
                bgpConfig.deleteLink(link.srcDeviceId(),
                                     link.srcInterface(),
                                     link.cost(),
                                     link.dstDeviceId(),
                                     link.dstInterface(),
                                     link.teCost(),
                                     link.maxReservedBandwidth(),
                                     link.maxBandwidth(),
                                     link.unReservedBandwidth());

            }
        }


        nodes = config.bgpLinks();
        for (int i = 0; i < nodes.size(); i++) {
            bgpConfig.addLink(DeviceId.deviceId(nodes.get(i).srcDeviceId()),
                              nodes.get(i).srcInterface(), nodes.get(i).cost(),
                              DeviceId.deviceId(nodes.get(i).dstDeviceId()),
                              nodes.get(i).dstInterface(), nodes.get(i).teCost(),
                              nodes.get(i).maxReservedBandwidth(),
                              nodes.get(i).maxBandwidth(),
                              nodes.get(i).unReservedBandwidth());

        }
    }

    /**
     * BGP config listener to populate the link configuration.
     */
    private class InternalConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            if (!event.configClass().equals(BgpAppLinkConfig.class)) {
                return;
            }

            switch (event.type()) {
                case CONFIG_ADDED:
                    readConfiguration();
                    break;
                case CONFIG_UPDATED:
                    updateConfiguration();
                    break;
                case CONFIG_REMOVED:
                default:
                    break;
            }
        }
    }
}
