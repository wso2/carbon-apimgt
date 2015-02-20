/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.apache.synapse.transport.nhttp.util;

import org.apache.synapse.commons.jmx.MBeanRegistrar;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.Date;
import java.util.Calendar;

/**
 * <p>LatencyView provides statistical information related to the latency (overhead) incurred by
 * the Synapse NHTTP transport, when mediating messages back and forth. Statistics are available
 * under two main categories, namely short term data and long term data. Short term data is
 * statistical information related to the last 15 minutes of execution and these metrics are
 * updated every 5 seconds. Long term data is related to the last 24 hours of execution and
 * they are updated every 5 minutes. Two timer tasks and a single threaded scheduled executor
 * is used to perform these periodic calculations.</p>
 *
 * <p>Latency calculation for a single invocation is carried out by taking timestamps on
 * following events:</p>
 *
 * <ul>
 *  <li>t1 - Receiving a new request (ServerHandler#requestReceived)</li>
 *  <li>t2 - Obtaining a connection to forward the request (ClientHandler#processConnection)</li>
 *  <li>t3 - Reading the complete response from the backend server (ClientHandler#inputReady)</li>
 *  <li>t4 - Writing the complete response to the client (ServerHandler#outputReady)</li>
 * <ul>
 *
 * <p>Having taken these timestamps, the latency for the invocation is calculated as follows:<br/>
 *    Latency = (t4 - t1) - (t3 - t2)
 * </p>
 *
 */
public class LatencyView implements LatencyViewMBean {

    private static final int SMALL_DATA_COLLECTION_PERIOD = 5;
    private static final int LARGE_DATA_COLLECTION_PERIOD = 5 * 60;
    private static final int SAMPLES_PER_MINUTE = 60/ SMALL_DATA_COLLECTION_PERIOD;
    private static final int SAMPLES_PER_HOUR = (60 * 60)/LARGE_DATA_COLLECTION_PERIOD;

    /** Keeps track of th last reported latency value */
    private AtomicLong lastLatency = new AtomicLong(0);

    /**
     * Queue of all latency values reported. The short term data collector clears this queue up
     * time to time thus ensuring it doesn't grow indefinitely.
     */
    private Queue<Long> latencyDataQueue = new ConcurrentLinkedQueue<Long>();

    /**
     * Queue of samples collected by the short term data collector. This is maintained
     * as a fixed length queue
     */
    private Queue<Long> shortTermLatencyDataQueue = new LinkedList<Long>();

    /**
     * Queue of samples collected by the long term data collector. This is maintained
     * as a fixed length queue
     */
    private Queue<Long> longTermLatencyDataQueue = new LinkedList<Long>();

    /** Scheduled executor on which data collectors are executed */
    private ScheduledExecutorService scheduler;

    private double allTimeAvgLatency = 0.0;
    private int count = 0;
    private Date resetTime = Calendar.getInstance().getTime();

    private String latencyMode;
    private String name;

	/** -Following are used to calculate BackEnd latency - */

	/** Keeps track of th last reported BE latency value */
	private AtomicLong lastLatencyBe = new AtomicLong(0);

	/**
	 * Queue of all Backend latency values reported. The short term data
	 * collector clears this queue up
	 * time to time thus ensuring it doesn't grow indefinitely.
	 */
	private Queue<Long> latencyDataQueueBe = new ConcurrentLinkedQueue<Long>();

	/**
	 * Queue of samples collected by the short term data collector. This is
	 * maintained
	 * as a fixed length queue
	 */
	private Queue<Long> shortTermLatencyDataQueueBe = new LinkedList<Long>();

	/**
	 * Queue of samples collected by the long term data collector. This is
	 * maintained
	 * as a fixed length queue
	 */
	private Queue<Long> longTermLatencyDataQueueBe = new LinkedList<Long>();

	private double allTimeAvgLatencyBe = 0.0;
	private int countBe = 0; 
    
    public LatencyView(final String latencyMode, boolean isHttps, String strPostfix) {
        this.latencyMode = latencyMode;
        name = "nio-" + strPostfix;

        scheduler =  Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, latencyMode + "-" + name + "-latency-view");
            }
        });

        scheduler.scheduleAtFixedRate(new ShortTermDataCollector(), SMALL_DATA_COLLECTION_PERIOD,
                SMALL_DATA_COLLECTION_PERIOD, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new LongTermDataCollector(), LARGE_DATA_COLLECTION_PERIOD,
                LARGE_DATA_COLLECTION_PERIOD, TimeUnit.SECONDS);
        boolean registered = false;
        try {
            registered = MBeanRegistrar.getInstance().registerMBean(this, this.latencyMode, name);
        } finally {
            if (!registered) {
                scheduler.shutdownNow();
            }
        }
    }    
    
    public LatencyView(final String latencyMode, boolean isHttps) {
        this.latencyMode = latencyMode;
        name = "nio-http" + (isHttps ? "s" : "");

        scheduler =  Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, latencyMode + "-" + name + "-latency-view");
            }
        });

        scheduler.scheduleAtFixedRate(new ShortTermDataCollector(), SMALL_DATA_COLLECTION_PERIOD,
                SMALL_DATA_COLLECTION_PERIOD, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new LongTermDataCollector(), LARGE_DATA_COLLECTION_PERIOD,
                LARGE_DATA_COLLECTION_PERIOD, TimeUnit.SECONDS);
        boolean registered = false;
        try {
            registered = MBeanRegistrar.getInstance().registerMBean(this, this.latencyMode, name);
        } finally {
            if (!registered) {
                scheduler.shutdownNow();
            }
        }
    }

    public void destroy() {
        MBeanRegistrar.getInstance().unRegisterMBean(latencyMode, name);
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Report the timestamp values captured during mediating messages back and forth
     *
     * @param reqArrival The request arrival time
     * @param reqDeparture The request departure time (backend connection establishment)
     * @param resArrival The resoponse arrival time
     * @param resDeparture The response departure time
     */
    public void notifyTimes(long reqArrival, long reqDeparture,
                            long resArrival, long resDeparture) {

		long latencyBe = (resArrival - reqDeparture);
		long latency = (resDeparture - reqArrival) - latencyBe;
		lastLatency.set(latency);
		latencyDataQueue.offer(latency);
		lastLatencyBe.set(latencyBe);
		latencyDataQueueBe.offer(latencyBe);
    }

    public double getAllTimeAvgLatency() {
        return allTimeAvgLatency;
    }

    public double getLastMinuteAvgLatency() {
        return getAverageLatencyByMinute(1);
    }

    public double getLast5MinuteAvgLatency() {
        return getAverageLatencyByMinute(5);
    }

    public double getLast15MinuteAvgLatency() {
        return getAverageLatencyByMinute(15);
    }

    public double getLastHourAvgLatency() {
        return getAverageLatencyByHour(1);
    }

    public double getLast8HourAvgLatency() {
        return getAverageLatencyByHour(8);
    }

    public double getLast24HourAvgLatency() {
        return getAverageLatencyByHour(24);
    }

	public double getAllTimeAvgLatencyBackEnd() {
		return allTimeAvgLatencyBe;
	}

	public double getLastMinuteAvgLatencyBackEnd() {
		return getAverageLatencyBackEndByMinute(1);
	}

	public double getLast5MinuteAvgLatencyBackEnd() {
		return getAverageLatencyBackEndByMinute(5);
	}

	public double getLast15MinuteAvgLatencyBackEnd() {
		return getAverageLatencyBackEndByMinute(15);
	}

	public double getLastHourAvgLatencyBackEnd() {
		return getAverageLatencyBackEndByHour(1);
	}

	public double getLast8HourAvgLatencyBackEnd() {
		return getAverageLatencyBackEndByHour(8);
	}

	public double getLast24HourAvgLatencyBackEnd() {
		return getAverageLatencyBackEndByHour(24);
	} 
    
    public void reset() {
        lastLatency.set(0);
        lastLatencyBe.set(0);
        allTimeAvgLatency = 0.0;
        allTimeAvgLatencyBe = 0.0;
        latencyDataQueue.clear();
        latencyDataQueueBe.clear();
        shortTermLatencyDataQueue.clear();
        shortTermLatencyDataQueueBe.clear();
        longTermLatencyDataQueue.clear();
        longTermLatencyDataQueueBe.clear();
        count = 0;
        countBe = 0;
        resetTime = Calendar.getInstance().getTime();
    }

    public Date getLastResetTime() {
        return resetTime;
    }

    private double getAverageLatencyByMinute(int n) {
        int samples = n * SAMPLES_PER_MINUTE;
        double sum = 0.0;
        Long[] array = shortTermLatencyDataQueue.toArray(new Long[shortTermLatencyDataQueue.size()]);

        if (samples > array.length) {
            // If we don't have enough samples collected yet
            // add up everything we have
            samples = array.length;
            for (int i = 0; i < array.length; i++) {
                sum += array[i];
            }
        } else {
            // We have enough samples to make the right calculation
            // Add up starting from the end of the queue (to give the most recent values)
            for (int i = 0; i < samples; i++) {
                sum += array[array.length - 1 - i];
            }
        }
        
        if (samples == 0) {
            return 0.0;
        }
        return sum/samples;
    }

    private double getAverageLatencyByHour(int n) {
        int samples = n * SAMPLES_PER_HOUR;
        double sum = 0.0;
        Long[] array = longTermLatencyDataQueue.toArray(new Long[longTermLatencyDataQueue.size()]);

        if (samples > array.length) {
            samples = array.length;
            for (int i = 0; i < array.length; i++) {
                sum += array[i];
            }
        } else {
            for (int i = 0; i < samples; i++) {
                sum += array[array.length - 1 - i];
            }
        }

        if (samples == 0) {
            return 0.0;
        }
        return sum/samples;
    }
    
	private double getAverageLatencyBackEndByMinute(int n) {
		int samples = n * SAMPLES_PER_MINUTE;
		double sum = 0.0;
		Long[] array =
		               shortTermLatencyDataQueueBe.toArray(new Long[shortTermLatencyDataQueueBe.size()]);

		if (samples > array.length) {
			// If we don't have enough samples collected yet
			// add up everything we have
			samples = array.length;
			for (int i = 0; i < array.length; i++) {
				sum += array[i];
			}
		} else {
			// We have enough samples to make the right calculation
			// Add up starting from the end of the queue (to give the most
			// recent values)
			for (int i = 0; i < samples; i++) {
				sum += array[array.length - 1 - i];
			}
		}

		if (samples == 0) {
			return 0.0;
		}
		return sum / samples;
	}

	private double getAverageLatencyBackEndByHour(int n) {
		int samples = n * SAMPLES_PER_HOUR;
		double sum = 0.0;
		Long[] array =
		               longTermLatencyDataQueueBe.toArray(new Long[longTermLatencyDataQueueBe.size()]);

		if (samples > array.length) {
			samples = array.length;
			for (int i = 0; i < array.length; i++) {
				sum += array[i];
			}
		} else {
			for (int i = 0; i < samples; i++) {
				sum += array[array.length - 1 - i];
			}
		}

		if (samples == 0) {
			return 0.0;
		}
		return sum / samples;
	}    
        
    private class ShortTermDataCollector implements Runnable {
        public void run() {
            long latency = lastLatency.get();

            // calculate all time average latency
            int size = latencyDataQueue.size();
            if (size > 0) {
                long sum = 0;
                for (int i = 0; i < size; i++) {
                    sum += latencyDataQueue.poll();
                }
                allTimeAvgLatency = (allTimeAvgLatency * count + sum)/(count + size);
                count = count + size;
            }

            long latencyBe = lastLatencyBe.get();

            // calculate all time average latency
            int sizeBe = latencyDataQueueBe.size();
            if (sizeBe > 0) {
                long sum = 0;
                for (int i = 0; i < sizeBe; i++) {
                    sum += latencyDataQueueBe.poll();
                }
                allTimeAvgLatencyBe = (allTimeAvgLatencyBe * countBe + sum)/(countBe + sizeBe);
                countBe = countBe + sizeBe;
            }            
            
            if (shortTermLatencyDataQueue.size() != 0 || latency != 0) {
                // take a sample for the short term latency calculation
                if (shortTermLatencyDataQueue.size() == SAMPLES_PER_MINUTE * 15) {
                    shortTermLatencyDataQueue.remove();
                }
    
                if (size == 0) {
                	// there's no latency data available -> no new requests received 
                    shortTermLatencyDataQueue.offer(new Long(0));
                } else {
                    shortTermLatencyDataQueue.offer(latency);
                }
            }
            
            if (shortTermLatencyDataQueueBe.size() != 0 || latencyBe != 0) {
                // take a sample for the short term latency calculation
                if (shortTermLatencyDataQueueBe.size() == SAMPLES_PER_MINUTE * 15) {
                    shortTermLatencyDataQueueBe.remove();
                }
    
                if (sizeBe == 0) {
                	// there's no latency data available -> no new requests received 
                    shortTermLatencyDataQueueBe.offer(new Long(0));
                } else {
                    shortTermLatencyDataQueueBe.offer(latencyBe);
                }
            }            
            
        }
    }

    private class LongTermDataCollector implements Runnable {
        public void run() {
            long latency = lastLatency.get();
            if (longTermLatencyDataQueue.size() != 0 || latency != 0) {
                if (longTermLatencyDataQueue.size() == SAMPLES_PER_HOUR * 24) {
                    longTermLatencyDataQueue.remove();
                }
                
                // adds the average latency value in last five minutes
                longTermLatencyDataQueue
                        .offer((long) getAverageLatencyByMinute(LARGE_DATA_COLLECTION_PERIOD / 60));
            }
            
            long latencyBe = lastLatencyBe.get();
            if (longTermLatencyDataQueueBe.size() != 0 || latencyBe != 0) {
                if (longTermLatencyDataQueueBe.size() == SAMPLES_PER_HOUR * 24) {
                    longTermLatencyDataQueueBe.remove();
                }
                
                // adds the average latency value in last five minutes
                longTermLatencyDataQueueBe
                        .offer((long) getAverageLatencyBackEndByMinute(LARGE_DATA_COLLECTION_PERIOD / 60));
            }            
            
        }
    }
}
