package cn.fxbin.bubble.plugin.satoken.util;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.SaJwtUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.fxbin.bubble.core.util.CollectionUtils;
import cn.fxbin.bubble.core.util.StringUtils;
import cn.fxbin.bubble.plugin.satoken.model.Tokens;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TokenUtils
 * Token 工具类
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 15:55
 */
@Slf4j
@UtilityClass
public class TokenUtils {

    /**
     * 登出
     *
     * @param userId 用户ID
     */
    public void logout(String userId) {
        StpUtil.logout(userId);
    }

    /**
     * 踢人下线
     *
     * @param userId 用户ID
     */
    public void kickout(String userId) {
        StpUtil.kickout(userId);
    }

    /**
     * 强制注销
     *
     * @param userId 用户ID
     */
    public void forceLogout(String userId) {
        StpUtil.logout(userId);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public String getCurrentUserId() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public String getCurrentUsername() {
        return (String) StpUtil.getExtra("username");
    }

    /**
     * 获取当前登录用户角色列表
     *
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getCurrentRoles() {
        return (List<String>) StpUtil.getExtra("roles");
    }

    /**
     * 获取当前登录用户权限列表
     *
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getCurrentPermissions() {
        return (List<String>) StpUtil.getExtra("permissions");
    }



    public Tokens getTokens(String userId) {
        return getTokens(userId, null, null, null);
    }


    /**
     * 获取当前登录用户 Token 信息
     *
     * @return Token 信息
     */
    public Tokens getTokens(String userId, String username, List<String> roles, List<String> permissions) {

        // 构建 JWT 数据
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);

        if (StringUtils.isNotBlank(username)) {
             payload.put("username", username);
        }

        if (CollectionUtils.isNotEmpty(roles)) {
            payload.put("roles", roles);
        }

        if (CollectionUtils.isNotEmpty(permissions)) {
            payload.put("permissions", permissions);
        }

        StpUtil.login(userId, SaLoginParameter.create().setExtraData(payload));

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String refreshToken = SaTempUtil.createToken(userId, 3600 * 24 * 30);


        // 构建 Token 信息
        return Tokens.builder()
                .accessToken(tokenInfo.getTokenValue())
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 创建 JWT Token
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param roles       角色列表
     * @param permissions 权限列表
     * @return JWT Token
     */
    public String createJwtToken(String userId, String username, List<String> roles, List<String> permissions) {
        // 获取 JWT 秘钥
        String jwtSecretKey = SaManager.getConfig().getJwtSecretKey();

        // 构建 JWT 数据
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("roles", roles);
        payload.put("permissions", permissions);

        // 创建 JWT Token
        return SaJwtUtil.createToken(payload, jwtSecretKey);
    }

    /**
     * 检查 Token 是否有效
     *
     * @param token Token
     * @return 是否有效
     */
    public boolean isValidToken(String token) {
        try {
            SaTokenDao tokenDao = SaManager.getSaTokenDao();
            // 检查 Token 是否存在于 Redis 中
            String tokenValue = tokenDao.get(StpUtil.getTokenName() + ":" + token);
            return tokenValue != null;
        } catch (Exception e) {
            log.error("检查 Token 是否有效失败", e);
            return false;
        }
    }
}