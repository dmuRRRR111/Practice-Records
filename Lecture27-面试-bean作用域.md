[toc]

| 单词        | 词性      | 音标            | 含义                                                         |
| ----------- | --------- | --------------- | ------------------------------------------------------------ |
| Retention   | n.        | /rɪˈtenʃn/      | 保留（Java注解的元注解`@Retention`，用于指定注解的保留策略，即注解在何时有效） |
| Policy      | n.        | /ˈpɒləsi/       | 策略（`RetentionPolicy`枚举，定义注解保留的范围，如`SOURCE`（源码中）、`CLASS`（类文件中）、`RUNTIME`（运行时）） |
| ElementType | n.        | /ˈelɪmənt taɪp/ | 元素类型（`ElementType`枚举，指定注解可应用的目标元素，如类、方法、字段等） |
| Alias       | n.        | /ˈeɪliəs/       | 别名（为Bean定义替代名称，Spring中通过`@Alias`或XML配置为Bean设置别名，方便引用） |
| singleton   | n. / adj. | /ˈsɪŋɡltən/     | 单例（Spring Bean的作用域之一，指容器中仅存在一个Bean实例，默认作用域） |
| prototype   | n. / adj. | /ˈprəʊtətaɪp/   | 原型（Spring Bean的作用域之一，指每次请求Bean时都创建新的实例） |

# Lecture27-面试-bean作用域

# 一、提出问题

默认情况下，Spring Ioc容器创建bean对象，是单实例的

```java
@Component
public class Demo13Comp {
}
```

测试：

```java
// 根据同一个 bean id，两次调用 getBean() 方法，看看返回的是否是同一个对象
Object demo13Comp01 = ioc.getBean("demo13Comp");
Object demo13Comp02 = ioc.getBean("demo13Comp");

// 返回 true：说明两个变量指向同一个对象
System.out.println("比较两个变量：" + (demo13Comp01 == demo13Comp02));
```

假如我们需要让Spring在创建对象时，是多实例的，怎么操作？

<br/>

学习目标：为什么要学这个？

因为bean作用域和bean生命周期相关，面试时候爱问这个

<br/>

# 二、bean的作用域

使用@Scope注解控制bean是否单例：

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {
    @AliasFor("scopeName")
    String value() default "";

    @AliasFor("value")
    String scopeName() default "";

    ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;
}
```

value属性取值有两个：

- singleton：默认值，代表单实例
- prototype：多实例

<br/>

# 三、bean对象创建时机

## 1、单实例情况

```java
@Component
@Scope("singleton")
public class Demo13Comp {
    
    public Demo13Comp() {
        System.out.println("★★★★★★★★★★★Demo13Comp 类创建了对象★★★★★★★★★★★");
    }
    
}
```

在IoC容器初始化过程中创建对象

![image-20250523170725001](./assets/image-20250523170725001.png)

<br/>

## 2、多实例情况

```java
@Component
@Scope("prototype")
public class Demo13Comp {

    public Demo13Comp() {
        System.out.println("★★★★★★★★★★★Demo13Comp 类创建了对象★★★★★★★★★★★");
    }

}
```

在IoC容器初始化完成之后，可以工作了，此时每一次调用getBean()方法时，才创建bean对象——这种现象被称之为“延迟创建”

![image-20250523170803853](./assets/image-20250523170803853.png)

<br/>

# 四、总结

- Spring IoC容器创建对象，默认是单例的
- 单例情况下：创建对象是在 IoC 容器初始化的过程中
- 通过 @Scope 注解可以把类创建对象设置为多实例的
- 多实例情况下：ioc.getBean() 获取 bean 对象时创建对象，每一次获取，就创建一个对象