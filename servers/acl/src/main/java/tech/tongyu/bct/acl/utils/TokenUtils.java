package tech.tongyu.bct.acl.utils;

import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.acl.common.UserStatus;
import tech.tongyu.bct.acl.config.security.exception.JwtAuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TokenUtils {

    public static String getTokenFromAuthServer(String ip, Integer port, String username, String password){
        return getLoginUserStatus(ip, port, username, password).getToken();
    }

    public static UserStatus getLoginUserStatus(String ip, Integer port, String username, String password){
        String url = String.format("http://%s:%d%s", ip, port, "/users/login");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        Map<String, String> params = Maps.newHashMap();
        params.put("username", username);
        params.put("password", password);
        StringEntity entity = new StringEntity(JsonUtils.toJson(params)
                , ContentType.create("application/json", "UTF-8"));
        httpPost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                return JsonUtils.fromJson(UserStatus.class, result);
            else
                throw new RuntimeException(result);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Optional<String> extractTokenString(HttpServletRequest httpServletRequest){
        String authToken = httpServletRequest.getHeader(TokenConstants.TOKEN_KEY);
        return Optional.ofNullable(authToken)
                .map(token -> {
                    if(token.startsWith(TokenConstants.TOKEN_PREFIX))
                        return token.substring(TokenConstants.TOKEN_PREFIX.length());
                    return null;
                });
    }

    public static String generateToken(String username, String issuer, Integer expiration, String secret, Boolean validIpBind) {
        Date exp = generateExpirationDate(expiration);
        Map<String, Object> claims = new HashMap<>();
        claims.put(TokenConstants.USERNAME, username);
        claims.put(TokenConstants.SUB, username);
        claims.put(TokenConstants.EXP, exp);
        claims.put(TokenConstants.ISS, issuer);
        claims.put(AuthConstants.TOKEN_VALID_IP_BOND, validIpBind);
        return Jwts.builder()
                .setIssuer(issuer)
                .setExpiration(exp)
                .setSubject(username)
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }

    public static Claims getClaimsFromToken(String secret, String token){
        try {
            return Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            throw new CustomException(TokenConstants.TOKEN_INVALID_MSG);
        }
    }

    private static Date generateExpirationDate(Integer expiration) {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }
}
