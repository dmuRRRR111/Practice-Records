[toc]

| 单词        | 词性 | 音标            | 含义                                                         |
| ----------- | ---- | --------------- | ------------------------------------------------------------ |
| Same        | adj. | /seɪm/          | 相同的（常用于“Same Origin Policy”，指同源策略，限制不同源的文档或脚本交互） |
| Origin      | n.   | /ˈɒrɪdʒɪn/      | 源（指URL的协议、域名、端口组合，同源即这三者完全一致；`Origin`请求头标识请求来源） |
| Policy      | n.   | /ˈpɒləsi/       | 策略（如“Same Origin Policy”即同源策略，是浏览器的安全机制；也指配置策略） |
| Cross       | adj. | /krɒs/          | 跨域的（指不同源的交互，如“Cross-Origin”即跨域，常涉及跨域资源共享问题） |
| resource    | n.   | /ˈriːsɔːs/      | 资源（指网络中的文件、数据等，如“Cross-Origin Resource Sharing”中的资源） |
| sharing     | n.   | /ˈʃeərɪŋ/       | 共享（“CORS”即“Cross-Origin Resource Sharing”，指跨域资源共享机制，允许跨域请求） |
| Allow       | v.   | /əˈlaʊ/         | 允许（CORS响应头`Access-Control-Allow-*`用于指定允许的跨域操作，如允许的源、方法等） |
| Credentials | n.   | /krəˈdenʃlz/    | 凭据（指Cookie、HTTP认证信息等；CORS中`withCredentials`控制是否发送凭据，需服务端允许） |
| registry    | n.   | /ˈredʒɪstri/    | 注册中心（存储和管理注册信息的组件，如服务注册中心、Bean定义注册中心等） |
| Configurer  | n.   | /kənˈfɪɡərə(r)/ | 配置器（用于配置组件的类，如Spring中的`WebMvcConfigurer`用于配置MVC相关功能） |

> 总目标：单表增删改查

# 一、物理建模

继续沿用之前案例的数据库和表：[传送门](https://gitee.com/heavy_code_industry/note-new-epoch-2025/blob/feature_ai/Part04-Web-BackEnd/Lecture27-Web-%E6%A1%88%E4%BE%8B.md)

<br/>

# 二、后端工程

## 1、创建工程

### ①pom.xml

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.12</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>

    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.30</version>
    </dependency>
</dependencies>
```

<br/>

### ②主启动类

![image-20250529112047149](./assets/image-20250529112047149.png)

```java
@SpringBootApplication
public class Module33DemoMainType {

    public static void main(String[] args) {
        SpringApplication.run(Module33DemoMainType.class, args);
    }

}
```

<br/>

### ③创建主配置文件

![image-20250529112918327](./assets/image-20250529112918327.png)

```properties
spring.application.name=module33-spring-mvc-demo
spring.datasource.username=atguigu
spring.datasource.password=atguigu
spring.datasource.url=jdbc:mysql://192.168.100.100/db_demo_crud
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

<br/>

### ④逻辑建模

![image-20250529112236730](./assets/image-20250529112236730.png)

```java
public class Tiger {

    private Integer tigerId;
    private String tigerName;
    private Integer tigerAge;
    private Double tigerSalary;
```

<br/>

### ⑤创建组件

![image-20250529112722522](./assets/image-20250529112722522.png)

<br/>

### ⑥加入Result类

继续沿用之前案例的 Result 类：[传送门]([Part05-Web-FrontEnd/Lecture23-综合-案例.md · 封捷/note-new-epoch-2025 - 码云 - 开源中国](https://gitee.com/heavy_code_industry/note-new-epoch-2025/blob/feature_ai/Part05-Web-FrontEnd/Lecture23-综合-案例.md))

<br/>

## 2、测试数据库连接

### ①创建测试类

![image-20250529113149277](./assets/image-20250529113149277.png)

```java
@SpringBootTest
public class DemoTest {
}
```

需要注意以下三点：

- 引入spring-boot-starter-test依赖
- 测试类所在的包，必须是主启动类所在包或子包
- 测试类上标记@SpringBootTest注解

使用过程中，需要哪个bean，自动装配进来即可

<br/>

### ②测试数据库连接

```java
@SpringBootTest
public class DemoTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void test01Conn() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println("connection = " + connection);
    }

}
```

<br/>

## 3、各个功能URL地址设置

| 功能                         | URL地址                                           | 请求方式 |
| ---------------------------- | ------------------------------------------------- | -------- |
| 查询列表                     | /tiger/list                                       | GET      |
| 删除记录                     | /tiger/{tigerId}                                  | DELETE   |
| 新增记录（通过请求参数接收） | /tiger?tigerName=xxx&tigerAge=xxx&tigerSalary=xxx | GET      |
| 查询单个对象                 | /tiger/{tigerId}                                  | GET      |
| 更新记录                     | /tiger                                            | PUT      |

>  这里为了练习“通过请求参数方式接收数据”新增记录没有遵循 REST 风格要求

<br/>

## 4、业务：返回数据列表

### ①需求

没有查询条件，不考虑分页，给前端返回全部数据组成的List集合

<br/>

### ②代码

#### [1]Controller方法

```java
    @Autowired
    private TigerService tigerService;

    @GetMapping("/tiger/list")
    public Result<List<Tiger>> showList() {
        try {
            List<Tiger> tigerList = tigerService.getList();
            return Result.ok(tigerList);
        } catch (Exception e) {
            return Result.failed(500, e.getMessage());
        }
    }
```

<br/>

#### [2]Service方法

```java
    @Override
    public List<Tiger> getList() {
        return tigerDao.selectList();
    }
```

<br/>

#### [3]Dao方法

```java
@Override
public List<Tiger> selectList() {
    String sql = "select tiger_id tigerId, tiger_name tigerName, tiger_age tigerAge, tiger_salary tigerSalary from t_tiger";

    return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Tiger.class));
}
```

<br/>

### ③测试

![image-20250529113149277](./assets/image-20250529113149277.png)

```java
    @Autowired
    private TigerService tigerService;

    @Test
    public void test02GetList() {
        List<Tiger> tigerList = tigerService.getList();
        for (Tiger tiger : tigerList) {
            System.out.println("tiger = " + tiger);
        }
    }
```

注：Controller方法通过浏览器测试

<br/>

## 5、业务：删除记录

### ①需求

根据id执行删除

> 在数据库中执行delete from ... SQL语句是物理删除
>
> 实际项目中更多采用逻辑删除：
>
> update xxx set del_status=0 from where id=?

<br/>

### ②代码

#### [1]Controller方法

```java
@DeleteMapping("/tiger/{tigerId}")
public Result<Void> doRemove(@PathVariable("tigerId") Integer tigerId) {
    try {
        tigerService.removeById(tigerId);
        return Result.ok();
    } catch (Exception e) {
        return Result.failed(500, e.getMessage());
    }
}
```

<br/>

#### [2]Service方法

```java
    @Override
    public void removeById(Integer tigerId) {
        tigerDao.deleteById(tigerId);
    }
```

<br/>

#### [3]Dao方法

```java
    @Override
    public void deleteById(Integer tigerId) {
        String sql = "delete from t_tiger where tiger_id=?";
        jdbcTemplate.update(sql, tigerId);
    }
```

<br/>

## 6、业务：新增记录

### ①需求

前端以请求参数的方式发送数据，后端也是基于请求参数来接收

<br/>

### ②代码

#### [1]Controller方法

```java
    @GetMapping("/tiger")
    public Result<Void> saveTiger(Tiger tiger) {

        try {
            tigerService.saveTiger(tiger);

            return Result.ok();
        } catch (Exception e) {
            return Result.failed(DemoConstant.GLOBAL_ERROR_CODE, e.getMessage());
        }
    }
```

<br/>

#### [2]Service方法

```java
@Override
public void saveTiger(Tiger tiger) {
    tigerDao.insertTiger(tiger);
}
```

<br/>

#### [3]Dao方法

```java
@Override
public void insertTiger(Tiger tiger) {
    String sql = """
            insert into t_tiger(tiger_name,
                              tiger_age,
                              tiger_salary) values(?, ?, ?)
            """;
    jdbcTemplate.update(sql, tiger.getTigerName(), tiger.getTigerAge(), tiger.getTigerSalary());
}
```

<br/>

## 7、业务：查询单个对象

### ①需求

根据前端提供的id，查询单个Tiger对象返回给前端<br/>

### ②代码

#### [1]Controller方法

```java
@GetMapping("/tiger/{tigerId}")
public Result<Tiger> getTigerById(@PathVariable("tigerId") String tigerId) {
    try {
        Tiger tiger = tigerService.getTigerById(tigerId);
        return Result.ok(tiger);
    } catch (Exception e) {
        return Result.failed(500, e.getMessage());
    }
}
```

<br/>

#### [2]Service方法

```java
    @Override
    public Tiger getTigerById(String tigerId) {
        return tigerDao.selectTigerById(tigerId);
    }
```

<br/>

#### [3]Dao方法

```java
@Override
public Tiger selectTigerById(String tigerId) {
    String sql = """
            select tiger_id tigerId, tiger_name tigerName, tiger_age tigerAge, tiger_salary tigerSalary 
            from t_tiger
            where tiger_id=?
            """;
    return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Tiger.class), tigerId);
}
```

<br/>

## 8、业务：执行更新

### ①需求

前端提交一个Tiger对象，后端根据这个Tiger对象执行更新

<br/>

### ②代码

#### [1]Controller方法

```java
    @PutMapping("/tiger")
    public Result<Void> doUpdate(@RequestBody Tiger tiger) {
        try {
            tigerService.updateTiger(tiger);
            return Result.ok();
        } catch (Exception e) {
            return Result.failed(500, e.getMessage());
        }
    }
```

<br/>

#### [2]Service方法

```java
    @Override
    public void updateTiger(Tiger tiger) {
        tigerDao.updateTiger(tiger);
    }
```

<br/>

#### [3]Dao方法

```java
    @Override
    public void updateTiger(Tiger tiger) {
        String sql = "update t_tiger set tiger_name=?, tiger_age=?, tiger_salary=? where tiger_id=?";
        jdbcTemplate.update(sql,
                tiger.getTigerName(),
                tiger.getTigerAge(),
                tiger.getTigerSalary(),
                tiger.getTigerId());
    }
```

<br/>



# 三、前端工程

## 1、总体说明

- 基于已有前端工程，调整请求路径和请求方式

<br/>

## 2、返回数据列表

![image-20250913101016600](./assets/image-20250913101016600.png)

<br/>

## 3、删除记录

![image-20250913101051973](./assets/image-20250913101051973.png)

<br/>

## 4、新增记录

![image-20250913101159815](./assets/image-20250913101159815.png)

<br/>

## 5、查询单个对象

![image-20250913101224283](./assets/image-20250913101224283.png)

<br/>

## 6、更新记录

![image-20250913101255785](./assets/image-20250913101255785.png)

<br/>

# 四、跨域

## 1、问题描述

同源策略（Same-Origin Policy）是浏览器的安全机制，用于限制**不同源的文档或脚本**之间的交互，防止跨站请求伪造（CSRF）、跨站脚本攻击（XSS）等安全风险

<br/>

![image-20250831222430404](./assets/image-20250831222430404.png)

<br/>

## 2、前端的解决方案

![image-20250831222708914](./assets/image-20250831222708914.png)

<br/>

## 3、跨域的判定

哪些情况属于跨域？

> 参照当前网页所在的网站（当前网页来源网站），每一个请求具体访问的URL地址是否和当前网站同源

| 情况描述           | 示例                                   |
| ------------------ | -------------------------------------- |
| 协议不同           | `http`与`https`                        |
| 主域名不同         | `www.jd.com` 与 `www.taobao.com`       |
| 子域名不同         | `item.jd.com` 与 `miaosha.jd.com`      |
| 域名相同，端口不同 | `www.jd.com:8080` 与 `www.jd.com:8081` |

<br/>

上述条件满足后，访问同源网站下不同路径是允许的：

> http://localhost:8080/tiger/1
>
> http://localhost:8080/tiger/list

<br/>

## 4、跨域问题的解决办法

目前比较常用的跨域解决方案有三种

### ①JSONP

最早的解决方案，利用script标签解决跨域问题。它有如下限制：

- 需要服务的支持
- 只能发起**GET**请求

<br/>

### ②Nginx反向代理

思路是通过Nginx反向代理服务机制，把跨域请求转换为非跨域请求

缺点是需要借助Nginx，在Nginx中进行配置，语义不清晰

<br/>

### ③CORS

规范化的跨域请求解决方案，安全可靠。

优势：

- 在服务器端进行相关配置，给浏览器返回确认授权
- 支持各种请求方式

缺点：

- 会产生额外的请求

<br/>

## 5、CORS

### ①简介

CORS（Cross-origin resource sharing）：跨域资源共享，是一个W3C标准

它允许浏览器向跨源服务器发出XMLHttpRequest请求，从而克服了Ajax只能同源使用的限制

CORS需要浏览器和服务器同时支持

目前，所有浏览器都支持该功能，IE浏览器不能低于IE10

- 浏览器端：整个CORS通信过程，都是浏览器自动完成，不需要用户参与

- 服务器端：CORS通信与Ajax没有任何差别，因此你不需要改变以前的业务逻辑

  只不过，浏览器会在请求中携带一些头信息，我们需要以此判断是否允许其跨域，然后在响应头中加入一些信息即可

  这一般通过过滤器完成即可

<br/>

### ②底层机制

![image-20250730165506683](./assets/image-20250730165506683.png)

#### [1]预检请求

跨域请求会在正式通信之前，增加一次HTTP查询请求，称为“预检”请求——preflight。

在预检请求中，浏览器询问服务器：当前请求是否在可以正常访问的白名单内，也就是说——**是否可以信任**。以及可以使用哪些HTTP动词和头信息字段。只有得到肯定答复，浏览器才会发出正式的XHR/Fetch请求，否则就报错

预检请求的请求消息头相关细节如下：

| 请求报文条目                                    | 作用和效果说明                                               |
| ----------------------------------------------- | ------------------------------------------------------------ |
| OPTIONS /xxx/xxx HTTP/1.1                       | 请求行中体现了预检请求的**请求方式为OPTIONS**                |
| Origin: http\://localhost:1000                  | 指出当前请求属于哪个域（协议+域名+端口）。&#xA;服务会根据这个值决定是否允许其跨域 |
| Access-Control-Request-Method: GET              | 接下来的正式请求会用到的请求方式，比如GET                    |
| Access-Control-Request-Headers: X-Custom-Header | 会额外用到的头信息                                           |

<br/>

#### [2]预检请求的响应

接收到预检请求后，服务器端会通过返回响应的方式告知浏览器，接下来要发送的正式请求是否被服务器所允许

| 响应报文条目                                        | 作用和效果说明                                               |
| --------------------------------------------------- | ------------------------------------------------------------ |
| HTTP/1.1 200 OK                                     | 响应状态行                                                   |
| Access-Control-Allow-Origin: http\://localhost:1000 | 可接受的域，是一个具体域名或者 \*（代表任意域名）            |
| Access-Control-Allow-Credentials: true              | 是否允许携带cookie，默认情况下，cors不会携带cookie，除非这个值是true |
| Access-Control-Allow-Methods: GET, POST, PUT        | 允许访问的方式                                               |
| Access-Control-Allow-Headers: X-Custom-Header       | 允许携带的头                                                 |
| Access-Control-Max-Age: 1728000                     | 本次许可的有效时长，单位是秒，过期之前的Ajax请求就无需再次进行预检了 |

<br/>

#### [3]开发者工具查看请求响应截图

![](./assets/image_kBI9wei8EG.png)

![](./assets/image_ns-V4f0gwG.png)

<br/>

开发者工具看到预检请求失败情况：

![image-20250530104539010](./assets/image-20250530104539010.png)

<br/>

#### [4]携带Cookie

要想操作cookie，需要满足3个条件：

- 服务的响应头中需要携带Access-Control-Allow-Credentials并且为true
- 浏览器发起ajax需要指定withCredentials 为true
- 响应头中的Access-Control-Allow-Origin一定不能为 \*，必须是指定的域名

<br/>

### ③用法

#### [1]取消Vite代理

```javascript
export default defineConfig({
    plugins: [vue()],
    // server: {
    //     proxy: {
    //         '/api': {
    //             target: 'http://localhost:8080', // 后端API服务器地址
    //             changeOrigin: true, // 允许跨域
    //             rewrite: (path) => path.replace(/^\/api/, '')
    //         }
    //     }
    // }
})
```

<br/>

#### [2]前端路径前缀调整

```javascript
const axiosInstance = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 100000
});
```

<br/>

#### [3]局部使用

在有需要的Controller类上加@CrossOrigin注解：

```java 
/**
 * @author: 小猪佩奇
 *
 */
@Api(tags = "商品属性接口")
@RestController
@RequestMapping("/admin/product")
@CrossOrigin
public class BaseManagerController {

    @Autowired
    private ManagerService managerService;
```

<br/>

#### [4]全局使用

![image-20250530104748692](./assets/image-20250530104748692.png)

```java 
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CrossConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 配置允许跨域的路径
                .allowedOrigins("*")  // 允许跨域的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的请求方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(false)  // 是否允许携带凭证（如 Cookie）
                .maxAge(3600);  // 预检请求的缓存时间（秒）
    }
}
```

<br/>

#### [5]响应式编程环境下配置(仅供参考)

目前没有这个环境，用不了这个配置，作为资料，仅供参考

```java
package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

// 注意导包的问题，用带有reactive的包
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter getCorsWebFilter() {
        // CORS跨域配置对象
        CorsConfiguration configuration = new CorsConfiguration();

        // 设置允许访问的网络
        configuration.addAllowedOrigin("*");

        // 设置是否从服务器获取cookie
        configuration.setAllowCredentials(false);

        // 设置请求方式 * 表示任意
        configuration.addAllowedMethod("*");

        // 允许携带请求头信息 * 表示任意
        configuration.addAllowedHeader("*");

        // 配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);

        // CORS过滤器对象
        return new CorsWebFilter(configurationSource);
    }

}
```

<br/>

# 五、前端代码打包

## 1、前端访问路径前缀

```javascript
const axiosInstance = axios.create({
    baseURL: "/", // 这里设置为后端工程的 ContextPath
    timeout: 100000
});
```

<br/>

## 2、前端工程打包

```bash
npm run build
```

<br/>

构建结果是生成如下文件：

![image-20250530111500328](./assets/image-20250530111500328.png)

<br/>

## 3、把前端页面放入SpringBoot微服务工程

![image-20250530111653165](./assets/image-20250530111653165.png)

<br/>

## 4、访问访问

- 启动SpringBoot工程
- 访问8080端口，此时static目录下的index.html就是默认的欢迎页面

![image-20250530111905071](./assets/image-20250530111905071.png)

<br/>

## 5、代码更改

此时，这种模式运行前端代码就不支持热更新了，前端代码修改必须重新构建，重新复制到后端目录下才能生效
