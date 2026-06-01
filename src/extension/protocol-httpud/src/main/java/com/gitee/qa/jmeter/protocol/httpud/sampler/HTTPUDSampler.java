package com.gitee.qa.jmeter.protocol.httpud.sampler;
import com.gitee.qa.jmeter.protocol.httpud.util.HTTPUDArgument;

import com.gitee.qa.jmeter.protocol.httpud.config.HTTPUDConfigElement;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.HTTPUDArgumentsGui;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPAbstractImpl;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPUDSampler extends HTTPSamplerBase {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPUDSampler.class);

    public static final String VARIABLE_NAME = "HTTPUDSampler.variable_name"; // $NON-NLS-1$

    // 当前sampler关联的HTTP User Defined Element Configuration 组件对象
    HTTPUDConfigElement config;

    private transient HTTPAbstractImpl impl;

    public static final String RegexMatch = "@\\{(\\w+)}";  // 参数声明匹配的正则规则

    // HTTP User Defined Element Configuration配置元件中的自定义参数，{参数名:参数默认值}
    Map<String, String> parametersValueMap;
    // HTTP User Defined Element Configuration配置元件中的自定义参数，{参数名:必传项}
    Map<String, Boolean> parametersRequiredMap;
    // HTTP User Defined Sampler中的传参，{参数名:参数值}
    Map<String, String> argumentsValueMap;
    // HTTP ser Defined Configuration配置元件中请求头参数，{参数:值}
    Map<String, String> httpHeaderArgumentsValueMap;

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
        super.testStarted();
    }

    @Override
    public void testEnded() {
        super.testEnded();
    }

    @Override
    public SampleResult sample(Entry e) {
        try {
            config = HTTPUDConfigElement.getHTTPUDConfigElement(getVariableName().trim());
            parametersValueMap = config.getUserDefinedParametersValueAsMap();
            parametersRequiredMap = config.getUserDefinedParametersRequiredAsMap();
            argumentsValueMap = getHTTPUDSamplerArgumentsValueAsMap();
            httpHeaderArgumentsValueMap = config.getHTTPHeaderArgumentsValueAsMap();
            // 当 HTTP User Defined Element Configuration 中自定义参数设置了必传参数，但HTTPUDSampler没有传惨的话需要报错返回
            StringBuilder requiredParameterNotPassError = new StringBuilder();
            for (String parameterName : parametersRequiredMap.keySet()){
                // 如果自定义参数中该参数设置必传，并且Sampler传参中没有传递该参数，则抛出异常
                if (parametersRequiredMap.get(parameterName) && !argumentsValueMap.containsKey(parameterName) ){
                    requiredParameterNotPassError.append(String.format("检测到HTTP User Defined Element Configuration中配置参数: %s 为必传参数，但当前请求传参中未包含该参数。\n", parameterName));
                }
            }
            if (StringUtils.isNotBlank(requiredParameterNotPassError.toString())){
                throw new RuntimeException(requiredParameterNotPassError.toString());
            }
        } catch (RuntimeException exception){
            HTTPSampleResult httpSampleResult = new HTTPSampleResult();
            httpSampleResult.setSampleLabel(getName());
            httpSampleResult.sampleStart();
            return errorResult(exception, httpSampleResult);
        }
        return super.sample(e);
    }

    @Override
    protected HTTPSampleResult sample(URL u, String method, boolean areFollowingRedirect, int depth) {
        // When Retrieve Embedded resources + Concurrent Pool is used
        // as the instance of Proxy is cloned, we end up with impl being null
        // testIterationStart will not be executed but it's not a problem for 51380 as it's download of resources
        // so SSL context is to be reused
        if (impl == null) { // Not called from multiple threads, so this is OK
            try {
                impl = HTTPSamplerFactory.getImplementation(getImplementation(), this);
            } catch (Exception ex) {
                return errorResult(ex, new HTTPSampleResult());
            }
        }
//        return impl.sample(u, method, areFollowingRedirect, depth);
        // TODO: Port sampleWithHTTPArguments to 5.6.3 HTTPAbstractImpl (Layer 3)
        try {
            java.lang.reflect.Method sampleMethod = HTTPAbstractImpl.class
                    .getDeclaredMethod("sample", URL.class, String.class, boolean.class, int.class);
            sampleMethod.setAccessible(true);
            return (HTTPSampleResult) sampleMethod.invoke(impl, u, method, areFollowingRedirect, depth);
        } catch (Exception ex) {
            return errorResult(ex, new HTTPSampleResult());
        }
    }

    // 判断属性值是否存在参数 形如：@{p_name}
    // TODO 思考必传参数是否还有必要填写默认值吗？目前的想法是没有必要
    public String replacePropertyValueWithArgument(String propertyValue) throws RuntimeException {
        Pattern rp = Pattern.compile(RegexMatch);
        Matcher rm = rp.matcher(propertyValue);
        while(rm.find()){
            String parameterName = rm.group(1);
            if (parametersValueMap.containsKey(parameterName)){  // 表示该参数已在HTTPUDConfigElement中自定义参数定义过
                // 判断该参数是否是必传
                if (parametersRequiredMap.get(parameterName)){  // 表示该参数必传
                    if (!argumentsValueMap.containsKey(parameterName)){  // 如果必传参数HTTPUDSampler没有传递，则抛出异常
                        throw new RuntimeException(String.format("参数: %s 在HTTP User Defined Element Configuration已定义为必传参数，但当前HTTP User Defined Sampler没有传该参数！", parameterName));
                    }else {  // 如果必传参数HTTPUDSampler有传递，则使用传递的参数值
                        String argumentsValue = argumentsValueMap.get(parameterName);
                        propertyValue = propertyValue.replace(String.format("@{%s}", parameterName), argumentsValue);
                    }
                }else {  // 表示该参数非必传
                    if (!argumentsValueMap.containsKey(parameterName)){  // 如果非必传参数HTTPUDSampler没有传递，则使用默认值
                        String parametersValue = parametersValueMap.get(parameterName);
                        propertyValue = propertyValue.replace(String.format("@{%s}", parameterName), parametersValue);
                    }else {  // 如果非必传参数HTTPUDSampler有传递，则使用传递的参数值
                        String argumentsValue = argumentsValueMap.get(parameterName);
                        propertyValue = propertyValue.replace(String.format("@{%s}", parameterName), argumentsValue);
                    }
                }
            }else {  // 对于参数没有在HTTPUDConfigElement中定义过，则不对原属性值进行处理
                // do nothing
            }
        }
        return propertyValue;
    }

    // 判断属性值中是否存在声明参数
    private boolean existParameter(String propertyValue){
        Pattern rp = Pattern.compile(RegexMatch);
        Matcher rm = rp.matcher(propertyValue);
        return rm.find();
    }

    public String getVariableName() {
        return getPropertyAsString(VARIABLE_NAME);
    }

    // 获取HTTPUDSampler传递的参数
    public Arguments getHTTPUDSamplerArguments() {
        Arguments args = (Arguments) getProperty(HTTPUDArgumentsGui.HTTP_USER_DEFINED_ARGUMENTS).getObjectValue();
        return args;
    }

    // 获取HTTPUDSampler传递的参数
    public Map<String, String> getHTTPUDSamplerArgumentsValueAsMap() {
        PropertyIterator iter = getHTTPUDSamplerArguments().iterator();
        Map<String, String> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            // Because CollectionProperty.mergeIn will not prevent adding two
            // properties of the same name, we need to select the first value so
            // that this element's values prevail over defaults provided by
            // configuration
            // elements:
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.getValue());
            }
        }
        return argMap;
    }

    @Override
    public String toString() {
        try {
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append(this.getUrl().toString());
            // Append body if it is a post or put
            String method = getMethod();
            if (HTTPConstants.POST.equals(method) || HTTPConstants.PUT.equals(method)) {
                stringBuffer.append("\nQuery Data: ");
                stringBuffer.append(getQueryString());
            }
            return stringBuffer.toString();
        } catch (MalformedURLException | NullPointerException e) {
            return "";
        }
    }

    // 协议
    public String getProtocol(){
        String protocol;
        protocol = config.getProtocol();
        // 如果从config（HTTP User Defined Element Configuration）组件中获取不到，则会从”HTTP请求默认值“组件中获取
        if (StringUtils.isBlank(protocol)){
            protocol = super.getProtocol();
        }
        return replacePropertyValueWithArgument(protocol);
    }

    // 服务器名称或IP
    public String getDomain(){
        String domain;
        domain = config.getDomain();
        if (StringUtils.isBlank(domain)){
            domain = super.getDomain();
        }
        return replacePropertyValueWithArgument(domain);
    }

    /**
     * Tell whether the default port for the specified protocol is used
     *
     * @return true if the default port number for the protocol is used, false otherwise
     */
    public boolean isProtocolDefaultPort() {
        int port;
        String portAsString = config.getPortAsString();
        if (existParameter(portAsString)){
            portAsString = replacePropertyValueWithArgument(portAsString.trim());
            try {
                port = Integer.parseInt(portAsString.trim());
            } catch (NumberFormatException e) {
                port = HTTPSampler.UNSPECIFIED_PORT;
            }
        }else {
            port = getPortIfSpecified();
        }
        final String protocol = getProtocol();
        boolean isDefaultHTTPPort = HTTPConstants.PROTOCOL_HTTP
                .equalsIgnoreCase(protocol)
                && port == HTTPConstants.DEFAULT_HTTP_PORT;
        boolean isDefaultHTTPSPort = HTTPConstants.PROTOCOL_HTTPS
                .equalsIgnoreCase(protocol)
                && port == HTTPConstants.DEFAULT_HTTPS_PORT;
        return port == UNSPECIFIED_PORT ||
                isDefaultHTTPPort ||
                isDefaultHTTPSPort;
    }

    // 端口号
    public int getPort(){
        int port;
        String portAsString = config.getPortAsString();
        if (existParameter(portAsString)){
            portAsString = replacePropertyValueWithArgument(portAsString.trim());
            try {
                port = Integer.parseInt(portAsString.trim());
            } catch (NumberFormatException e) {
                port = HTTPSampler.UNSPECIFIED_PORT;
            }
        }else {
            port = config.getPort();
        }
        if (port == HTTPSampler.UNSPECIFIED_PORT){  // 如果获取到当前端口号为0，则取HTTP请求默认值中的端口号
            port = super.getPort();
        }
        if (port == HTTPSampler.UNSPECIFIED_PORT) {  // 如果HTTP请求默认值也为0
            String protocol = getProtocol();
            if (HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(protocol)) {
                return HTTPConstants.DEFAULT_HTTPS_PORT;
            }
            if (!HTTPConstants.PROTOCOL_HTTP.equalsIgnoreCase(protocol)) {
                LOG.warn("Unexpected protocol: {}", protocol);
            }
            return HTTPConstants.DEFAULT_HTTP_PORT;
        }
        return port;
    }

    public String getMethod(){
        String method;
        method = config.getMethod();
        if (StringUtils.isBlank(method)){
            method = super.getMethod();
        }
        return replacePropertyValueWithArgument(method);
    }

    public String getPath(){
        String path;
        path = config.getPath();
        if (StringUtils.isBlank(path)){
            path = super.getPath();
        }
        return replacePropertyValueWithArgument(path);
    }

    public String getContentEncoding(){
        String ce;
        ce = config.getContentEncoding();
        if (StringUtils.isBlank(ce)){
            ce = super.getContentEncoding();
        }
        return replacePropertyValueWithArgument(ce);
    }

    // 参数
    public Arguments getArguments() {
        Arguments args;
        // 处理声明的参数
        Arguments newArgs = new Arguments();
        args = config.getArguments();
        if (args.getArgumentCount() == 0){
            args = super.getArguments();
        }
        for (JMeterProperty jMeterProperty : args) {
            HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
            HTTPArgument newArg = new HTTPArgument(
                    replacePropertyValueWithArgument(arg.getName()),
                    replacePropertyValueWithArgument(arg.getValue())
            );
            newArg.setMetaData(arg.getMetaData());
            newArg.setUseEquals(arg.isUseEquals());
            newArg.setContentType(replacePropertyValueWithArgument(arg.getContentType()));
            newArg.setAlwaysEncoded(arg.isAlwaysEncoded());  // 需要再次设置该属性
            newArgs.addArgument(newArg);
        }
        return newArgs;
    }

    // 自动重定向
    public boolean getAutoRedirects() {
        // 自动重定向直接获取配置组件(HTTP User Defined Element Configuration)中的数值，因为HTTP请求默认值中无该字段
        return config.getPropertyAsBoolean(AUTO_REDIRECTS);
    }

    // 跟随重定向
    public boolean getFollowRedirects() {
        return config.getPropertyAsBoolean(FOLLOW_REDIRECTS);
    }

    // 使用KeepAlive
    public boolean getUseKeepAlive() {
        return config.getPropertyAsBoolean(USE_KEEPALIVE);
    }

    // 对POST使用multipart / form-data
    public boolean getDoMultipart() {
        return config.getPropertyAsBoolean(DO_MULTIPART_POST, false);
    }

    // 与浏览器兼容的头
    public boolean getDoBrowserCompatibleMultipart() {
        return config.getPropertyAsBoolean(BROWSER_COMPATIBLE_MULTIPART, BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
    }

    // 客户端实现类型
    public String getImplementation() {
        String implementation;
        implementation = config.getImplementation();
        if (StringUtils.isBlank(implementation)){
            implementation = super.getImplementation();
        }
        return implementation;
    }

    // 连接超时时间
    public int getConnectTimeout() {
        int ct;
        ct = config.getConnectTimeout();
        if (ct == 0){
            ct = super.getConnectTimeout();
        }
        return ct;
    }

    // 响应超时时间
    public int getResponseTimeout() {
        int rt;
        rt = config.getResponseTimeout();
        if (rt == 0){
            rt = super.getResponseTimeout();
        }
        return rt;
    }

    // 从HTML文件获取所有内含的资源
    public boolean isImageParser() {
        // 存在 HTTPUDConfiguration 和 HTTP请求默认值 组件同时存在的情况，优先使用config的属性
        return config.isImageParser() || super.isImageParser();
    }

    // 并行下载
    public boolean isConcurrentDwn() {  // TODO: 为啥不禁用会报错空指针异常?
        if (config == null) {  // testEnded 执行期间 config 成员为 null
            return super.isConcurrentDwn();
        } else {
            // 存在 HTTPUDConfiguration 和 HTTP请求默认值 组件同时存在的情况，优先使用config的属性
            return config.isConcurrentDwn() || super.isConcurrentDwn();
        }
    }

    // 并行下载数量
    public String getConcurrentPool() {
        String cp;
        cp = config.getConcurrentPool();
        if (cp.equals(HTTPUDConfigElement.CONCURRENT_POOL_DEFAULT)){
            cp = super.getConcurrentPool();
        }
        return cp;
    }

    // 网址必须匹配
    public String getEmbeddedUrlRE() {
        String embeddedUrlRE;
        embeddedUrlRE = config.getEmbeddedUrlRE();
        if (StringUtils.isBlank(embeddedUrlRE)){
            embeddedUrlRE = super.getEmbeddedUrlRE();
        }
        return embeddedUrlRE;
    }

    // 源地址类型
    public int getIpSourceType() {
        int ipSourceType;
        ipSourceType = config.getIpSourceType();
        if (ipSourceType == HTTPSampler.SOURCE_TYPE_DEFAULT){
            ipSourceType = super.getIpSourceType();
        }
        return ipSourceType;
    }

    // 源地址
    public String getIpSource() {
        String ipSource;
        ipSource = config.getIpSource();
        if (StringUtils.isBlank(ipSource)){
            ipSource = super.getIpSource();
        }
        return ipSource;
    }

    // 代理服务器Scheme
    public String getProxyScheme(){
        String proxyScheme;
        proxyScheme = config.getProxyScheme();
        if (StringUtils.isBlank(proxyScheme)){
            proxyScheme = super.getProxyScheme();
        }
        return proxyScheme;
    }

    // 代理服务器名称或IP
    public String getProxyHost() {
        String proxyHost;
        proxyHost = config.getProxyHost();
        if (StringUtils.isBlank(proxyHost)){
            proxyHost = super.getProxyHost();
        }
        return proxyHost;
    }

    // 代理服务器端口号
    public int getProxyPortInt() {
        int proxyPort;
        proxyPort = config.getProxyPortInt();
        if (0 == proxyPort){
            proxyPort = super.getProxyPortInt();
        }
        return proxyPort;
    }

    // 代理服务器用户名
    public String getProxyUser() {
        String proxyUser;
        proxyUser = config.getProxyUser();
        if (StringUtils.isBlank(proxyUser)){
            proxyUser = super.getProxyUser();
        }
        return proxyUser;
    }

    // 代理服务器密码
    public String getProxyPass() {
        String proxyPass;
        proxyPass = config.getProxyPass();
        if (StringUtils.isBlank(proxyPass)){
            proxyPass = super.getProxyPass();
        }
        return proxyPass;
    }

    // 保存响应为MD5哈希
    public boolean useMD5() {
        return config.useMD5() || super.useMD5();
    }

    // 文件上传
    public HTTPFileArg[] getHTTPFiles() {
        HTTPFileArg[] args = config.getHTTPFiles();
        HTTPFileArgs newArgs = new HTTPFileArgs();
        for (HTTPFileArg arg: args){
            HTTPFileArg newArg = new HTTPFileArg(
                replacePropertyValueWithArgument(arg.getPath()),
                replacePropertyValueWithArgument(arg.getParamName()),
                replacePropertyValueWithArgument(arg.getMimeType())
            );
            newArgs.addHTTPFileArg(newArg);
        }
        return newArgs.asArray();
    }
}

