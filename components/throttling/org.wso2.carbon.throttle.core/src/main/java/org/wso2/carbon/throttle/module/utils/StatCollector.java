/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.throttle.module.utils;

import org.wso2.carbon.throttle.module.utils.impl.DummyHandler;
import org.wso2.carbon.throttle.core.AccessInformation;
import org.wso2.carbon.throttle.core.ThrottleConstants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StatCollector {

    private Map statsPerUser = new ConcurrentHashMap();
    private Map statsPerDomain = new ConcurrentHashMap();
    private Map statsPerIP = new ConcurrentHashMap();

    private AtomicLong stTime_User = new AtomicLong(-1);
    private AtomicLong stTime_IP = new AtomicLong(-1);
    private AtomicLong stTime_DOMAIN = new AtomicLong(-1);

    private long startTime = 0;
    public static long statCollectionPeriod = 0;
    public static long statDisplayWindow = 0;

    private static volatile StatCollector instance = null;

    public static boolean enableStatCollection = false;

    private StatCollector() {
        startTime = System.nanoTime();
    }

    private void storeStats(AccessInformation accessInfo, String callerID, int throttleType) {
        long timeStamp = System.nanoTime();
        System.out.println(timeStamp);
        if (ThrottleConstants.DOMAIN_BASE == throttleType) {
            stTime_DOMAIN.compareAndSet(-1, timeStamp);
            statsPerDomain.put(new Long(timeStamp), new StatDO(accessInfo.isAccessAllowed(), callerID, new Long(timeStamp)));
        } else if (ThrottleConstants.IP_BASE == throttleType) {
            stTime_IP.compareAndSet(-1, timeStamp);
            statsPerIP.put(new Long(timeStamp), new StatDO(accessInfo.isAccessAllowed(), callerID, new Long(timeStamp)));
        } else if (ThrottleConstants.ROLE_BASE == throttleType) {
            stTime_User.compareAndSet(-1, timeStamp);
            statsPerUser.put(new Long(timeStamp), new StatDO(accessInfo.isAccessAllowed(), callerID, new Long(timeStamp)));
        }
    }

    public static StatCollector getInstance() {
        if (instance == null) {
            synchronized (StatCollector.class) {
                if (instance == null) {
                    instance = new StatCollector();
                }
            }
        }
        return instance;
    }

    public static void setStatCollectionPeriod(long period) {
        statCollectionPeriod = period;
    }

    public static void setStatDisplayWindow(long period) {
        statDisplayWindow = period;
    }

    public static void collect(AccessInformation accessInfo, String callerID, int type) {
        if(!enableStatCollection){
            return;
        }
        getInstance().storeStats(accessInfo, callerID, type);
    }

    private void flushAll() {
        statsPerDomain.clear();
        statsPerUser.clear();
        statsPerIP.clear();

        stTime_User = new AtomicLong(-1);
        stTime_IP = new AtomicLong(-1);
        stTime_DOMAIN = new AtomicLong(-1);

        statCollectionPeriod = 0;
        statDisplayWindow = 0;

        startTime = 0;
    }

    public static void flushStats(){
        getInstance().flushAll();
    }

    public static void displayStats(int throttleType) {
        StatCollector collector = getInstance();
        Map sortedMap = null;
        if (ThrottleConstants.DOMAIN_BASE == throttleType) {
            System.out.println("Start Time : Domain Base Throttling ::" + collector.stTime_DOMAIN);
            sortedMap = sortByComparator(collector.statsPerDomain);
        } else if (ThrottleConstants.IP_BASE == throttleType) {
            System.out.println("Start Time : IP Base Throttling ::" + collector.stTime_IP);
            sortedMap = sortByComparator(collector.statsPerIP);
        } else if (ThrottleConstants.ROLE_BASE == throttleType) {
            System.out.println("Start Time : Role Base Throttling ::" + collector.stTime_User);
            sortedMap = sortByComparator(collector.statsPerUser);
        }
        Set set = sortedMap.entrySet();
        Iterator iterator = set.iterator();
        String prevCallerID = null;
        boolean prevAccessState = false;
        Long phaseStartTimestamp = null;
        Long phaseEndTimestamp = null;
        Long prevTimestamp = null;

        String callerID = null;

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Long timestamp = (Long) entry.getKey();
            callerID = ((StatDO) entry.getValue()).getCallerID();
            boolean currAccessState = ((StatDO) entry.getValue()).isAccessAllowed();

            if (prevCallerID == null || callerID.equals(prevCallerID)) {
                if (prevCallerID != null && prevAccessState != currAccessState) {
                    //a phase changes
                    phaseEndTimestamp = prevTimestamp;
                    System.out.println("Access Phase change for :" + callerID + "  duration of this phase :" + (phaseEndTimestamp - phaseStartTimestamp) / 1000.0 + " ms");
                    phaseStartTimestamp = timestamp;
                } else {
                    //same access phase
                    if (phaseStartTimestamp == null) {
                        phaseStartTimestamp = timestamp;
                    }
                }
            } else {
                //caller changes
                phaseStartTimestamp = timestamp;
            }
            prevCallerID = callerID;
            prevAccessState = currAccessState;
            prevTimestamp = timestamp;
        }
//        System.out.println("Access Phase change for :" + callerID + "  duration of this phase :" + (phaseEndTimestamp - phaseStartTimestamp) / 1000.0 + " ms");

        iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            System.out.println("Timestamp : " + entry.getKey()
                               + " CallerID : " + ((StatDO) entry.getValue()).getCallerID() +
                                " Role : " + DummyHandler.apiKey2roleMap.get(((StatDO) entry.getValue()).getCallerID()) +
                               (((StatDO) entry.getValue()).isAccessAllowed() ? " --> Allowed" : "--> Denied"));

        }

    }

    private static Map sortByComparator(Map unsortMap) {

        List list = new LinkedList(unsortMap.entrySet());

        //sort list based on comparator
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        //put sorted list into map again
        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private class StatDO implements Comparable {
        private boolean accessAllowed = false;
        private String callerID = "";
        private Long timestamp;

        public StatDO(boolean accessAllowed, String callerID, Long timestamp) {
            this.accessAllowed = accessAllowed;
            this.callerID = callerID;
            this.timestamp = timestamp;
        }

        public boolean isAccessAllowed() {
            return accessAllowed;
        }

        public String getCallerID() {
            return callerID;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public int compareTo(Object o) {
            int result = 0;
            result = callerID.compareTo(((StatDO) o).getCallerID());
            if (result != 0) {
                return result;
            }
            return timestamp.compareTo(((StatDO) o).getTimestamp());
        }
    }

    public static void main(String[] args) {
        AccessInformation acAllwd = new AccessInformation();
        acAllwd.setAccessAllowed(true);

        AccessInformation acDenied = new AccessInformation();
        acDenied.setAccessAllowed(false);

        StatCollector.collect(acAllwd, "nq21LN39VlKe6OezcOndBx", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "nq21LN39VlKe6OezcOndBx", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "nq21LN39VlKe6OezcOndBx", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "nq21LN39VlKe6OezcOndBx", ThrottleConstants.ROLE_BASE);

        StatCollector.collect(acAllwd, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);

        StatCollector.collect(acDenied, "nq21LN39VlKe6OezcOndBx", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acDenied, "nq21LN39VlKe6OezcOndBx", ThrottleConstants.ROLE_BASE);

        StatCollector.collect(acDenied, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acDenied, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);
        StatCollector.collect(acAllwd, "asdadsadg33332424Gasdad", ThrottleConstants.ROLE_BASE);

        StatCollector.displayStats(ThrottleConstants.ROLE_BASE);
    }
}
