package com.gitee.qa.jmeter.functions;

import org.apache.commons.codec.binary.Base64;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


@AutoService(Function.class)
public class RSAEncrypt extends AbstractFunction{

    private static final Logger log = LoggerFactory.getLogger(RSAEncrypt.class);

    /*定义函数名*/
    public static final String KEY = "__rsaEncrypt";

    /*定义function参数的描述*/
    public static final List<String> desc = new LinkedList<String>();
    static {
        desc.add("参数1-明文：要加密的字符串");
        desc.add("参数2-公钥：公钥密钥");
        desc.add("参数3-变量名：将加密密文保存在此变量中");
    }
    private static final int MAX_PARA_COUNT = 3; // 参数最大个数
    private static final int MIN_PARA_COUNT = 2; // 参数最小个数

    // 参数在execute内以局部变量获取，不使用实例变量
    private Object[] values = null;

    // 加解密算法
    final String algorithmStr = "RSA";

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
    public String execute(SampleResult arg0, Sampler arg1) throws InvalidVariableException{
        /*
         * JMeter会将上次运行的SampleResult和当前的Sampler作为参数传入到该方法里，返回值就是在运行该function后得到的值，以String类型返回。
         * 该方法如果操作了非线程安全的对象（比如文件），则需要将对该方法进行线程同步保护。
         */
        // 参数必须放到execute进行获取 execute和setParameters不再一个线程上下文执行
        // 性能优化改成局部变量
        String content = ((CompoundVariable) values[0]).execute().trim(); 		// 将每个参数值获取出来
        String key = ((CompoundVariable) values[1]).execute().trim(); 		// 将每个参数值获取出来

        String varName = null;
        if(values.length > 2) {
            varName = ((CompoundVariable) values[2]).execute().trim(); 		// 将每个参数值获取出来
        }

        if ("".equals(key)){
            log.error("请填写公钥");
            return null;
        }

        String encryptText = null;

        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(key));
            KeyFactory keyFactory = KeyFactory.getInstance(algorithmStr);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher = Cipher.getInstance(algorithmStr);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] result = cipher.doFinal(content.getBytes());
            encryptText = Base64.encodeBase64String(result);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
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
