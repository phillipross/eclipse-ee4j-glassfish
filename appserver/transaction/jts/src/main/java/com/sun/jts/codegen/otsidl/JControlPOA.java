/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jts.codegen.otsidl;


/**
* com/sun/jts/codegen/otsidl/JControlPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from com/sun/jts/ots.idl
* Tuesday, February 5, 2002 12:57:23 PM PST
*/


//#-----------------------------------------------------------------------------
public abstract class JControlPOA extends org.omg.PortableServer.Servant
 implements com.sun.jts.codegen.otsidl.JControlOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("getGlobalTID", 0);
    _methods.put ("getLocalTID", 1);
    _methods.put ("getTranState", 2);
    _methods.put ("setTranState", 3);
    _methods.put ("get_terminator", 4);
    _methods.put ("get_coordinator", 5);
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // otsidl/JControl/getGlobalTID
       {
         org.omg.CosTransactions.StatusHolder status = new org.omg.CosTransactions.StatusHolder ();
         org.omg.CosTransactions.otid_t $result = null;
         $result = this.getGlobalTID (status);
         out = $rh.createReply();
         org.omg.CosTransactions.otid_tHelper.write (out, $result);
         org.omg.CosTransactions.StatusHelper.write (out, status.value);
         break;
       }


  // transaction, and a value that indicates the state of the transaction.
       case 1:  // otsidl/JControl/getLocalTID
       {
         org.omg.CosTransactions.StatusHolder status = new org.omg.CosTransactions.StatusHolder ();
         long $result = (long)0;
         $result = this.getLocalTID (status);
         out = $rh.createReply();
         out.write_longlong ($result);
         org.omg.CosTransactions.StatusHelper.write (out, status.value);
         break;
       }


  // value that indicates the state of the transaction.
       case 2:  // otsidl/JControl/getTranState
       {
         org.omg.CosTransactions.Status $result = null;
         $result = this.getTranState ();
         out = $rh.createReply();
         org.omg.CosTransactions.StatusHelper.write (out, $result);
         break;
       }


  // Returns the state of the transaction as the Control object knows it.
       case 3:  // otsidl/JControl/setTranState
       {
         org.omg.CosTransactions.Status state = org.omg.CosTransactions.StatusHelper.read (in);
         this.setTranState (state);
         out = $rh.createReply();
         break;
       }

       case 4:  // CosTransactions/Control/get_terminator
       {
         try {
           org.omg.CosTransactions.Terminator $result = null;
           $result = this.get_terminator ();
           out = $rh.createReply();
           org.omg.CosTransactions.TerminatorHelper.write (out, $result);
         } catch (org.omg.CosTransactions.Unavailable $ex) {
           out = $rh.createExceptionReply ();
           org.omg.CosTransactions.UnavailableHelper.write (out, $ex);
         }
         break;
       }

       case 5:  // CosTransactions/Control/get_coordinator
       {
         try {
           org.omg.CosTransactions.Coordinator $result = null;
           $result = this.get_coordinator ();
           out = $rh.createReply();
           org.omg.CosTransactions.CoordinatorHelper.write (out, $result);
         } catch (org.omg.CosTransactions.Unavailable $ex) {
           out = $rh.createExceptionReply ();
           org.omg.CosTransactions.UnavailableHelper.write (out, $ex);
         }
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:otsidl/JControl:1.0",
    "IDL:omg.org/CosTransactions/Control:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public JControl _this()
  {
    return JControlHelper.narrow(
    super._this_object());
  }

  public JControl _this(org.omg.CORBA.ORB orb)
  {
    return JControlHelper.narrow(
    super._this_object(orb));
  }


} // class JControlPOA
