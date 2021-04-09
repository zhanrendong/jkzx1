package tech.tongyu.bct.auth.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.tongyu.bct.acl.common.UserStatus;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.captcha.CaptchaGenerator;
import tech.tongyu.bct.auth.captcha.CaptchaValidator;
import tech.tongyu.bct.auth.common.user.LoginUser;
import tech.tongyu.bct.auth.config.ExternalConfig;
import tech.tongyu.bct.auth.controller.response.RpcResponseGenerator;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.UserTypeEnum;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.auth.service.impl.TokenServiceImpl;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.common.api.response.JsonRpcResponse;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.HttpUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.SM4Utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static tech.tongyu.bct.acl.utils.TokenConstants.TOKEN;
import static tech.tongyu.bct.acl.utils.TokenConstants.IP;
import static tech.tongyu.bct.auth.AuthConstants.USERNAME;

@RestController
public class AuthController {

    private TokenServiceImpl tokenService;
    private DefaultKaptcha defaultKaptcha;
    private CaptchaValidator captchaValidator;
    private CaptchaGenerator captchaGenerator;
    private UserManager userManager;
    private ExternalConfig externalConfig;

    @Autowired
    public AuthController(
            TokenServiceImpl tokenService
            , DefaultKaptcha defaultKaptcha
            , CaptchaGenerator captchaGenerator
            , CaptchaValidator captchaValidator
            , UserManager userManager,
            ExternalConfig externalConfig){
        this.tokenService = tokenService;
        this.captchaValidator = captchaValidator;
        this.defaultKaptcha = defaultKaptcha;
        this.captchaGenerator = captchaGenerator;
        this.userManager = userManager;
        this.externalConfig = externalConfig;
    }

    @PostMapping(value = RouterConstants.USERS_CHANGE_PASSWORD, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authChangePassword(@RequestBody Map<String, Object> data) {
        try {
            String oldPassword = (String) data.get(AuthConstants.OLD_PASSWORD);
            String newPassword = (String) data.get(AuthConstants.NEW_PASSWORD);
            String username = (String) data.get(USERNAME);
            UserDTO userDTO;
            try {
                SM4Utils sm4Utils = new SM4Utils();
                sm4Utils.setSecretKey(externalConfig.getSecretKey());
                sm4Utils.setIv(externalConfig.getIv());
                oldPassword = sm4Utils.decryptData_ECB(oldPassword);
                newPassword = sm4Utils.decryptData_ECB(newPassword);
                username = sm4Utils.decryptData_ECB(username);
            }catch (Exception e){
                throw new CustomException(ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT.getMessage());
            }
            if (!CommonUtils.checkPasswordLength(newPassword)){
                throw new CustomException(ReturnMessageAndTemplateDef.Errors.PASSWORD_LENGTH_INVALID.getMessage());
            }
            if (!userManager.isUserExists(username)){
                throw new CustomException(ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT.getMessage());
            }
            userDTO = userManager.updateOwnPassword(username, oldPassword, newPassword);
            String successMessage = "密码修改成功";
            return RpcResponseGenerator.getOldResponseEntity(JsonUtils.toJson(new UserStatus(userDTO, null, successMessage, "0")));
        } catch (CustomException e) {
            return RpcResponseGenerator.getErrorResponseEntity(e);
        }
    }

    @PostMapping(value = RouterConstants.USERS_ENCRYPTED_CHANGE_PASSWORD, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authEncryptedChangePassword(@RequestBody Map<String, Object> data) {
        try {
            String oldPassword = (String) data.get(AuthConstants.OLD_PASSWORD);
            String newPassword = (String) data.get(AuthConstants.NEW_PASSWORD);
            String username = (String) data.get(USERNAME);
            if(StringUtils.isEmpty(username) || StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword)){
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT);
            }
            SM4Utils sm4Utils = new SM4Utils();
            sm4Utils.setSecretKey(externalConfig.getSecretKey());
            sm4Utils.setIv(externalConfig.getIv());
            if(StringUtils.isEmpty(sm4Utils.decryptData_CBC(username))
                    || StringUtils.isEmpty(sm4Utils.decryptData_CBC(oldPassword))
                    || StringUtils.isEmpty(sm4Utils.decryptData_CBC(newPassword))){
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT);
            }
            if (!CommonUtils.checkPasswordLength(sm4Utils.decryptData_CBC(newPassword))){
                throw new CustomException(ReturnMessageAndTemplateDef.Errors.PASSWORD_LENGTH_INVALID.getMessage());
            }
            if (!userManager.isUserExists(sm4Utils.decryptData_CBC(username))){
                throw new CustomException(ReturnMessageAndTemplateDef.Errors.USERNAME_OR_PASSWORD_INCORRECT.getMessage());
            }
            UserDTO userDTO = userManager.updateOwnPassword(
                    sm4Utils.decryptData_CBC(username),
                    sm4Utils.decryptData_CBC(oldPassword),
                    sm4Utils.decryptData_CBC(newPassword));
            userDTO.setUsername(sm4Utils.encryptData_CBC(userDTO.getUsername()));
            userDTO.setRoleName(null);

            String successMessage = "密码修改成功";
            return RpcResponseGenerator.getOldResponseEntity(JsonUtils.toJson(new UserStatus(userDTO, null, successMessage, "0")));
        } catch (CustomException e) {
            return RpcResponseGenerator.getErrorResponseEntity(e);
        }
    }

    @PostMapping(value = RouterConstants.USERS_LOGIN, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authLogin(@RequestBody Map<String, Object> data, HttpServletRequest httpServletRequest) {
        try {
            if(Objects.isNull(data))
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.ILLEGAL_REQUEST_BODY);

            SM4Utils sm4Utils = new SM4Utils();
            sm4Utils.setSecretKey(externalConfig.getSecretKey());
            sm4Utils.setIv(externalConfig.getIv());
            LoginUser user = LoginUser.of(data, sm4Utils);
            if (!userManager.isUserExists(user.getUsername())){
                UserDTO updatedUser = new UserDTO();
                updatedUser.setUsername(user.getUsername());
                updatedUser.setExpired(false);
                updatedUser.setLocked(false);
                updatedUser.setRoleName(Lists.newArrayList());
                return RpcResponseGenerator
                        .getOldResponseEntity(Optional.of(new UserStatus(updatedUser,
                                ReturnMessageAndTemplateDef.Errors.USER_LOGIN_FAILED.getMessage(), "0"))
                                .map(JsonUtils::toJson)
                                .orElse(null));
            }
            UserDTO userDto = userManager.getUserByUserName(user.getUsername());
            if (!captchaValidator.validateCaptcha(httpServletRequest, userDto, user.getCaptcha()))
                return RpcResponseGenerator.getErrorResponseEntity(ReturnMessageAndTemplateDef.Errors.CAPTCHA_NOT_VALID);

            String userName = (String) data.get(AuthConstants.USERNAME);
            if(StringUtils.isNotBlank(userName))
                return RpcResponseGenerator.getOldResponseEntity(
                        tokenService.verifyAndGetResponseJson(userDto, user.getPassword(), HttpUtils.getIPAddress(httpServletRequest)).orElse(null));

            return RpcResponseGenerator.getResponseEntity(
                    tokenService.verifyAndGetResponseJson(userDto, user.getPassword(), HttpUtils.getIPAddress(httpServletRequest)).orElse(null));
        } catch (CustomException e){
            return RpcResponseGenerator.getErrorResponseEntity(e);
        }
    }

    @PostMapping(value = RouterConstants.USERS_ENCRYPTED_LOGIN, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authEncryptedLogin(@RequestBody Map<String, Object> data, HttpServletRequest httpServletRequest) {
        try {
            if(Objects.isNull(data))
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.ILLEGAL_REQUEST_BODY);
            SM4Utils sm4Utils = new SM4Utils();
            sm4Utils.setSecretKey(externalConfig.getSecretKey());
            sm4Utils.setIv(externalConfig.getIv());
            LoginUser user = LoginUser.ofEncrypted(data, sm4Utils);
            if (!userManager.isUserExists(user.getUsername())){
                UserDTO updatedUser = new UserDTO();
                updatedUser.setUsername(sm4Utils.encryptData_CBC(user.getUsername()));
                updatedUser.setExpired(false);
                updatedUser.setLocked(false);
                updatedUser.setRoleName(Lists.newArrayList());
                return RpcResponseGenerator
                        .getOldResponseEntity(Optional.of(new UserStatus(updatedUser,
                                ReturnMessageAndTemplateDef.Errors.USER_LOGIN_FAILED.getMessage(), "0"))
                                .map(JsonUtils::toJson)
                                .orElse(null));
            }
            UserDTO userDto = userManager.getUserByUserName(user.getUsername());
            if (!captchaValidator.validateCaptcha(httpServletRequest, userDto, user.getCaptcha()))
                return RpcResponseGenerator.getErrorResponseEntity( ReturnMessageAndTemplateDef.Errors.CAPTCHA_NOT_VALID);

            String userName = (String) data.get(AuthConstants.USERNAME);
            if(StringUtils.isNotBlank(userName))
                return RpcResponseGenerator.getOldResponseEntity(
                        tokenService.verifyAndGetEncryptedResponseJson(userDto, user.getPassword(), HttpUtils.getIPAddress(httpServletRequest)).orElse(null));

            return RpcResponseGenerator.getResponseEntity(
                    tokenService.verifyAndGetEncryptedResponseJson(userDto, user.getPassword(), HttpUtils.getIPAddress(httpServletRequest)).orElse(null));
        } catch (CustomException e){
            return RpcResponseGenerator.getErrorResponseEntity(e);
        }
    }

    @GetMapping(value = RouterConstants.USERS_CAPTCHA)
    public void authCaptchaGet(HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse){
        try {
            //生产验证码字符串并保存到session中
            String createText = defaultKaptcha.createText();
            httpServletRequest.getSession().setAttribute(AuthConstants.CAPTCHA, createText);

            //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
            httpServletResponse.setHeader("Cache-Control", "no-store");
            httpServletResponse.setHeader("Pragma", "no-cache");
            httpServletResponse.setDateHeader("Expires", 0);
            httpServletResponse.setContentType("image/jpeg");
            ServletOutputStream responseOutputStream =
                    httpServletResponse.getOutputStream();
            responseOutputStream.write(captchaGenerator.getCaptchaJpeg(createText));
            responseOutputStream.flush();
            responseOutputStream.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }

    }

    @PostMapping(value = RouterConstants.USERS_TOKEN_LOGIN, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authTokenLogin(@RequestBody Map<String, Object> data, HttpServletRequest httpServletRequest) {
        try {
            if(Objects.isNull(data))
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.ILLEGAL_REQUEST_BODY);

            String token = data.get(TOKEN).toString();

            return RpcResponseGenerator.getOldResponseEntity(tokenService.decodeToken(token, HttpUtils.getIPAddress(httpServletRequest)).orElse(null));
        } catch (CustomException e){
            return RpcResponseGenerator.getErrorResponseEntity(e, String.valueOf(JsonRpcResponse.ErrorCode.TOKEN_INVALID.getCode()));
        }
    }

    @PostMapping(value = RouterConstants.USERS_VERIFY_IP_TOKEN_BOUND,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authVerifyTokenIpBound(@RequestBody Map<String, Object> data, HttpServletRequest httpServletRequest) {
        try {
            if(Objects.isNull(data))
                throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.ILLEGAL_REQUEST_BODY);
            Boolean verifyRes = true;
            String token = data.get(TOKEN).toString();
            String ip = data.get(IP).toString();
            String username = data.get(USERNAME).toString();
            UserDTO userDto = userManager.getUserByUserName(username);
            if (userDto == null){
                verifyRes = false;
            }else if (userDto.getUserType() != UserTypeEnum.SCRIPT){
                verifyRes = tokenService.verifyIpAndTokenBound(ip, token);
            }else {
                verifyRes = true;
            }
            return RpcResponseGenerator.getResponseEntity(verifyRes.toString());
        } catch (CustomException e){
            return RpcResponseGenerator.getErrorResponseEntity(e);
        }
    }
}
