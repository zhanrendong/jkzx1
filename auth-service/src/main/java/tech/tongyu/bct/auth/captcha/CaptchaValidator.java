package tech.tongyu.bct.auth.captcha;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.enums.UserTypeEnum;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Component
public class CaptchaValidator {

    @Value("${captcha.enabled:true}")
    private Boolean captchaEnabled;

    @Value("${settings.specialCaptcha:CAPTCHA@tongyu%bct78}")
    private String specialCaptcha;

    public Boolean validateCaptcha(HttpServletRequest request, UserDTO userDto, String captcha){
        Object sysCaptcha = request.getSession().getAttribute(AuthConstants.CAPTCHA);
        request.getSession().removeAttribute(AuthConstants.CAPTCHA);
        return !captchaEnabled // 未激活验证码功能
//                || Objects.isNull(captcha)
//                || Objects.equals(userDto.getUserType(), UserTypeEnum.SCRIPT) // 用户为脚本用户
                || Objects.equals(specialCaptcha, captcha) // 特殊验证码
                || (Objects.equals(sysCaptcha, captcha) && StringUtils.isNotBlank(captcha)); // 验证码验证通过
    }
}
