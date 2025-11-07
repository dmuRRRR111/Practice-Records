[toc]

| 单词   | 词性 | 音标       | 含义                                                         |
| ------ | ---- | ---------- | ------------------------------------------------------------ |
| header | n.   | /ˈhedə(r)/ | 头部（HTTP协议中的请求头或响应头，包含元数据信息，如`Content-Type`、`Authorization`等；Spring MVC中可用`@RequestHeader`获取请求头信息） |
| cookie | n.   | /ˈkʊki/    | 小甜饼；Cookie（Web开发中指服务器发送给客户端的小型数据片段，用于存储用户状态信息；Spring MVC中可用`@CookieValue`获取Cookie值） |

# Lecture06-请求-实验05-请求头和Cookie

# 一、获取请求头

## 1、作用

通过这个注解获取请求消息头中的具体数据。

<br/>

## 2、用法

```java
@RequestMapping("/request/header")
public String getRequestHeader(
    
        // 使用 @RequestHeader 注解获取请求消息头信息
        // name 或 value 属性：指定请求消息头名称
        // defaultValue 属性：设置默认值
        @RequestHeader(name = "Accept", defaultValue = "missing") String accept
) {
    
    System.out.println("accept = " +accept);
    
    return "target";
}
```

<br/>

# 二、获取Cookie

## 1、作用

获取当前请求中的 Cookie 数据。

<br/>

## 2、用法

```java
@RequestMapping("/request/cookie")
public String getCookie(
    
        // 使用 @CookieValue 注解获取指定名称的 Cookie 数据
        // name 或 value 属性：指定Cookie 名称
        // defaultValue 属性：设置默认值
        @CookieValue(value = "JSESSIONID", defaultValue = "missing") String cookieValue,
    
        // 形参位置声明 HttpSession 类型的参数即可获取 HttpSession 对象
        HttpSession session
) {
    
    logger.debug("cookieValue = " + cookieValue);
    
    return "target";
}
```

