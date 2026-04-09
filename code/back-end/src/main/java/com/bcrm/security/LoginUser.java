package com.bcrm.security;

import com.bcrm.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * 登录用户身份权限
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * Token
     */
    private String token;

    public LoginUser() {
    }

    public LoginUser(SysUser user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.realName = user.getRealName();
        this.phone = user.getPhone();
        this.avatar = user.getAvatar();
        this.gender = user.getGender();
        this.status = user.getStatus();
    }

    /**
     * 用户实体对象
     */
    private SysUser user;

    /**
     * 获取用户实体
     */
    public SysUser getUser() {
        if (this.user == null) {
            SysUser sysUser = new SysUser();
            sysUser.setId(this.userId);
            sysUser.setUsername(this.username);
            sysUser.setPassword(this.password);
            sysUser.setRealName(this.realName);
            sysUser.setPhone(this.phone);
            sysUser.setAvatar(this.avatar);
            sysUser.setGender(this.gender);
            sysUser.setStatus(this.status);
            this.user = sysUser;
        }
        return this.user;
    }

    /**
     * 设置用户实体
     */
    public void setUser(SysUser user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.password = user.getPassword();
            this.realName = user.getRealName();
            this.phone = user.getPhone();
            this.avatar = user.getAvatar();
            this.gender = user.getGender();
            this.status = user.getStatus();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }
}
