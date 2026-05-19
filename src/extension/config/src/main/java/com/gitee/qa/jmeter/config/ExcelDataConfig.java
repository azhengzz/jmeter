package com.gitee.qa.jmeter.config;

import com.alibaba.excel.util.ListUtils;
import com.gitee.qa.jmeter.config.util.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoConfigMerge;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelDataConfig extends ConfigTestElement implements TestBean, LoopIterationListener, NoConfigMerge, TestStateListener {

    private static final Logger log = LoggerFactory.getLogger(ExcelDataConfig.class);

    private static final String EOFVALUE = // value to return at EOF
            JMeterUtils.getPropDefault("csvdataset.eofstring", "<EOF>"); //$NON-NLS-1$ //$NON-NLS-2$

    private transient String filename;

    private transient String variableNames;

    private transient boolean recycle = true;

    private transient boolean stopThread;

    private transient String[] vars;

    private transient String alias;

    private transient String shareMode;

    private boolean ignoreFirstLine = false;

    private boolean firstLineIsNames = false;
    private final int INDEX_NULL_VALUE = -1;
    private List<Map<Integer, String>> ALL_DATA = ListUtils.newArrayList();
    private int ALL_DATA_LENGTH = 0;
    String[] HEADER = null;

    static final Map<String, Integer> filesIndex = new HashMap<>();  // 记录不同线程共享模式下读取文件行数


    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        String fileName = getFilename().trim();  // TODO 需要处理下相对路径 类似include控制器中的include逻辑
        File file = new File(fileName);
        if (!file.exists() && !file.isAbsolute()) {
            file = new File(FileServer.getFileServer().getBaseDir(), fileName);
            if (!file.canRead() || !file.isFile()) {
                log.error("ExcelDataConfig '{}' can't load '{}'", this.getName(), fileName);
            }
        }
        final String fileAbsolutePath = file.getAbsolutePath();

        final JMeterContext context = getThreadContext();

        if (vars == null) {
            // 如果是第一次循环，读取Excel数据
            synchronized (ExcelDataConfig.class){  // 防止其他线程读取其他文件，导致数据错乱
                if (ignoreFirstLine) {
                    ExcelUtil.readExcel(fileAbsolutePath, 1);
                }else {
                    ExcelUtil.readExcel(fileAbsolutePath, 0);
                }
                HEADER = ExcelUtil.getHeaderAsArray();
                ALL_DATA = ExcelUtil.getAllData();
                ALL_DATA_LENGTH = ALL_DATA.size();
            }
            String[] firstLineData = ALL_DATA.get(0).values().toArray(new String[0]);
            String mode = getShareMode();
            int modeInt = ExcelDataConfigBeanInfo.getShareModeAsInt(mode);
            switch(modeInt){
                case ExcelDataConfigBeanInfo.SHARE_ALL:
                    alias = fileName;
                    break;
                case ExcelDataConfigBeanInfo.SHARE_GROUP:
                    alias = fileName+"@"+System.identityHashCode(context.getThreadGroup());
                    break;
                case ExcelDataConfigBeanInfo.SHARE_THREAD:
                    alias = fileName+"@"+System.identityHashCode(context.getThread());
                    break;
                default:
                    alias = fileName+"@"+mode; // user-specified key
                    break;
            }
            synchronized (ExcelDataConfig.class) {
                int index = filesIndex.getOrDefault(alias, INDEX_NULL_VALUE);
                if (index == INDEX_NULL_VALUE){  // 第一次执行
                    filesIndex.put(alias, 0);  // 从第一条开始读取
                }
            }
            final String names = getVariableNames();
            if (StringUtils.isEmpty(names)) {
                if (ignoreFirstLine){  // 如果忽略了首行，则直接拿HEADER作为变量
                    vars = HEADER;
                }else {  // 如果没有忽略首行，则拿第一行数据作为变量
                    firstLineIsNames = true;
                    vars = firstLineData;
                }
            } else {
                vars = JOrphanUtils.split(names, ","); // $NON-NLS-1$
            }
            trimVarNames(vars);
        }

        JMeterVariables threadVars = context.getVariables();
        String[] lineValues = {};
        try{
            synchronized (ExcelDataConfig.class) {
                int index = filesIndex.get(alias);
                if (recycle && (index == ALL_DATA_LENGTH || index == 0)){  // 满足重复循环读取条件
                    if (firstLineIsNames){  // 当拿第一行数据作为变量时，从第二行开始读取
                        index = 1;
                    }else {
                        index = 0;  // 否则从第一行开始读取
                    }
                }
                lineValues = ALL_DATA.get(index).values().toArray(new String[0]);
                filesIndex.put(alias, ++index);
            }
            for (int a = 0; a < vars.length && a < lineValues.length; a++) {
                threadVars.put(vars[a], lineValues[a]);
            }
        } catch (IndexOutOfBoundsException e){
            if (stopThread) {
                throw new JMeterStopThreadException("End of file:"+ getFilename()+" detected for CSV DataSet:"
                        +getName()+" configured with stopThread:"+ getStopThread()+", recycle:" + getRecycle());
            }
            for (String var :vars) {
                threadVars.put(var, EOFVALUE);
            }
        }
    }

    /**
     * @return Returns the filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename
     *            The filename to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return Returns the variableNames.
     */
    public String getVariableNames() {
        return variableNames;
    }

    /**
     * @param variableNames
     *            The variableNames to set.
     */
    public void setVariableNames(String variableNames) {
        this.variableNames = variableNames;
    }

    public boolean getRecycle() {
        return recycle;
    }

    public void setRecycle(boolean recycle) {
        this.recycle = recycle;
    }

    public boolean getStopThread() {
        return stopThread;
    }

    public void setStopThread(boolean value) {
        this.stopThread = value;
    }

    public String getShareMode() {
        return shareMode;
    }

    public void setShareMode(String value) {
        this.shareMode = value;
    }

    /**
     * @return the ignoreFirstLine
     */
    public boolean isIgnoreFirstLine() {
        return ignoreFirstLine;
    }

    /**
     * @param ignoreFirstLine the ignoreFirstLine to set
     */
    public void setIgnoreFirstLine(boolean ignoreFirstLine) {
        this.ignoreFirstLine = ignoreFirstLine;
    }

    /**
     * trim content of array varNames
     * @param varsNames
     */
    private void trimVarNames(String[] varsNames) {
        for (int i = 0; i < varsNames.length; i++) {
            varsNames[i] = varsNames[i].trim();
        }
    }

    @Override
    public void testStarted() {
    }

    @Override
    public void testStarted(String host) {

    }

    @Override
    public void testEnded() {
        filesIndex.clear();
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }
}
