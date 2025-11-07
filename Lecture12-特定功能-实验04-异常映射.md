[toc]

# 单词表

| 单词   | 词性 | 发音      | 含义                        |
| ------ | ---- | --------- | --------------------------- |
| advice | 动词 | /ədˈvaɪs/ | 本意：建议 开发中含义：通知 |

# Lecture12-特定功能-实验04-异常映射

# 一、需求

为了让开发人员在编写业务逻辑时，不必针对每一个请求都自己 try ... catch 处理异常

所以SpringMVC框架提供了一个功能，在全局范围内声明异常映射

把异常类型和处理异常的方法进行映射，捕获到特定类型的异常对象，就执行对应的处理方法，返回响应

<br/>

# 二、实现方案

## 1、后端渲染视图

- 异常映射类：@ControllerAdvice注解
- 处理异常的方法返回值：ModelAndView

<br/>

## 2、后端不渲染视图

- 异常映射类：@RestControllerAdvice注解
- 处理异常的方法返回值：Result.failed()
  也就是说，把处理异常的方法的返回值作为响应体返回给前端

<br/>

## 3、映射异常的注解

标记在方法上，和具体异常类型建立映射关系的注解：@ExceptionHandler

<br/>

## 4、类比

|            | 常规请求映射                       | 异常映射                                     |
| ---------- | ---------------------------------- | -------------------------------------------- |
| 类上注解   | @Controller<br />@RestController   | @ControllerAdvice<br />@RestControllerAdvice |
| 方法上注解 | @RequestMapping                    | @ExceptionHandler                            |
| 映射关系   | 请求 ----映射---> 处理请求的方法上 | 异常 ----映射---> 处理异常的方法上           |
| 处理完成   | 返回响应                           | 返回响应                                     |

<br/>

# 三、代码实现

## 1、创建异常映射类

```java
@RestControllerAdvice
public class MyExceptinHandler {
}
```

<br/>

## 2、声明异常映射方法

- 有精确匹配的异常类型，就采纳精确匹配
- 没有精确匹配的异常类型，就采纳全局配置

```java
@ExceptionHandler(value = ArithmeticException.class)
public Result<Void> resolveArithmeticException(Exception e) {
    return Result.failed(500, e.getClass() + " " + e.getMessage() + " 猪头");
}

@ExceptionHandler(value = Exception.class)
public Result<Void> resolveException(Exception e) {
    return Result.failed(500, e.getClass() + " " + e.getMessage() + " 牛头");
}
```
