[toc]

| 单词        | 词性    | 音标           | 含义                                                         |
| ----------- | ------- | -------------- | ------------------------------------------------------------ |
| ambiguous   | adj.    | /æmˈbɪɡjuəs/   | 模糊的；歧义的（指请求映射或依赖注入时存在多个匹配项，导致无法确定唯一目标，如Spring MVC中多个方法匹配同一请求路径） |
| invalid     | adj.    | /ɪnˈvælɪd/     | 无效的（指不符合规则或规范，如无效的请求参数、无效的Bean定义等） |
| GET         | n. / v. | /ɡet/          | 获取（HTTP方法之一，用于请求获取资源，参数暴露在URL中，具有幂等性和安全性） |
| POST        | n. / v. | /pəʊst/        | 提交（HTTP方法之一，用于向服务器提交数据，请求体携带数据，常用于创建资源） |
| HEAD        | n. / v. | /hed/          | 头部（HTTP方法之一，类似GET但仅返回响应头，用于获取资源元信息） |
| PUT         | n. / v. | /pʊt/          | 放置（HTTP方法之一，用于更新资源，具有幂等性，通常替换整个资源） |
| DELETE      | n. / v. | /dɪˈliːt/      | 删除（HTTP方法之一，用于请求删除指定资源，具有幂等性）       |
| TRACE       | n. / v. | /treɪs/        | 追踪（HTTP方法之一，用于回显服务器收到的请求，用于诊断）     |
| PATCH       | n. / v. | /pætʃ/         | 补丁（HTTP方法之一，用于部分更新资源，非幂等，仅修改资源的部分属性） |
| OPTIONS     | n.      | /ˈɒpʃnz/       | 选项（HTTP方法之一，用于获取服务器支持的HTTP方法等信息）     |
| consumes    | v.      | /kənˈsjuːmz/   | 消费（Spring MVC中`@RequestMapping`的属性，指定请求可接受的媒体类型，如`consumes = "application/json"`） |
| raw         | adj.    | /rɔː/          | 原始的（指未经处理的数据格式，如HTTP请求体中的原始文本、二进制数据等） |
| unsupported | adj.    | /ˌʌnsəˈpɔːtɪd/ | 不支持的（指服务器不支持的操作或格式，如不支持的HTTP方法、媒体类型等） |
| media       | n.      | /ˈmiːdiə/      | 媒体（`media type`指互联网媒体类型，如`application/json`、`text/html`，用于标识数据格式） |
| produces    | v.      | /prəˈdjuːsɪz/  | 生成（Spring MVC中`@RequestMapping`的属性，指定响应的媒体类型，如`produces = "application/json"`） |

# Lecture03-请求-实验02-请求映射

# 一、请求路径匹配

## 1、精确匹配

明确指定整个路径，不使用任何通配符，在当前整个 IoC 容器范围内，同一个路径不能映射到多个不同方法，哪怕这些方法在不同 Controller 类中——SpringMVC要求在整个 Web 应用范围内，同一个路径不能映射到不同方法上

```java
    @ResponseBody
    @RequestMapping("/aaa/bbb/ccc")
    public String test01() {
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/aaa/bbb/ccc")
    public String test02() {
        return "ok";
    }
```

![image-20250527205153514](./assets/image-20250527205153514.png)

<br/>

往往实际开发时出现上述问题，是因为不同Controller中不同方法映射了同一个地址，所以为了避免这种情况，可以让每一个映射路径都带有当前Controller的特征：

```java
@Controller
public class UserController {
    @RequestMapping("/user/login")
    public String login() {}
    @RequestMapping("/user/register")
    public String register() {}
}

@Controller
public class OrderController {
    @RequestMapping("/order/submit")
    public String submit() {}
    @RequestMapping("/order/save")
    public String save() {}
}
```

<br/>

然后每个方法上 @RequestMapping 中开头重复的部分可以提取到类上：

```java
@Controller
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/login")
    public String login() {}
    @RequestMapping("/register")
    public String register() {}
}

@Controller
@RequestMapping("/order")
public class OrderController {
    @RequestMapping("/submit")
    public String submit() {}
    @RequestMapping("/save")
    public String save() {}
}
```

<br/>

## 2、模糊匹配

> 友情提示：实际开发时，处理请求的具体方法，通常不会使用模糊匹配
>
> 这里我们介绍模糊匹配的规则，是因为后面 SpringMVC 拦截器会用到

### ①路径的层级

路径可以由多个斜杠组成，每一个斜杠代表路径的一层

![image-20250527210054179](./assets/image-20250527210054179.png)

路径后面可以有后缀：

![image-20250527210822616](./assets/image-20250527210822616.png)

<br/>

### ②常用通配符

| 通配符 | 作用                                                         |
| ------ | ------------------------------------------------------------ |
| ?      | 匹配“/”之外的任意一个字符                                    |
| *      | 在一层路径范围内，匹配任意数量的字符，同样不包括“/”          |
| \*\*   | 匹配多层路径，但/**只能作为路径的最后一部分，不能在开头或中间 |
| *.xxx  | 匹配指定后缀                                                 |
| {xxx}  | 匹配路径变量                                                 |

<br/>

### ③匹配举例

#### [1]问号通配符

- 路径模式：/user/?.html

- 匹配路径：/user/a.html、/user/1.html
- 不匹配路径：/user/ab.html、/user/.html

<br/>

#### [2]单个星号

- 路径模式：/user/*/info
- 匹配路径：/user/admin/info、/user/123/info
- 不匹配路径：/user/test/sub/info

<br/>

#### [3]两个星号

- 路径模式：/user/**
- 匹配路径：/user、/user/info、/user/test/sub
- 无效模式：/user/**/info

![image-20250528103907893](./assets/image-20250528103907893.png)

<br/>

#### [4]后缀匹配

- 路径模式：/user/*.json
- 匹配路径：/user/data.json、/user/info.json
- 不匹配路径：/user/data.xml

<br/>

#### [5]路径变量

相对于使用一个星号匹配的情况，路径变量不只能够实现路径匹配，而且还能把和指定变量名匹配的数据获取到

- 路径模式：/user/{id}
- 匹配路径：/user/123、/user/admin

<br/>

### ④优先级规则

当多个路径模式都能匹配同一个 URL 时，SpringMVC 会按照以下规则确定优先级：

1. 精确路径匹配的优先级最高，例如`/user/info`。
2. 其次是带有路径变量的匹配，如`/user/{id}`。
3. 然后是带有单路径段通配符的匹配，像`/user/*`。
4. 最后是带有多层路径通配符的匹配，例如`/user/**`。

<br/>

# 二、请求方式匹配

## 1、复习：请求方式

HTTP是在TCP/IP协议上层（应用层）的协议，定义了前端和后端之间数据传递、请求、响应的格式和规范

HTTP的规范全部体现在HTTP报文中

HTTP报文：

- 请求报文
- 响应报文

![image-20250528111154707](./assets/image-20250528111154707.png)

![image-20250528111224327](./assets/image-20250528111224327.png)

<br/>

## 2、单个

### ①method属性

method属性是数组类型，所以可以设置一个值或多个值

![image-20250528111840149](./assets/image-20250528111840149.png)

<br/>

### ②代码

注意：value指定的请求路径和method指定的请求方式，两个条件必须都满足才能映射成功，二者是“且”的关系

```java
@ResponseBody
// 效果：对于请求路径 /demo02/method/get 必须是 GET 请求才能够映射
@RequestMapping(value = "/demo02/method/get", method = RequestMethod.GET)
public String test06() {
    return "ok get";
}

@ResponseBody
// 效果：对于请求路径 /demo02/method/post 必须是 POST 请求才能够映射
@RequestMapping(value = "/demo02/method/post", method = RequestMethod.POST)
public String test07() {
    return "ok post";
}
```

<br/>

## 3、多个

给 method 属性设置的数组中多个请求方式之间是“或”的关系

例如：下面代码中 test08() 匹配的请求：

- 路径：必须是 /demo02/method/get/or/post
- 请求方式：GET 或 POST

```java
@ResponseBody
@RequestMapping(value = "/demo02/method/get/or/post", method = {RequestMethod.GET, RequestMethod.POST})
public String test08() {
    return "ok get or post";
}
```

<br/>

## 4、405响应状态码

当 SpringMVC 检测到对应的路径映射，但是不满足请求方式映射要求，会返回405

![image-20250528112813865](./assets/image-20250528112813865.png)

<br/>

## 5、进阶注解

### ①列表

- @GetMapping("路径")

  @RequestMapping(value = "路径", method = RequestMethod.GET)


<br/>

- @PostMapping("路径")

  @RequestMapping(value = "路径", method = RequestMethod.POST)


<br/>

- @PutMapping("路径")

  @RequestMapping(value = "路径", method = RequestMethod.PUT)


<br/>

- @DeleteMapping("路径")

  @RequestMapping(value = "路径", method = RequestMethod.DELETE)

<br/>

### ②代码举例

```java
@ResponseBody
@GetMapping("/demo02/method/get/annotation")
public String test09() {
    return "ok @GetMapping";
}
```

<br/>

# 三、请求参数限定[没用]

## 1、举例说明

```java
@RequestMapping(
    value = {"/user/login"} ,
    params={"username","age=18","gender!=1","!height"})
```

1. 必须包含username参数，值是什么无所谓
2. 必须包含age且值必须是18
3. gender不能等于1，其实不写也是不等于1，但是如果gender请求参数值是1那么就不匹配
4. 不能包含height

<br/>

## 2、请求头限定

把params属性换成headers属性，规则一致

<br/>

# 四、内容类型限定

## 1、复习：内容类型

### ①为什么需要有这个概念？

因为Web系统中，数据类型实在太多，HTTP协议需要支持各种不同数据类型

<br/>

### ②查看内容类型的定义

![image-20250528114723945](./assets/image-20250528114723945.png)

```xml
<mime-mapping>
    <!-- 文件扩展名 -->
    <extension>json</extension>
    <!-- 内容类型 -->
    <mime-type>application/json</mime-type>
</mime-mapping>
<mime-mapping>
    <extension>docx</extension>
    <mime-type>application/vnd.openxmlformats-officedocument.wordprocessingml.document</mime-type>
</mime-mapping>
<mime-mapping>
    <extension>html</extension>
    <mime-type>text/html</mime-type>
</mime-mapping>
<mime-mapping>
    <extension>jpg</extension>
    <mime-type>image/jpeg</mime-type>
</mime-mapping>
```

<br/>

### ③描述的目标

通常来说，只有针对请求体、响应体才会涉及到内容类型

请求消息头、响应消息头中Content-Type就是描述请求体、响应体中数据的内容类型

<br/>

## 2、请求体：consumes属性

要求请求体数据的内容类型必须满足consumes的设定

- 满足设定：匹配
- 不满足设定：不匹配

```java
@ResponseBody
@RequestMapping(value = "/demo02/consumes", consumes = "application/json")
public String test11() {
    return "ok consumes";
}
```

![image-20250528140300512](./assets/image-20250528140300512.png)

<br/>

后端报错信息含义是：不支持的媒体类型

![image-20250528140409426](./assets/image-20250528140409426.png)

<br/>

## 3、响应体：produces属性

要求后端返回的响应数据必须符合produces属性的设定

```java
// 实际响应数据和produces限定一致
@ResponseBody
@RequestMapping(value = "/demo02/produces", produces = "text/plain")
public String test12() {
    return "ok produces";
}
```

<br/>

```java
// 实际响应数据和produces限定不一致
@ResponseBody
@RequestMapping(value = "/demo02/produces.html", produces = "application/json")
public String test12() {
    return "<html><body><p>aaa</p></body></html>";
}
```

![image-20250528141311754](./assets/image-20250528141311754.png)

注意：实际响应数据和produces设置不一致时，后端没有报错，但很可能造成前端解析失败，所以需要程序员自己注意

<br/>

# 五、总结

- 路径匹配单层路径：/xxx/*

- 路径匹配多层路径：/xxx/**
- 请求方式匹配：
  - @GetMapping
  - @PostMapping
  - @PutMapping
  - @DeleteMapping
- 请求方式不匹配时，返回405