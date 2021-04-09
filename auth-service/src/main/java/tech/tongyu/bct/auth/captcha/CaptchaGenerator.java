package tech.tongyu.bct.auth.captcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class CaptchaGenerator {

    private DefaultKaptcha defaultKaptcha;

    public CaptchaGenerator(DefaultKaptcha defaultKaptcha){
        this.defaultKaptcha = defaultKaptcha;
    }

    public byte[] getCaptchaJpeg(String createText){
        try {
            ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
            //使用验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = defaultKaptcha.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
            return jpegOutputStream.toByteArray();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }

}
