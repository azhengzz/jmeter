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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@AutoService(Function.class)
public class TimeToTimestamp extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(TimeToTimestamp.class);

    /*定义函数名*/
    public static final String KEY = "__timeToTimestamp";

    /*定义function参数的描述*/
    public static final List<String> desc = new LinkedList<String>();
    static {
        desc.add("参数1-时间格式：如 yyyy-MM-dd HH:mm:ss:SSS");
        desc.add("参数2-时间：如 2022-09-01 09:41:23:589");
        desc.add("参数3-变量名：把转换后的时间戳存到当前变量中");
    }

    private static final int MAX_PARA_COUNT = 3; // 参数最大个数
    private static final int MIN_PARA_COUNT = 2; // 参数最小个数

    private Object[] values = null;

    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        // 参数必须放到execute进行获取 execute和setParameters不再一个线程上下文执行
        String format = ((CompoundVariable) values[0]).execute().trim();
        String time = ((CompoundVariable) values[1]).execute().trim();
        String varName;
        if(values.length > 2) {
            varName = ((CompoundVariable) values[2]).execute().trim();
        }else {
            varName = null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        long longTime = simpleDateFormat.parse(time, new ParsePosition(0)).getTime();

        String ret = String.valueOf(longTime);

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
}
