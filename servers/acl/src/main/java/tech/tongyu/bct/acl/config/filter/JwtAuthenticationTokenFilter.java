package tech.tongyu.bct.acl.config.filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.tongyu.bct.acl.config.security.PathConstants;
import tech.tongyu.bct.acl.config.security.exception.JwtAuthenticationException;
import tech.tongyu.bct.acl.utils.TokenConstants;
import tech.tongyu.bct.acl.utils.TokenUtils;
import tech.tongyu.bct.acl.common.UserInfo;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.UserTypeEnum;
import tech.tongyu.bct.auth.service.TokenService;
import tech.tongyu.bct.auth.service.UserService;
import tech.tongyu.bct.common.api.response.JsonRpcResponse;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.HttpUtils;
import tech.tongyu.bct.common.util.JsonUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Value("${secret:dkJ34Bdadf098adf}")
    private String secret = "dkJ34Bdadf098adf";

    private UserInfo userInfo;

    @Autowired(required = false)
    private TokenService tokenService;
    @Autowired(required = false)
    private UserService userService;

    private final static List<String>  doNotCheckPath = Lists.newArrayList(
            PathConstants.USERS_CAPTCHA,
            PathConstants.USERS_LOGIN,
            PathConstants.USERS_TOKEN_LOGIN,
            PathConstants.USERS_ENCRYPTED_LOGIN,
            PathConstants.USERS_CHANGE_PASSWORD,
            PathConstants.USERS_ENCRYPTED_CHANGE_PASSWORD,
            PathConstants.API_DOC,
            PathConstants.USERS_VERIFY_IP_TOKEN_BOUND
    );
    private final static List<String>  doNotCheckPathStartWith = Lists.newArrayList(
            PathConstants.WS_END_POINT,
            PathConstants.FILE_DOWNLOAD
    );
    private final static String newLoginUser = "newLoginUser";

    private static Map<String, WebAuthenticationDetails>  detailsContainer = Maps.newHashMap();
    private static Map<String, UserDetailsImpl>  userDetailsContainer = Maps.newHashMap();
    private static Map<String, UsernamePasswordAuthenticationToken> authenticationContainer = Maps.newHashMap();

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    public JwtAuthenticationTokenFilter(UserInfo userInfo){
        this.userInfo = userInfo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        boolean doNotCheck = doNotCheckPath.contains(request.getServletPath());

        doNotCheck = doNotCheck || doNotCheckPathStartWith.stream().anyMatch(v -> StringUtils.startsWith(request.getServletPath(), v));

        UserDetailsImpl userDetails;
        UsernamePasswordAuthenticationToken authentication;
        if(doNotCheck){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            userDetails = generateUserDetails(newLoginUser, calendar.getTime());
            authentication = generateUsernamePasswordAuthenticationToken(newLoginUser, userDetails);
            authentication.setDetails(generateWebAuthenticationDetails(newLoginUser, request));
        }
        else {
            Optional<String> tokenOpt = TokenUtils.extractTokenString(request);
            if(!tokenOpt.isPresent()){
                throw new JwtAuthenticationException();
            }

            Claims claims = null;
            try {
                claims = TokenUtils.getClaimsFromToken(secret, tokenOpt.get());
                String username = (String) claims.get(TokenConstants.USERNAME);
                Boolean validIpBond = (Boolean) claims.get(AuthConstants.TOKEN_VALID_IP_BOND);
                userInfo.setUserName(username);
                userInfo.setToken(tokenOpt.get());
                if ((validIpBond == null || validIpBond) && userService != null && tokenService != null){
                    UserDTO userDTO = userService.authUserByNameGet(username);
                    if (userDTO != null && userDTO.getUserType() != UserTypeEnum.SCRIPT){
                        String ip = HttpUtils.getIPAddress(request);
                        if (!tokenService.verifyIpAndUsernameBound(ip, username)){
                            throw new CustomException(String.format("当前IP[%s]与用户[%s]未绑定", ip, username));
                        }
                    }
                }
            } catch (CustomException e){
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().println(JsonUtils.toJson(
                        new JsonRpcResponse(JsonRpcResponse.ErrorCode.TOKEN_INVALID,
                                TokenConstants.TOKEN_INVALID_MSG)));
                return;
            }

            userDetails = generateUserDetails(userInfo.getUserName(), claims.getExpiration());
            authentication = generateUsernamePasswordAuthenticationToken(userInfo.getUserName(), userDetails);
            authentication.setDetails(generateWebAuthenticationDetails(userInfo.getUserName(), request));

        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
        removeExpiredSessionInformation(authentication.getName());
    }

    private UserDetailsImpl generateUserDetails(String username, Date lastPasswordResetDate){
        UserDetailsImpl userDetails = userDetailsContainer.get(username);
        if (Objects.isNull(userDetails)){
            userDetails = new UserDetailsImpl(username, "", Lists.newArrayList(), true, lastPasswordResetDate);
            userDetailsContainer.put(username, userDetails);
        }else {
            userDetails.setLastPasswordResetDate(lastPasswordResetDate);
        }
        return userDetails;
    }

    private UsernamePasswordAuthenticationToken generateUsernamePasswordAuthenticationToken(String username, UserDetailsImpl userDetails){
        UsernamePasswordAuthenticationToken authentication = authenticationContainer.get(username);
        if (Objects.isNull(authentication) || Objects.isNull(authentication.getPrincipal())){
            authentication = new UsernamePasswordAuthenticationToken(userDetails, null, Lists.newArrayList());
            authenticationContainer.put(username, authentication);
        }
        return authentication;
    }

    private WebAuthenticationDetails generateWebAuthenticationDetails(String username, HttpServletRequest request){
        WebAuthenticationDetails details = detailsContainer.get(username);
        if (Objects.isNull(details)
                || (StringUtils.isBlank(details.getRemoteAddress()) && StringUtils.isNotBlank(request.getRemoteAddr()))
                || (StringUtils.isNotBlank(details.getRemoteAddress()) && StringUtils.isBlank(request.getRemoteAddr()))
                || (StringUtils.isNotBlank(details.getRemoteAddress()) && !details.getRemoteAddress().equals(request.getRemoteAddr()))
                || (StringUtils.isBlank(details.getSessionId()) && Objects.nonNull(request.getSession(false)) && StringUtils.isNotBlank(request.getSession(false).getId()))
                || (StringUtils.isNotBlank(details.getSessionId()) && (Objects.isNull(request.getSession(false)) || StringUtils.isBlank(request.getSession(false).getId())))
                || (StringUtils.isNotBlank(details.getSessionId()) && !details.getSessionId().equals(request.getSession(false).getId()))
        ){
            details = new WebAuthenticationDetailsSource().buildDetails(request);
            detailsContainer.put(username, details);
        }
        return details;
    }

    private void removeExpiredSessionInformation(String username){
        List<Object> principals= sessionRegistry.getAllPrincipals();
        if (CollectionUtils.isNotEmpty(principals)){
            for (Object principal : principals) {
                if (principal instanceof UserDetailsImpl) {
                    final UserDetailsImpl loggedUser = (UserDetailsImpl) principal;
                    if (username.equals(loggedUser.getUsername())) {
                        List<SessionInformation> sessionsInfos = sessionRegistry.getAllSessions(principal, true);
                        if (CollectionUtils.isNotEmpty(sessionsInfos)) {
                            for (SessionInformation sessionInformation : sessionsInfos) {
                                sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
                            }
                        }
                    }
                }
            }
        }
    }

}
