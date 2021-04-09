package tech.tongyu.bct.auth.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.common.user.LoginUser;
import tech.tongyu.bct.auth.dao.entity.UserDbo;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.UserTypeEnum;
import tech.tongyu.bct.auth.exception.AuthBlankParamException;
import tech.tongyu.bct.common.util.SM3Utils;
import tech.tongyu.bct.common.util.SystemConfig;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static tech.tongyu.bct.auth.AuthConstants.EXPIRATION_DAYS;

public class CommonUtils {

    private static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static Boolean isAdminUser(String username){
        return StringUtils.equalsIgnoreCase(username, AuthConstants.ADMIN);
    }

    public static Boolean isAdminUser(UserDbo user){
        return isAdminUser(user.getUsername());
    }

    public static Boolean isAdminUser(LoginUser user){
        return isAdminUser(user.getUsername());
    }

    public static String hashPassword(String password){
        return SM3Utils.plainEncrypt(password);
    }

    public static Boolean checkPassword(String password, String hashPassword){
        return Objects.equals(SM3Utils.plainEncrypt(password), hashPassword);
    }

    public static Boolean checkPasswordLength(String password){
        if (StringUtils.isEmpty(password)){
            return Boolean.FALSE;
        }
        return password.length() >= AuthConstants.PASSWORD_LENGTH_MIN && password.length() <= AuthConstants.PASSWORD_LENGTH_MAX;
    }

    public static String getDefaultRoleName(String username){
        return String.format("_%s", username);
    }

    public static Timestamp getPasswordExpirationTimestamp(){
        LocalDate now = LocalDate.now();
        Period p = Period.ofDays((Integer) SystemConfig.get(EXPIRATION_DAYS));
        LocalDate expiration = now.plus(p);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = expiration.atStartOfDay(zoneId);
        return Timestamp.from(zdt.toInstant());
    }

    public static Timestamp getScriptUserPasswordExpirationTimestamp(){
        LocalDate now = LocalDate.now();
        Period p = Period.ofYears(100);
        LocalDate expiration = now.plus(p);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = expiration.atStartOfDay(zoneId);
        return Timestamp.from(zdt.toInstant());
    }

    public static Timestamp getPasswordExpirationTimestamp(UserTypeEnum userType){
        switch (userType){
            case SCRIPT:
                return getScriptUserPasswordExpirationTimestamp();
            default:
                return getPasswordExpirationTimestamp();
        }
    }

    public static Optional<String> generateToken(UserDTO user, String secret, String issuer, Boolean isTemp){
        if(Objects.equals(user.getUserType(), UserTypeEnum.NORMAL) && (user.getLocked() || user.getExpired()))
            return Optional.empty();
        String[] roles = user.getRoleName().toArray(new String[user.getRoleName().size()]);
        return Optional.of(generateToken(user.getUsername(), roles, secret, issuer, isTemp));
    }

    public static String generateToken(String username, String[] roles, String secret, String issuer, Boolean isTemp){
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("roles", roles);
        long expMillis = System.currentTimeMillis() + (isTemp ? 10 * 1000L : 24 * 60 * 60 * 1000L);
        claims.put("sub", username);
        claims.put("exp", new Date(expMillis));
        claims.put("iss", issuer);
        //claims.put("nbf", new Date());
        claims.put("iat", new Date());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(expMillis))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    public static void checkBlankParam(Map<String, String> params) {
        String[] paramNames = params.entrySet().stream()
                .filter(p -> StringUtils.isBlank(p.getValue())).map(Map.Entry::getKey).distinct().toArray(String[]::new);

        if (paramNames.length > 0)
            throw new AuthBlankParamException(paramNames);
    }
}
