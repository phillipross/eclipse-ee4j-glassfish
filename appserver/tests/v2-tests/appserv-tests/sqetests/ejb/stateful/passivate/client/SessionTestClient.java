/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * SessionTestClient.java
 *
 * Created on October 17, 2003, 4:14 PM
 */

package sqetests.ejb.stateful.passivate.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import sqetests.ejb.stateful.passivate.util.*;
import sqetests.ejb.stateful.passivate.ejb.stateful.*;
import java.io.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  Deepa Singh
 */
public class SessionTestClient {
    private String testSuiteID="";
    private SimpleReporterAdapter stat;

    public Context initial;
    public Object objref;
    SessionRemoteHome home=null;
    SessionRemote remote=null;

    boolean beanLocated=false;
    String m_action="all";
    int m_clients=1;

    /** Creates a new instance of SessionTestClient */
    public SessionTestClient(String ts_id,String numClients,String action) {
        stat =new SimpleReporterAdapter("appserv-tests");
        stat.addDescription("This testsuites tests lifecycle of sfsb");
        testSuiteID=ts_id;
        m_clients=new Integer(numClients).intValue();
        m_action=action;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length <3){
            System.out.println("Enter testsuite name SessionTestClient <ts_name> <no_clients> <action> all|create|run");
            return;
        }
        SessionTestClient Client =new SessionTestClient(args[0],args[1],args[2]);
        if(Client.runSetup()){
        Client.runStatefulTest();
        }
        else
            System.err.println("Test didn't run");
    }

    public boolean runSetup() {
        System.out.println("Test Execution Starts---------->");
        try{
            initial = new InitialContext();
            System.out.println("Looking up SFSB0");
            objref = initial.lookup("java:comp/env/ejb/SFSBSession");
            home=(SessionRemoteHome)PortableRemoteObject.narrow(objref,
            SessionRemoteHome.class);
            beanLocated=true;
        }catch(Throwable e){
            System.out.println("Lookup of beans failed");
            e.printStackTrace();
            beanLocated=false;
        }
        return beanLocated;

    }

    public void runStatefulTest(){
         SessionRemote[] remote=new SessionRemote[10];
        /*
         *Due to performance reasons, beans are not immediately
         passivated after they are identified as candidates for passivation.
         The container performs passivations in batches
         (that is after it accumulates some number of beans - which is set to 8 internally)
         *This is the reason why you do not see a passivation.
         If you had created more than 8 sessions then you would see the expected behaviour.
         *hence number 10 is chosen.
         *
         **/
        try {
            for(int i=0;i<10;i++){
        remote[i]=home.create("<"+i+">");
        }

            System.out.println("Started transaction on Stateful Bean 5,\n shouldn't get passivated");
            System.out.println(remote[5].txMethod());
            System.out.println("Now going to sleep for 40 secs to passivate beans");
            Thread.sleep(40000);
            //System.out.println("after getting activated"+remote[9].getMessage());
            stat.addStatus(testSuiteID+" "+"10 SFSB Creation",stat.PASS);
        }catch(jakarta.ejb.CreateException e){
            System.out.println("Error while creating beans");
            e.printStackTrace();
            stat.addStatus(testSuiteID+" "+"10 SFSB Creation",stat.FAIL);
        }catch(java.lang.InterruptedException e){
            System.out.println("Error while sleeping");
            e.printStackTrace();
        }catch(Throwable e){
            System.out.println("Something unexpected happened,check logs");
            e.printStackTrace();
            stat.addStatus(testSuiteID+" "+"10 SFSB Creation",stat.FAIL);
        }
         try{
             for(int i=0;i<10;i++){
                 System.out.println("......"+remote[i].getMessage());
                 remote[i].afterActivationBusinessMethod();
            }

         }catch(java.rmi.NoSuchObjectException e){
             System.out.println("java.rmi.NoSuchObjectException");
             System.out.println("Bean 9 removed");

             e.getMessage();
         }catch(java.rmi.RemoteException e){
             System.out.println("unforseen circumstances");
             e.printStackTrace();
         }catch(Throwable e){
             e.printStackTrace();
         }

         try{
             HashMap finalResult=new HashMap();
             finalResult=remote[9].getEJBRecorder();
             System.out.println("Result Map====="+finalResult.toString());
             //i <9 instead of 10 as 9th bean is removed
             for(int i=0;i<10;i++){
                 String beankey=new String("<"+i+">");
                 //first echo results for one bean
                 HashMap singleBeanResult=(HashMap)finalResult.get(beankey);
                 String passivateresult=singleBeanResult.get(new String("passivate")).toString();
                 String activateresult=singleBeanResult.get(new String("activate")).toString();
                 System.out.println("ejbPassivate for bean <"+ i+"> :"+passivateresult);
                 System.out.println("ejbActivate for bean <"+ i+"> :"+activateresult);
                 if(i==5){
                     if(activateresult.equalsIgnoreCase("false")){
                         System.out.println("Bean 5 expectedly fails activation");

                     }
                 }
             }

            // Test run is over,remove all SFSB after this(productization of test,returns server to clean state
            //close all resources in SFSB remove methods
            for(int i=0;i<10;i++){
                try{
                    remote[i].remove();

                }catch(jakarta.ejb.RemoveException e){
                    System.out.println("Error while removing  :"+i+"SFSB");
                    if(i==5)
                        System.out.println("Bean 5 throws RemoveException");
                }catch(java.rmi.NoSuchObjectException e){
                    System.out.println("Error while removing  :"+i+"SFSB");
                    if(i==9)
                        System.out.println("Bean 9 is already removed");
                }catch(Exception e){
                    System.out.println("Error while removing  :"+i+"SFSB");
                    e.printStackTrace();
                }
            }
            stat.addStatus(testSuiteID+" "+"SFSB_removal",stat.PASS);
        }catch(Throwable e){
            e.printStackTrace();
            stat.addStatus(testSuiteID+" "+"SFSB_removal",stat.FAIL);
        }
        stat.printSummary();
    }

}
