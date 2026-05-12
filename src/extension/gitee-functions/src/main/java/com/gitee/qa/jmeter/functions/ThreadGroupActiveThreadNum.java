package com.gitee.qa.jmeter.functions;

import com.google.auto.service.AutoService;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContextService;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * 获取当前线程组的活跃线程数
 */
@AutoService(Function.class)
public class ThreadGroupActiveThreadNum extends AbstractFunction {

    /*定义函数名*/
    public static final String KEY = "__threadGroupActiveThreadNum";
    /*定义function参数的描述*/
    public static final List<String> DESC = new LinkedList<String>();
    static {
        DESC.add("参数1-梯度：梯度大小");
    }

    private static final int MAX_PARA_COUNT = 1; // 参数最大个数
    private static final int MIN_PARA_COUNT = 0; // 参数最小个数

    private Object[] values;

    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        // 防止在测试启动阶段线程组未初始化导致NPE
        if (JMeterContextService.getContext().getThreadGroup() == null) {
            return "0";
        }

        int tn = JMeterContextService.getContext().getThreadGroup().getNumberOfThreads();  // 当前线程组的活跃线程数
        if (values.length > 0){
            int step = Integer.parseInt(((CompoundVariable) values[0]).execute().trim());
            if (tn % step != 0 ){  // 如果余数不为0，则线程数设置为梯度的整数倍，向上取整
                tn = tn / step * step + step;
            }
        }
        return String.valueOf(tn);
//        return String.valueOf(JMeterContextService.getNumberOfThreads());  // 当前JMeter的活跃线程数
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
        return DESC;
    }
}
