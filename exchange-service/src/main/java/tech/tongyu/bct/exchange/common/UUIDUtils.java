package tech.tongyu.bct.exchange.common;

import java.util.UUID;

public class UUIDUtils {

    public static UUID fromString(String id) {
        String uid = id.replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(uid);
    }
}
