/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.bmp.robean.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import samples.ejb.bmp.robean.ejb.*;
import com.sun.ejb.ReadOnlyBeanNotifier;
import com.sun.ejb.containers.ReadOnlyBeanHelper;

import static org.testng.Assert.*;
import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ROBClient {

    Customer customer = null;
    CustomerRefresh customerRefresh = null;
    CustomerProgRefresh customerProgRefresh = null;

    String SSN="123456789";

    public static void main(String[] args) {
        org.testng.TestNG testng = new org.testng.TestNG();
        testng.setTestClasses(
            new Class[] { samples.ejb.bmp.robean.client.ROBClient.class } );
        testng.run();
    }

    @Configuration(beforeTestClass = true)
    public void initialize() throws Exception {
        InitialContext ic = new InitialContext();

        AddressHome addressHome = (AddressHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/address"), AddressHome.class);

        System.out.println("Narrowed AddressHome !!");

        CustomerHome customerHome = (CustomerHome)PortableRemoteObject.narrow(
            ic.lookup("java:comp/env/ejb/customer"), CustomerHome.class);

        System.out.println("Narrowed CustomerHome !!");

        CustomerTransactionalHome customerTransactionalHome =
            (CustomerTransactionalHome) PortableRemoteObject.narrow(
                ic.lookup("java:comp/env/ejb/customerTransactional"),
                CustomerTransactionalHome.class);

        System.out.println("Narrowed CustomerTransactionalHome !!");

        CustomerRefreshHome customerRefreshHome =
            (CustomerRefreshHome)PortableRemoteObject.narrow(
                ic.lookup("java:comp/env/ejb/customerRefresh"),
                CustomerRefreshHome.class);
        System.out.println("Narrowed CustomerRefreshHome !!");


        CustomerProgRefreshHome customerProgRefreshHome =
            (CustomerProgRefreshHome)PortableRemoteObject.narrow(
                ic.lookup("java:comp/env/ejb/customerProgRefresh"),
                CustomerProgRefreshHome.class);
        System.out.println("Narrowed CustomerProgRefreshHome !!");

        customer = customerHome.findByPrimaryKey(new PKString(SSN));
        customerRefresh = customerRefreshHome.findByPrimaryKey(SSN);
        customerProgRefresh =
            customerProgRefreshHome.findByPrimaryKey(new PKString1(SSN));
    }

    @Test
    public void test1() throws Exception {
        System.out.println("current balance before credit of 100 - " +
                           customer.getBalance());
        System.out.println("current prog balance before credit of 100 - " +
                           customerProgRefresh.getBalance());

        customer.doCredit(100);

        System.out.println("current balance after credit of 100 - "
                           + customer.getBalance());
        System.out.println("current prog balance after credit of 100 - " +
                           customerProgRefresh.getBalance());

        Assert.assertTrue(
            (customer.getBalance() != customerProgRefresh.getBalance()),
            "Test ReadOnlyBean is not updated");
    }

    @Test
    public void test2() throws Exception {
        ReadOnlyBeanNotifier notifier =
            ReadOnlyBeanHelper.getReadOnlyBeanNotifier(
                "java:comp/env/ejb/customerProgRefresh");
        notifier.refresh(new PKString1(SSN));

        System.out.println("current balance before credit of 150 - " +
                           customer.getBalance());
        System.out.println("current prog balance before credit of 150 - " +
                           customerProgRefresh.getBalance());

        customer.doCredit(150);

        System.out.println("current balance after credit of 150 - "
                           + customer.getBalance());
        System.out.println("current prog balance after credit of 150 - " +
                           customerProgRefresh.getBalance());

        Assert.assertTrue(
            (customer.getBalance() == customerProgRefresh.getBalance()),
            "Test ReadOnlyBean is updated programatically");
    }

    @Test
    public void test3() throws Exception {

        System.out.println("current balance - " + customer.getBalance());
        System.out.println("current refresh balance - " +
                           customerRefresh.getBalance());

        customer.doDebit(25);

        System.out.println("current balance - " + customer.getBalance());
        System.out.println("current refresh balance - " +
                           customerRefresh.getBalance());


        //CustomerRefresh Bean is refresh every 15 seconds
        Thread.sleep(20000);

        System.out.println("current prog balance - " +
                           customerRefresh.getBalance());
        Assert.assertTrue(
            (customer.getBalance() == customerRefresh.getBalance()),
            "Test ReadOnlyBean is updated periodically");
    }
}
