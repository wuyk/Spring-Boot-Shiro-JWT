package com.demo.util;

import com.alibaba.fastjson.JSONObject;
import com.demo.pojo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class JWTUtil {

    // 过期时间5分钟
    private static final long EXPIRE_TIME = 60*60*1000;
    //加密secret
    private static final String SECRET = "demo";

    /**
     * 生成签名,expireDate后过期
     * @return 加密的token
     */
    public static String generateToken(User user) {
        //过期时间
        Date expireDate = new Date(System.currentTimeMillis()+EXPIRE_TIME);
        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(JSONObject.toJSONString(user))
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 解析出来claim
     * @return
     */
    public static Claims getClaimByToken(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            log.debug("validate is token error ", e);
            return null;
        }
    }

    /**
     * 得到user
     * @param claims
     * @return
     */
    public static User getUser(Claims claims) {
        return JSONObject.parseObject(claims.getSubject(), User.class);
    }

    /**
     * token是否过期
     * @return true：过期
     */
    public static boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }
}
