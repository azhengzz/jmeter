package com.gitee.qa.jmeter.protocol.s3.config;

import com.gitee.qa.jmeter.protocol.s3.util.S3Config;
import com.gitee.qa.jmeter.protocol.s3.util.S3Service;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class S3ConfigElement extends ConfigTestElement implements TestStateListener {

    public static final String S3_UID_NAME = "S3ConfigElement.s3UidName"; // $NON-NLS-1$
    public static final String S3_ENDPOINT = "S3ConfigElement.s3EndPoint"; // $NON-NLS-1$
    public static final String S3_REGION = "S3ConfigElement.s3Region"; // $NON-NLS-1$
    public static final String S3_ACCESS_KEY_ID = "S3ConfigElement.s3AccessKeyId"; // $NON-NLS-1$
    public static final String S3_SECRET_ACCESS_KEY = "S3ConfigElement.s3SecretAccessKey"; // $NON-NLS-1$

    private static final Logger log = LoggerFactory.getLogger(S3ConfigElement.class);

    S3Service s3Service = null;

    public String getS3UidName(){
        return getPropertyAsString(S3_UID_NAME);
    }

    public void setS3UidName(String s3UidName){
        setProperty(S3_UID_NAME, s3UidName);
    }

    public String getS3Endpoint(){
        return getPropertyAsString(S3_ENDPOINT);
    }

    public void setS3EndPoint(String s3EndPoint){
        setProperty(S3_ENDPOINT, s3EndPoint);
    }

    public String getS3Region(){
        return getPropertyAsString(S3_REGION);
    }

    public void setS3Region(String s3Region){
        setProperty(S3_REGION, s3Region);
    }

    public String getS3AccessKeyId(){
        return getPropertyAsString(S3_ACCESS_KEY_ID);
    }

    public void setS3AccessKeyId(String s3AccessKeyId){
        setProperty(S3_ACCESS_KEY_ID, s3AccessKeyId);
    }

    public String getS3SecretAccessKey(){
        return getPropertyAsString(S3_SECRET_ACCESS_KEY);
    }

    public void setS3SecretAccessKey(String s3SecretAccessKey){
        setProperty(S3_SECRET_ACCESS_KEY, s3SecretAccessKey);
    }


    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        JMeterVariables variables = getThreadContext().getVariables();
        String s3UidName = getS3UidName();
        if(JOrphanUtils.isBlank(s3UidName)) {
            throw new IllegalArgumentException("S3UiName Name must not be empty for element:" + getName());
        } else if (variables.getObject(s3UidName) != null) {
            log.error("S3 Connection Configuration already defined for: {}", s3UidName);
        } else {
            synchronized(this){
                S3Config config = new S3Config(getS3Endpoint(), getS3Region(), getS3AccessKeyId(), getS3SecretAccessKey());
                variables.putObject(s3UidName, new S3Service(config));
                log.info("S3 Service Connected! S3UidName: {}", getS3UidName());
            }
        }
    }

    @Override
    public void testStarted(String host) {
        this.testStarted();
    }

    @Override
    public void testEnded() {
        synchronized (this) {
            if (s3Service != null) {
                try {
                    s3Service.close();
                    log.info("S3 Service Closed! S3UidName: {}", getS3UidName());
                } catch (Exception ex) {
                    log.error("Error closing s3Service: {} S3UidName: {}", getName(), getS3UidName(), ex);
                }
            }
            s3Service = null;
        }
    }

    @Override
    public void testEnded(String host) {
        this.testEnded();
    }

    public static S3Service getS3Service(String s3UidName) throws Exception {
        Object object =
                JMeterContextService.getContext().getVariables().getObject(s3UidName);
        if (object == null) {
            throw new Exception("No s3 service found named: " + s3UidName + ", ensure s3UidName matches object of S3 Connection Configuration");
        } else {
            if(object instanceof S3Service) {
                return (S3Service) object;
            } else {
                String errorMsg = "Found object stored under variable:'" + s3UidName + "' with class:"
                        + object.getClass().getName() + " and value: '" + object
                        + " but it's not a S3Service, check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new Exception(errorMsg);
            }
        }
    }

}
