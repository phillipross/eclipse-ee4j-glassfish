/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.mr8;

import java.net.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Security::JACCMR8";
    private static String contextPath = "/jaccmr8";

    private String host;
    private String port;
    private String username;
    private String password;
    private String rolename;
    private String otheruser;
    private String ejbmode = "None";

    public static void main(String[] args) {
        stat.addDescription(testSuite);
        Client client = new Client(args);
        client.doTests();
        stat.printSummary();
    }

    public Client(String[] args) {
        host = args[0];
        port = args[1];
        username = args[2];
        password = args[3];
        rolename = args[4];
        otheruser = args[5];
        System.out.println("      Host: " + host);
        System.out.println("      Port: " + port);
        System.out.println("  Username: " + username);
        System.out.println("  Rolename: " + rolename);
        System.out.println("Other User: " + otheruser);
    }

    public void doTests() {
        // Use the stateful EJB inside the servlet
        // The stateful EJB uses annotations to protect the EJB
        ejbmode = "stateful";
        testAnyAuthUser();
        testAnyAuthUserOther();
        testAnyAuthUserNone();
        testDenyUncovered();
        testDenyUncoveredOther();
        testDenyUncoveredNone();
        testStar();
        testStarOther();
        testStarNone();
        testServlet();
        testServletOther();
        testServletNone();
        testAuthUser();
        testAuthUserOther();
        testAuthUserNone();

        // Use the stateless EJB inside the servlet
        // The stateless EJB uses the deployment descriptor to protect the EJB
        // Only repeat tests that actually can invoke the servlet
        ejbmode = "stateless";
        testAnyAuthUser();
        testAnyAuthUserOther();
        testStar();
        testAuthUser();
        testServlet();
        testServletOther();
        testServletNone();
    }

    public void testAnyAuthUser() {
        String servlet = "/anyauthuser";
        String description = servlet+"-"+username+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,true,true,true,true,true,true);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testAnyAuthUserOther() {
        String servlet = "/anyauthuser";
        String description = servlet+"-"+otheruser+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, otheruser, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,false,true,true,false,false,true);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testAnyAuthUserNone() {
        String servlet = "/anyauthuser";
        String description = servlet+"--"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 401, null, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testAuthUser() {
        String servlet = "/authuser";
        String description = servlet+"-"+username+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,true,true,true,true,true,true);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testAuthUserOther() {
        String servlet = "/authuser";
        String description = servlet+"-"+otheruser+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 403, otheruser, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testAuthUserNone() {
        String servlet = "/authuser";
        String description = servlet+"--"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 401, null, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testStar() {
        String servlet = "/star";
        String description = servlet+"-"+username+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,true,true,true,true,true,true);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testStarOther() {
        String servlet = "/star";
        String description = servlet+"-"+otheruser+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 403, otheruser, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testStarNone() {
        String servlet = "/star";
        String description = servlet+"--"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 401, null, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testServlet() {
        String servlet = "/servlet";
        String description = servlet+"-"+username+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, username, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,false,false,false,false,false,false);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testServletOther() {
        String servlet = "/servlet";
        String description = servlet+"-"+otheruser+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, otheruser, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,false,false,false,false,false,false);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testServletNone() {
        String servlet = "/servlet";
        String description = servlet+"--"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 200, null, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // Check results in output
        success = checkResults(output,false,false,false,false,false,false);
        if (!success) {
            System.out.println("Incorrect results:" + description);
            stat.addStatus(description, stat.FAIL);
            return;
        }

        stat.addStatus(description, stat.PASS);
    }

    public void testDenyUncovered() {
        String servlet = "/denyuncoveredpost";
        String description = servlet+"-"+username+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 403, username, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testDenyUncoveredOther() {
        String servlet = "/denyuncoveredpost";
        String description = servlet+"-"+otheruser+"-"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 403, otheruser, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    public void testDenyUncoveredNone() {
        String servlet = "/denyuncoveredpost";
        String description = servlet+"--"+ejbmode;
        StringBuffer output = new StringBuffer();
        boolean success = doIndividualTest(servlet, 403, null, ejbmode, output);
        if (!success) {
            stat.addStatus(description, stat.FAIL);
            return;
        }

        // No results to check!
        stat.addStatus(description, stat.PASS);
    }

    // Validate that all the passed in results are as expected
    // If any value is not as expected the overall results are false
    private boolean checkResults(StringBuffer results,
            boolean EJBisCallerInRole, boolean EJBisUserInAnyAuthUserRole,
            boolean EJBInvokeAnyAuthUser, boolean EJBInvokeAuthUser,
            boolean WEBisUserInRole, boolean WEBisUserInAnyAuthUserRole) {
        int index;
        boolean result = true;

        if (EJBisCallerInRole)
            index = results.indexOf("EJB isCallerInRole: true");
        else
            index = results.indexOf("EJB isCallerInRole: false");
        if (index == -1)
            result = false;

        if (!result) return result;

        if (EJBisUserInAnyAuthUserRole)
            index = results.indexOf("EJB isUserInAnyAuthUserRole: true");
        else
            index = results.indexOf("EJB isUserInAnyAuthUserRole: false");
        if (index == -1)
            result = false;

        if (!result) return result;

        if (WEBisUserInRole)
            index = results.indexOf("WEB isUserInRole: true");
        else
            index = results.indexOf("WEB isUserInRole: false");
        if (index == -1)
            result = false;

        if (!result) return result;

        if (WEBisUserInAnyAuthUserRole)
            index = results.indexOf("WEB isUserInAnyAuthUserRole: true");
        else
            index = results.indexOf("WEB isUserInAnyAuthUserRole: false");
        if (index == -1)
            result = false;

        if (!result) return result;

        index = results.indexOf("EJB Invoke AnyAuthUser: Yes");
        if (EJBInvokeAnyAuthUser)
            result = (index != -1);
        else
            result = (index == -1);

        if (!result) return result;

        index = results.indexOf("EJB Invoke AuthUser: Yes");
        if (EJBInvokeAuthUser)
            result = (index != -1);
        else
            result = (index == -1);

        return result;
    }

    private boolean doIndividualTest(String servlet, int code, String user, String mode, StringBuffer output) {
        boolean result = false;
        try {
            int rtncode;
            String url = "http://" + host + ":" + port + contextPath + servlet;

            Hashtable ht = new Hashtable();
            ht.put("mode", URLEncoder.encode(mode,"UTF-8"));
            ht.put("name", URLEncoder.encode(rolename,"UTF-8"));

            System.out.println("\nInvoking servlet at " + url);
            rtncode = invokeServlet(url, ht, user, output);
            System.out.println("The servlet return code: " + rtncode);
            if (rtncode != code) {
                System.out.println("Incorrect return code, expecting: " + code);
            }
            else result = true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.toString());
            //ex.printStackTrace();
        }
        return result;
    }

    private int invokeServlet(String url, Hashtable contentHash, String user, StringBuffer output) throws Exception {
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
        c1.setAllowUserInteraction(true);
        if ((user != null) && (user.length() > 0)) {
            // Add BASIC header for authentication
            String auth =  user + ":" + password;
            String authEncoded = new sun.misc.BASE64Encoder().encode(auth.getBytes());
            c1.setRequestProperty("Authorization", "Basic " + authEncoded);
        }
        c1.setDoOutput(true);
        c1.setUseCaches(false);

        // get the output stream to POST to.
        DataOutputStream out;
        out = new DataOutputStream(c1.getOutputStream());
        String content = "";

        // Create a single String value to be POSTED from the parameters passed
        // to us. This is done by making "name"="value" pairs for all the keys
        // in the Hashtable passed to us.
        Enumeration e = contentHash.keys();
        boolean first = true;
        while (e.hasMoreElements()) {
            // For each key and value pair in the hashtable
            Object key = e.nextElement();
            Object value = contentHash.get(key);

            // If this is not the first key-value pair in the hashtable,
            // concantenate an "&" sign to the constructed String
            if (!first)
                content += "&";

            // append to a single string. Encode the value portion
            content += (String) key + "=" + URLEncoder.encode((String) value,"UTF-8");

            first = false;
        }

        // Write out the bytes of the content string to the stream.
        out.writeBytes(content);
        out.flush();
        out.close();

        // Connect and get the response code and/or output to verify
        c1.connect();
        int code = c1.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = c1.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                while ((line = input.readLine()) != null) {
                    output.append(line);
                    System.out.println(line);
                }
            }
            finally {
                try { if (is != null) is.close(); }
                catch (Exception exc) {}
                try { if (input != null) input.close(); }
                catch (Exception exc) {}
            }
        }
        return code;
    }
}
