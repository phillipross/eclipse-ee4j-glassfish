/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.ctl;

import javax.naming.ldap.BasicControl;

import java.io.IOException;

/**
 * This class implements the LDAP response control for Authorization Identity
 * Response control. This control retrieves the current authorization identity
 * resulting from an LDAP bind operation..
 * When {@link AuthorizationIDControl} is included in the LDAP bind request,
 * the server bind response includes the Authorization Identity Response
 * Control.
 * <p>
 * The Authorization Identity Response Control is defined in <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-auth-response-08.txt">draft-weltman-ldapv3-auth-response-08</a>.
 * <p>
 * The object identifier used by Authorization identity response  control is
 * 2.16.840.1.113730.3.4.15 and the control value returned is the authorization
 * identity. The control's value has the following ASN.1 definition:
 * <pre>
 *
 *     AuthzId ::= LDAPString ; containing an authzId as defined in RFC 2829
 *                            ; or an empty value
 *
 *     authzId    = dnAuthzId / uAuthzId
 *
 *     ; distinguished-name-based authz id.
 *     dnAuthzId  = "dn:" dn
 *     dn         = utf8string    ; with syntax defined in RFC 2253
 *
 *     ; unspecified userid, UTF-8 encoded.
 *     uAuthzId   = "u:" userid
 *     userid     = utf8string    ; syntax unspecified
 *
 * </pre>
 * <p>
 * The following code sample shows how the control may be used:
 * <pre>
 *
 *     // create an authorization identity response control
 *     Control[] reqControls = new Control[]{
 *         new AuthorizationIDControl()
 *     };
 *
 *     // create an initial context using the supplied environment properties
 *     // and the supplied control
 *     LdapContext ctx = new InitialLdapContext(env, reqControls);
 *     Control[] respControls;
 *
 *     // retrieve response controls
 *     if ((respControls = ctx.getResponseControls()) != null) {
 *         for (int i = 0; i < respControls.length; i++) {
 *
 *             // locate the authorization identity response control
 *             if (respControls[i] instanceof AuthorizationIDResponseControl) {
 *                 System.out.println("My identity is " +
 *                     ((AuthorizationIDResponseControl) respControls[i])
 *                         .getAuthorizationID());
 *             }
 *         }
 *     }
 *
 * </pre>
 *
 * @see AuthorizationIDControl
 * @see com.sun.jndi.ldap.ext.WhoAmIRequest
 * @author Vincent Ryan
 */
public class AuthorizationIDResponseControl extends BasicControl {

    /**
     * The authorization identity response control's assigned object identifier is
     * 2.16.840.1.113730.3.4.15.
     */
    public static final String OID = "2.16.840.1.113730.3.4.15";

    /**
     * Authorization Identity of the bound user
     * @serial
     */
    private String authzId;

    private static final long serialVersionUID = -7740841453439127143L;

    /**
     * Constructs a control to indicate the authorization identity.
     *
     * @param   id              The control's object identifier string.
     * @param   criticality     The control's criticality.
     * @param   value           The control's ASN.1 BER encoded value.
     *                          May be null.
     * @exception               IOException if an error is encountered
     *                          while decoding the control's value.
     */
    AuthorizationIDResponseControl(String id, boolean criticality,
    byte[] value) throws IOException {

    super(id, criticality, value);
    if ((value == null) || (value.length == 0)){
        authzId = "";
    } else {
        authzId = new String(value, "UTF8");
    }
    }

    /**
     * Retrieves the authorization identity.
     * An empty string is returned when anonymous authentication is used.
     *
     * @return The authorization identity.
     */
    public String getAuthorizationID() {
    return authzId;
    }

    /**
     * Retrieves the authorization identity control response's ASN.1 BER
     * encoded value.
     *
     * @return A possibly null byte array representing the ASN.1 BER
     *            encoded value of the LDAP response control.
     */
    public byte[] getEncodedValue() {

    if (value == null) {
        return null;
    }

        // return a copy of value
        byte[] retval = new byte[value.length];
        System.arraycopy(value, 0, retval, 0, value.length);
        return retval;
    }
}
