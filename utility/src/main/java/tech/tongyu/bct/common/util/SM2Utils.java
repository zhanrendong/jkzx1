package tech.tongyu.bct.common.util;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class SM2Utils {
    public static void main(String[] args) {
        SM2KeyPair kp = SM2.generateKeyPair();
        String sign = SM2.sign(kp.getPrivateKey(), "要签名的信息");
        boolean verify = SM2.verify(kp.getPublicKey(), "要签名的信息", sign);
        String cipherData = SM2.encrypt(kp.getPublicKey(), "待加密的信息");
        String content = SM2.decrypt(kp.getPrivateKey(), cipherData);
    }
}

class SM2 {

    private final static ECCurve.Fp curve;
    private final static ECPoint g;
    private final static ECDomainParameters ec_domain_parameters;
    private final static ECKeyGenerationParameters ec_key_generation_parameters;
    private final static ECKeyPairGenerator ec_key_pair_generator;
    /**
     * SM2椭圆曲线公钥密码算法推荐曲线参数
     * 推荐使用素数域256位椭圆曲线
     * 椭圆曲线方程:y2 = x3 + ax + b
     */
    private static BigInteger n = new BigInteger("FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "7203DF6B" + "21C6052B" + "53BBF409" + "39D54123", 16);
    private static BigInteger p = new BigInteger("FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFF", 16);
    private static BigInteger a = new BigInteger("FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFC", 16);
    private static BigInteger b = new BigInteger("28E9FA9E" + "9D9F5E34" + "4D5A9E4B" + "CF6509A7" + "F39789F5" + "15AB8F92" + "DDBCBD41" + "4D940E93", 16);
    private static BigInteger gx = new BigInteger("32C4AE2C" + "1F198119" + "5F990446" + "6A39C994" + "8FE30BBF" + "F2660BE1" + "715A4589" + "334C74C7", 16);
    private static BigInteger gy = new BigInteger("BC3736A2" + "F4F6779C" + "59BDCEE3" + "6B692153" + "D0A9877C" + "C62A4740" + "02DF32E5" + "2139F0A0", 16);
    private static boolean debug = true;

    static {
        curve = new ECCurve.Fp(p, a, b, null, null);
        g = curve.createPoint(gx, gy);
        ec_domain_parameters = new ECDomainParameters(curve, g, n);
        ec_key_generation_parameters = new ECKeyGenerationParameters(ec_domain_parameters, new SecureRandom());
        ec_key_pair_generator = new ECKeyPairGenerator();
        ec_key_pair_generator.init(ec_key_generation_parameters);
    }

    SM2(boolean debug) {
        SM2.debug = debug;
    }

    static SM2KeyPair generateKeyPair() {
        AsymmetricCipherKeyPair key = ec_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
        BigInteger privateKey = ecpriv.getD();
        ECPoint publicKey = ecpub.getQ();
        if (debug) {
            System.out.println("私钥: " + Hex.toHexString(privateKey.toByteArray()).toUpperCase());
            System.out.println("公钥: " + Hex.toHexString(publicKey.getEncoded(false)).toUpperCase());
        }
        return new SM2KeyPair(publicKey, privateKey);
    }

    static String encrypt(ECPoint publicKey, String content) {
        ECPoint pukPoint = curve.decodePoint(publicKey.getEncoded(false));
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, ec_domain_parameters);

        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(publicKeyParameters, new SecureRandom()));

        byte[] arrayOfBytes = null;
        try {
            byte[] in = content.getBytes(StandardCharsets.UTF_8);
            arrayOfBytes = sm2Engine.processBlock(in, 0, in.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (debug) {
            System.out.println("加密: " + Hex.toHexString(arrayOfBytes).toUpperCase());
        }
        return Hex.toHexString(arrayOfBytes);
    }

    static String decrypt(BigInteger privateKey, String cipherData) {
        byte[] cipherDataByte = Hex.decode(cipherData);

        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKey, ec_domain_parameters);

        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, privateKeyParameters);

        String result = null;
        try {
            byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
            result = new String(arrayOfBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (debug) {
            System.out.println("解密: " + result);
        }
        return result;
    }

    static String sign(BigInteger privateKey, String content) {
        byte[] message = content.getBytes(StandardCharsets.UTF_8);

        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKey, ec_domain_parameters);

        SM2Signer sm2Signer = new SM2Signer();
        //初始化签名实例,带上ID,国密的要求,ID默认值:1234567812345678
        sm2Signer.init(true, new ParametersWithID(new ParametersWithRandom(privateKeyParameters, new SecureRandom()), Strings.toByteArray("1234567812345678")));
        sm2Signer.update(message, 0, message.length);

        byte[] arrayOfBytes = null;
        try {
            arrayOfBytes = sm2Signer.generateSignature();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (debug) {
            System.out.println("签名: " + Hex.toHexString(arrayOfBytes).toUpperCase());
        }
        return Hex.toHexString(arrayOfBytes);
    }

    static boolean verify(ECPoint publicKey, String content, String signer) {
        byte[] message = content.getBytes(StandardCharsets.UTF_8);
        byte[] sign = Hex.decode(signer);

        ECPoint pukPoint = curve.decodePoint(publicKey.getEncoded(false));
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(pukPoint, ec_domain_parameters);

        SM2Signer sm2Signer = new SM2Signer();
        //初始化签名实例,带上ID,国密的要求,ID默认值:1234567812345678
        sm2Signer.init(false, new ParametersWithID(publicKeyParameters, Strings.toByteArray("1234567812345678")));
        sm2Signer.update(message, 0, message.length);

        boolean verify = sm2Signer.verifySignature(sign);
        if (debug) {
            System.out.println("验证: " + (verify ? "成功" : "失败"));
        }
        return verify;
    }
}

/**
 * SM2密钥对
 */
class SM2KeyPair {

    private final ECPoint publicKey;
    private final BigInteger privateKey;

    SM2KeyPair(ECPoint publicKey, BigInteger privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    ECPoint getPublicKey() {
        return publicKey;
    }

    BigInteger getPrivateKey() {
        return privateKey;
    }
}