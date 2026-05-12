package com.gitee.qa.jmeter.functions;

import com.google.auto.service.AutoService;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@AutoService(Function.class)
public class ObjectVariableEscape extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(ObjectVariableEscape.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__Oe"; //$NON-NLS-1$  // __Oe means ObjectVariable escape

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 2;

    static {
        desc.add("对象.JsonPath表达式"); //$NON-NLS-1$
        desc.add("提取后存放到变量(可选)"); //$NON-NLS-1$
    }

    private Object[] values;

    public ObjectVariableEscape() {
        //used for test
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        ObjectVariable ov = new ObjectVariable();
        ov.setValues(values);
        String resStr = ov.execute(previousResult, currentSampler);
        if (resStr != null){
            return this.escape(resStr);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    private String escape(String value){
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
