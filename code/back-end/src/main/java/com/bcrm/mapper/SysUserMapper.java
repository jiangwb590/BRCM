package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统用户Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询美容师列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE r.role_code = 'beautician' AND u.status = 1 AND u.deleted = 0")
    List<SysUser> selectBeauticians();

    /**
     * 分页查询用户（包含角色信息）
     */
    Page<SysUser> selectUserPage(Page<SysUser> page, @Param("username") String username, 
                                  @Param("realName") String realName, @Param("phone") String phone, 
                                  @Param("status") Integer status);

    /**
     * 根据用户ID查询角色ID
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} LIMIT 1")
    Long selectRoleIdByUserId(@Param("userId") Long userId);
}
