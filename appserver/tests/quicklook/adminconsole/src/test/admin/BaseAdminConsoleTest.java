/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.admin;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;

import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.base.DomainRoot;
import java.io.File;

/** The base class for admin console tests. Designed for extension.
 * @author jdlee@dev.java.net
 * @since GlassFish v3 Prelude
 */
public class BaseAdminConsoleTest {

    protected String adminUrl;
    private HttpClient client;
    private static final int AC_TEST_DELAY = 1000; // One second
    private static final int AC_TEST_ITERATIONS = 60; // One minute
    private String host;
    private int port;
    private volatile MBeanServerConnection mMBeanServerConnection;
    private volatile DomainRoot mDomainRoot;
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String CONSOLE_DIR_PATH = FILE_SEP + "lib" + FILE_SEP +
            "install" + FILE_SEP + "applications" + FILE_SEP + "__admingui";
    private static final String GLASSFISH_DIR = FILE_SEP + ".." + FILE_SEP +"glassfish";

    // Copied from Lloyd's AMX tests
    void setUpEnvironment(int port) {
        try {
            if (mMBeanServerConnection == null) {
                host = System.getProperty("http.host");
                this.port = port;

                mMBeanServerConnection = _getMBeanServerConnection();
                mDomainRoot = _getDomainRoot(mMBeanServerConnection);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This BeforeTest method will verify that the login form is available.  Once
     * it is found, the login form is submitted.  If the login succeeds, then
     * the tests are allowed to continue.  If the login fails, the each test will
     * fail.
     * @param url
     * @throws java.lang.Exception
     */
    @BeforeTest
    @Parameters({"admin.console.url", "amx.rmiport"})
    public void loginBeforeTest(String url, int port) throws Exception {
        this.adminUrl = url;
        setUpEnvironment(port);
        client = new HttpClient();

        boolean formFound = false;
        int iteration = 0;
        if (!checkForAdminConsoleDirectory()) {
            Assert.fail("The admin console directory was not found at " +
                this.getDomainRoot().getInstallDir() + GLASSFISH_DIR + CONSOLE_DIR_PATH +
                ".  Please check your installation.");
        }

        while (!formFound && iteration < AC_TEST_ITERATIONS) {
            iteration++;

            formFound = getUrlAndTestForStrings(adminUrl + "login.jsf","id=\"Login.username\"");
            if (!formFound) {
                System.err.println("***** Login page not found.  Sleeping to allow app to deploy (" +
                        iteration + " of " + AC_TEST_ITERATIONS + ")...");
                Thread.sleep(AC_TEST_DELAY);
            }
        }

        if (!formFound) {
            Assert.fail("The login form was not found.");
        }

        // The login for was found, so let's now POST the form to authenticate our session.
        PostMethod post = new PostMethod(adminUrl + "j_security_check");
        post.setRequestBody(new NameValuePair[]{
                    new NameValuePair("j_username", "admin"), new NameValuePair("j_password", "")
                });
        post.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

        int statusCode = client.executeMethod(post);
        if (statusCode == 302) {
            Header locationHeader = post.getResponseHeader("location");
            if (locationHeader != null) {
                Assert.assertTrue(locationHeader.getValue().startsWith(this.adminUrl));
            } else {
                Assert.fail("Failed to login: no redirect header");
            }
        } else if (statusCode != HttpStatus.SC_OK) {
            Assert.fail("Login failed: " + post.getStatusLine() + ": " + statusCode);
        }
    }

    /**
     * This method uses the AMX API to get the install directory, then checks to
     * see if the admin console directory is present.
     * @return
     */
    protected boolean checkForAdminConsoleDirectory() {
        // Hard-coding "../glassfish" to help cover a deficiency in IPS
        File directory = new File(this.getDomainRoot().getInstallDir() + GLASSFISH_DIR + CONSOLE_DIR_PATH);
        return directory.exists();
    }

    @AfterTest
    public void shutdownClient() {
        client = null;
    }

    /**
     * This method will request the specified URL and examine the response for the
     * needle specified.
     * @param url
     * @param needle
     * @return
     * @throws java.lang.Exception
     */
    protected boolean getUrlAndTestForStrings(String url, String... needles) throws IOException {
        String haystack = getUrl(url);
        boolean allFound = true;
        for (String needle : needles) {
            if (haystack.indexOf(needle) == -1) {
                allFound = false;
            }
        }

        return allFound;
    }

    /**
     * Request the specified URL and return the contents as a String
     * @param url
     * @return
     * @throws java.io.IOException
     */
    protected String getUrl(String url) throws IOException {
        GetMethod get = new GetMethod(url);
        get.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        get.setFollowRedirects(true);

        int statusCode = client.executeMethod(get);
        //Also accepts SC_ACCPTED ie 202 for the case when the console is still loading.
        if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_ACCEPTED)  ) {
            Assert.fail("BaseAdminConsoleTest.getUrlAndTestForString() failed.  HTTP Status Code:  " + statusCode);
        }
        String response = getString(get.getResponseBodyAsStream());
        get.releaseConnection();
        return response;
    }

    /**
     * Read the entire contents of the InputStream and return them as a String
     * @param in
     * @return
     * @throws java.io.IOException
     */
    protected String getString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        in.close();
        return out.toString();
    }

    /*
     * These methods were all copied from Lloyd's AMX QL tests.
     */

    protected DomainRoot getDomainRoot() {
        return mDomainRoot;
    }

    protected DomainRoot _getDomainRoot(final MBeanServerConnection conn)
            throws MalformedURLException, IOException, java.net.MalformedURLException {
        final ObjectName domainRootObjectName = AMXGlassfish.DEFAULT.bootAMX(conn);
        final DomainRoot domainRoot = ProxyFactory.getInstance(conn).getDomainRootProxy();
        return domainRoot;
    }

    private MBeanServerConnection _getMBeanServerConnection()
            throws MalformedURLException, IOException {
        // service:jmx:rmi:///jndi/rmi://192.168.1.8:8686/jmxrmi
        // service:jmx:jmxmp://localhost:8888
        // CHANGE to RMI once it's working
        //
        // final String urlStr = "service:jmx:jmxmp://" + mHost + ":" + mPort;
        final String urlStr = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";

        final JMXServiceURL url = new JMXServiceURL(urlStr);

        final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
        final MBeanServerConnection conn = jmxConn.getMBeanServerConnection();
        conn.getDomains();    // sanity check
        return conn;
    }
}

