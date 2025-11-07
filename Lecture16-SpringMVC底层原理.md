[toc]

# 单词表

| 单词       | 词性 | 发音                            | 含义   |
| ---------- | ---- | ------------------------------- | ------ |
| Strategy   | 名词 | 英/ˈstrætədʒi/ 美/ˈstrætədʒi/   | 策略   |
| dispatch   | 动词 | 英/dɪˈspætʃ/ 美/dɪˈspætʃ/       | 分发   |
| dispatcher | 名词 | 英/dɪˈspætʃə(r)/ 美/dɪˈspætʃər/ | 分发器 |
| adapter    | 名词 | 英/əˈdæptə/ 美/əˈdæptər/        | 适配器 |
| Execute    | 动词 | /ˈeksɪkjuːt/                    | 执行   |
| Execution  | 名词 | /ˌeksɪˈkjuːʃn/                  | 执行   |
| Chain      | 名词 | /tʃeɪn/                         | 链条   |

# Lecture16-SpringMVC底层原理

> 面试中出现的频率：中等

# 一、DispatcherServlet

SpringMVC内部的核心组件，负责所有请求的处理的全过程，并且由它的父类执行初始化的过程

<br/>

# 二、初始化过程

## 1、Servlet 生命周期回顾

![img002](./assets/img002.png)

| 生命周期环节 | 调用的方法                                                   | 时机                                      | 次数 |
| ------------ | ------------------------------------------------------------ | ----------------------------------------- | ---- |
| 创建对象     | 无参构造器                                                   | 默认：第一次请求<br />修改：Web应用启动时 | 一次 |
| 初始化       | init(ServletConfig servletConfig)                            | 创建对象后                                | 一次 |
| 处理请求     | service(ServletRequest servletRequest, ServletResponse servletResponse) | 接收到请求后                              | 多次 |
| 清理操作     | destroy()                                                    | Web应用卸载之前                           | 一次 |

在SpringBoot环境下，DispatcherServlet遵循Servlet默认生命周期，在第一次请求时创建对象（延迟加载）

可通过下面配置项修改：

```properties
spring.mvc.servlet.load-on-startup=1
```

- 取值：-1（默认情况，第一次请求时创建对象）
- 取值：0或正数（Web应用启动时创建对象）

<br/>

## 2、初始化操作调用路线图

### ①类和接口之间的关系

![img116](./assets/img116.png)

<br/>

### ②调用线路图

调用线路图所示是方法调用的顺序，但是实际运行的时候本质上都是调用 DispatcherServlet 对象的方法。包括这里涉及到的接口的方法，也不是去调用接口中的『抽象方法』。毕竟抽象方法是没法执行的。抽象方法一定是在某个实现类中有具体实现才能被调用。

而对于最终的实现类：DispatcherServlet 来说，所有父类的方法最后也都是在 DispatcherServlet 对象中被调用的。

![img005](./assets/img005.png)

<br/>

## 3、IoC容器对象创建

org.springframework.web.servlet.FrameworkServlet.initWebApplicationContext()方法：

![image-20250603154250616](./assets/image-20250603154250616.png)

<br/>

## 4、将IoC容器对象存入应用域

org.springframework.web.servlet.FrameworkServlet.initWebApplicationContext()方法：

![image-20250603154338745](./assets/image-20250603154338745.png)

由于上面的操作，IoC容器对象存入了ServletContext域，所以可以通过工具方法：WebApplicationContextUtils.getWebApplicationContext(ServletContext sc)获取IoC容器对象

![image-20250603154550779](./assets/image-20250603154550779.png)

通常用在IoC容器外部想要获取IoC容器对象时

<br/>

## 5、请求映射初始化

### ①调用初始化策略方法

DispatcherServlet类的父类FrameworkServlet负责IoC容器初始化：

![image-20250603152520078](./assets/image-20250603152520078.png)

DispatcherServlet类中，onRefresh()方法在监听到refresh事件时，执行策略初始化：

![image-20250603150332173](./assets/image-20250603150332173.png)

<br/>

### ②请求映射是策略之一

![image-20250603150651359](./assets/image-20250603150651359.png)

<br/>

### ③请求映射数据封装

![image-20250603151030055](./assets/image-20250603151030055.png)

<br/>

### ④handlerMappings类型

```java
private List<HandlerMapping> handlerMappings;
```

<br/>

## 6、小结

整个启动过程我们关心如下要点：

- DispatcherServlet 本质上是一个 Servlet，所以天然的遵循 Servlet 的生命周期。所以宏观上是 Servlet 生命周期来进行调度。
- DispatcherServlet 的父类是 FrameworkServlet。
  - FrameworkServlet 负责框架本身相关的创建和初始化。
  - DispatcherServlet 负责请求处理相关的初始化。
- FrameworkServlet 创建 IoC 容器对象之后会存入应用域。
- FrameworkServlet 完成初始化会调用 IoC 容器的刷新方法。
- 刷新方法完成触发刷新事件，在刷新事件的响应函数中，调用 DispatcherServlet 的初始化方法，初始化所有策略。
- 在 DispatcherServlet 的初始化方法中初始化了请求映射等。

<br/>

# 三、请求处理过程

## 1、前言

### ①核心组件

- HandlerMapping：封装了所有请求映射数据
- HandlerAdapter：Handler适配器，作用是调用目标Controller方法，为此必须准备好所需参数
  - 所需参数情况一：框架提供（HttpServletRequest、HttpEntity……）
  - 所需参数情况二：由前端提供数据，然后经框架解析后传入（请求参数、路径变量、请求体JSON……）

- HandlerExecutionChain：执行链，也就是被拦截器方法包围的目标Controller方法
- HttpMessageConverter：JSON数据和实体类对象之间进行相关转换
  - 请求：请求体 --> JSON字符串 --> @RequestBody --> HttpMessageConverter的read()方法 --> 实体类 --> Controller方法入参实体类对象
  - 响应：Controller方法返回值实体类对象 --> @ResponseBody --> HttpMessageConverter的write()方法 --> JSON字符串 --> 响应体

<br/>

### ②doDispatch()方法

整个请求处理过程都是doDispatch()方法在宏观上协调和调度，把握了这个方法就理解了 SpringMVC 总体上是如何处理请求的。

所在类：**DispatcherServlet**

所在方法：doDispatch()

核心方法中的核心代码：

- ha：HandlerAdapter对象
- handle()方法：调用目标Controller方法，但是这个过程中包括调用拦截器方法
- mv：ModelAndView对象

```Java
// Actually invoke the handler.
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

<br/>

## 2、总体流程

- 目标 handler 方法执行前
  - 建立调用链，确定整个执行流程
  - 拦截器的 preHandle() 方法
  - 数据绑定（注入请求参数或HttpMessageConverter read()）
  - 准备目标 handler 方法所需所有参数
- 调用目标 handler 方法
- 目标 handler 方法执行后
  - HttpMessageConverter write()【看情况】
  - 拦截器的 postHandle() 方法
  - 渲染视图【看情况】
  - 拦截器的 afterCompletion() 方法

<br/>

## 3、具体步骤：建立调用链

静态查看代码：

![image-20250603161636821](./assets/image-20250603161636821.png)

动态查看代码：

![image-20250603161853158](./assets/image-20250603161853158.png)

- handler属性：当前要执行的目标Controller对象
- interceptorList：目前所有拦截器组成的集合
- interceptorIndex：拦截器执行过程中的索引，初始值-1表示一开始在第一个元素前面；每执行一个拦截器就+1

<br/>

## 4、具体步骤：调用preHandle()方法

如果拦截器的preHandle()方法返回false，那么doDispatch()方法就会直接返回，后面代码不执行：

![image-20250603162210166](./assets/image-20250603162210166.png)

<br/>

![image-20250603162342592](./assets/image-20250603162342592.png)

<br/>

## 5、具体步骤：数据绑定

### ①概念

SpringMVC中把下面这些操作成为数据绑定：

- 请求参数注入
- 路径变量注入
- 请求体数据注入

<br/>

### ②代码

![image-20250603164457291](./assets/image-20250603164457291.png)

![image-20250603163051044](./assets/image-20250603163051044.png)

<br/>

## 6、具体步骤：调用目标方法

![image-20250603163342402](./assets/image-20250603163342402.png)

<br/>

## 7、具体步骤：HttpMessageConverter write()

![image-20250603163535223](./assets/image-20250603163535223.png)

<br/>

## 8、具体步骤：调用postHandle()方法

![image-20250603163628075](./assets/image-20250603163628075.png)

<br/>

![image-20250603163715185](./assets/image-20250603163715185.png)

<br/>

## 9、具体步骤：调用afterCompletion()方法

![image-20250603164116430](./assets/image-20250603164116430.png)

![image-20250603164200720](./assets/image-20250603164200720.png)

![image-20250603164251810](./assets/image-20250603164251810.png)

<br/>

## 10、所有断点位置汇总

- SpringBoot版本：3.3.12
- Spring Framework版本：6.1.20
- 所有查看源码的类，都下载源码

![image-20250603165524514](./assets/image-20250603165524514.png)

| 所属类                               | 所属方法                 | 断点位置           |
| ------------------------------------ | ------------------------ | ------------------ |
| AbstractJackson2HttpMessageConverter | writeInternal()          | 方法内任意位置即可 |
| AbstractJackson2HttpMessageConverter | read()                   | 方法内任意位置即可 |
| DispatcherServlet                    | doDispatch()             | 1065               |
| DispatcherServlet                    | doDispatch()             | 1072               |
| DispatcherServlet                    | doDispatch()             | 1084               |
| DispatcherServlet                    | doDispatch()             | 1089               |
| DispatcherServlet                    | doDispatch()             | 1096               |
| DispatcherServlet                    | processDispatchResult()  | 1086               |
| HandlerExecutionChain                | applyPreHandle()         | 方法内任意位置即可 |
| HandlerExecutionChain                | applyPostHandle()        | 方法内任意位置即可 |
| HandlerExecutionChain                | triggerAfterCompletion() | 方法内任意位置即可 |

还有目标Controller方法内任意位置

最终整体断点列表如下：

![image-20250802095409311](./assets/image-20250802095409311.png)

<br/>

![image-20250915161631883](./assets/image-20250915161631883.png)
