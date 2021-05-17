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

import java.io.IOException;
import javax.naming.ldap.BasicControl;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;

/**
 * This class implements the LDAP request control for proxied authorization.
 * This control is used to request that the accompanying operation be
 * performed using the supplied authorization identity, overriding any
 * existing authorization identity.
 * The control may be included in any LDAP operation except in those that
 * cause change in authentication, authorization or data confidentiality, such
 * as bind and startTLS.
 * <p>
 * The Proxied Authorization control is defined in <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-proxy-12.txt">draft-weltman-ldapv3-proxy-12</a>.
 * <p>
 * The object identifier for the Proxied Authorization control is 2.16.840.1.113730.3.4.18
 *  and the control value is the authorization identity to be used. The control
 * value is empty if anonymous identity is to be used. The control's value has
 * the following ASN.1 definition:
 * <p>
 * <pre>
 *
 *     ProxiedAuth ::= LDAPString ; containing an authzId as defined in RFC 2829
 *                                ; or an empty value
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
 *     // create an initial context using the supplied environment properties
 *     LdapContext ctx = new InitialLdapContext(env, null);
 *
 *     // examine the authorization identity and set the appropriate prefix
 *     String authzId = isDN(authzId) ? "dn:" + authzId : "u:" + authzId;
 *
 *     // activate the control
 *     ctx.setRequestControls(new Control[] {
 *         new ProxiedAuthorizationControl(authzId)
 *     };
 *
 *     // perform an operation using the authorization identity
 *     ctx.getAttributes("");
 *
 * </pre>
 *
 * @author Vincent Ryan
 * @see AuthorizationIDControl
 * @see com.sun.jndi.ldap.ext.WhoAmIRequest
 */

public class ProxiedAuthorizationControl extends BasicControl {

    private static final long serialVersionUID = 552016610613918389L;

    /**
     * The proxied authorization control's assigned object identifier is
     * 2.16.840.1.113730.3.4.18.
     */
    public static final String OID = "2.16.840.1.113730.3.4.18";

    /**
     * Constructs a control to perform an operation using the supplied
     * authorization identity. The control is always marked critical.
     *
     * @param authzId A non null authorization identity to use. authzId
     *              must be set to an empty string if anonymous identity
     *              is to be used.
     * @exception IOException If a BER encoding error occurs.
     */
    public ProxiedAuthorizationControl(String authzId) throws IOException {
    super(OID, true, null);
    value = setEncodedValue(authzId);
    }

    /*
     * Encodes the control's value using ASN.1 BER.
     * The result includes the BER tag and length for the control's value but
     * does not include the control's object identifer and criticality setting.
     *
     * @param authzId The authorization identity to use.
     * @return A byte array representing the ASN.1 BER encoded value of the
     *           LDAP control.
     * @exception IOException If a BER encoding error occurs.
     */
    private static byte[] setEncodedValue(String authzId) throws IOException {

        // build the ASN.1 BER encoding
        BerEncoder ber = new BerEncoder(2 * authzId.length() + 5);
        ber.encodeString(authzId, true);

        return ber.getTrimmedBuf();
    }
}
