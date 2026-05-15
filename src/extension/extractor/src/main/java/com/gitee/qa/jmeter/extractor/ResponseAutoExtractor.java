package com.gitee.qa.jmeter.extractor;

import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseAutoExtractor extends AbstractTestElement  implements PostProcessor, Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String DELIMITER = "ResponseAutoExtractor.delimiter";
	public static final String PREFIX = "ResponseAutoExtractor.prefix";
	public static final String FIRSTROW = "ResponseAutoExtractor.firstrow";

	@Override
	public void process() {
		JMeterContext context = getThreadContext();
        SampleResult previousResult = context.getPreviousResult();  // 上一个应答结果
        JMeterVariables vars = context.getVariables();  // 上下文中变量
        
        // 处理应答
        String response = previousResult.getResponseDataAsString();
//        System.out.println("previousResult.getResponseDataAsString(): " + response);
        if (response == null) return;
        this.responseDataToVars(response, vars);
	}
	
	// 将应答数据转换为变量
	private void responseDataToVars(String response, JMeterVariables vars) {
	    // 编译正则表达式
        Pattern pattern = Pattern.compile("result_list=\\[([\\s\\S]*)\\]");
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            //System.out.println(matcher.group(1)); // 查出匹配到的第一个括号里的内容
            String str_2 = matcher.group(1);
            Matcher matcher_2 = Pattern.compile("\\{([\\s\\S]*?)\\}").matcher(str_2);
            
            int count = 0;
            while (matcher_2.find()) {
                //System.out.println(matcher_2.group(1)); // 打印每条数据
                String str_3 = matcher_2.group(1);
                // 键值分割符
                String delimiter = this.getDelimiterProperty();
                // 处理一行数据
                String [] strs = str_3.split(delimiter);
                //System.out.println("处理一行数据");
                for (String s : strs) {
                    //System.out.println(s); // 打印键值
                    // 处理键值对
                    String [] strs_2 = s.trim().split("=");
                    String key = "";
                    String value = "";
                    if (strs_2.length > 1) {
                        key = strs_2[0]; // 字段
                        value = strs_2[1]; // 值
                    }
                    else { // 字段值为空
                        key = strs_2[0]; // 字段
                        value = "";  // 值
                    }
                    // 前缀
                    String prefix = this.getFrefixProperty();
                    String n_key = prefix + key + "_" + String.valueOf(count);
                    String n_value = value;
                    //System.out.println(n_key + " : " + n_value);
                    vars.put(n_key, n_value);
                }
                count += 1;
                // 如果只处理第1行则终止下次处理
                if (this.getFirstRowProperty()) {
                    return;
                }
            }
        }
	}
	
	public void setDelimiterProperty(String value) {
	    this.setProperty(DELIMITER, value);
	}

	public String getDelimiterProperty() {
	    return this.getPropertyAsString(DELIMITER);
	}
	
    public void setFrefixProperty(String value) {
        this.setProperty(PREFIX, value);
    }
	
    public String getFrefixProperty() {
        return this.getPropertyAsString(PREFIX);
    }
    
    public void setFirstRowProperty(boolean value) {
        this.setProperty(FIRSTROW, value);
    }
    
    public boolean getFirstRowProperty() {
        return this.getPropertyAsBoolean(FIRSTROW);
    }
    
}
