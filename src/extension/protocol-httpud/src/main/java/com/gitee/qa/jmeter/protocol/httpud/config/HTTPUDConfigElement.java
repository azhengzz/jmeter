package com.gitee.qa.jmeter.protocol.httpud.config;

import com.gitee.qa.jmeter.protocol.httpud.util.HTTPUDArgument;
import com.gitee.qa.jmeter.protocol.httpud.util.gui.HTTPUDArgumentsGui;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;


public class HTTPUDConfigElement extends ConfigTestElement implements TestStateListener{

    public static final String VARIABLE_NAME = "HTTPUDConfigElement.variable_name"; // $NON-NLS-1$

    public static final String HTTP_HEADER_PARAMETERS_NAME = "HTTPUDConfigElement.http_header_parameters_name";

    // Must be private, as the file list needs special handling
    private static final String FILE_ARGS = "HTTPUDConfigElement.Files"; // $NON-NLS-1$

    private static final Logger log = LoggerFactory.getLogger(HTTPUDConfigElement.class);

    public static final String CONCURRENT_POOL_DEFAULT = Integer.toString(HTTPSampler.CONCURRENT_POOL_SIZE); // default for concurrent pool
    protected static final String PROXY_SCHEME = System.getProperty("http.proxyScheme","http");

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        TestBeanHelper.prepare(this);
        JMeterVariables variables = getThreadContext().getVariables();
        String variableName = getVariableName();
        if(JOrphanUtils.isBlank(variableName)) {
            throw new IllegalArgumentException("Variable Name must not be empty for element:" + getName());
        } else if (variables.getObject(variableName) != null) {
            log.error("HTTP User Defined Element Configuration already defined for: {}, Please check whether there are duplicate configurations that contain the same Variable Name, and if so, delete or rename it", variableName);
        } else {
            variables.putObject(variableName, this); // pool will be created later
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String host) {

    }

    public static HTTPUDConfigElement getHTTPUDConfigElement(String variableName) throws RuntimeException {
        Object config =
                JMeterContextService.getContext().getVariables().getObject(variableName);
        if (config == null) {
            throw new RuntimeException("HTTP User Defined Element Configuration No found named: '" + variableName + "', ensure Variable Name matches Variable Name of HTTP User Defined Element Configuration");
        } else {
            if(config instanceof HTTPUDConfigElement) {
                return (HTTPUDConfigElement) config;
            } else {
                String errorMsg = "Found object stored under variable:'" + variableName + "' with class:"
                        + config.getClass().getName() + " and value: '" + config
                        + " but it's not a HTTPUDConfigElement, check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
    }


    public String getVariableName(){
        return getPropertyAsString(VARIABLE_NAME);
    }

    public String getProtocol() {
        return getPropertyAsString(HTTPSampler.PROTOCOL);
    }

    // copy 自 HTTPSamplerBase
    public String getDomain(){
        return getPropertyAsString(HTTPSampler.DOMAIN);
    }

    /**
     * Get the port number from the port string, allowing for trailing blanks.
     *
     * @return port number or UNSPECIFIED_PORT (== 0)
     */
    // copy 自 HTTPSamplerBase
    public int getPortIfSpecified() {
        String portAsString = getPropertyAsString(HTTPSampler.PORT);
        if(portAsString == null || portAsString.isEmpty()) {
            return HTTPSampler.UNSPECIFIED_PORT;
        }

        try {
            return Integer.parseInt(portAsString.trim());
        } catch (NumberFormatException e) {
            return HTTPSampler.UNSPECIFIED_PORT;
        }
    }

    /**
     * Get the port; apply the default for the protocol if necessary.
     *
     * @return the port number, with default applied if required.
     */
    // copy 自 HTTPSamplerBase
    public int getPort() {
        return getPortIfSpecified();
    }

    // 返回端口号输入框的值
    public String getPortAsString(){
        return getPropertyAsString(HTTPSampler.PORT);
    }

    // copy 自 HTTPSamplerBase
    public String getMethod() {
        return getPropertyAsString(HTTPSampler.METHOD);
    }

    // copy 自 HTTPSamplerBase
    protected String encodeSpaces(String path) {
        return JOrphanUtils.replaceAllChars(path, ' ', "%20"); // $NON-NLS-1$
    }

    // copy 自 HTTPSamplerBase
    public String getPath() {
        String p = getPropertyAsString(HTTPSampler.PATH);
        return encodeSpaces(p);
    }

    /**
     *
     * @return the encoding of the content, i.e. its charset name
     */
    // copy 自 HTTPSamplerBase
    public String getContentEncoding() {
        return getPropertyAsString(HTTPSampler.CONTENT_ENCODING);
    }

    // copy 自 HTTPSamplerBase
    public String getImplementation() {
        return this.getPropertyAsString(HTTPSampler.IMPLEMENTATION);
    }

    // copy 自 HTTPSamplerBase
    public int getConnectTimeout() {
        return getPropertyAsInt(HTTPSampler.CONNECT_TIMEOUT, 0);
    }

    // copy 自 HTTPSamplerBase
    public int getResponseTimeout() {
        return getPropertyAsInt(HTTPSampler.RESPONSE_TIMEOUT, 0);
    }

    // copy 自 HTTPSamplerBase
    public boolean isImageParser() {
        return getPropertyAsBoolean(HTTPSampler.IMAGE_PARSER, false);
    }

    // copy 自 HTTPSamplerBase
    public boolean isConcurrentDwn() {
        return getPropertyAsBoolean(HTTPSampler.CONCURRENT_DWN, false);
    }

    /**
     * Get the pool size for concurrent thread pool to get embedded resources.
     *
     * @return the pool size
     */
    // copy 自 HTTPSamplerBase
    public String getConcurrentPool() {
        return getPropertyAsString(HTTPSampler.CONCURRENT_POOL, CONCURRENT_POOL_DEFAULT);
    }

    /**
     * Get the regular expression URLs must match.
     *
     * @return regular expression (or empty) string
     */
    // copy 自 HTTPSamplerBase
    public String getEmbeddedUrlRE() {
        return getPropertyAsString(HTTPSampler.EMBEDDED_URL_RE, "");
    }

    /**
     * get IP/address source type to use
     *
     * @return address source type
     */
    // copy 自 HTTPSamplerBase
    public int getIpSourceType() {
        return getPropertyAsInt(HTTPSampler.IP_SOURCE_TYPE, HTTPSampler.SOURCE_TYPE_DEFAULT);
    }

    /**
     * get IP/address source type to use
     *
     * @return address source type
     */
    // copy 自 HTTPSamplerBase
    public String getIpSource() {
        return getPropertyAsString(HTTPSampler.IP_SOURCE, "");
    }

    public String getProxyScheme() {
        return getPropertyAsString(HTTPSampler.PROXYSCHEME, "");
    }

    // copy 自 HTTPSamplerBase
    public String getProxyHost() {
        return getPropertyAsString(HTTPSampler.PROXYHOST);
    }

    // copy 自 HTTPSamplerBase
    public int getProxyPortInt() {
        return getPropertyAsInt(HTTPSampler.PROXYPORT, 0);
    }

    // copy 自 HTTPSamplerBase
    public String getProxyUser() {
        return getPropertyAsString(HTTPSampler.PROXYUSER);
    }

    // copy 自 HTTPSamplerBase
    public String getProxyPass() {
        return getPropertyAsString(HTTPSampler.PROXYPASS);
    }

    // copy 自 HTTPSamplerBase
    public boolean useMD5() {
        return this.getPropertyAsBoolean(HTTPSampler.MD5, false);
    }

    // copy 自 HTTPSamplerBase
    public Arguments getArguments() {
        return (Arguments) getProperty(HTTPSampler.ARGUMENTS).getObjectValue();
    }

    /**
     * Saves the list of files.
     * The first file is saved in the Filename/field/mimetype properties.
     * Any additional files are saved in the FILE_ARGS array.
     *
     * @param files list of files to save
     */
    // copy 自 HTTPSamplerBase
    public void setHTTPFiles(HTTPFileArg[] files) {
        HTTPFileArgs fileArgs = new HTTPFileArgs();
        // Weed out the empty files
        if (files.length > 0) {
            for (HTTPFileArg file : files) {
                if (file.isNotEmpty()) {
                    fileArgs.addHTTPFileArg(file);
                }
            }
        }
        setHTTPFileArgs(fileArgs);
    }

    /*
     * Method to set files list to be uploaded.
     *
     * @param value
     *   HTTPFileArgs object that stores file list to be uploaded.
     */
    // copy 自 HTTPSamplerBase
    private void setHTTPFileArgs(HTTPFileArgs value) {
        if (value.getHTTPFileArgCount() > 0) {
            setProperty(new TestElementProperty(FILE_ARGS, value));
        } else {
            removeProperty(FILE_ARGS); // no point saving an empty list
        }
    }

    /**
     * Get the collection of files as a list.
     * The list is built up from the filename/filefield/mimetype properties,
     * plus any additional entries saved in the FILE_ARGS property.
     *
     * If there are no valid file entries, then an empty list is returned.
     *
     * @return an array of file arguments (never null)
     */
    // copy 自 HTTPSamplerBase
    public HTTPFileArg[] getHTTPFiles() {
        final HTTPFileArgs fileArgs = getHTTPFileArgs();
        return fileArgs == null ? new HTTPFileArg[] {} : fileArgs.asArray();
    }

    /*
     * Method to get files list to be uploaded.
     */
    // copy 自 HTTPSamplerBase
    private HTTPFileArgs getHTTPFileArgs() {
        return (HTTPFileArgs) getProperty(FILE_ARGS).getObjectValue();
    }

    // 自定义参数
    public Arguments getUserDefinedParameters() {
        return (Arguments) getProperty(HTTPUDArgumentsGui.HTTP_USER_DEFINED_ARGUMENTS).getObjectValue();
    }

    // 自定义参数默认值
    public Map<String, String> getUserDefinedParametersValueAsMap() {
        PropertyIterator iter = getUserDefinedParameters().iterator();
        Map<String, String> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            HTTPUDArgument arg = (HTTPUDArgument) iter.next().getObjectValue();
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

    // 自定义参数是否必传
    public Map<String, Boolean> getUserDefinedParametersRequiredAsMap() {
        PropertyIterator iter = getUserDefinedParameters().iterator();
        Map<String, Boolean> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            HTTPUDArgument arg = (HTTPUDArgument) iter.next().getObjectValue();
            // Because CollectionProperty.mergeIn will not prevent adding two
            // properties of the same name, we need to select the first value so
            // that this element's values prevail over defaults provided by
            // configuration
            // elements:
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.isRequired());
            }
        }
        return argMap;
    }

    // 自定义参数描述
    public Map<String, String> getUserDefinedParametersDescAsMap() {
        PropertyIterator iter = getUserDefinedParameters().iterator();
        Map<String, String> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            HTTPUDArgument arg = (HTTPUDArgument) iter.next().getObjectValue();
            // Because CollectionProperty.mergeIn will not prevent adding two
            // properties of the same name, we need to select the first value so
            // that this element's values prevail over defaults provided by
            // configuration
            // elements:
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.getDescription());
            }
        }
        return argMap;
    }

    // HTTP请求头参数
    public Arguments getHTTPHeaderArguments() {
        return (Arguments) getProperty(HTTPUDConfigElement.HTTP_HEADER_PARAMETERS_NAME).getObjectValue();
    }

    // HTTP请求参数Map
    public Map<String, String> getHTTPHeaderArgumentsValueAsMap() {
        PropertyIterator iter = getHTTPHeaderArguments().iterator();
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
}
