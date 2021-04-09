package tech.tongyu.bct.document.dto;

import tech.tongyu.bct.common.api.doc.BctField;

public class EmailConfigInfoDTO {

    @BctField(
            name = "emailAddress",
            description = "邮箱地址",
            type = "String",
            order = 1
    )
    private String emailAddress;
    @BctField(
            name = "emailPassword",
            description = "邮箱密码",
            type = "String",
            order = 2
    )
    private String emailPassword;
    @BctField(
            name = "emailServerHost",
            description = "邮箱IP",
            type = "String",
            order = 3
    )
    private String emailServerHost;
    @BctField(
            name = "emailServerPort",
            description = "端口号",
            type = "String",
            order = 4
    )
    private String emailServerPort;

    public EmailConfigInfoDTO() {
    }

    public EmailConfigInfoDTO(String emailAddress, String emailPassword, String emailServerHost, String emailServerPort) {
        this.emailAddress = emailAddress;
        this.emailPassword = emailPassword;
        this.emailServerHost = emailServerHost;
        this.emailServerPort = emailServerPort;
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
