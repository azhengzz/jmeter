package com.gitee.qa.jmeter.functions;

import com.google.auto.service.AutoService;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;


@AutoService(Function.class)
public class AESEncrypt extends AbstractFunction{

    private static final Logger log = LoggerFactory.getLogger(AESEncrypt.class);

    /*定义函数名*/
    public static final String KEY = "__aesEncrypt";

    /*定义function参数的描述*/
    public static final List<String> desc = new LinkedList<String>();
    static {
        desc.add("参数1-明文：要加密的字符串");
        desc.add("参数2-密钥：16/24/32字节长度，参数为空取默认密钥值");  // ABCDEFGHIJKL_key
        desc.add("参数3-偏移量：16字节长度，参数为空取默认偏移量值");  // ABCDEFGHIJKLM_iv
        desc.add("参数4-变量名：将加密密文保存在此变量中");
    }

    static {
        // 性能优化
        // 初始化放到static中
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int MAX_PARA_COUNT = 4; // 参数最大个数
    private static final int MIN_PARA_COUNT = 3; // 参数最小个数

    // 性能优化去掉锁后，不使用实例变量
//    private String content; 	// 参数1-明文
//    private String key; 	    // 参数2-密钥
//    private String iv; 	        // 参数3-偏移量
//    private String varName; 	// 参数4-变量名
    private Object[] values = null;

    // 算法名称
    final String KEY_ALGORITHM = "AES";
    // 加解密算法/模式/填充方式
    final String algorithmStr = "AES/CBC/PKCS5Padding";

    /*定义函数名*/
    @Override
    public String getReferenceKey() {
        /*
         * 这个就是function的名字。
         * JMeter的命名规则是在方法名前面加入双下划线"__"。
         * 比如"__GetEnv"，
         * function的名字跟实现该类的类名应该一致，而且该名字应该以static final的方式在实现类中定义好，避免在运行的时候更改它。
         */
        return KEY;
    }

    /*定义function参数的描述*/
    public List<String> getArgumentDesc() {
        /*提供一个方法来告诉JMeter关于你实现的function的描述。*/
        return desc;
    }

    /*参数检查和参数提取*/
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        /*
         * 这个方法在用于传递用户在执行过程当中传入的实际参数值。
         * 该方法在function没有参数情况下也会被调用。
         * 一般该方法传入的参数会被保存在类内全局变量里，并被后面调用的execute方法中使用到。
         */
        checkParameterCount(parameters, MIN_PARA_COUNT, MAX_PARA_COUNT); 	//检查参数的个数是否正确
        this.values = parameters.toArray();
    }


    @Override
    // 性能优化，不再使用锁
//    public synchronized String execute(SampleResult arg0, Sampler arg1) throws InvalidVariableException{
    public String execute(SampleResult arg0, Sampler arg1) throws InvalidVariableException{
        /*
         * JMeter会将上次运行的SampleResult和当前的Sampler作为参数传入到该方法里，返回值就是在运行该function后得到的值，以String类型返回。
         * 该方法如果操作了非线程安全的对象（比如文件），则需要将对该方法进行线程同步保护。
         */
        // 参数必须放到execute进行获取 execute和setParameters不再一个线程上下文执行
        // 性能优化改成局部变量
        String content = ((CompoundVariable) values[0]).execute().trim(); 		// 将每个参数值获取出来
        String key = ((CompoundVariable) values[1]).execute().trim(); 		// 将每个参数值获取出来
        String iv = ((CompoundVariable) values[2]).execute().trim(); 		// 将每个参数值获取出来

        String varName = null;
        if(values.length > 3) {
            varName = ((CompoundVariable) values[3]).execute().trim(); 		// 将每个参数值获取出来
        }

        if ("".equals(key)){
            key = "ABCDEFGHIJKL_key";
        }
        if ("".equals(iv)){
            iv = "ABCDEFGHIJKLM_iv";
        }
        if (key.length() != 16 && key.length() != 24 && key.length() != 32){
            log.error("密钥参数要求16、24或32字节长度");
            return null;
        }
        if (iv.length() != 16){
            log.error("偏移量参数要求16字节长度");
            return null;
        }

        String encryptText = null;

        Cipher cipher = null;
        byte[] encryptedText = null;
        byte[] keyBytes = key.getBytes();

// 放到 static{} 中初始化
//        // 初始化
//        Security.addProvider(new BouncyCastleProvider());
        // 转化成JAVA的密钥格式
        Key secretKeySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        try {
            // 初始化cipher
            cipher = Cipher.getInstance(algorithmStr);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv.getBytes()));
            encryptedText = cipher.doFinal(content.getBytes());
            encryptText = new String(Base64.getEncoder().encode(encryptedText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            log.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }

        if (varName != null) {
            JMeterVariables vars = getVariables(); // JMeter变量池
            final String varTrim = varName;
            if (vars != null && varTrim.length() > 0){// vars will be null on TestPlan
                vars.put(varTrim, encryptText); // 将自定义的函数名放入到JMeter的变量池中
            }
        }

        return encryptText;

    }

}
