package com.gitee.qa.jmeter.functions;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JsonProvider;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoService(Function.class)
public class ObjectVariable extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(ObjectVariable.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__O"; //$NON-NLS-1$  // __O means ObjectVariable

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 2;

    static {
        desc.add("对象.JsonPath表达式"); //$NON-NLS-1$
        desc.add("提取后存放到变量(可选)"); //$NON-NLS-1$
    }

    private Object[] values;

    public ObjectVariable() {
        //used for test
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        String expression = ((CompoundVariable) values[0]).execute();
        String varName = null;
        if (values.length > 1){
            varName = ((CompoundVariable) values[1]).execute().trim();
            this.putObject(varName, "");
        }

        // 根据参数1提取对象名和jsonPath表达式
        String[] arr = this.getVariableAndJsonPath(expression);
        if (arr != null){
            String objName = arr[0];
            String jp = arr[1];
            log.debug(String.format("varName: %s, jp: %s", objName, jp));
            Object obj = getVariables().getObject(objName);
            if (obj == null){
                return null;
            }

            // 将obj解析为jsonStr
            String js;
            JsonProvider jsonProvider = Configuration.defaultConfiguration().jsonProvider();
            if (obj instanceof String){
                js = (String) obj;
            } else if (obj instanceof List || obj instanceof Map){
                js = jsonProvider.toJson(obj);
            }  else {
                log.error(obj.getClass().getName() + "此类对象不支持转为JSON");
                return null;
            }
            // 如果jsonPath为空，则返回变量的字符串值
            if ("".equals(jp)){
                this.putObject(varName, js);
                return js;
            }
            // 如果jsonPath不为空，则进行解析
            String resObjStr = this.readJsonPath(js, jp);
            this.putObject(varName, resObjStr);
            return resObjStr;

        } else {
            log.error("expression: " + expression + " 未提取到对象和jsonPath表达式");
            return null;
        }
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

    String[] getVariableAndJsonPath(String expression){
        String pattern = "^([a-zA-Z_\\d]+)([\\S\\s]*)$";
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(expression);
        if (m.find()) {
            return new String[] {m.group(1), m.group(2)};
        } else {
            return null;
        }
    }

    private void putObject(String varName, Object obj){
        if (varName != null & obj != null){
            getVariables().putObject(varName, obj);
        } else if (varName != null & obj == null){
            getVariables().putObject(varName, "");
        }
    }

    String readJsonPath(String js, String jp){
        JsonProvider jsonProvider = Configuration.defaultConfiguration().jsonProvider();
        try {
            Object jsObj = jsonProvider.parse(js);
            Object resObj = JsonPath.read(jsObj, "$" + jp);
            if (resObj != null){
                if (resObj instanceof Map || resObj instanceof List){
                    return jsonProvider.toJson(resObj);
                } else {
                    return resObj.toString();
                }
            } else {
                return null;
            }
        } catch (InvalidJsonException e){
            String errMsg = "JsonStr不合法，JsonStr: " + js;
            log.error(errMsg.replace("\"", "\\\""));
        } catch(InvalidPathException e) {
            String errMsg = "错误的jsonPath表达式: " + "$" + jp + "\n" + "JsonStr: " + js + "\n" + org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            log.error(errMsg.replace("\"", "\\\""));
        }
        return null;
    }

    void setValues(Object[] values){
        this.values = values;
    }
}
