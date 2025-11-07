[toc]

| 单词    | 词性    | 音标       | 含义                                                         |
| ------- | ------- | ---------- | ------------------------------------------------------------ |
| Request | n. / v. | /rɪˈkwest/ | 请求（Web开发中表示客户端向服务器发送的请求，Spring MVC中`@Request`相关注解用于处理请求数据，如`@RequestBody`获取请求体） |
| Mapping | n. / v. | /ˈmæpɪŋ/   | 映射（Spring MVC中`@RequestMapping`注解用于将请求URL与处理方法关联，定义请求路径、方法、参数等映射规则） |
| Param   | n.      | /ˈpærəm/   | 参数（指请求参数，`@RequestParam`注解用于绑定请求参数到方法参数，可指定参数名、是否必需等属性） |

# Lecture01-SpringMVC简介

## 1、SpringMVC 优势

SpringMVC 是 Spring 为表述层开发提供的一整套完备的解决方案。在表述层框架历经 Strust、WebWork、Strust2 等诸多产品的历代更迭之后，目前业界普遍选择了 SpringMVC 作为 Java EE 项目表述层开发的<span style="color:blue;font-weight:bold;">首选方案</span>。之所以能做到这一点，是因为 SpringMVC 具备如下显著优势：

- <span style="color:blue;font-weight:bold;">Spring 家族原生产品</span>，与 IOC 容器等基础设施无缝对接
- 表述层各细分领域需要解决的问题<span style="color:blue;font-weight:bold;">全方位覆盖</span>，提供<span style="color:blue;font-weight:bold;">全面解决方案</span>
- <span style="color:blue;font-weight:bold;">代码清新简洁</span>，大幅度提升开发效率
- 内部组件化程度高，可插拔式组件<span style="color:blue;font-weight:bold;">即插即用</span>，想要什么功能配置相应组件即可
- <span style="color:blue;font-weight:bold;">性能卓著</span>，尤其适合现代大型、超大型互联网项目要求

<br/>

## 2、表述层框架要解决的基本问题

- 请求映射
- 数据输入
- 视图界面
- 请求分发
- 表单回显
- 会话控制
- 过滤拦截
- 异步交互
- 文件上传
- 文件下载
- 数据校验
- 类型转换

<br/>

## 3、SpringMVC 代码对比

### ①基于原生 Servlet API 开发代码片段

- Servlet 无法将请求路径直接映射到方法上，而仅仅只能映射到类上
- API 非常冗长
- 基于编程式实现功能，需要自己编写代码

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {   
    
    String userName = request.getParameter("userName");
    
    System.out.println("userName="+userName);
    
    request.getRequestDispatcher("/pages/result.jsp").forward(request, response);
}
```

<br/>

### ②基于 SpringMVC 开发代码片段

- SpringMVC可以把请求路径直接映射到方法上
- API 非常简洁
- 基于声明式实现功能，大大减少了要编写的代码

```java
@RequestMapping("/user/login")
public String login(@RequestParam("userName") String userName){
    
    System.out.println("userName="+userName);
    
    return "result";
}
```

