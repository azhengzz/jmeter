package kg.apc.jmeter.threads;

import com.gitee.qa.jmeter.threads.common.gui.PerforAutoArgsPanel;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.DateTimeRenderer;
import kg.apc.charting.GraphPanelChart;
import kg.apc.charting.rows.GraphRowSumValues;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.JLabeledChoice;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class PerforAutoSteppingThreadGroupGui
        extends AbstractThreadGroupGui {

    private class FieldChangesListener implements DocumentListener {

        private final JTextField tf;

        public FieldChangesListener(JTextField field) {
            tf = field;
        }

        private void update() {
            refreshPreview();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (tf.hasFocus()) {
                update();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (tf.hasFocus()) {
                update();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (tf.hasFocus()) {
                update();
            }
        }
    }

    public static final String WIKIPAGE = "PerforAutoSteppingThreadGroup";

    protected ConcurrentHashMap<String, AbstractGraphRow> model;
    private GraphPanelChart chart;
    private JTextField initialDelay;
    private JTextField incUserCount;
    private JTextField incUserCountBurst;
    private JTextField incUserPeriod;
    private JTextField flightTime;
    private JTextField decUserCount;
    private JTextField decUserPeriod;
    private JTextField totalThreads;
    private LoopControlPanel loopPanel;
    private JTextField rampUp;

    private JLabeledChoice lcScenario;  // 压测场景选项

    private JTextField recordFilePath;  // 压测记录文件

    private JTextField stopDelay;  // 线程延迟结束时间

    private PerforAutoArgsPanel perforAutoArgsPanel;  // 性能自动化参数面板

    private static final class Scenario {

        public final static String SINGLE_BASIS_TRADE = "单交易基准";

        public final static String SINGLE_LOAD_GRAD_TRADE = "单交易负载梯度";

        public final static String MIX_CAPACITY_GRAD_TRADE = "混合容量梯度";

        public final static String STABILITY_TRADE = "稳定性测试";

    }

    public PerforAutoSteppingThreadGroupGui() {
        super();
        init();
        initGui();
    }

    protected final void init() {
        JMeterPluginsUtils.addHelpLinkToPanel(this, WIKIPAGE);
        JPanel containerPanel = new VerticalPanel();
        containerPanel.add(createParamsPanel());
        containerPanel.add(createPTPanel());

        chart = new GraphPanelChart(false, true);
        model = new ConcurrentHashMap<>();
        chart.setRows(model);
        chart.getChartSettings().setDrawFinalZeroingLines(true);

        chart.setxAxisLabel("Elapsed time");
        chart.setYAxisLabel("Number of active threads");

        chart.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        containerPanel.add(GuiBuilderHelper.getComponentWithMargin(chart, 2, 2, 0, 2), BorderLayout.CENTER);

        add(containerPanel, BorderLayout.CENTER);

        // this magic LoopPanel provides functionality for thread loops
        createControllerPanel();
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initGui();
    }

    // Initialise the gui field values
    private void initGui() {
        totalThreads.setText("100");
        initialDelay.setText("0");
        incUserCount.setText("10");
        incUserCountBurst.setText("0");
        incUserPeriod.setText("30");
        flightTime.setText("60");
        decUserCount.setText("5");
        decUserPeriod.setText("1");
        rampUp.setText("5");
        lcScenario.setText(Scenario.SINGLE_BASIS_TRADE);
        recordFilePath.setText("");
        stopDelay.setText("5");
        perforAutoArgsPanel.initGui();
    }

    private JPanel createParamsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 5, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Threads Scheduling Parameters"));

        panel.add(new JLabel("This group will start", JLabel.RIGHT));
        totalThreads = new JTextField(5);
        panel.add(totalThreads);
        panel.add(new JLabel("threads:", JLabel.LEFT));
        panel.add(new JLabel());
        panel.add(new JLabel());

        panel.add(new JLabel("First, wait for", JLabel.RIGHT));
        initialDelay = new JTextField(5);
        panel.add(initialDelay);
        panel.add(new JLabel("seconds;", JLabel.LEFT));
        panel.add(new JLabel());
        panel.add(new JLabel());

        panel.add(new JLabel("Then start", JLabel.RIGHT));
        incUserCountBurst = new JTextField(5);
        panel.add(incUserCountBurst);
        panel.add(new JLabel("threads; ", JLabel.LEFT));
        panel.add(new JLabel(""));
        panel.add(new JLabel());

        panel.add(new JLabel("Next, add", JLabel.RIGHT));
        incUserCount = new JTextField(5);
        panel.add(incUserCount);
        panel.add(new JLabel("threads every", JLabel.CENTER));
        incUserPeriod = new JTextField(5);
        panel.add(incUserPeriod);
        panel.add(new JLabel("seconds, ", JLabel.LEFT));

        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(new JLabel("using ramp-up", JLabel.RIGHT));
        rampUp = new JTextField(5);
        panel.add(rampUp);
        panel.add(new JLabel("seconds.", JLabel.LEFT));

        panel.add(new JLabel("Then hold load for", JLabel.RIGHT));
        flightTime = new JTextField(5);
        panel.add(flightTime);
        panel.add(new JLabel("seconds.", JLabel.LEFT));
        panel.add(new JLabel());
        panel.add(new JLabel());

        panel.add(new JLabel("Finally, stop", JLabel.RIGHT));
        decUserCount = new JTextField(5);
        panel.add(decUserCount);
        panel.add(new JLabel("threads every", JLabel.CENTER));
        decUserPeriod = new JTextField(5);
        panel.add(decUserPeriod);
        panel.add(new JLabel("seconds.", JLabel.LEFT));

        registerJTextfieldForGraphRefresh(totalThreads);
        registerJTextfieldForGraphRefresh(initialDelay);
        registerJTextfieldForGraphRefresh(incUserCount);
        registerJTextfieldForGraphRefresh(incUserCountBurst);
        registerJTextfieldForGraphRefresh(incUserPeriod);
        registerJTextfieldForGraphRefresh(flightTime);
        registerJTextfieldForGraphRefresh(decUserCount);
        registerJTextfieldForGraphRefresh(decUserPeriod);
        registerJTextfieldForGraphRefresh(rampUp);

        return panel;
    }

    /**
     * 性能自动化配置面板
     * */
    private JPanel createPTPanel(){
        VerticalPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "性能自动化配置")); // $NON-NLS-1$

        // 压测场景
        JPanel panelScenario = new HorizontalPanel();
        panelScenario.setLayout(new FlowLayout(FlowLayout.LEFT));
        lcScenario = new JLabeledChoice("选择压测场景", // $NON-NLS-1$
                new String[] {
                        Scenario.SINGLE_BASIS_TRADE,
                        Scenario.SINGLE_LOAD_GRAD_TRADE,
                        Scenario.MIX_CAPACITY_GRAD_TRADE,
                        Scenario.STABILITY_TRADE
                }, false, false);
        panelScenario.add(lcScenario);

        // 记录数据保存路径
        JPanel panelRecordPath = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel("数据保存路径"); // $NON-NLS-1$
        panelRecordPath.add(label, BorderLayout.WEST);
        recordFilePath = new JTextField();
        panelRecordPath.add(recordFilePath, BorderLayout.CENTER);

        // 线程延迟结束时间
        JPanel panelStopDelay = new JPanel(new BorderLayout(5, 0));
        JLabel labelStopDelay = new JLabel("线程延迟结束时间（秒）"); // $NON-NLS-1$
        panelStopDelay.add(labelStopDelay, BorderLayout.WEST);
        stopDelay = new JTextField();
        panelStopDelay.add(stopDelay, BorderLayout.CENTER);

        // 性能自动化参数
        perforAutoArgsPanel = new PerforAutoArgsPanel();

        panel.add(panelScenario);
        panel.add(panelRecordPath);
        panel.add(panelStopDelay);
        panel.add(perforAutoArgsPanel);

        return panel;
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return "jp@gc - Stepping Thread Group(性能自动化)";  // $NON-NLS-1$
    }

    @Override
    public TestElement createTestElement() {
        PerforAutoSteppingThreadGroup tg = new PerforAutoSteppingThreadGroup();
        modifyTestElement(tg);
        tg.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
        return tg;
    }

    private void refreshPreview() {
        PerforAutoSteppingThreadGroup tgForPreview = new PerforAutoSteppingThreadGroup();
        tgForPreview.setNumThreads(new CompoundVariable(totalThreads.getText()).execute());
        tgForPreview.setThreadGroupDelay(new CompoundVariable(initialDelay.getText()).execute());
        tgForPreview.setInUserCount(new CompoundVariable(incUserCount.getText()).execute());
        tgForPreview.setInUserCountBurst(new CompoundVariable(incUserCountBurst.getText()).execute());
        tgForPreview.setInUserPeriod(new CompoundVariable(incUserPeriod.getText()).execute());
        tgForPreview.setOutUserCount(new CompoundVariable(decUserCount.getText()).execute());
        tgForPreview.setOutUserPeriod(new CompoundVariable(decUserPeriod.getText()).execute());
        tgForPreview.setFlightTime(new CompoundVariable(flightTime.getText()).execute());
        tgForPreview.setRampUp(new CompoundVariable(rampUp.getText()).execute());

        if (tgForPreview.getInUserCountAsInt() == 0) {
            tgForPreview.setInUserCount(new CompoundVariable(totalThreads.getText()).execute());
        }
        if (tgForPreview.getOutUserCountAsInt() == 0) {
            tgForPreview.setOutUserCount(new CompoundVariable(totalThreads.getText()).execute());
        }

        updateChart(tgForPreview);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);

        if (te instanceof PerforAutoSteppingThreadGroup) {
            PerforAutoSteppingThreadGroup tg = (PerforAutoSteppingThreadGroup) te;
            tg.setProperty(PerforAutoSteppingThreadGroup.NUM_THREADS, totalThreads.getText());
            tg.setThreadGroupDelay(initialDelay.getText());
            tg.setInUserCount(incUserCount.getText());
            tg.setInUserCountBurst(incUserCountBurst.getText());
            tg.setInUserPeriod(incUserPeriod.getText());
            tg.setOutUserCount(decUserCount.getText());
            tg.setOutUserPeriod(decUserPeriod.getText());
            tg.setFlightTime(flightTime.getText());
            tg.setRampUp(rampUp.getText());
            tg.setSamplerController((LoopController) loopPanel.createTestElement());
            tg.setProperty(PerforAutoSteppingThreadGroup.SCENARIO, lcScenario.getText());
            tg.setProperty(PerforAutoSteppingThreadGroup.RECORD_FILE_PATH, recordFilePath.getText());
            tg.setProperty(PerforAutoSteppingThreadGroup.STOP_DELAY, stopDelay.getText());
            perforAutoArgsPanel.modifyTestElement(tg);
            refreshPreview();
        }
    }

    @Override
    public void configure(TestElement te) {
        super.configure(te);
        PerforAutoSteppingThreadGroup tg = (PerforAutoSteppingThreadGroup) te;
        totalThreads.setText(tg.getNumThreadsAsString());
        initialDelay.setText(tg.getThreadGroupDelay());
        incUserCount.setText(tg.getInUserCount());
        incUserCountBurst.setText(tg.getInUserCountBurst());
        incUserPeriod.setText(tg.getInUserPeriod());
        decUserCount.setText(tg.getOutUserCount());
        decUserPeriod.setText(tg.getOutUserPeriod());
        flightTime.setText(tg.getFlightTime());
        rampUp.setText(tg.getRampUp());
        lcScenario.setText(tg.getPropertyAsString(PerforAutoSteppingThreadGroup.SCENARIO));
        recordFilePath.setText(tg.getPropertyAsString(PerforAutoSteppingThreadGroup.RECORD_FILE_PATH));
        stopDelay.setText(tg.getPropertyAsString(PerforAutoSteppingThreadGroup.STOP_DELAY));
        perforAutoArgsPanel.configure(tg);

        TestElement controller = (TestElement) tg.getProperty(AbstractThreadGroup.MAIN_CONTROLLER).getObjectValue();
        if (controller != null) {
            loopPanel.configure(controller);
        }
    }

    private void updateChart(PerforAutoSteppingThreadGroup tg) {
        model.clear();

        GraphRowSumValues row = new GraphRowSumValues();
        row.setColor(Color.RED);
        row.setDrawLine(true);
        row.setMarkerSize(AbstractGraphRow.MARKER_SIZE_NONE);
        row.setDrawThickLines(true);

        final HashTree hashTree = new HashTree();
        hashTree.add(new LoopController());
        JMeterThread thread = new JMeterThread(hashTree, null, null);

        long now = System.currentTimeMillis();

        // test start
        chart.setxAxisLabelRenderer(new DateTimeRenderer(DateTimeRenderer.HHMMSS, now - 1)); //-1 because row.add(thread.getStartTime() - 1, 0)
        row.add(now, 0);
        row.add(now + tg.getThreadGroupDelayAsInt(), 0);

        int numThreads = tg.getNumThreads();

        // users in
        for (int n = 0; n < numThreads; n++) {
            thread.setThreadNum(n);
            tg.scheduleThread(thread, now);
            row.add(thread.getStartTime() - 1, 0);
            row.add(thread.getStartTime(), 1);
        }

        // users out
        for (int n = 0; n < numThreads; n++) {
            thread.setThreadNum(n);
            tg.scheduleThread(thread, now);
            row.add(thread.getEndTime() - 1, 0);
            row.add(thread.getEndTime(), -1);
        }

        model.put("Expected Active Users Count", row);
        chart.invalidateCache();
        chart.repaint();
    }

    private JPanel createControllerPanel() {
        loopPanel = new LoopControlPanel(false);
        LoopController looper = (LoopController) loopPanel.createTestElement();
        looper.setLoops(-1);
        looper.setContinueForever(true);
        loopPanel.configure(looper);
        return loopPanel;
    }

    private void registerJTextfieldForGraphRefresh(final JTextField tf) {
        tf.addActionListener(loopPanel);
        tf.getDocument().addDocumentListener(new FieldChangesListener(tf));
    }
}
