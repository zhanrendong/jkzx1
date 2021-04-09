package tech.tongyu.bct.auth.dao.entity;

import tech.tongyu.bct.auth.dao.common.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(schema = EntityConstants.AUTH_SERVICE,
        name = EntityConstants.IP_TOKEN_BIND,
        indexes = {@Index(name = "ipaddr_index", columnList = "ipaddr")})
public class IpTokenBindDbo extends BaseEntity {

    @Column(unique = true)
    private String ipaddr;

    @Column
    private String token;

    public IpTokenBindDbo() {
    }

    public IpTokenBindDbo(String ipaddr, String token) {
        this.ipaddr = ipaddr;
        this.token = token;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
