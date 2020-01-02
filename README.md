## 序言

本项目是基于SpringBoot+Shiro+JWT技术实现的脚手架，可用于快速搭建项目

## 准备Maven文件

新建一个 Maven 工程，添加相关的 dependencies。

## 分环境打包（Maven动态选择环境）

1. pom.xml中添加profiles模块：
```xml
 <profiles>
    <profile>
        <id>dev</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <profileActive>dev</profileActive>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <profileActive>pro</profileActive>
        </properties>
    </profile>
 </profiles>
```
2. application.ymm文件中添加：
```yml
 spring:
   profiles:
     active: @profileActive@
```

3、 pom.xml文件中添加build模块：
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
            <!-- 打包时过滤配置文件-->
            <excludes>
                <exclude>application*.yml</exclude>
                <exclude>static/**</exclude>
            </excludes>
        </resource>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
            <!--根据profile中的变量profileActive指定对应的配置文件-->
            <includes>
                <include>application.yml</include>
                <include>application-${profileActive}.yml</include>
            </includes>
        </resource>
    </resources>
 </build>
```

4、 Terminal控制台输入maven打包命令：
- 选择dev环境（默认）：
```text
mvn clean package -DskipTest
```
- 选择prod环境：
```text
mvn clean package -DskipTest -Pprod
```

## 程序逻辑

1. 我们 POST 用户名与密码到 `/login` 进行登入，如果成功返回一个加密 token，失败的话直接返回 401 错误。
2. 之后用户访问每一个需要权限的网址请求必须在 `header` 中添加 `token` 字段，例如 `token: xxxx` ，`xxxx` 为密钥。
3. 后台会进行 `token` 的校验，如果有误会直接返回 401。

## Token加密说明

- 携带了 `username` 信息在 token 中。
- 设定了过期时间。
- 使用用户登入密码对 `token` 进行加密。

## Token校验流程

1. 获得 `token` 中携带的 `username` 信息。
2. 进入数据库搜索这个用户，得到他的密码。
3. 使用用户的密码来检验 `token` 是否正确。

## 配置 JWT

引入JJWT（java Json web token）生成和验证token,并且设置有效时间后过期。

```java
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
```

## 构建URL

**自定义异常**

手动抛出异常 `UnauthorizedException.java`

```java
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String msg) {
        super(msg);
    }

    public UnauthorizedException() {
        super();
    }
}
```

**URL结构**

| URL                 | 作用                      |
| ------------------- | ----------------------- |
| /login              | 登入                      |
| /users              | 所有人都可以访问，但是用户与游客看到的内容不同 |
| /require_auth       | 登入的用户才可以进行访问            |
| /require_role       | admin的角色用户才可以登入         |
| /require_permission | 拥有view和edit权限的用户才可以访问   |

**处理框架异常**

规范restful统一返回的格式，利用@RestControllerAdvice全局处理 `Spring Boot` 的抛出异常。

## 配置 Shiro

大家可以先看下官方的 [Spring-Shiro](http://shiro.apache.org/spring.html) 整合教程，有个初步的了解。不过既然我们用了 `Spring-Boot`，那我们肯定要争取零配置文件。

**实现JWTToken**

`JWTToken` 差不多就是 `Shiro` 用户名密码的载体。因为我们是前后端分离，服务器无需保存用户状态，所以不需要 `RememberMe` 这类功能，我们简单的实现下 `AuthenticationToken` 接口即可。因为 `token` 自己已经包含了用户名等信息，所以这里我就弄了一个字段。如果你喜欢钻研，可以看看官方的 `UsernamePasswordToken` 是如何实现的。

```java
public class JWTToken implements AuthenticationToken {

    private String token;

    public JWTToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
```

**实现Realm**

`realm` 的用于处理用户是否合法的这一块，需要我们自己实现。

在 `doGetAuthenticationInfo()` 中用户可以自定义抛出很多异常，详情见文档。

***重写 Filter***

所有的请求都会先经过 `Filter`，所以我们继承官方的 `BasicHttpAuthenticationFilter` ，并且重写鉴权的方法。

代码的执行流程 `preHandle` -> `isAccessAllowed` -> `isLoginAttempt` -> `executeLogin` 。

**配置Shiro**