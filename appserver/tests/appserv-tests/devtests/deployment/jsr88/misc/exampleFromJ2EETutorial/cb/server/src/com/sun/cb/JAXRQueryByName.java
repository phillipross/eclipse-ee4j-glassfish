/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.net.*;
import java.util.*;

/**
 * The JAXRQueryByName class consists of a main method, a
 * makeConnection method, an executeQuery method, and some
 * helper methods. It searches a registry for
 * information about organizations whose names contain a
 * user-supplied string.
 *
 * To run this program, use the command
 *
 *     ant -Dquery-string=<value> run-query
 *
 * after starting Tomcat and Xindice.
 */
public class JAXRQueryByName {
    static Connection connection = null;

    public JAXRQueryByName() {}

    public static void main(String[] args) {
      String queryURL = URLHelper.getQueryURL();
      String publishURL = URLHelper.getPublishURL();

        if (args.length < 1) {
            System.out.println("Usage: ant " +
                "-Dquery-string=<value> run-query");
            System.exit(1);
        }
        String queryString = new String(args[0]);
        System.out.println("Query string is " + queryString);

        JAXRQueryByName jq = new JAXRQueryByName();

        connection = jq.makeConnection(queryURL, publishURL);

        jq.executeQuery(queryString);
    }

    /**
     * Establishes a connection to a registry.
     *
     * @param queryUrl    the URL of the query registry
     * @param publishUrl    the URL of the publish registry
     * @return the connection
     */
    public Connection makeConnection(String queryUrl,
        String publishUrl) {

        /*
         * Edit to provide your own proxy information
         *  if you are going beyond your firewall.
         * Host format: "host.subdomain.domain.com".
         * Port is usually 8080.
         * Leave blank to use Registry Server.
         */
        String httpProxyHost = "";
        String httpProxyPort = "";

        /*
         * Define connection configuration properties.
         * For simple queries, you need the query URL.
         * To obtain the connection factory class, set a System
         *   property.
         */
        Properties props = new Properties();
        props.setProperty("javax.xml.registry.queryManagerURL",
            queryUrl);
        props.setProperty("com.sun.xml.registry.http.proxyHost",
            httpProxyHost);
        props.setProperty("com.sun.xml.registry.http.proxyPort",
            httpProxyPort);

        try {
            // Create the connection, passing it the
            // configuration properties
            ConnectionFactory factory =
                ConnectionFactory.newInstance();
            factory.setProperties(props);
            connection = factory.createConnection();
            System.out.println("Created connection to registry");
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (JAXRException je) {}
            }
        }
        return connection;
    }

    /**
     * Returns  organizations containing a string.
     *
     * @param qString    the string argument
     * @return a collection of organizations
     */
    public Collection executeQuery(String qString) {
        RegistryService rs = null;
        BusinessQueryManager bqm = null;
        Collection orgs = null;

        try {
            // Get registry service and query manager
            rs = connection.getRegistryService();
            bqm = rs.getBusinessQueryManager();
            System.out.println("Got registry service and " + "query manager");

            // Define find qualifiers and name patterns
            Collection findQualifiers = new ArrayList();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection namePatterns = new ArrayList();
            // % still doesn't work
            namePatterns.add(qString);
            //namePatterns.add("%" + qString + "%");

            // Find using the name
            BulkResponse response =
                bqm.findOrganizations(findQualifiers,
                    namePatterns, null, null, null, null);
            orgs = response.getCollection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return orgs;
    }

    /**
     * Returns the name value for a registry object.
     *
     * @param ro    a RegistryObject
     * @return        the String value
     */
    public String getName(RegistryObject ro)
        throws JAXRException {

        try {
            return ro.getName().getValue();
        } catch (NullPointerException npe) {
            return "No Name";
        }
    }

    /**
     * Returns the description value for a registry object.
     *
     * @param ro    a RegistryObject
     * @return        the String value
     */
    public String getDescription(RegistryObject ro)
        throws JAXRException {
        try {
            return ro.getDescription().getValue();
        } catch (NullPointerException npe) {
            return "No Description";
        }
    }

    /**
     * Returns the key id value for a registry object.
     *
     * @param ro    a RegistryObject
     * @return        the String value
     */
    public String getKey(RegistryObject ro)
        throws JAXRException {

        try {
            return ro.getKey().getId();
        } catch (NullPointerException npe) {
            return "No Key";
        }
    }
}
