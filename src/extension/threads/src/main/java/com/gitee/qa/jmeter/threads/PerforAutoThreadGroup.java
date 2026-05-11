/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.gitee.qa.jmeter.threads;

import com.gitee.qa.jmeter.util.JMeterNewUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import com.gitee.qa.jmeter.util.CsvUtil;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.util.JMeterStopTestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ThreadGroup holds the settings for a JMeter thread group.
 *
 * This class is intended to be ThreadSafe.
 */
@GUIMenuSortOrder(1)
public class PerforAutoThreadGroup extends AbstractThreadGroup {
    private static final long serialVersionUID = 282L;

    private static final Logger log = LoggerFactory.getLogger(PerforAutoThreadGroup.class);

    private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds

    /** How often to check for shutdown during ramp-up, default 1000ms */
    private static final int RAMPUP_GRANULARITY =
            JMeterUtils.getPropDefault("jmeterthread.rampup.granularity", 1000); // $NON-NLS-1$

    //+ JMX entries - do not change the string values

    /** Ramp-up time */
    public static final String RAMP_TIME = "PerforAutoThreadGroup.ramp_time";

    /** Whether thread startup is delayed until required */
    public static final String DELAYED_START = "PerforAutoThreadGroup.delayedStart";

    /** Whether scheduler is being used */
    public static final String SCHEDULER = "PerforAutoThreadGroup.scheduler";

    /** Scheduler duration, overrides end time */
    public static final String DURATION = "PerforAutoThreadGroup.duration";

    /** Scheduler start delay, overrides start time */
    public static final String DELAY = "PerforAutoThreadGroup.delay";

    // 压测场景
    public static final String SCENARIO = "PerforAutoThreadGroup.scenario";

    // 数据保存路径
    public static final String RECORD_FILE_PATH = "PerforAutoThreadGroup.record_file_path";

    // 线程延迟结束时间
    public static final String STOP_DELAY = "PerforAutoThreadGroup.stop_delay";

    // 性能自动化线程补充参数
    public static final String PERFOR_AUTO_ARGS = "PerforAutoThreadGroup.perfor_auto_args";

    // 记录数据保存路径
    private String recordFilePath;
    // 线程延迟结束时间
    private int stopDelay;
    // 记录线程组启动开始和结束时间
    private String startTime;
    private String endTime;
    // 记录是否已经等待过线程组停止
    private boolean isWaitThreadsStopped = false;

    //- JMX entries

    private transient Thread threadStarter;

    // List of active threads
    private final ConcurrentHashMap<JMeterThread, Thread> allThreads = new ConcurrentHashMap<>();

    private transient Object addThreadLock = new Object();

    /** Is test (still) running? */
    private volatile boolean running = false;

    /** Thread Group number */
    private int groupNumber;

    /** Are we using delayed startup? */
    private boolean delayedStartup;

    /** Thread safe class */
    private ListenerNotifier notifier;

    /** This property will be cloned */
    private ListedHashTree threadGroupTree;

    /**
     * No-arg constructor.
     */
    public PerforAutoThreadGroup() {
        super();
    }

    /**
     * Set whether scheduler is being used
     *
     * @param scheduler true is scheduler is to be used
     */
    public void setScheduler(boolean scheduler) {
        setProperty(new BooleanProperty(SCHEDULER, scheduler));
    }

    /**
     * Get whether scheduler is being used
     *
     * @return true if scheduler is being used
     */
    public boolean getScheduler() {
        return getPropertyAsBoolean(SCHEDULER);
    }

    /**
     * Get the desired duration of the thread group test run
     *
     * @return the duration (in secs)
     */
    public long getDuration() {
        return getPropertyAsLong(DURATION);
    }

    /**
     * Set the desired duration of the thread group test run
     *
     * @param duration
     *            in seconds
     */
    public void setDuration(long duration) {
        setProperty(new LongProperty(DURATION, duration));
    }

    /**
     * Get the startup delay
     *
     * @return the delay (in secs)
     */
    public long getDelay() {
        return getPropertyAsLong(DELAY);
    }

    /**
     * Set the startup delay
     *
     * @param delay
     *            in seconds
     */
    public void setDelay(long delay) {
        setProperty(new LongProperty(DELAY, delay));
    }

    /**
     * Set the ramp-up value.
     *
     * @param rampUp
     *            the ramp-up value.
     */
    public void setRampUp(int rampUp) {
        setProperty(new IntegerProperty(RAMP_TIME, rampUp));
    }

    /**
     * Get the ramp-up value.
     *
     * @return the ramp-up value.
     */
    public int getRampUp() {
        return getPropertyAsInt(PerforAutoThreadGroup.RAMP_TIME);
    }


    public void setScenario(String scenario) {
        setProperty(new StringProperty(SCENARIO, scenario));
    }

    public String getScenario(){
        return getPropertyAsString(PerforAutoThreadGroup.SCENARIO);
    }

    public String getRecordFilePath(){
        return getPropertyAsString(PerforAutoThreadGroup.RECORD_FILE_PATH);
    }

    public int getStopDelay(){
        return getPropertyAsInt(PerforAutoThreadGroup.STOP_DELAY);
    }

    private boolean isDelayedStartup() {
        return getPropertyAsBoolean(DELAYED_START);
    }

    public String getPerforAutoArgs() {
        return getPropertyAsString(PerforAutoThreadGroup.PERFOR_AUTO_ARGS);
    }

    /**
     * This will schedule the time for the JMeterThread.
     *
     * @param thread JMeterThread
     * @param now in milliseconds
     */
    private void scheduleThread(JMeterThread thread, long now) {

        if (!getScheduler()) { // if the Scheduler is not enabled
            return;
        }

        if (getDelay() >= 0) { // Duration is in seconds
            thread.setStartTime(getDelay() * 1000 + now);
        } else {
            throw new JMeterStopTestException("Invalid delay " + getDelay() + " set in Thread Group:" + getName());
        }

        // set the endtime for the Thread
        if (getDuration() > 0) {// Duration is in seconds
            thread.setEndTime(getDuration() * 1000 + (thread.getStartTime()));
        } else {
            throw new JMeterStopTestException("Invalid duration " + getDuration() + " set in Thread Group:" + getName());
        }
        // Enables the scheduler
        thread.setScheduled(true);
    }

    @Override
    public void start(int groupNum, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        this.startTime = this.getCurrentTime();
        log.info("ThreadGroupName: " + this.getName() + " Scenario: " + this.getScenario() + " StartTime: " + this.startTime);

//        TODO 为什么分布式调用的时候在jmeter.properties中定义的属性值，无法在用户定义变量中直接获取。例如 influxdbip

//        log.info("==================== JMeterUtils.getJMeterProperties 1111111111111 ===============");
//        Properties props = JMeterUtils.getJMeterProperties();
//        Set<Object> keys =  props.keySet();
//        for (Object key: keys) {
//            log.info("JMeterProperties key: {} value: {}",key.toString(), props.get(key).toString());
//        }
//        log.info("==================== JMeterUtils.getJMeterProperties 2222222222222 ===============");
//
//        log.info("==================== System.getJMeterProperties 3333333333333333 ===============");
//        Properties sys_props = System.getProperties();
//        Set<Object> sys_keys =  sys_props.keySet();
//        for (Object sys_key: sys_keys) {
//            if (sys_key != null) {
//                log.info("SystemProperties key: {} value: {}", sys_key, props.get(sys_key));
//            }
//        }
//        log.info("==================== System.getJMeterProperties 44444444444444444 ===============");

        this.recordFilePath = this.getRecordFilePath();
        this.stopDelay = this.getStopDelay();
        this.running = true;
        this.groupNumber = groupNum;
        this.notifier = notifier;
        this.threadGroupTree = threadGroupTree;
        int numThreads = getNumThreads();
        int rampUpPeriodInSeconds = getRampUp();
        delayedStartup = isDelayedStartup(); // Fetch once; needs to stay constant
        log.info("Starting thread group... number={} threads={} ramp-up={} delayedStart={}", groupNumber,
                numThreads, rampUpPeriodInSeconds, delayedStartup);
        if (delayedStartup) {
            threadStarter = new Thread(new ThreadStarter(notifier, threadGroupTree, engine), getName()+"-ThreadStarter");
            threadStarter.setDaemon(true);
            threadStarter.start();
            // N.B. we don't wait for the thread to complete, as that would prevent parallel TGs
        } else {
            final JMeterContext context = JMeterContextService.getContext();
            long lastThreadStartInMillis = 0;
            int delayForNextThreadInMillis = 0;
            final int perThreadDelayInMillis = Math.round((float) rampUpPeriodInSeconds * 1000 / numThreads);
            for (int threadNum = 0; running && threadNum < numThreads; threadNum++) {
                long nowInMillis = System.currentTimeMillis();
                if(threadNum > 0) {
                    long timeElapsedToStartLastThread = nowInMillis - lastThreadStartInMillis;
                    delayForNextThreadInMillis += perThreadDelayInMillis - timeElapsedToStartLastThread;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Computed delayForNextThreadInMillis:{} for thread:{}", delayForNextThreadInMillis);
                }
                lastThreadStartInMillis = nowInMillis;
                startNewThread(notifier, threadGroupTree, engine, threadNum, context, nowInMillis,
                        Math.max(0, delayForNextThreadInMillis));
            }
        }
        log.info("Started thread group number {}", groupNumber);  // 线程组启动成功打印日志
    }

    /**
     * Start a new {@link JMeterThread} and registers it
     * @param notifier {@link ListenerNotifier}
     * @param threadGroupTree {@link ListedHashTree}
     * @param engine {@link StandardJMeterEngine}
     * @param threadNum Thread number
     * @param context {@link JMeterContext}
     * @param now Nom in milliseconds
     * @param delay int delay in milliseconds
     * @return {@link JMeterThread} newly created
     */
    private JMeterThread startNewThread(ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine,
                                        int threadNum, final JMeterContext context, long now, int delay) {
        JMeterThread jmThread = makeThread(notifier, threadGroupTree, engine, threadNum, context);
        scheduleThread(jmThread, now); // set start and end time
        jmThread.setInitialDelay(delay);
        Thread newThread = new Thread(jmThread, jmThread.getThreadName());
        registerStartedThread(jmThread, newThread);
        newThread.start();
        return jmThread;
    }

    /*
     * Fix NPE for addThreadLock transient object in remote mode (BZ60829)
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        addThreadLock = new Object();
    }

    /**
     * Register Thread when it starts
     * @param jMeterThread {@link JMeterThread}
     * @param newThread Thread
     */
    private void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
        allThreads.put(jMeterThread, newThread);
    }

    /**
     * Create {@link JMeterThread} cloning threadGroupTree
     * @param notifier {@link ListenerNotifier}
     * @param threadGroupTree {@link ListedHashTree}
     * @param engine {@link StandardJMeterEngine}
     * @param threadNumber int thread number
     * @param context {@link JMeterContext}
     * @return {@link JMeterThread}
     */
    private JMeterThread makeThread(
            ListenerNotifier notifier, ListedHashTree threadGroupTree,
            StandardJMeterEngine engine, int threadNumber,
            JMeterContext context) { // N.B. Context needs to be fetched in the correct thread
        boolean onErrorStopTest = getOnErrorStopTest();
        boolean onErrorStopTestNow = getOnErrorStopTestNow();
        boolean onErrorStopThread = getOnErrorStopThread();
        boolean onErrorStartNextLoop = getOnErrorStartNextLoop();
        String groupName = getName();
        final JMeterThread jmeterThread = new JMeterThread(doCloneTree(threadGroupTree), this, notifier);
        jmeterThread.setThreadNum(threadNumber);
        jmeterThread.setThreadGroup(this);
        jmeterThread.putVariables(context.getVariables());
        String distributedPrefix =
                JMeterUtils.getPropDefault(JMeterUtils.THREAD_GROUP_DISTRIBUTED_PREFIX_PROPERTY_NAME, "");
        final String threadName = distributedPrefix + (distributedPrefix.isEmpty() ? "":"-") +groupName + " " + groupNumber + "-" + (threadNumber + 1);
        jmeterThread.setThreadName(threadName);
        jmeterThread.setEngine(engine);
        jmeterThread.setOnErrorStopTest(onErrorStopTest);
        jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
        jmeterThread.setOnErrorStopThread(onErrorStopThread);
        jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
        return jmeterThread;
    }

    @Override
    public JMeterThread addNewThread(int delay, StandardJMeterEngine engine) {
        long now = System.currentTimeMillis();
        JMeterContext context = JMeterContextService.getContext();
        JMeterThread newJmThread;
        int numThreads;
        synchronized (addThreadLock) {
            numThreads = getNumThreads();
            setNumThreads(numThreads + 1);
        }
        newJmThread = startNewThread(notifier, threadGroupTree, engine, numThreads, context, now, delay);
        JMeterContextService.addTotalThreads( 1 );
        log.info("Started new thread in group {}", groupNumber);
        return newJmThread;
    }

    /**
     * Stop thread called threadName:
     * <ol>
     *  <li>stop JMeter thread</li>
     *  <li>interrupt JMeter thread</li>
     *  <li>interrupt underlying thread</li>
     * </ol>
     * @param threadName String thread name
     * @param now boolean for stop
     * @return true if thread stopped
     */
    @Override
    public boolean stopThread(String threadName, boolean now) {
        for (Entry<JMeterThread, Thread> threadEntry : allThreads.entrySet()) {
            JMeterThread jMeterThread = threadEntry.getKey();
            if (jMeterThread.getThreadName().equals(threadName)) {
                stopThread(jMeterThread, threadEntry.getValue(), now);
                return true;
            }
        }
        return false;
    }

    /**
     * Hard Stop JMeterThread thrd and interrupt JVM Thread if interrupt is true
     * @param jmeterThread {@link JMeterThread}
     * @param jvmThread {@link Thread}
     * @param interrupt Interrupt thread or not
     */
    private void stopThread(JMeterThread jmeterThread, Thread jvmThread, boolean interrupt) {
        jmeterThread.stop();
        jmeterThread.interrupt(); // interrupt sampler if possible
        if (interrupt && jvmThread != null) { // Bug 49734
            jvmThread.interrupt(); // also interrupt JVM thread
        }
    }

    /**
     * Called by JMeterThread when it finishes
     */
    @Override
    public void threadFinished(JMeterThread thread) {
        if (log.isDebugEnabled()) {
            log.debug("Ending thread {}", thread.getThreadName());
        }
        allThreads.remove(thread);
    }

    public void tellThreadsToStop(boolean now) {
        running = false;
        if (delayedStartup) {
            try {
                threadStarter.interrupt();
            } catch (Exception e) {
                log.warn("Exception occurred interrupting ThreadStarter", e);
            }
        }

        allThreads.forEach((key, value) -> stopThread(key, value, now));
    }

    /**
     * This is an immediate stop interrupting:
     * <ul>
     *  <li>current running threads</li>
     *  <li>current running samplers</li>
     * </ul>
     * For each thread, invoke:
     * <ul>
     * <li>{@link JMeterThread#stop()} - set stop flag</li>
     * <li>{@link JMeterThread#interrupt()} - interrupt sampler</li>
     * <li>{@link Thread#interrupt()} - interrupt JVM thread</li>
     * </ul>
     */
    @Override
    public void tellThreadsToStop() {
        tellThreadsToStop(true);
    }

    /**
     * This is a clean shutdown.
     * For each thread, invoke:
     * <ul>
     * <li>{@link JMeterThread#stop()} - set stop flag</li>
     * </ul>
     */
    @Override
    public void stop() {
        running = false;
        if (delayedStartup) {
            try {
                threadStarter.interrupt();
            } catch (Exception e) {
                log.warn("Exception occurred interrupting ThreadStarter", e);
            }
        }
        allThreads.keySet().forEach(JMeterThread::stop);
    }

    /**
     * @return number of active threads
     */
    @Override
    public int numberOfActiveThreads() {
        return allThreads.size();
    }

    /**
     * @return boolean true if all threads stopped
     */
    @Override
    public boolean verifyThreadsStopped() {
        boolean stoppedAll = true;
        if (delayedStartup) {
            stoppedAll = verifyThreadStopped(threadStarter);
        }
        for (Thread t : allThreads.values()) {
            stoppedAll = stoppedAll && verifyThreadStopped(t);
        }
        return stoppedAll;
    }

    /**
     * Verify thread stopped and return true if stopped successfully
     * @param thread Thread
     * @return boolean
     */
    private boolean verifyThreadStopped(Thread thread) {
        boolean stopped = true;
        if (thread != null && thread.isAlive()) {
            try {
                thread.join(WAIT_TO_DIE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (thread.isAlive()) {
                stopped = false;
                if (log.isWarnEnabled()) {
                    log.warn("Thread won't exit: {}", thread.getName());
                }
            }
        }
        return stopped;
    }

    /**
     * Wait for all Group Threads to stop
     */
    @Override
    public void waitThreadsStopped() {
        if (delayedStartup) {
            waitThreadStopped(threadStarter);
        }
        /* @Bugzilla 60933
         * Threads can be added on the fly during a test into allThreads
         * we have to check if allThreads is really empty before stopping
         */
        while (!allThreads.isEmpty()) {
            allThreads.values().forEach(this::waitThreadStopped);
        }
        if (!isWaitThreadsStopped){
            isWaitThreadsStopped = true;
            this.endTime = this.getCurrentTime();
            log.info("ThreadGroupName: " + this.getName() + " Scenario: " + this.getScenario() + " EndTime: " + this.endTime);
            try {
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                String duration = JMeterNewUtils.TimeDifference(this.startTime, this.endTime, "yyyyMMdd-HHmmss", "mm");
                String jmxName = FileServer.getFileServer().getScriptName();
                // 写入文件
                CsvUtil csvUtil = new CsvUtil(this.recordFilePath);
                csvUtil.write(
                        new ArrayList<>(Arrays.asList(
                                "JmxName",
                                "ThreadGroupObj",
                                "Uuid",
                                "ThreadGroupName",
                                "Scenario",
                                "StartTime",
                                "EndTime",
                                "Duration",
                                "NumThreads",
                                "BucketThread",
                                "ExtraArgs",
                                "HTTPArgs"
                        )),
                        new ArrayList<>(Arrays.asList(
                                jmxName,
                                this.toString(),
                                uuid,
                                this.getName(),
                                this.getScenario(),
                                this.startTime,
                                this.endTime,
                                duration,
                                String.valueOf(getNumThreads()),
                                "",
                                this.getPerforAutoArgs(),
                                this.getHTTPSamplerArgs()
                        ))
                );
                log.info("线程结束写入文件完成");
            } catch (Exception e) {
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                log.error("线程结束写入文件失败：\n" + stringWriter.toString());
            }
            try {
                Thread.sleep(this.stopDelay * 1000);
            } catch (Exception e) {
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                log.error("线程延迟停止失败：\n" + stringWriter.toString());
            }
        }
    }

    /*
    * 获取当前线程组内所有HTTPSampler请求的Method+URL
    * */
    private String getHTTPSamplerArgs() {
        ListedHashTree tree = this.threadGroupTree;
        SearchByClass<HTTPSamplerProxy> httpSamplerProxySearchByClass = new SearchByClass<>(HTTPSamplerProxy.class);
        // 查找匹配的节点并添加到 httpSamplerProxySearchByClass.objectsOfClass 中
        tree.traverse(httpSamplerProxySearchByClass);
        // 获取当前线程组中所有匹配到的 HTTPSamplerProxy 节点
        Collection<HTTPSamplerProxy> httpSamplerProxies = httpSamplerProxySearchByClass.getSearchResults();

        String httpArgsStr = "|";
        for (HTTPSamplerProxy httpSamplerProxy : httpSamplerProxies) {
            if (!httpSamplerProxy.isEnabled()) continue;
            // System.out.println(httpSamplerProxy.getMethod() + " " + httpSamplerProxy.getPath());
            httpArgsStr += httpSamplerProxy.getMethod() + " " + httpSamplerProxy.getPath() + "|";
        }
        return httpArgsStr;
    }

    /**
     * Wait for thread to stop
     * @param thread Thread
     */
    private void waitThreadStopped(Thread thread) {
        if (thread == null) {
            return;
        }
        while (thread.isAlive()) {
            try {
                thread.join(WAIT_TO_DIE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * @param tree {@link ListedHashTree}
     * @return a clone of tree
     */
    private ListedHashTree doCloneTree(ListedHashTree tree) {
        TreeCloner cloner = new TreeCloner(true);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }

    private String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat();  // 格式化时间
        sdf.applyPattern("yyyyMMdd-HHmmss");  // a为am/pm的标记
        Date date = new Date();  // 获取当前时间
        return sdf.format(date);
    }

    /**
     * Starts Threads using ramp up
     */
    class ThreadStarter implements Runnable {

        private final ListenerNotifier notifier;
        private final ListedHashTree threadGroupTree;
        private final StandardJMeterEngine engine;
        private final JMeterContext context;

        public ThreadStarter(ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
            super();
            this.notifier = notifier;
            this.threadGroupTree = threadGroupTree;
            this.engine = engine;
            // Store context from Root Thread to pass it to created threads
            this.context = JMeterContextService.getContext();
        }

        /**
         * Pause ms milliseconds
         * @param ms long milliseconds
         */
        private void pause(long ms){
            try {
                TimeUnit.MILLISECONDS.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Wait for delay with RAMPUP_GRANULARITY
         * @param delay delay in ms
         */
        private void delayBy(long delay) {
            if (delay > 0) {
                long start = System.currentTimeMillis();
                long end = start + delay;
                long now;
                long pause = RAMPUP_GRANULARITY; // maximum pause to use
                while(running && (now = System.currentTimeMillis()) < end) {
                    long togo = end - now;
                    if (togo < pause) {
                        pause = togo;
                    }
                    pause(pause); // delay between checks
                }
            }
        }

        @Override
        public void run() {
            try {
                // Copy in ThreadStarter thread context from calling Thread
                JMeterContextService.getContext().setVariables(this.context.getVariables());
                long endtime = 0;
                final boolean usingScheduler = getScheduler();
                if (usingScheduler) {
                    // set the start time for the Thread
                    if (getDelay() > 0) {// Duration is in seconds
                        delayBy(getDelay() * 1000);
                    }
                    // set the endtime for the Thread
                    endtime = getDuration();
                    if (endtime > 0) {// Duration is in seconds, starting from when the threads start
                        endtime = endtime *1000 + System.currentTimeMillis();
                    }
                }
                final int numThreads = getNumThreads();
                final float rampUpOriginInMillis = (float) getRampUp() * 1000;
                final long startTimeInMillis = System.currentTimeMillis();
                for (int threadNumber = 0; running && threadNumber < numThreads; threadNumber++) {
                    if (threadNumber > 0) {
                        long elapsedInMillis = System.currentTimeMillis() - startTimeInMillis;
                        final int perThreadDelayInMillis =
                                Math.round((rampUpOriginInMillis - elapsedInMillis) / (float) (numThreads - threadNumber));
                        pause(Math.max(0, perThreadDelayInMillis)); // ramp-up delay (except first)
                    }
                    if (usingScheduler && System.currentTimeMillis() > endtime) {
                        break; // no point continuing beyond the end time
                    }
                    JMeterThread jmThread = makeThread(notifier, threadGroupTree, engine, threadNumber, context);
                    jmThread.setInitialDelay(0);   // Already waited
                    if (usingScheduler) {
                        jmThread.setScheduled(true);
                        jmThread.setEndTime(endtime);
                    }
                    Thread newThread = new Thread(jmThread, jmThread.getThreadName());
                    newThread.setDaemon(false); // ThreadStarter is daemon, but we don't want sampler threads to be so too
                    registerStartedThread(jmThread, newThread);
                    newThread.start();
                }
            } catch (Exception ex) {
                log.error("An error occurred scheduling delay start of threads for Thread Group: {}", getName(), ex);
            }
        }
    }
}
