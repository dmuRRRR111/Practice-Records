[toc]

# 单词表

| 单词          | 词性 | 发音                                 | 含义                            |
| ------------- | ---- | ------------------------------------ | ------------------------------- |
| intercept     | 动词 | 英/ˌɪntəˈsept/ 美/ˌɪntərˈsept/       | 拦截                            |
| interceptor   | 名词 | 英/ˌɪntəˈseptə(r)/ 美/ˌɪntərˈseptər/ | 拦截器                          |
| handle        | 动词 | 英/ˈhændl/ 美/ˈhændl/                | 处理                            |
| handler       | 名词 | 英/ˈhændlə(r)/ 美/ˈhændlər/          | 处理器                          |
| preHandle     | 动词 | /priː ˈhændl/                        | 在目标Handler方法之前执行的操作 |
| postHandle    | 动词 | /pəʊst ˈhændl/                       | 在目标Handler方法之后执行的操作 |
| complete      | 动词 | 英/kəmˈpliːt/ 美/kəmˈpliːt/          | 完成                            |
| completion    | 名词 | 英/kəmˈpliːʃn/ 美/kəmˈpliːʃn/        | 完成                            |
| configurer    | 名词 | /kənˈfɪɡərə(r)/                      | 配置者、配置器                  |
| configuration | 名词 | /kənˌfɪɡəˈreɪʃn/                     | 配置                            |
| registry      | 名词 | 英/ˈredʒɪstri/ 美/ˈredʒɪstri/        | 登记处；注册处                  |
| add           | 动词 | 英/æd/ 美/æd/                        | 添加                            |
| path          | 名词 | 英/pɑːθ/ 美/pæθ/                     | 路径                            |
| pattern       | 名词 | 英/ˈpætn/ 美/ˈpætərn                 | 匹配模式                        |
| exclude       | 动词 | 英/ɪkˈskluːd/ 美/ɪkˈskluːd/          | 排除                            |

# Lecture09-特定功能-实验01-拦截器

## 1、概念

### ①拦截器和过滤器解决类似的问题

#### [1]生活中坐地铁的场景

为了提高乘车效率，在乘客进入站台前统一检票：

![./images](./assets/img008-1748570161743-4.png)

<br/>

#### [2]程序中

在程序中，使用拦截器在请求到达具体 handler 方法前，统一执行检测。

![./images](./assets/img009.png)

<br/>

### ②拦截器 VS 过滤器

#### [1]相似点

三要素相同

- 拦截：必须先把请求拦住，才能执行后续操作
- 过滤：拦截器或过滤器存在的意义就是对请求进行统一处理
- 放行：对请求执行了必要操作后，放请求过去，让它访问原本想要访问的资源

<br/>

#### [2]不同点

- 工作平台不同
  - 过滤器工作在 Servlet 容器中
  - 拦截器工作在 SpringMVC 的基础上
- 拦截的范围
  - 过滤器：能够拦截到的最大范围是整个 Web 应用
  - 拦截器：能够拦截到的最大范围是整个 SpringMVC 负责的请求
- IoC 容器支持
  - 过滤器：想得到 IoC 容器需要调用专门的工具方法，是间接的
  - 拦截器：它自己就在 IoC 容器中，所以可以直接从 IoC 容器中装配组件，也就是可以直接得到 IoC 容器的支持

<br/>

#### [3]选择

功能需要如果用 SpringMVC 的拦截器能够实现，就不使用过滤器。

<br/>

## 2、用法：单个拦截器

### ①创建拦截器类

![image-20250530140841930](./assets/image-20250530140841930.png)

```java
package com.atguigu.spring.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 拦截器执行的总体顺序（单个拦截器）第一种情况（目标Controller方法加了@ResponseBody）：
 * preHandle() 方法
 * 目标 Controller 方法
 * SpringMVC 底层调用 HttpMessageConverter 方法把目标 Controller 方法返回值转为 JSON，再写入响应流
 * postHandle() 方法
 * afterCompletion() 方法
 *
 * 拦截器执行的总体顺序（单个拦截器）第二种情况（目标Controller方法没加@ResponseBody）：
 * preHandle() 方法
 * 目标 Controller 方法
 * postHandle() 方法
 * 根据目标 Controller 方法返回的视图名称渲染视图
 * afterCompletion() 方法
 */
@Component
public class Demo01Interceptor implements HandlerInterceptor {

    // preHandle() 方法在目标 Controller 方法之前执行
    // 参数一：原生 request 对象
    // 参数二：原生 response 对象
    // 参数三：目标 Controller 对象
    // 返回值：返回 true 放行；返回 false 不放行，原本要执行的后续代码都不执行了，相当于没有给前端返回响应
    // 所以如果 return false，我们一定要编写代码，自己返回响应
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("Demo01Interceptor preHandle() 方法执行了");
        return true;
    }

	// preHandle() 放行之后，后续执行顺序如下：
    // 1、目标 Controller 方法
    // 2、【★有 @ResponseBody】HttpMessageConverter 负责把目标 Controller 方法返回值转换为 JSON 写入响应流
    // 3、postHandle() 方法执行
    // 4、【★无 @ResponseBody】针对目标 Controller 方法返回的视图名称，渲染视图
    // 5、afterCompletion() 方法执行
    // ※★标记的这两个步骤，满足哪个条件就执行哪一个
    // 参数一：原生 request 对象
    // 参数二：原生 response 对象
    // 参数三：目标 Controller 对象
    // 参数四：ModelAndView 是 SpringMVC 底层常用的对象，里面封装了模型和视图（视图是后端渲染情况下才会用到）
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("Demo01Interceptor postHandle() 方法执行了");
    }

    // 参数一：原生 request 对象
    // 参数二：原生 response 对象
    // 参数三：目标 Controller 对象
    // 参数四：前面代码抛出的异常，“前面代码”包括preHandle()、postHandle()和目标 Controller 方法
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("Demo01Interceptor afterCompletion() 方法执行了");
    }
}
```

<br/>

### ②配置注册拦截器

#### [1]创建配置类

![image-20250530140913266](./assets/image-20250530140913266.png)

<br/>

#### [2]在配置类中注册拦截器

```java
@Configuration
public class DemoConfig implements WebMvcConfigurer {

    @Autowired
    private Demo01Interceptor demo01Interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(demo01Interceptor);
    }
}
```

<br/>

### ③匹配规则

#### [1]精确匹配

```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(demo01Interceptor);

        // 注册其它拦截器
        registry.addInterceptor(demo02Interceptor)
                .addPathPatterns("/demo/private/target03");
    }
```

访问指定的路径：/demo/private/target03（Demo02拦截器执行了）

![image-20250530141606737](./assets/image-20250530141606737.png)

<br/>

访问未指定的路径：/demo/private/target04（Demo02拦截器没执行）

![image-20250530141630272](./assets/image-20250530141630272.png)

<br/>

#### [2]模糊匹配：匹配单层路径

```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(demo01Interceptor);

        // 注册其它拦截器
        registry.addInterceptor(demo02Interceptor)
                .addPathPatterns("/demo/private/*");
    }
```

访问不匹配的路径：/demo/public/target01（Demo02不执行）

![image-20250530141820613](./assets/image-20250530141820613.png)

<br/>

访问匹配的路径：/demo/private/target02（Demo02执行）

![image-20250530141857564](./assets/image-20250530141857564.png)

<br/>

#### [3]模糊匹配：匹配多层路径

```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(demo01Interceptor);

        // 注册其它拦截器
        registry.addInterceptor(demo02Interceptor)
                .addPathPatterns("/demo/**");
    }
```

访问测试：/demo/public/target01

![image-20250530142102429](./assets/image-20250530142102429.png)

<br/>

访问测试：/demo/private/target02

![image-20250530142125425](./assets/image-20250530142125425.png)

<br/>

#### [4]配置不拦截路径

在前面拦截的范围内，通过excludePathPatterns()指定一个特殊情况范围

```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(demo01Interceptor);

        // 注册其它拦截器
        registry.addInterceptor(demo02Interceptor)
                .addPathPatterns("/demo/**")
                .excludePathPatterns("/demo/private/target03");
    }
```

访问测试：/demo/public/target01

![image-20250530142404643](./assets/image-20250530142404643.png)

<br/>

访问测试：/demo/private/target03

![image-20250530142447642](./assets/image-20250530142447642.png)

<br/>

## 3、用法：多个拦截器

### ①默认顺序

当一个请求匹配多个拦截器时，这些拦截器就都会执行：

- 所有匹配的拦截器的preHandle()方法依次执行，顺序和注册顺序一致
- 目标 Controller 方法
- HttpMessageConverter的write()方法（仅在目标 Controller 方法标记@ResponseBody时生效）
- 所有匹配的拦截器的postHandle()方法依次执行，顺序和注册顺序相反
- 渲染视图（仅在目标 Controller 方法没有标记@ResponseBody，需要SpringMVC渲染视图时生效）
- 所有匹配的拦截器的afterCompletion()方法依次执行，顺序和注册顺序相反

<br/>

拦截器相关方法源码位置：

- 源码所在的类：org.springframework.web.servlet.HandlerExecutionChain
- 调用所有拦截器preHandle()的方法：applyPreHandle()
- 调用所有拦截器postHandle()的方法：applyPostHandle()
- 调用所有拦截器afterCompletion()的方法：triggerAfterCompletion()

HttpMessageConverter接口实现类源码位置：

- 实现类全类名：org.springframework.http.converter.AbstractGenericHttpMessageConverter
- 方法名：write()

<br/>

### ②明确指定

在order()方法中指定一个整数：数值越小，优先级越高

- 优先级高的拦截器：外层（先开始，后结束）
- 优先级低的拦截器：内层（后开始，先结束）

```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册其它拦截器
        registry.addInterceptor(demo02Interceptor)
                .addPathPatterns("/demo/**")
                .excludePathPatterns("/demo/private/target03")
                .order(2)
        ;

        // 注册拦截器
        registry.addInterceptor(demo01Interceptor).order(1);
    }
```

<br/>

### ③某个拦截器不放行（了解）

- 外层拦截器preHandle()方法不放行：后续所有操作都不执行了
  - 内层拦截器的所有方法
  - 目标Controller方法
- 内层拦截器preHandle()方法不放行：
  - 对内层拦截器自己的影响：
    - 目标Controller方法不执行了
    - 自己的postHandle()不执行
    - 自己的afterCompletion()不执行
  - 对外层拦截器的映射：
    - 外层的preHandle()方法不受影响（原因：在内层 preHandle() 之前已经执行完）
    - 外层的postHandle()方法没有执行（原因：目标 Controller 方法没有执行）
    - 外层的afterCompletion()方法不受影响
