/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.glassfish.bootstrap.osgi;

import com.sun.enterprise.glassfish.bootstrap.LogFacade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * This is a specialized {@link BundleProvisioner} that installs only a minimum set of of bundles.
 * It derives the set of bundles to be included from the list of bundles to be started and all fragment bundles
 * available in the installation locations.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
public class MinimalBundleProvisioner extends BundleProvisioner {
    private Logger logger = LogFacade.BOOTSTRAP_LOGGER;
    private List<Long> installedBundleIds;
    static class MinimalCustomizer extends DefaultCustomizer {
        private Logger logger = LogFacade.BOOTSTRAP_LOGGER;
        public MinimalCustomizer(Properties config) {
            super(config);
        }

        public Jar getLatestJar() {
            File latestFile = null;
            for (URI uri : getConfiguredAutoInstallLocations()) {
                File file = null;
                try {
                    file = new File(uri);
                } catch (Exception e) {
                    continue; // not a file, skip to next one
                }
                if (latestFile == null) {
                    latestFile = file;
                }
                if (file.lastModified() > latestFile.lastModified()) {
                    latestFile = file;
                }
                if (file.isDirectory()) {
                    // do only one-level search as configured auto install locations are not recursive.
                    for (File child : file.listFiles()) {
                        if (child.lastModified() > latestFile.lastModified()) {
                            latestFile = child;
                        }
                    }
                }
            }
            return latestFile != null ? new Jar(latestFile) : null;
        }

        @Override
        public List<URI> getAutoInstallLocations() {
            // We only install those bundles that are required to be started  or those bundles that are fragments
            List<URI> installLocations = getAutoStartLocations();
            List<URI> fragments = selectFragmentJars(super.getAutoInstallLocations());
            installLocations.addAll(fragments);
            logger.log(Level.INFO, LogFacade.SHOW_INSTALL_LOCATIONS, new Object[]{installLocations});
            return installLocations;
        }

        private List<URI> selectFragmentJars(List<URI> installLocations) {
            List<URI> fragments = new ArrayList<URI>();
            for (URI uri : installLocations) {
                InputStream is = null;
                JarInputStream jis = null;
                try {
                    is = uri.toURL().openStream();
                    jis = new JarInputStream(is);
                    Manifest m = jis.getManifest();
                    if (m != null && m.getMainAttributes().getValue(Constants.FRAGMENT_HOST) != null) {
                        logger.logp(Level.FINE, "MinimalBundleProvisioner$MinimalCustomizer", "selectFragmentJars",
                                "{0} is a fragment", new Object[]{uri});
                        fragments.add(uri);
                    }
                } catch (IOException e) {
                    LogFacade.log(logger, Level.INFO, LogFacade.CANT_TELL_IF_FRAGMENT, e, uri);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (jis != null) {
                            jis.close();
                        }
                    } catch (IOException e1) {
                        // ignore
                    }
                }
            }
            return fragments;
        }
    }

    public MinimalBundleProvisioner(BundleContext bundleContext, Properties config) {
        super(bundleContext, new MinimalCustomizer(config));
    }

    @Override
    public List<Long> installBundles() {
        BundleContext bctx = getBundleContext();
        final int n = bctx.getBundles().length;
        List<Long> bundleIds;
        if (n > 1) {
            // This is not the first run of the program, so don't do anything
            logger.logp(Level.FINE, "MinimalBundleProvisioner", "installBundles",
                    "Skipping installation of bundles as there are already {0} no. of bundles.", new Object[]{n});
            bundleIds = Collections.emptyList();
        } else {
            bundleIds = super.installBundles();
        }
        return installedBundleIds = bundleIds;
    }

    @Override
    public void startBundles() {
        if (installedBundleIds.isEmpty()) {
            logger.log(Level.INFO, LogFacade.SKIP_STARTING_ALREADY_PROVISIONED_BUNDLES);
        } else {
            super.startBundles();
        }
    }

    @Override
    public boolean hasAnyThingChanged() {
        long latestBundleTimestamp = -1;
        Bundle latestBundle = null;
        for (Bundle b : getBundleContext().getBundles()) {
            if (b.getLastModified() > latestBundleTimestamp) {
                latestBundleTimestamp = b.getLastModified();
                latestBundle = b;
            }
        }
        Jar latestJar = getCustomizer().getLatestJar();
        final boolean chnaged = latestJar.getLastModified() > latestBundle.getLastModified();
        logger.log(Level.INFO, LogFacade.LATEST_FILE_IN_INSTALL_LOCATION,
                new Object[]{chnaged, latestJar.getURI(), latestBundle.getLocation()});
        return chnaged;
    }

    @Override
    public void refresh() {
        // uninstall everything and start afresh
        for (Bundle b : getBundleContext().getBundles()) {
            // TODO(Sahoo): We should call getCustomizer().isManaged(new Jar(b)),
            // but obr gives us the ability to encode information in url
            if (b.getBundleId() != 0) {
                try {
                    b.uninstall();
                } catch (BundleException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        installBundles();
        super.refresh();
        setSystemBundleUpdationRequired(true);
    }

    @Override
    public MinimalCustomizer getCustomizer() {
        return (MinimalCustomizer) super.getCustomizer();
    }
}
