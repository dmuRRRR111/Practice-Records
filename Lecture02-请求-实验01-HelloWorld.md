[toc]

| 单词          | 词性 | 音标             | 含义                                                         |
| ------------- | ---- | ---------------- | ------------------------------------------------------------ |
| Response Body | -    | /rɪˈspɒns ˈbɒdi/ | 响应体（Spring MVC中`@ResponseBody`注解，用于标识控制器方法的返回值直接作为HTTP响应体内容，而非视图名，通常用于返回JSON、XML等数据） |

# Lecture02-请求-实验01-HelloWorld

# 一、功能目标

访问程序URL地址，返回响应数据

<br/>

# 二、代码实现

## 1、创建工程

![image-20250527170030803](./assets/image-20250527170030803.png)

若手动创建则导入依赖如下：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
```

其它操作和测试 IoC 容器是一样

<br/>

## 2、创建控制器类

控制器类扮演的角色相当于我们以前的Servlet

![image-20250527170313174](./assets/image-20250527170313174.png)

```java
package com.atguigu.spring.controller;

import org.springframework.stereotype.Controller;

@Controller
public class Demo01HelloWorld {
    
    
    
}
```

<br/>

## 3、创建请求映射方法

![image-20250527170802069](./assets/image-20250527170802069.png)

```java
    @ResponseBody
    @RequestMapping("/demo01/hello/world")
    public String sayHello() {
        return "hello SpringMVC!!!";
    }
```

<br/>

## 4、测试

运行主启动类，看到启动日志：

![image-20250527171027249](./assets/image-20250527171027249.png)

![image-20250527171112663](./assets/image-20250527171112663.png)

![image-20250527171149815](./assets/image-20250527171149815.png)

由于我们没有提供默认的欢迎页，例如：index.html

所以打开localhost:8080报错显示404是正常的

我们自己手动输入地址：

> localhost:8080/demo01/hello/world

![image-20250527171326068](./assets/image-20250527171326068.png)

<br/>

# 三、补充

Web 应用相关配置信息可以在主配置文件中修改：

```properties
# 设置应用名称
spring.application.name=module32-spring-mvc

# 修改 Tomcat 启动时占用的端口号
server.port=15000

# 修改当前 Web 应用的 ContextPath
server.servlet.context-path=/demo
```

