package com.gitee.qa.jmeter.assertions;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.FieldComparisonFailure;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonAutoAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable {

    private static final long serialVersionUID = 240L;

    //++ For TestBean implementations only
    private String jsonStr;

    private boolean strictOrdering = false;

    private boolean extensible = true;
    //-- For TestBean implementations only

    /*
    * 匹配json串中正则表达式的规则
    * {
    *   "id": "/\\d{10}/g",
    *   "date": "/\\d{4}-\\d{2}-\\d{2}/g",
    *   "value": 10
    * }
    *
    * */
    private static final String RegexMatch = "^/([\\s\\S]*)/g$";

    public JsonAutoAssertion() {
        super();
    }

    /**
     * Returns the AssertionResult object encapsulating information about the
     * success or failure of the assertion.
     *
     * @param response the SampleResult containing information about the Sample
     *                 (duration, success, etc)
     * @return the AssertionResult containing the information about whether the
     * assertion passed or failed.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        String failureMsg = "\n";
        boolean failure = false;
        boolean error = false;
        JSONCompareMode mode;
        Pattern rp = Pattern.compile(RegexMatch);

        AssertionResult result = new AssertionResult(getName());

        try{
            if (!isExtensible() && isStrictOrdering()){
                mode = JSONCompareMode.STRICT;
            }else if (isExtensible() && !isStrictOrdering()){
                mode = JSONCompareMode.LENIENT;
            }else if (!isExtensible() && !isStrictOrdering()){
                mode = JSONCompareMode.NON_EXTENSIBLE;
            }else{
                mode = JSONCompareMode.STRICT_ORDER;
            }
            JSONCompareResult jsonCompareResult = JSONCompare.compareJSON(getJsonStr(), response.getResponseDataAsString(), mode);

            String expectedValue;
            String actualValue;
            boolean regexMatchSuccess;
            for (FieldComparisonFailure fieldComparisonFailure: jsonCompareResult.getFieldFailures()) {
                regexMatchSuccess = false;
                expectedValue = fieldComparisonFailure.getExpected().toString();  // 期望值: 用户自定义的断言值
                actualValue = fieldComparisonFailure.getActual().toString();  // 实际值: 接口实际返回的数值
                if (isValidRegExp(expectedValue)){
                    // 提取正则表达式
                    Matcher rm = rp.matcher(expectedValue);
                    if (rm.matches()){
                        String regExp = rm.group(1);
                        Pattern p = Pattern.compile(regExp);
                        Matcher m = p.matcher(actualValue);
                        regexMatchSuccess = m.find();
                    }
                }
                if (!regexMatchSuccess){
                    // 实际字段值和期望字段值不一致
                    failureMsg += String.format("断言失败字段: %s\n", fieldComparisonFailure.getField());
                    failureMsg += String.format("- 期望值: %s\n", fieldComparisonFailure.getExpected());
                    failureMsg += String.format("- 实际值: %s\n", fieldComparisonFailure.getActual());
                    failure = true;
                }
            }
            for (FieldComparisonFailure fieldComparisonFailure: jsonCompareResult.getFieldMissing()) {
                // 实际值没有字段但期望值有
                failureMsg += String.format("多余字段: %s\n", fieldComparisonFailure.getField());
                failureMsg += String.format("- 期望值: %s\n", fieldComparisonFailure.getExpected());
                failureMsg += String.format("- 实际值: %s\n", fieldComparisonFailure.getActual());
                failure = true;
            }
            for (FieldComparisonFailure fieldComparisonFailure: jsonCompareResult.getFieldUnexpected()) {
                // 实际值包含字段但期望值没有
                failureMsg += String.format("缺少字段: %s\n", fieldComparisonFailure.getField());
                failureMsg += String.format("- 期望值: %s\n", fieldComparisonFailure.getExpected());
                failureMsg += String.format("- 实际值: %s\n", fieldComparisonFailure.getActual());
                failure = true;
            }
            if ((jsonCompareResult.getFieldFailures().size() + jsonCompareResult.getFieldMissing().size() +
                    jsonCompareResult.getFieldUnexpected().size()) == 0) {
                failure = jsonCompareResult.failed();
                failureMsg += jsonCompareResult.getMessage();
            }
        }catch (Exception e){
            error = true;
            failure = true;
            failureMsg += e.getMessage();
        }
        result.setFailure(failure);
        result.setError(error);
        result.setFailureMessage(failureMsg);
        return result;
    }

    // 是否是有效的正则表达式
    private static boolean isValidRegExp(String regRex){
        return Pattern.compile(RegexMatch).matcher(regRex).matches();
    }

    public String getJsonStr(){
        return jsonStr;
    }

    public void setJsonStr(String s){
        jsonStr = s;
    }

    public boolean isStrictOrdering() {
        return strictOrdering;
    }

    public void setStrictOrdering(boolean b) {
        strictOrdering = b;
    }

    public void setExtensible(boolean b) {
        extensible = b;
    }

    public boolean isExtensible() {
        return extensible;
    }
}
