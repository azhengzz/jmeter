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

import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.security.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * JMeter自定义函数：__sm2Encrypt
 *
 * 用于Gitee登录密码SM2加密，对应前端JS加密逻辑：
 * hex(sm2.doEncrypt(password, publicKey))
 * js文件：http://bosc-dev.gitee.work:8000/assets/one/static/js/main.0e17a722.chunk.js
 *
 * 使用方式（JMeter表达式）：
 * ${__sm2Encrypt(qq123456)}
 * ${__sm2Encrypt(${password})}
 *
 * JMeter Functions 参考：
 * https://jmeter.apache.org/usermanual/functions.html
 */
@AutoService(Function.class)
public class SM2Encrypt extends AbstractFunction{

    private static final Logger log = LoggerFactory.getLogger(SM2Encrypt.class);

    /*定义函数名*/
    public static final String KEY = "__sm2Encrypt";

    /*定义function参数的描述*/
    public static final List<String> desc = new LinkedList<String>();
    static {
        // 注册BouncyCastle安全提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        desc.add("参数1-明文密码：必填，要加密的字符串");
        desc.add("参数2-SM2公钥hex：可选，不传则使用默认公钥");
    }
    private static final int MAX_PARA_COUNT = 2; // 参数最大个数
    private static final int MIN_PARA_COUNT = 1; // 参数最小个数

    private static final String SM2_PUBLIC_KEY_HEX =
            "0494b03f50d87e77c9b11b21dd2dd6059fa28277c53c49564aad71a99ccacfc87" +
            "ddabbe14888d37c94f59741686688d8db16b50982fdb88457b032ccf31a692441";

    private static final String CHARSET = "UTF-8";

    // 参数在execute内以局部变量获取，不使用实例变量
    private Object[] values = null;

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
        String password = ((CompoundVariable) values[0]).execute().trim(); 		// 参数1-明文密码

        // 获取公钥（可选参数，不传或为空则使用默认公钥）
        String publicKeyHex = SM2_PUBLIC_KEY_HEX;
        if(values.length > 1) {
            String keyParam = ((CompoundVariable) values[1]).execute().trim(); 		// 参数2-SM2公钥hex
            if (!"".equals(keyParam)) {
                publicKeyHex = keyParam;
            }
        }

        if ("".equals(password)){
            log.error("明文密码不能为空");
            return null;
        }

        String encryptText = null;

        try {
            encryptText = encryptBySM2(password, publicKeyHex);
        } catch (Exception e) {
            log.error(org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
        }

        return encryptText;
    }

    /**
     * SM2加密主逻辑
     *
     * @param plainPassword 明文密码
     * @param publicKeyHex  公钥hex字符串（带04前缀）
     * @return hex(ciphertext) 格式的加密结果（Bouncy Castle的processBlock已包含C1的04前缀）
     */
    private String encryptBySM2(String plainPassword, String publicKeyHex) throws Exception {
        // 1. 获取SM2曲线参数（sm2p256v1 = 国密标准曲线）
        ECNamedCurveParameterSpec sm2Spec = ECNamedCurveTable.getParameterSpec("sm2p256v1");

        // 2. 解码公钥点
        byte[] pubKeyBytes = hexToBytes(publicKeyHex);
        ECPoint point = sm2Spec.getCurve().decodePoint(pubKeyBytes);

        // 3. 构建公钥对象（需将ECParameterSpec转为ECDomainParameters）
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, sm2Spec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        BCECPublicKey pubKey = (BCECPublicKey) keyFactory.generatePublic(pubKeySpec);
        ECDomainParameters domainParams = new ECDomainParameters(
                sm2Spec.getCurve(), sm2Spec.getG(), sm2Spec.getN(), sm2Spec.getH());
        ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(
                pubKey.getQ(), domainParams);

        // 4. SM2加密（C1C3C2模式，匹配前端JS）
        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
        engine.init(true, new ParametersWithRandom(pubKeyParams));

        byte[] plaintext = plainPassword.getBytes(CHARSET);
        byte[] ciphertext = engine.processBlock(plaintext, 0, plaintext.length);

        // 5. 转hex输出（Bouncy Castle的processBlock已包含C1的04前缀，无需额外添加）
        return bytesToHex(ciphertext);
    }

    // ==================== 字节工具方法 ====================

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("hex字符串长度必须为偶数: " + len);
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

}
