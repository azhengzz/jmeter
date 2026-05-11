package kg.apc.jmeter.threads;

import com.gitee.qa.jmeter.util.JMeterNewUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.threads.*;
import com.gitee.qa.jmeter.util.CsvUtil;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ThreadGroup;

public abstract class AbstractPerforAutoSimpleThreadGroup extends AbstractThreadGroup {
    private static final Logger log = LoggerFactory.getLogger(AbstractPerforAutoSimpleThreadGroup.class);

    private static final long WAIT_TO_DIE = JMeterUtils.getPropDefault("jmeterengine.threadstop.wait", 5 * 1000); // 5 seconds
    public static final String THREAD_GROUP_DISTRIBUTED_PREFIX_PROPERTY_NAME = "__jm.D_TG"; // FIXME: use JMeterUtils.THREAD_GROUP_DISTRIBUTED_PREFIX_PROPERTY_NAME when dependency is updated

    // 压测场景
    public static final String SCENARIO = "AbstractPerforAutoSimpleThreadGroup.scenario";

    // 数据保存路径
    public static final String RECORD_FILE_PATH = "AbstractPerforAutoSimpleThreadGroup.record_file_path";

    // 线程延迟结束时间
    public static final String STOP_DELAY = "AbstractPerforAutoSimpleThreadGroup.stop_delay";

    // 性能自动化线程补充参数
    public static final String PERFOR_AUTO_ARGS = "AbstractPerforAutoSimpleThreadGroup.perfor_auto_args";

    private ListedHashTree threadGroupTree;

    // 记录是否已经等待过线程组停止
    private boolean isWaitThreadsStopped = false;

    // 记录数据保存路径
    private String recordFilePath;
    // 记录线程组启动开始和结束时间
    private String startTime;
    private String endTime;
    // 线程延迟结束时间
    private int stopDelay;

    // 记录梯度加压各个梯度的时间戳
    public LinkedHashMap<Integer, HashMap<String, Long>> bucketThreadMap = new LinkedHashMap<>();
    // 记录上一个梯度编号，如果为null表示结束计算
    public Integer lastBucketNo = null;

    // List of active threads
    protected final Map<JMeterThread, Thread> allThreads = new ConcurrentHashMap<>();

    /**
     * Is test (still) running?
     */
    private volatile boolean running = false;

    //JMeter 2.7 Compatibility
    private long tgStartTime = -1;
    private static final long TOLERANCE = 1000;


    /**
     * No-arg constructor.
     */
    public AbstractPerforAutoSimpleThreadGroup() {
    }

    protected abstract void scheduleThread(JMeterThread thread, long now);

    //JMeter 2.7 compatibility
    public void scheduleThread(JMeterThread thread) {
        if (System.currentTimeMillis() - tgStartTime > TOLERANCE) {
            tgStartTime = System.currentTimeMillis();
        }
        scheduleThread(thread, tgStartTime);
    }


    @Override
    public void start(int groupNum, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        this.startTime = this.getCurrentTime();
        this.recordFilePath = this.getRecordFilePath();
        this.stopDelay = this.getStopDelay();
        this.threadGroupTree = threadGroupTree;
        running = true;

        int numThreads = getNumThreads();

        log.info("Starting thread group number " + groupNum + " threads " + numThreads);

        long now = System.currentTimeMillis(); // needs to be same time for all threads in the group
        final JMeterContext context = JMeterContextService.getContext();
        for (int i = 0; running && i < numThreads; i++) {
            JMeterThread jmThread = makeThread(groupNum, notifier, threadGroupTree, engine, i, context);
            scheduleThread(jmThread, now); // set start and end time
            Thread newThread = new Thread(jmThread, jmThread.getThreadName());
            registerStartedThread(jmThread, newThread);
            newThread.start();
        }

        log.info("Started thread group number " + groupNum);
    }

    private void registerStartedThread(JMeterThread jMeterThread, Thread newThread) {
        allThreads.put(jMeterThread, newThread);
    }

    private JMeterThread makeThread(int groupNum,
                                    ListenerNotifier notifier, ListedHashTree threadGroupTree,
                                    StandardJMeterEngine engine, int threadNum,
                                    JMeterContext context) { // N.B. Context needs to be fetched in the correct thread
        boolean onErrorStopTest = getOnErrorStopTest();
        boolean onErrorStopTestNow = getOnErrorStopTestNow();
        boolean onErrorStopThread = getOnErrorStopThread();
        boolean onErrorStartNextLoop = getOnErrorStartNextLoop();

        String groupName = getName();
        String distributedPrefix = JMeterUtils.getPropDefault(THREAD_GROUP_DISTRIBUTED_PREFIX_PROPERTY_NAME, "");
        final String threadName = distributedPrefix + (distributedPrefix.isEmpty() ? "" : "-") + groupName + " " + groupNum + "-" + (threadNum + 1);

        final JMeterThread jmeterThread = new JMeterThread(doCloneTree(threadGroupTree), this, notifier);
        jmeterThread.setThreadNum(threadNum);
        jmeterThread.setThreadGroup(this);
        jmeterThread.putVariables(context.getVariables());
        jmeterThread.setThreadName(threadName);
        jmeterThread.setEngine(engine);
        jmeterThread.setOnErrorStopTest(onErrorStopTest);
        jmeterThread.setOnErrorStopTestNow(onErrorStopTestNow);
        jmeterThread.setOnErrorStopThread(onErrorStopThread);
        jmeterThread.setOnErrorStartNextLoop(onErrorStartNextLoop);
        return jmeterThread;
    }

    @Override
    public boolean stopThread(String threadName, boolean now) {
        for (Entry<JMeterThread, Thread> entry : allThreads.entrySet()) {
            JMeterThread thrd = entry.getKey();
            if (thrd.getThreadName().equals(threadName)) {
                thrd.stop();
                thrd.interrupt();
                if (now) {
                    Thread t = entry.getValue();
                    if (t != null) {
                        t.interrupt();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        log.debug("Ending thread " + thread.getThreadName());
        allThreads.remove(thread);
    }

    public String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat();  // 格式化时间
        sdf.applyPattern("yyyyMMdd-HHmmss");  // a为am/pm的标记
        Date date = new Date();  // 获取当前时间
        return sdf.format(date);
    }

    public String formatDate(Long time) {
        ZoneId zone = ZoneId.of(JMeterUtils.getProperty("user.timezone"));  // 获取时区
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return dateTime.format(formatter);
    }

    @Override
    public void tellThreadsToStop() {
        running = false;
        for (Entry<JMeterThread, Thread> entry : allThreads.entrySet()) {
            JMeterThread item = entry.getKey();
            item.stop(); // set stop flag
            item.interrupt(); // interrupt sampler if possible
            Thread t = entry.getValue();
            if (t != null) { // Bug 49734
                t.interrupt(); // also interrupt JVM thread
            }
        }
    }

    @Override
    public void stop() {
        running = false;
        for (JMeterThread item : allThreads.keySet()) {
            item.stop();
        }
    }

    @Override
    public int numberOfActiveThreads() {
        return allThreads.size();
    }

    @Override
    public boolean verifyThreadsStopped() {
        boolean stoppedAll = true;
        for (Thread t : allThreads.values()) {
            stoppedAll = stoppedAll && verifyThreadStopped(t);
        }
        return stoppedAll;
    }

    private boolean verifyThreadStopped(Thread thread) {
        boolean stopped = true;
        if (thread != null) {
            if (thread.isAlive()) {
                try {
                    thread.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                    log.debug("Interrupted", e);
                }
                if (thread.isAlive()) {
                    stopped = false;
                    log.warn("Thread won't exit: " + thread.getName());
                }
            }
        }
        return stopped;
    }

    @Override
    public void waitThreadsStopped() {
        for (Thread t : allThreads.values()) {
            waitThreadStopped(t);
        }
        if (!isWaitThreadsStopped){
            isWaitThreadsStopped = true;
            this.endTime = this.getCurrentTime();
            log.info("ThreadGroupName: " + this.getName() + " Scenario: " + this.getScenario() + " EndTime: " + this.endTime);
            // 获取线程组各梯度信息
            String bucketThreadMapStr = "";
            if (!bucketThreadMap.isEmpty()) {
                bucketThreadMapStr += "|";
                for (Integer bucketNo: bucketThreadMap.keySet()) {
                    HashMap<String, Long> threadBucket = bucketThreadMap.get(bucketNo);
                    bucketThreadMapStr += threadBucket.get("threadNum") + ":" + formatDate(threadBucket.get("rampUpBucketStartTime")) + ":" + formatDate(threadBucket.get("rampUpBucketEndTime")) + "|";
                }
            }
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
                        // NumThreads 设置为NA 标识梯度压测的线程数不固定的数值
                        new ArrayList<>(Arrays.asList(
                                jmxName,
                                this.toString(),
                                uuid,
                                this.getName(),
                                this.getScenario(),
                                this.startTime,
                                this.endTime,
                                duration,
                                "NA",
                                bucketThreadMapStr,
                                this.getPerforAutoArgs(),
                                this.getHTTPSamplerArgs()
                        ))
                );
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

    private void waitThreadStopped(Thread thread) {
        if (thread != null) {
            while (thread.isAlive()) {
                try {
                    thread.join(WAIT_TO_DIE);
                } catch (InterruptedException e) {
                    log.debug("Interrupted", e);
                }
            }
        }
    }

    public String getScenario(){
        return getPropertyAsString(AbstractPerforAutoSimpleThreadGroup.SCENARIO);
    }

    public String getRecordFilePath(){
        return getPropertyAsString(AbstractPerforAutoSimpleThreadGroup.RECORD_FILE_PATH);
    }

    public int getStopDelay(){
        return getPropertyAsInt(AbstractPerforAutoSimpleThreadGroup.STOP_DELAY);
    }

    public String getPerforAutoArgs() {
        return getPropertyAsString(AbstractPerforAutoSimpleThreadGroup.PERFOR_AUTO_ARGS);
    }

    private ListedHashTree doCloneTree(ListedHashTree tree) {
        TreeCloner cloner = new TreeCloner(true);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }

    /**
     * Add a new {@link JMeterThread} to this {@link ThreadGroup} for engine
     *
     * @param delay  Delay in milliseconds
     * @param engine {@link StandardJMeterEngine}
     * @return {@link JMeterThread}
     */
    @Override
    public JMeterThread addNewThread(int delay, StandardJMeterEngine engine) {
        return null;
    }
}

