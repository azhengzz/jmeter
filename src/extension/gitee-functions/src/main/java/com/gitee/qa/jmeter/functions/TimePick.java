package com.gitee.qa.jmeter.functions;

import com.google.auto.service.AutoService;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

@AutoService(Function.class)
public class TimePick extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(TimePick.class);

    /*定义函数名*/
    public static final String KEY = "__timePick";

    /*定义function参数的描述*/
    public static final List<String> desc = new LinkedList<String>();
    static {
        desc.add("参数1-周/月/年(必传)：如 W/M/Y");
        desc.add("参数2-第N天(必传)：如 1表示第1天，如 -1表示最后一天，注意周第1天是周1");
        desc.add("参数3-unix时间戳：不传默认取当前时间戳，或输入指定时间戳如 1643832306000");
        desc.add("参数4-输出格式：不传默认返回unix时间戳，或输入yyyy-MM-dd HH:mm:ss:SSS指定格式");
        desc.add("参数5-变量名：把输出的时间存到当前变量中");
    }

    private static final int MAX_PARA_COUNT = 5; // 参数最大个数
    private static final int MIN_PARA_COUNT = 2; // 参数最小个数

    private Object[] values = null;

    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        // 参数必须放到execute进行获取 execute和setParameters不再一个线程上下文执行
        String timeType = ((CompoundVariable) values[0]).execute().trim();
        String num = ((CompoundVariable) values[1]).execute().trim();
        String timeStamp = null;
        String timeFormat = null;
        String varName = null;
        String ret = null;
        if ("".equals(timeType) || "".equals(num)){
            log.error("必传参数未传，请检查");
        }
        if(values.length > 2) {
            timeStamp = ((CompoundVariable) values[2]).execute().trim();
        }
        if(values.length > 3) {
            timeFormat = ((CompoundVariable) values[3]).execute().trim();
        }
        if(values.length > 4) {
            varName = ((CompoundVariable) values[4]).execute().trim();
        }
        if (timeStamp == null || "".equals(timeStamp)){  // 如果当前时间戳为空，则获取当前时间戳
            timeStamp = String.valueOf(System.currentTimeMillis());
        }
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        if (timeStamp.length() == 13){
            cal.setTimeInMillis(Long.parseLong(timeStamp));
        }else if(timeStamp.length() == 10){
            cal.setTimeInMillis(Long.parseLong(timeStamp) * 1000);
        }else {
            log.error("参数3-unix时间戳长度非法，请传入13位或10位时间戳");
            return null;
        }
        if ("M".equals(timeType)){
            ret = this.formatDate(
                    this.pickDayOfMonth(Integer.parseInt(num), cal),
                    timeFormat);
        }else if ("W".equals(timeType)){
            ret = this.formatDate(
                    this.pickDayByWeek(Integer.parseInt(num), cal),
                    timeFormat);
        }else if ("Y".equals(timeType)){
            ret = this.formatDate(
                    this.pickDayByYear(Integer.parseInt(num), cal),
                    timeFormat);
        }else {
            log.error("参数1-周/月/年传入类型错误");
            return null;
        }
        if (varName != null) {
            JMeterVariables vars = getVariables(); // JMeter变量池
            vars.put(varName, ret);
        }
        return ret;
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARA_COUNT, MAX_PARA_COUNT); 	//检查参数的个数是否正确
        this.values = parameters.toArray();
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    private Date pickDayOfMonth(int num, Calendar cal){
        if (num >= 0){
            cal.set(Calendar.DAY_OF_MONTH, num);
        }else {
            num += 1;
            // 从当前月最后1天倒叙获取
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH) + num);
        }
        return cal.getTime();
    }

    private Date pickDayByWeek(int num, Calendar cal){
        if (num > 0){
            num -= 1;
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + num);
        }else {
            log.error("周的第几天必须选择1/2/3/4/5/6/7");
        }
        return cal.getTime();
    }

    private Date pickDayByYear(int num, Calendar cal){
        if (num >= 0){
            cal.set(Calendar.DAY_OF_YEAR, num);
        }else {
            num += 1;
            // 从当前年最后1天倒叙获取
            cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR) + num);
        }
        return cal.getTime();
    }

    private String formatDate(Date date, String timeFormat){
        if (timeFormat == null || "".equals(timeFormat)){
            return String.valueOf(date.getTime());
        }else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
            return simpleDateFormat.format(date);
        }
    }
}
