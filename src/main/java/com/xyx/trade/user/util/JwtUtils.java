package com.xyx.trade.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {
    // 密钥，使用更长的Base64编码字符串以满足HS512算法要求（至少512位）
    private static final String BASE64_SECRET = "c2VjcmV0X2tleV9mb3JfY2FtcHVzX3NlY3JldF93aXRoX2Jhc2U2NF9lbmNvZGluZ190aGlzX2lzX2EgbG9uZ2VyIGtleQ==";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(BASE64_SECRET));

    // 令牌有效期（毫秒），设置为7天
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 获取密钥
     * 
     * @return 密钥
     */
    public Key getSecretKey() {
        return SECRET_KEY;
    }

    /**
     * 生成JWT令牌
     * 
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return JWT令牌
     */
    public String generateToken(Long userId, String username, String role) {
        // 当前时间
        Date now = new Date();
        // 过期时间
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        // 生成令牌
        return Jwts.builder()
                // 设置主题（用户ID）
                .setSubject(userId.toString())
                // 设置用户名
                .claim("username", username)
                // 设置用户角色
                .claim("role", role)
                // 设置签发时间
                .setIssuedAt(now)
                // 设置过期时间
                .setExpiration(expiration)
                // 使用HS512算法签名
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                // 压缩
                .compact();
    }

    /**
     * 从JWT令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            // 解析令牌
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 获取主题（用户ID）并转换为Long类型
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            // 如果解析失败，返回null
            return null;
        }
    }

    /**
     * 验证JWT令牌是否有效
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 解析令牌
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            // 解析成功，令牌有效
            return true;
        } catch (Exception e) {
            // 解析失败，令牌无效
            return false;
        }
    }

}
