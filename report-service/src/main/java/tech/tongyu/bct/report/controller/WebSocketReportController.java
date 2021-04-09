package tech.tongyu.bct.report.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketReportController {

    private Logger logger = LoggerFactory.getLogger(WebSocketReportController.class);

    @MessageMapping("/message")
    public String sendMessage(String message){
        logger.debug(String.format("发送消息为:%s,时间:%s", message, LocalDateTime.now().toString()));
        return message;
    }
}
