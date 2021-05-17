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

import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
import org.glassfish.grizzly.test.http2.*;

/*
 * Unit test for deny-uncovered-http-methods.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-4.0-mapping-discovery";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;
    private String appName;
    private String serverLog;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
        appName = args[3];
        serverLog = args[4];

    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for mapping discovery");
        WebTest webTest = new WebTest(args);

        try {
            boolean contextRootMatch = webTest.run("GET", 200, false, "/", ".*ServletC.MappingImpl\\{matchValue=.*pattern=/.*servletName=.*,.*mappingMatch=CONTEXT_ROOT\\}.*FORWARD_MAPPING: null.*INCLUDE_MAPPING: null.*ASYNC_MAPPING: null.*");
            boolean defaultMatch = webTest.run("GET", 200, false, "//foo", ".*ServletC.MappingImpl\\{matchValue=.*pattern=/,.*servletName=ServletC.*mappingMatch=DEFAULT\\}.*FORWARD_MAPPING: null.*INCLUDE_MAPPING: null.*ASYNC_MAPPING: null.*");
            boolean exactMatch = webTest.run("GET", 200, false, "/ServletC", ".*ServletC.MappingImpl\\{matchValue=ServletC.*pattern=/ServletC.*servletName=ServletC.*mappingMatch=EXACT\\}.*FORWARD_MAPPING: null.*INCLUDE_MAPPING: null.*ASYNC_MAPPING: null.*");
            boolean extensionMatch = webTest.run("GET", 200, false, "/foo.Issue73", ".*ServletC.MappingImpl\\{matchValue=foo.*pattern=\\*\\.Issue73.*servletName=ServletC.*mappingMatch=EXTENSION\\}.*FORWARD_MAPPING: null.*INCLUDE_MAPPING: null.*ASYNC_MAPPING: null.*");
            boolean pathMatch = webTest.run("GET", 200, false, "/path/foo", ".*ServletC.MappingImpl\\{matchValue=foo.*pattern=/path/\\*.*servletName=ServletC.*mappingMatch=PATH\\}.*FORWARD_MAPPING: null.*INCLUDE_MAPPING: null.*ASYNC_MAPPING: null.*");

            stat.addStatus(TEST_NAME + "-simple", ((contextRootMatch &&
                    defaultMatch &&
                    exactMatch &&
                    extensionMatch &&
                    pathMatch)? stat.PASS : stat.FAIL));

            boolean asyncMatch1 = webTest.run("GET", 200, false, "/AAsyncDispatchToC", ".*ServletC.MappingImpl\\{matchValue=AAsyncDispatchToC.*pattern=/AAsyncDispatchToC.*servletName=AAsyncDispatchToC,.*mappingMatch=EXACT\\}.*FORWARD_MAPPING: null.*INCLUDE_MAPPING: null.*ASYNC_MAPPING:.*MappingImpl\\{matchValue=AAsyncDispatchToC.*pattern=/AAsyncDispatchToC.*servletName=AAsyncDispatchToC.*mappingMatch=EXACT}.*");

            boolean asyncMatch2 = webTest.run("GET", 200, false, "/BIncludeDispatchServletNamedDispatcher", ".*In.ServletC.MappingImpl\\{matchValue=BIncludeDispatchServletNamedDispatcher,.pattern=/BIncludeDispatchServletNamedDispatcher,.servletName=BIncludeDispatchServletNamedDispatcher,.mappingMatch=EXACT\\}.*FORWARD_MAPPING:.null.*INCLUDE_MAPPING:.null.*ASYNC_MAPPING:.MappingImpl\\{matchValue=BIncludeDispatchServletNamedDispatcher,.pattern=/BIncludeDispatchServletNamedDispatcher,.servletName=BIncludeDispatchServletNamedDispatcher,.mappingMatch=EXACT\\}.*");

            stat.addStatus(TEST_NAME + "-async",
                           asyncMatch1 &&
                           asyncMatch2 ? stat.PASS : stat.FAIL);

            boolean forwardMatch1 = webTest.run("GET", 200, false, "/AForwardToB", ".*ServletC.MappingImpl\\{matchValue=ServletC,.*pattern=/ServletC,.*servletName=ServletC,.*mappingMatch=EXACT\\}.*FORWARD_MAPPING:.MappingImpl\\{matchValue=AForwardToB,.pattern=/AForwardToB,.servletName=AForwardToB,.mappingMatch=EXACT\\}.*INCLUDE_MAPPING:.null.*ASYNC_MAPPING:.null.*");
            boolean forwardMatch2 = webTest.run("GET", 200, false, "/BForwardToC", ".*ServletC.MappingImpl\\{matchValue=ServletC,.*pattern=/ServletC,.*servletName=ServletC,.*mappingMatch=EXACT\\}.*FORWARD_MAPPING:.MappingImpl\\{matchValue=BForwardToC,.pattern=/BForwardToC,.servletName=BForwardToC,.mappingMatch=EXACT\\}.*INCLUDE_MAPPING:.null.*ASYNC_MAPPING:.null.*");

            stat.addStatus(TEST_NAME + "-forward", ((forwardMatch1 &&
                    forwardMatch2)? stat.PASS : stat.FAIL));

            boolean includeMatch1 = webTest.run("GET", 200, false, "/AIncludesB", ".*AIncludesB.MappingImpl\\{matchValue=AIncludesB,.pattern=/AIncludesB,.servletName=AIncludesB,.mappingMatch=EXACT\\}.*FORWARD_MAPPING:.null.*INCLUDE_MAPPING:.null.*In.BIncludesC.MappingImpl\\{matchValue=AIncludesB,.pattern=/AIncludesB,.servletName=AIncludesB,.mappingMatch=EXACT\\}.*FORWARD_MAPPING:.null.*INCLUDE_MAPPING:.MappingImpl\\{matchValue=BIncludesC,.pattern=/BIncludesC,.servletName=BIncludesC,.mappingMatch=EXACT\\}.*In.ServletC.MappingImpl\\{matchValue=AIncludesB,.pattern=/AIncludesB,.servletName=AIncludesB,.mappingMatch=EXACT\\}.*FORWARD_MAPPING:.null.*INCLUDE_MAPPING:.MappingImpl\\{matchValue=ServletC,.pattern=/ServletC,.servletName=ServletC,.mappingMatch=EXACT\\}.*ASYNC_MAPPING:.null.*");
            boolean includeMatch2 = webTest.run("GET", 200, false, "/BIncludesC", ".*In.BIncludesC.MappingImpl\\{matchValue=BIncludesC,.pattern=/BIncludesC,.servletName=BIncludesC,.mappingMatch=EXACT\\}.*.FORWARD_MAPPING:.null.*.INCLUDE_MAPPING:.null.*In.ServletC.MappingImpl\\{matchValue=BIncludesC,.pattern=/BIncludesC,.servletName=BIncludesC,.mappingMatch=EXACT\\}.*.FORWARD_MAPPING:.null.*.INCLUDE_MAPPING:.MappingImpl\\{matchValue=ServletC,.pattern=/ServletC,.servletName=ServletC,.mappingMatch=EXACT\\}.*.ASYNC_MAPPING:.null.*");
            stat.addStatus(TEST_NAME + "-include", ((includeMatch1 &&
                    includeMatch2)? stat.PASS : stat.FAIL));

            boolean boundsMatch1 = webTest.run("GET", 200, false, "/a/foo", ".*");
            boolean boundsMatch2 = webTest.run("GET", 200, false, "/f", ".*");
            stat.addStatus(TEST_NAME + "-bounds", ((boundsMatch1 && boundsMatch2)? stat.PASS : stat.FAIL));


            boolean namedDispatchMatch1 = webTest.run("GET", 200, false, "/BForwardToCNamedDispatcher", "..*In.ServletC.MappingImpl\\{matchValue=BForwardToCNamedDispatcher,.pattern=/BForwardToCNamedDispatcher,.servletName=BForwardToCNamedDispatcher,.mappingMatch=EXACT\\}</p><p>.FORWARD_MAPPING:.null</p><p>.INCLUDE_MAPPING:.null</p><p>.ASYNC_MAPPING:.null.*");
            boolean namedDispatchMatch2 = webTest.run("GET", 200, false, "/BIncludeCNamedDispatcher", ".*In.ServletC.MappingImpl\\{matchValue=BIncludeCNamedDispatcher,.pattern=/BIncludeCNamedDispatcher,.servletName=BIncludeCNamedDispatcher,.mappingMatch=EXACT\\}</p><p>.FORWARD_MAPPING:.null</p><p>.INCLUDE_MAPPING:.null</p><p>.ASYNC_MAPPING:.null.*");

            stat.addStatus(TEST_NAME + "-namedDispatch", ((namedDispatchMatch1 && namedDispatchMatch2)? stat.PASS : stat.FAIL));


        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private boolean run(String method,
            int status, boolean checkOKStatusOnly, String path, String matchRegex) throws Exception {
        System.out.println("host: " + host + " port: " + port + " path: " + path);
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(path).method(method).build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();

            int code = httpResponse.getStatus();
            boolean ok = (code == status);
            if (checkOKStatusOnly) {
                return ok;
            }
            BufferedReader bis = null;
            String line = null;

            try {
                bis = new BufferedReader(new StringReader(httpResponse.getBody()));
                line = bis.readLine();
                System.out.println(line);
                ok = line.matches(matchRegex);
                System.out.println("matches: " + ok);
            } catch( Exception ex){
                ex.printStackTrace();
                throw new Exception("Test UNPREDICTED-FAILURE");
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }

            return ok;
        }
    }

}
