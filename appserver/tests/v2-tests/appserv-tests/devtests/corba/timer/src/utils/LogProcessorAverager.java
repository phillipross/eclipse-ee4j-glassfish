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

//
// Created       : 2005 Apr 29 (Fri) 07:09:21 by Harold Carr.
// Last Modified : 2005 Jun 09 (Thu) 14:09:51 by Harold Carr.
//

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Note: Although this source file lives in impl.orbutil.timer it
 * does NOT contain a package declaration.  Further, it makes has
 * no dependencies on the CORBA codebase.  Finally, it is not compiled
 * as part of the CORBA build.
 *
 * If you want to use it, just compile it with nothing extra in the classpath.
 *
 * It assumes that named points come in pairs:
 * BEGIN -point1-
 * END -point1-
 *
 * It assumes that the point names and the data are separated by: #####
 *
 * It assumes the times given in the log file are in nanoseconds.
 *
 * Usage: give it the name of the log file.
 *
 * Prints the average of of BEGIN/END point to standard out.
 *
 * @author Harold Carr
 */
public class LogProcessorAverager
{
    static NumberFormat format = null;

    static {
        format =  NumberFormat.getInstance();
        format.setMaximumFractionDigits(10);
        format.setMinimumFractionDigits(10);
    }

    static Map<Integer, Point> data;
    static Map<String, Integer> beginPointNameToInt;

    static String filename;

    private static final long SCALE = 1000000000;

    public static void main(String[] av)
    {
    data = new HashMap<Integer, Point>();
    beginPointNameToInt = new HashMap<String, Integer>();
    filename = av[0];

    try {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        readPoints(br);
        readData(br);
        showResults();
    } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readPoints(BufferedReader br)
    throws Exception
    {
    String line;
    int id = 0;
    while ((line = br.readLine()) != null) {
        if (line.equals("#####")) {
        return;
        }
        String[] beginOrEndAndPointName = line.split(" ");
        String beginOrEnd = beginOrEndAndPointName[0];
        String name       = beginOrEndAndPointName[1];
        int intId = id++;
        if (beginOrEnd.equals("BEGIN")) {
        beginPointNameToInt.put(name, intId);
        data.put(intId, new BeginPoint(intId, name));
        } else {
        data.put(intId,
             new EndPoint(intId, name, data, beginPointNameToInt));
        }
    }
    }

    public static void readData(BufferedReader br)
    throws Exception
    {
    String line;
    while ((line = br.readLine()) != null) {
        String[] idAndTime = line.split(" ");
        int id = Integer.parseInt(idAndTime[0]);
        long time = Long.parseLong(idAndTime[1]);
        data.get(id).setValue(time);
    }
    }

    public static void showResults()
    {
    // Remove begin points.
    for (Integer id : beginPointNameToInt.values()) {
        data.remove(id);
    }
    System.out.println("----------------------------------------");
    System.out.println(filename);
    for (Point point : data.values()) {
        System.out.println(point.getName()
                   + " "
                   + point.getAverage() / SCALE
                   + " "
                   + format(point.getAverage() / SCALE)
                   + " "
                   + " iterations " + point.getIterations());
    }
    System.out.println("----------------------------------------");
    }

    public static String format(double d) {
        return format.format(d);
    }
}

class Point
{
    int intId;
    String name;
    long value;

    Point(int intId, String name)
    {
    this.intId = intId;
    this.name = name;
    value = -1;
    }
    int getIntId() { return intId; }
    String getName() { return name; }
    long getValue() { return value; }
    void setValue(long x) { value = x; }
    double getAverage() { throw new RuntimeException("Not Implemented"); }
    int getIterations() { throw new RuntimeException("Not Implemented"); }
    String toStringString()
    {
    return intId + " " + name  + " " + value;
    }
}

class BeginPoint extends Point
{
    BeginPoint(int intId, String name)
    {
    super(intId, name);
    }
    public String toString()
    {
    return "BeginPoint[ " + toStringString() + " ]";
    }
}

class EndPoint extends Point
{
    Map<Integer, Point> data;
    Map<String, Integer> beginPointNameToInt;
    int iterations;

    EndPoint(int intId, String name, Map<Integer, Point> data,
         Map<String, Integer> beginPointNameToInt)
    {
    super(intId, name);
    this.data = data;
    this.beginPointNameToInt = beginPointNameToInt;
    iterations = 0;
    }
    void setValue(long endTime)
    {
    int startId = beginPointNameToInt.get(name);
    long startTime = data.get(startId).getValue();
    long elapsedTime = endTime - startTime;
    value += elapsedTime;
    iterations++;
    }
    double getAverage()
    {
    return value / iterations;
    }
    int getIterations()
    {
    return iterations;
    }
    public String toString()
    {
    return "EndPoint[ " + toStringString() + " " + iterations + " ]";
    }
}

// End of file.
