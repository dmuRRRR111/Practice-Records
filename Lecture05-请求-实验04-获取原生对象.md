[toc]

| 单词     | 词性      | 音标        | 含义                                                         |
| -------- | --------- | ----------- | ------------------------------------------------------------ |
| origin   | n.        | /ˈɒrɪdʒɪn/  | 起源；来源（指事物的起点或源头，如请求的来源地址`Origin`头字段，标识请求来自哪个域名；也可指数据的原始来源） |
| original | adj. / n. | /əˈrɪdʒənl/ | 原始的；原作（形容词指未经过修改的、最初的，如原始请求数据、原始Bean实例；名词指原物或原作） |

# Lecture05-请求-实验04-获取原生对象

## 1、原生 Servlet API

- HttpServletRequest
- HttpServletResponse
- HttpSession
- ServletContext

原生：最原始的、本真的，没有经过任何的加工、包装和处理。

API：直接翻译过来是应用程序接口的意思。对我们来说，提到 API 这个词的时候，通常指的是在某个特定的领域，已经封装好可以直接使用的一套技术体系。很多时候，特定领域的技术规范都是对外暴露一组接口作为这个领域的技术标准，然后又在这个标准下有具体实现。

<br/>

## 2、三个可以直接获取的对象

```java
@ResponseBody
@RequestMapping("/original/api/direct")
public String getOriginalAPIDirect(
        
        // 有需要使用的 Servlet API 直接在形参位置声明即可。
        // 需要使用就写上，不用就不写，开发体验很好，这里给 SpringMVC 点赞
        HttpServletRequest request,
        HttpServletResponse response,
        HttpSession session
) {
    
    System.out.println(request.toString());
    System.out.println(response.toString());
    System.out.println(session.toString());
    
    return "ok";
}
```

<br/>

## 3、获取ServletContext

### ①方法一：通过HttpSession获取

```java
@ResponseBody
@RequestMapping("/original/servlet/context/first/way")
public String originalServletContextFirstWay(HttpSession session) {
    
    // 获取ServletContext对象的方法一：通过HttpSession对象获取
    ServletContext servletContext = session.getServletContext();
    System.out.println(servletContext.toString());
    
    return "ok";
}
```

<br/>

### ②方法二：通过 IoC 容器注入

```java
// 获取ServletContext对象的方法二：从 IOC 容器中直接注入
@Autowired
private ServletContext servletContext;
    
@ResponseBody
@RequestMapping("/original/servlet/context/second/way")
public String originalServletContextSecondWay() {
    
    System.out.println(this.servletContext.toString());
    
    return "target";
}
```

<br/>

## 4、原生对象和 IoC 容器关系

![./images](./assets/img003.png)

<br/>

![img0121](./assets/img0121.png)
