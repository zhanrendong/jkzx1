package tech.tongyu.bct.auth.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class CommonUtilsTest {

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    private static String password = "12345a.";
    private static String otherPassword = "54321a.";
    private static String hashedPassword;

    @Test
    public void hashPassword() {
        hashedPassword = CommonUtils.hashPassword("12345");
        logger.info("admin:" + hashedPassword);
        logger.info("scripts:" + CommonUtils.hashPassword("123456a."));
    }

    @Test
    @Ignore
    public void checkPassword() {
        Assert.assertTrue(CommonUtils.checkPassword(password, hashedPassword));
        Assert.assertFalse(CommonUtils.checkPassword(otherPassword, hashedPassword));
    }
}