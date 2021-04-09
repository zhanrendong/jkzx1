package tech.tongyu.bct.common.bo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email")
public  class MailConfigBO {

    private String fileDir;//附件保存目录
    private String emailAddress;
    private String emailPassword;
    private String emailServerHost;
    private String emailServerPort;

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailServerHost() {
        return emailServerHost;
    }

    public void setEmailServerHost(String emailServerHost) {
        this.emailServerHost = emailServerHost;
    }

    public String getEmailServerPort() {
        return emailServerPort;
    }

    public void setEmailServerPort(String emailServerPort) {
        this.emailServerPort = emailServerPort;
    }
}
