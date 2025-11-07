[toc]

| 单词          | 词性 | 音标             | 含义                                                         |
| ------------- | ---- | ---------------- | ------------------------------------------------------------ |
| Parameter     | n.   | /pəˈræmɪtə(r)/   | 参数（指方法参数或请求参数，Spring MVC中用于接收请求数据，如`@RequestParam`绑定的请求参数） |
| required      | adj. | /rɪˈkwaɪəd/      | 必需的（参数属性，如`@RequestParam(required = true)`表示该参数为必传，缺失则报错） |
| multi         | adj. | /ˈmʌlti/         | 多个的（指多值参数，如接收多个相同名称的请求参数，通常映射为数组或集合） |
| Path Variable | -    | /pɑːθ ˈveəriəbl/ | 路径变量（Spring MVC中`@PathVariable`注解，用于绑定URL路径中的占位符到方法参数，如`/user/{id}`中的`id`） |
| Entity        | n.   | /ˈentəti/        | 实体（指业务实体对象，通常对应数据库表结构，如`@Entity`标识JPA实体类；也指请求/响应中的数据载体） |

# Lecture04-请求-实验03-接收前端发送的数据

> 总体来说，前端给后端发送数据，有很多种形式：
>
> 【1】请求参数
>
> userName=tom2025&password=123456&userNick=tomPig
>
> 【2】路径变量
>
> - 实际请求路径举例：/user/info/2345
> - 声明路径变量：/user/info/{userId}
>
> 【3】请求体整个是一个 JSON
>
> {"stuId":25, "stuName":"tom}

![image-20250729153055399](./assets/image-20250729153055399.png)

# 一、使用@RequestParam注解

> @RequestParam("请求参数名")
>
> request.getParameter("请求参数名")
>
> request.getParameterValues("请求参数名")

## 1、一名一值

### ①请求路径

```html
/param/one/name/one/value?userName=tom
```

<br/>

### ②@RequestParam注解

#### [1]最基本的用法

```java
@ResponseBody
@RequestMapping("/param/one/name/one/value")
public String oneNameOneValue(
        // 使用@RequestParam注解标记handler方法的形参
        // SpringMVC 会将获取到的请求参数从形参位置给我们传进来
        @RequestParam("userName") String userName
) {
    
    System.out.println("获取到请求参数：" + userName);
    
    return "ok";
}
```

<br/>

#### [2]@RequestParam注解省略的情况

```java
@ResponseBody
@RequestMapping("/param/one/name/one/value")
public String oneNameOneValue(
        // 当请求参数名和形参名一致，可以省略@RequestParam("userName")注解
        // 但是，省略后代码可读性下降而且将来在SpringCloud中不能省略，所以建议还是不要省略
        String userName
) {
    
    System.out.println("★获取到请求参数：" + userName);
    
    return "ok";
}
```

<br/>

#### [3]必须的参数没有提供

![./images](./assets/img008.png)

页面信息说明：

- 响应状态码：400（在 SpringMVC 环境下，400通常和数据注入相关）
- 说明信息：必需的 String 请求参数 'userName' 不存在

原因可以参考 @RequestParam 注解的 required 属性：默认值为true，表示请求参数默认必须提供

```java
	/**
	 * Whether the parameter is required.
	 * <p>Defaults to {@code true}, leading to an exception being thrown
	 * if the parameter is missing in the request. Switch this to
	 * {@code false} if you prefer a {@code null} value if the parameter is
	 * not present in the request.
	 * <p>Alternatively, provide a {@link #defaultValue}, which implicitly
	 * sets this flag to {@code false}.
	 */
	boolean required() default true;
```

<br/>

#### [4]关闭请求参数必需

required 属性设置为 false 表示这个请求参数可有可无：

```java
@RequestParam(value = "userName", required = false)
```

<br/>

#### [5]给请求参数设置默认值

使用 defaultValue 属性给请求参数设置默认值：

```java
@RequestParam(value = "userName", required = false, defaultValue = "missing")
```

此时 required 属性可以继续保持默认值：

```java
@RequestParam(value = "userName", defaultValue = "missing")
```

<br/>

## 2、一名多值

### ①表单

```html
<form action="/param/one/name/multi/value" method="post">
    请选择你最喜欢的球队：
    <input type="checkbox" name="team" value="Brazil"/>巴西
    <input type="checkbox" name="team" value="German"/>德国
    <input type="checkbox" name="team" value="French"/>法国
    <input type="checkbox" name="team" value="Holland"/>荷兰
    <input type="checkbox" name="team" value="Italian"/>意大利
    <input type="checkbox" name="team" value="China"/>中国
    <br/>
    <input type="submit" value="保存"/>
</form>
```

<br/>

### ②handler方法

```java
@ResponseBody
@RequestMapping("/param/one/name/multi/value")
public String oneNameMultiValue(
    
        // 在服务器端 handler 方法中，使用一个能够存储多个数据的容器就能接收一个名字对应的多个值请求参数
        @RequestParam("team") List<String> teamList
        ) {
    
    for (String team : teamList) {
        System.out.println("team = " + team);
    }
    
    return "ok";
}
```

<br/>

# 二、使用实体类对象

## 1、表单对应模型

### ①表单

```html
<form action="/param/form/to/entity" method="post">
    姓名：<input type="text" name="empName"/><br/>
    年龄：<input type="text" name="empAge"/><br/>
    工资：<input type="text" name="empSalary"/><br/>
    <input type="submit" value="保存"/>
</form>
```

<br/>

### ②实体类

要求：请求参数名必须和实体类对象的属性名一致

实体类对象属性名：getXxx()、setXxx()方法定义的

实体类对象如何定义属性名：

- getUserName()方法名去掉get
- UserName
- 剩下的部分首字母小写：userName
- 这个属性名和成员变量名可以不同

```java
public class Employee {
    
    private Integer empId;
    private String empName;
    private int empAge;
    private double empSalary;
    ……
```

<br/>

### ③handler方法

```java
@ResponseBody
@RequestMapping("/param/form/to/entity")
public String formToEntity(
    
        // SpringMVC 会自动调用实体类中的 setXxx() 注入请求参数
        Employee employee) {
    
    System.out.println(employee.toString());
    
    return "ok";
}
```

<br/>

## 2、表单对应实体类包含级联属性[了解]

### ①实体类

```java
public class Student {
    
    private String stuName;
    private School school;
    private List<Subject> subjectList;
    private Subject[] subjectArray;
    private Set<Teacher> teacherSet;
    private Map<String, Double> scores;
    
    public Student() {
        //在各种常用数据类型中，只有Set类型需要提前初始化
        //并且要按照表单将要提交的对象数量进行初始化
        //Set类型使用非常不便，要尽可能避免使用Set
        teacherSet = new HashSet<>();
        teacherSet.add(new Teacher());
        teacherSet.add(new Teacher());
        teacherSet.add(new Teacher());
        teacherSet.add(new Teacher());
        teacherSet.add(new Teacher());
    }
    ……
```

<br/>

```java
public class School {
    
    private String schoolName;
    ……
```

<br/>

```java
public class Subject {
    
    private String subjectName;
    ……
```

<br/>

```java
public class Teacher {
    
    private String teacherName;
    ……
```

<br/>

### ②表单

表单项中的 name 属性值必须严格按照级联对象的属性来设定：

```html
<!-- 提交数据的表单 -->
<form action="/param/cascad" method="post">
    stuName：<input type="text" name="stuName" value="tom"/><br/>
    school.schoolName:<input type="text" name="school.schoolName" value="atguigu"/><br/>
    subjectList[0].subjectName:<input type="text" name="subjectList[0].subjectName" value="java"/><br/>
    subjectList[1].subjectName:<input type="text" name="subjectList[1].subjectName" value="php"/><br/>
    subjectList[2].subjectName:<input type="text" name="subjectList[2].subjectName" value="javascript"/><br/>
    subjectList[3].subjectName:<input type="text" name="subjectList[3].subjectName" value="css"/><br/>
    subjectList[4].subjectName:<input type="text" name="subjectList[4].subjectName" value="vue"/><br/>
    subjectArray[0].subjectName:<input type="text" name="subjectArray[0].subjectName" value="spring"/><br/>
    subjectArray[1].subjectName:<input type="text" name="subjectArray[1].subjectName" value="SpringMVC"/><br/>
    subjectArray[2].subjectName:<input type="text" name="subjectArray[2].subjectName" value="mybatis"/><br/>
    subjectArray[3].subjectName:<input type="text" name="subjectArray[3].subjectName" value="maven"/><br/>
    subjectArray[4].subjectName:<input type="text" name="subjectArray[4].subjectName" value="mysql"/><br/>
    tearcherSet[0].teacherName:<input type="text" name="tearcherSet[0].teacherName" value="t_one"/><br/>
    tearcherSet[1].teacherName:<input type="text" name="tearcherSet[1].teacherName" value="t_two"/><br/>
    tearcherSet[2].teacherName:<input type="text" name="tearcherSet[2].teacherName" value="t_three"/><br/>
    tearcherSet[3].teacherName:<input type="text" name="tearcherSet[3].teacherName" value="t_four"/><br/>
    tearcherSet[4].teacherName:<input type="text" name="tearcherSet[4].teacherName" value="t_five"/><br/>
    scores['Chinese']：input type="text" name="scores['Chinese']" value="100"/><br/>
    scores['English']：<input type="text" name="scores['English']" value="95" /><br/>
    scores['Mathematics']：<input type="text" name="scores['Mathematics']" value="88"/><br/>
    scores['Chemistry']：<input type="text" name="scores['Chemistry']" value="63"/><br/>
    scores['Biology']：<input type="text" name="scores['Biology']" value="44"/><br/>
    <input type="submit" value="保存"/>
</form>
```

<br/>

### ③handler方法

```java
@ResponseBody
@RequestMapping("/param/form/to/nested/entity")
public String formToNestedEntity(
    
        // SpringMVC 自己懂得注入级联属性，只要属性名和对应的getXxx()、setXxx()匹配即可
        Student student) {
    
    System.out.println(student.toString());
    
    return "ok";
}
```

<br/>

# 三、JSON请求体数据

## 1、前端发送数据代码举例

```javascript
const clickHandler04 = async () => {
  let student = {stuId:66, stuName:"justin", stuSalary:25454};
  let {data} = await axiosInstance.post("/api/Servlet03ReceiveJson", student);
  content.value = data.data;
}
```

<br/>

## 2、开发者工具中的显示

![image-20250528151805902](./assets/image-20250528151805902.png)

<br/>

## 3、实体类

![image-20250528152403453](./assets/image-20250528152403453.png)

```java
public class Student {

    private Integer stuId;
    private String stuName;
    private Double stuSalary;
```

<br/>

## 4、@RequestBody注解接收

```java
@ResponseBody
@RequestMapping("/demo03/save/student/json")
public String test04(
        // 实体类对象前加 @RequestBody 对应请求体的 JSON 数据
        // 实体类对象属性和 JSON 格式中的属性一致
        @RequestBody Student student) {
    return "ok " + student;
}
```

<br/>

## 5、测试

SpringMVC框架会自动把JSON转换为实体类对象

![image-20250528152447242](./assets/image-20250528152447242.png)

<br/>

## 6、把Student改成级联对象

```java
public class Student {

    private Integer stuId;
    private String stuName;
    private Double stuSalary;
    
    private School school;

    private List<Subject> subjectList;
```

<br/>

```java
public class School {

    private String schoolName;
    private Integer schoolAge;
```

<br/>

```java
public class Subject {

    private Integer subjectId;
    private String subjectName;
```

<br/>

前端发送的JSON数据：

```json
{
          "stuId": 5,
          "stuName": "aaa",
          "stuSalary": 5555.55,
          "school": {
                    "schoolName": "atguigu",
                    "schoolAge": "13"
          },
          "subjectList": [
                    {
                              "subjectId": 3,
                              "subjectName": "Java"
                    },
                    {
                              "subjectId": 5,
                              "subjectName": "MySQL"
                    }
          ]
}
```

<br/>

后端打印效果：

![image-20250528153538164](./assets/image-20250528153538164.png)

<br/>

# 四、路径变量

## 1、概念

把数据作为路径的一部分，发送给后端

<br/>

## 2、用法

```java
    // /subject/java
    // /subject/php
    // /subject/MySQL
    @RequestMapping("/subject/{lecture}")
    public String testPathVariable(@PathVariable("lecture") String lecture) {
        return "Path Variable:" + lecture;
    }

    // 1、路径中可以包含多个路径变量
    // 2、使用 IDEA 自动创建 @PathVariable 代码时，默认都是 String 类型，需要其它类型就自己修改一下
    // 3、{变量名}如果和接收路径变量的形参名一致时，@PathVariable 注解中可以省略名称
    @RequestMapping("/book/{bookName}/{author}/{price}")
    public String testPathVariableMulti(
            @PathVariable String author,
            @PathVariable String bookName,
            @PathVariable Double price) {
        return "bookName = " + bookName + " author = " + author + " price = " + price;
    }
```

<br/>

# 五、HttpEntity[炫技]

> 定位：
>
> - 开发的时候，如果要接收前端发送的数据，不需要使用HttpEntity
> - HttpEntity是SpringMVC内部针对HTTP报文封装的实体

在SpringMVC里，`HttpEntity`是个相当重要的类，其主要功能是封装HTTP请求或者响应中的实体内容与头部信息。该类属于Spring框架，包路径为`org.springframework.http`。下面为你详细介绍它的关键特性和常见用途。

## 1、核心特性
1. **构造方法**
   
   - `HttpEntity(T body)`：此构造方法仅封装请求或响应的主体内容。
   - `HttpEntity(MultiValueMap<String, String> headers)`：只包含HTTP头部信息。
   - `HttpEntity(T body, MultiValueMap<String, String> headers)`：能同时封装主体内容和头部信息。
   
2. **泛型支持**
   
   - 借助泛型，`HttpEntity`可以处理任意类型的主体内容。比如：
     ```java
     HttpEntity<String> entity = new HttpEntity<>("Hello World");
     ```
   - 对于复杂对象，它也能自动进行序列化操作，像JSON格式：
     ```java
     User user = new User("John", "Doe");
     HttpEntity<User> entity = new HttpEntity<>(user);
     ```
   
3. **常用方法**
   
   - `getBody()`：用于获取实体中的主体内容。
   - `getHeaders()`：可获取实体的HTTP头部信息。
   - `hasBody()`：判断实体是否包含主体内容。

<br/>

## 2、使用测试

### ①使用HttpEntity封装请求报文信息

```java
@ResponseBody
@RequestMapping("/demo04/receive/http/entity")
public Map<String, Object> test05(HttpEntity<String> httpEntity) {

    String requestBody = httpEntity.getBody();
    HttpHeaders headers = httpEntity.getHeaders();
    List<MediaType> acceptList = headers.getAccept();
    InetSocketAddress host = headers.getHost();
    MediaType contentType = headers.getContentType();

    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("requestBody", requestBody);
    responseMap.put("acceptList", acceptList);
    responseMap.put("host", host.getHostName());
    responseMap.put("contentType", contentType);

    return responseMap;
}
```

<br/>

### ②测试结果

#### [1]请求体设定

![image-20250528155007713](./assets/image-20250528155007713.png)

<br/>

#### [2]请求头设定

![image-20250528155030473](./assets/image-20250528155030473.png)

<br/>

#### [3]最终结果

```json
{
          "requestBody": "aaaaaaaaaaaaaaaaaaaa",
          "host": "localhost",
          "acceptList": [
                    {
                              "type": "*",
                              "subtype": "*",
                              "parameters": {},
                              "qualityValue": 1.0,
                              "charset": null,
                              "wildcardType": true,
                              "wildcardSubtype": true,
                              "concrete": false,
                              "subtypeSuffix": null
                    }
          ],
          "contentType": {
                    "type": "text",
                    "subtype": "plain",
                    "parameters": {
                              "charset": "UTF-8"
                    },
                    "qualityValue": 1.0,
                    "charset": "UTF-8",
                    "wildcardType": false,
                    "wildcardSubtype": false,
                    "concrete": true,
                    "subtypeSuffix": null
          }
}
```

<br/>

# 六、总结

![接收请求数据](./assets/接收请求数据.png)

