/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.catalina.authenticator;

import static com.sun.enterprise.util.Utility.isEmpty;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.catalina.Globals.CERTIFICATES_ATTR;
import static org.apache.catalina.Globals.SSL_CERTIFICATE_ATTR;
import static org.apache.catalina.LogFacade.CANNOT_AUTHENTICATE_WITH_CREDENTIALS;
import static org.apache.catalina.LogFacade.LOOK_UP_CERTIFICATE_INFO;
import static org.apache.catalina.LogFacade.NO_CERTIFICATE_INCLUDED_INFO;
import static org.apache.catalina.LogFacade.NO_CLIENT_CERTIFICATE_CHAIN;
import static org.apache.catalina.LogFacade.PRINCIPAL_BEEN_AUTHENTICATED_INFO;
import static org.apache.catalina.authenticator.Constants.CERT_METHOD;
import static org.apache.catalina.authenticator.Constants.REQ_SSOID_NOTE;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.deploy.LoginConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication that utilizes SSL certificates to identify
 * client users.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/04/17 21:33:22 $
 */

public class SSLAuthenticator extends AuthenticatorBase {

    // ------------------------------------------------------------- Properties

    /**
     * Descriptive information about this implementation.
     */
    protected static final String info = "org.apache.catalina.authenticator.SSLAuthenticator/1.0";

    @Override
    protected String getAuthMethod() {
        return HttpServletRequest.CLIENT_CERT_AUTH;
    }

    /**
     * Return descriptive information about this Valve implementation.
     */
    @Override
    public String getInfo() {
        return info;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Authenticate the user by checking for the existence of a certificate chain, and optionally asking a trust manager to
     * validate that we trust this user.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config Login configuration describing how authentication should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public boolean authenticate(HttpRequest request, HttpResponse response, LoginConfig config) throws IOException {
        // Have we already authenticated someone?
        Principal callerPrincipal = ((HttpServletRequest) request.getRequest()).getUserPrincipal();
        if (callerPrincipal != null) {
            if (debug >= 1) {
                log(MessageFormat.format(rb.getString(PRINCIPAL_BEEN_AUTHENTICATED_INFO), callerPrincipal.getName()));
            }

            return true;
        }

        // Retrieve the certificate chain for this client
        HttpServletResponse httpServletResponse = (HttpServletResponse) response.getResponse();
        if (debug >= 1) {
            log(rb.getString(LOOK_UP_CERTIFICATE_INFO));
        }

        X509Certificate certificates[] = (X509Certificate[]) request.getRequest().getAttribute(CERTIFICATES_ATTR);
        if (isEmpty(certificates)) {
            certificates = (X509Certificate[]) request.getRequest().getAttribute(SSL_CERTIFICATE_ATTR);
        }

        if (isEmpty(certificates)) {
            if (debug >= 1) {
                log(rb.getString(NO_CERTIFICATE_INCLUDED_INFO));
            }

            httpServletResponse.sendError(SC_BAD_REQUEST);
            response.setDetailMessage(rb.getString(NO_CLIENT_CERTIFICATE_CHAIN));
            return false;
        }

        // Authenticate the specified certificate chain
        callerPrincipal = context.getRealm().authenticate(certificates);
        if (callerPrincipal == null) {
            if (debug >= 1) {
                log("Realm.authenticate() returned false");
            }

            httpServletResponse.sendError(SC_UNAUTHORIZED);
            response.setDetailMessage(rb.getString(CANNOT_AUTHENTICATE_WITH_CREDENTIALS));
            return false;
        }

        // Cache the principal (if requested) and record this authentication
        register(request, response, callerPrincipal, CERT_METHOD, null, null);

        String ssoId = (String) request.getNote(REQ_SSOID_NOTE);
        if (ssoId != null) {
            getSession(request, true);
        }

        return true;
    }

}
