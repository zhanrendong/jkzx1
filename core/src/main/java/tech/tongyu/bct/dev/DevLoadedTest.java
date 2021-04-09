package tech.tongyu.bct.dev;

import javax.annotation.PostConstruct;

public interface DevLoadedTest {

    @PostConstruct
    default void started(){
        LogUtils.springLoaded(this.getClass());
    }
}
