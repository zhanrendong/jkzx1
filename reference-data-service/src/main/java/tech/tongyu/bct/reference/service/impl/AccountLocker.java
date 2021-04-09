package tech.tongyu.bct.reference.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class AccountLocker {
    private ConcurrentHashMap<String, Object> accounts;
    private static Logger logger = LoggerFactory.getLogger(AccountLocker.class);

    private static class InstanceHolder {
        private static final AccountLocker INSTANCE = new AccountLocker();
    }

    public static AccountLocker getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private AccountLocker() {
        accounts = new ConcurrentHashMap<>();
    }


    public <T> T lockAndExecute(Supplier<T> supplier, String accountId) {
        long start = System.currentTimeMillis();
        Object lock = accounts.get(accountId);
        if (lock == null) {
            synchronized (AccountLocker.class) {
                lock = accounts.get(accountId);
                if (lock == null) {
                    accounts.put(accountId, new Object());
                    lock = accounts.get(accountId);
                }
            }
        }
        synchronized (lock) {
            T res = supplier.get();
            long end = System.currentTimeMillis();
            logger.info("API takes {} millis", (end - start));
            return res;
        }
    }

}