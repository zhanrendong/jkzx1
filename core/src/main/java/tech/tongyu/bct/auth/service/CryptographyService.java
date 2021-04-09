package tech.tongyu.bct.auth.service;

public interface CryptographyService {
    /**
     * @param plainText 明文
     * @param key       密钥
     * @return 返回密文
     */

    String decrypt(String plainText, String key);

    /**
     * @param cipherText 密文
     * @param key        密钥
     * @return 返回明文
     */
    String encrypt(String cipherText, String key);

}
