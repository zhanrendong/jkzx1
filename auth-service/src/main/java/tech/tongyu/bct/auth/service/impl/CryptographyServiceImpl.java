package tech.tongyu.bct.auth.service.impl;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.service.CryptographyService;

@Service
public class CryptographyServiceImpl implements CryptographyService {
    private static final String ALGO = "PBEWithMD5AndDES";

    @Override
    public String decrypt(String plainText, String key) {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setAlgorithm(ALGO);
        config.setPassword(key);
        standardPBEStringEncryptor.setConfig(config);
        return standardPBEStringEncryptor.decrypt(plainText);
    }

    @Override
    public String encrypt(String cipherText, String key) {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        EnvironmentPBEConfig config = new EnvironmentPBEConfig();
        config.setAlgorithm(ALGO);
        config.setPassword(key);
        standardPBEStringEncryptor.setConfig(config);
        return standardPBEStringEncryptor.encrypt(cipherText);
    }
}



