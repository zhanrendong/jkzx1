package tech.tongyu.bct.acl.config.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * used for
 */
public class UserDetailsImpl implements UserDetails {

    private final String userName;
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    private final boolean enabled;

    private Date lastPasswordResetDate;

    private static UserDetailsImpl instance;

    static{
        Set<GrantedAuthority> authoritySet = new HashSet<GrantedAuthority>();
        authoritySet.add(new SimpleGrantedAuthority("ROLE_SUPER"));
        instance = new UserDetailsImpl("super","super",authoritySet,true,null);
    }

    public UserDetailsImpl(String userName, String password, Collection<? extends GrantedAuthority> authorities,
                           boolean enabled, Date lastPasswordResetDate) {
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

    public static UserDetailsImpl getSuperUser(){
        return instance;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return userName;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    public Date getLastPasswordResetDate() {
        return lastPasswordResetDate;
    }

    public void setLastPasswordResetDate(Date lastPasswordResetDate) {
        this.lastPasswordResetDate = lastPasswordResetDate;
    }

}
