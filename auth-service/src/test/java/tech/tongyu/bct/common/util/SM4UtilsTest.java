package tech.tongyu.bct.common.util;

import org.junit.Test;

import java.io.IOException;

public class SM4UtilsTest {

    @Test
    public void testSM4_2(){
        String secretKey = "KaYup#asD1%79iYu";
        String iv = "KaYup#asD1%79iYu";

        String username = "script";
        String password = "123456a.";
        SM4Utils sm4Utils = new SM4Utils();
        sm4Utils.setSecretKey(secretKey);
        sm4Utils.setIv(iv);
        System.out.println("CBC admin: " + sm4Utils.encryptData_CBC(username));
        System.out.println("CBC 123456a.: " + sm4Utils.encryptData_CBC(password));

        System.out.println("ECB admin: " + sm4Utils.encryptData_ECB(username));
        System.out.println("ECB 123456a.: " + sm4Utils.encryptData_ECB(password));

    }

    @Test
    public void testSM4() throws IOException
    {
        String plainText = "admin";

        SM4Utils sm4 = new SM4Utils();
        sm4.setSecretKey("KaYup#asD1%79iYu");
        sm4.setHexString(false);

        System.out.println("ECB模式");
        String cipherText = sm4.encryptData_ECB(plainText);
        System.out.println("密文: " + cipherText);
        System.out.println("");

        plainText = sm4.decryptData_ECB(cipherText);
        System.out.println("明文: " + plainText);
        System.out.println("");

        System.out.println("CBC模式");
        sm4.setIv("KaYup#asD1%79iYu");
        cipherText = sm4.encryptData_CBC(plainText);
        System.out.println("密文: " + cipherText);
        System.out.println("");

        plainText = sm4.decryptData_CBC(cipherText);
        System.out.println("明文: " + plainText);
    }
}