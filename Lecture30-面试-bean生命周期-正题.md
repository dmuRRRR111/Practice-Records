[toc]

| 单词           | 词性          | 音标               | 含义                                                         |
| -------------- | ------------- | ------------------ | ------------------------------------------------------------ |
| Life Cycle     | n.            | /laɪf ˈsaɪkl/      | 生命周期（指Bean从创建、初始化、使用到销毁的完整过程，Spring容器管理Bean的整个生命周期） |
| Post Construct | -             | /pəʊst kənˈstrʌkt/ | 构造后（`@PostConstruct`注解，标识Bean构造方法执行后调用的初始化方法） |
| Initializing   | v. (现在分词) | /ɪˈnɪʃəlaɪzɪŋ/     | 初始化（`InitializingBean`接口，Bean实现该接口后，`afterPropertiesSet()`方法在属性设置后执行） |
| destroy        | v. / n.       | /dɪˈstrɔɪ/         | 销毁（指Bean生命周期结束时的清理操作，`destroy()`方法用于释放资源） |
| Disposable     | adj.          | /dɪˈspəʊzəbl/      | 可销毁的（`DisposableBean`接口，Bean实现该接口后，`destroy()`方法在容器关闭时被调用） |
| Processor      | n.            | /ˈprəʊsesə(r)/     | 处理器（如`BeanPostProcessor`，用于在Bean初始化前后插入自定义处理逻辑的组件） |

# Lecture30-面试-bean生命周期-正题

# 一、初级形态

## 1、总体概括

- 第一步：创建对象
- 第二步：设置属性，包括自动装配
- 第三步：正式使用

<br/>

## 2、代码测试

### ①被装配的类

```java
@Component
public class Demo19Entity {
}
```

<br/>

### ②观察目标类

```java
@Component
public class DemoLifeCycle {

    private Demo19Entity demo19Entity;

    public DemoLifeCycle() {
        System.out.println("○○○DemoLifeCycle创建了对象○○○");
    }

    @Autowired
    public void setDemo19Entity(Demo19Entity demo19Entity) {
        this.demo19Entity = demo19Entity;
        System.out.println("○○○DemoLifeCycle设置属性了○○○");
    }

}
```

![image-20250726094555671](./assets/image-20250726094555671.png)

<br/>

# 二、生命周期方法

## 1、总体概括

- 第一步：创建对象
- 第二步：设置属性，包括自动装配
- <span style="background-color:yellow;color:blue;font-weight:bolder;">第三步：初始化阶段</span>
  - @PostConstruct指定的初始化方法（public void）
  - InitializingBean接口指定的初始化方法（public void）
  - @Bean指定的初始化方法（public void）
- 第四步：正式使用
- <span style="background-color:yellow;color:blue;font-weight:bolder;">第五步：销毁阶段</span> (IoC容器关闭之前)
  - @PreDestroy指定的销毁方法（public void）
  - DisposableBean接口指定的销毁方法（public void）
  - @Bean指定的销毁方法（public void）

![image-20250909103033494](./assets/image-20250909103033494.png)

<br/>

## 2、代码测试

### ①配置类

```java
@Configuration
public class Demo19Config {

    @Bean(initMethod = "initByBeanAnnotation", destroyMethod = "destroyByBeanAnnotation")
    public DemoLifeCycle getDemoLifeCycle() {
        return new DemoLifeCycle();
    }

}
```

<br/>

### ②观察目标类

```java
public class DemoLifeCycle implements InitializingBean, DisposableBean {

    private Demo19Entity demo19Entity;

    // ------------------初始化阶段代码------------------
    @PostConstruct
    public void initByPostConstruct() {
        System.out.println("○○○DemoLifeCycle初始化了[Post Construct注解]○○○");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("○○○DemoLifeCycle初始化了[InitializingBean接口]○○○");
    }

    public void initByBeanAnnotation() {
        System.out.println("○○○DemoLifeCycle初始化了[Bean Annotation注解]○○○");
    }
    // ------------------销毁阶段代码------------------
    @PreDestroy
    public void destroyByPreDestroy() {
        System.out.println("○○○DemoLifeCycle将要销毁了[Pre Destroy注解]○○○");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("○○○DemoLifeCycle将要销毁了[DisposableBean接口]○○○");
    }

    public void destroyByBeanAnnotation() {
        System.out.println("○○○DemoLifeCycle将要销毁了[Bean Annotation注解]○○○");
    }

    // ------------------设置属性，自动装配代码------------------
    @Autowired
    public void setDemo19Entity(Demo19Entity demo19Entity) {
        this.demo19Entity = demo19Entity;
        System.out.println("○○○DemoLifeCycle设置属性了○○○");
    }

    public DemoLifeCycle() {
        System.out.println("○○○DemoLifeCycle创建了对象○○○");
    }
}	
```

![image-20250726101702713](./assets/image-20250726101702713.png)

<br/>

# 三、外挂

## 1、Bean的后置处理器简介

- 接口全类名：org.springframework.beans.factory.config.BeanPostProcessor

- 前置方法：postProcessBeforeInitialization()

  意思是在初始化操作之前，执行“后置处理”操作

- 后置方法：postProcessAfterInitialization()

  意思是在初始化操作之后，执行“后置处理”操作

- 处理对象：IoC容器中的所有对象

<br/>

## 2、完整版生命周期流程

- 第一步：创建对象
- 第二步：设置属性，包括自动装配
- <span style="background-color:blue;color:yellow;font-weight:bolder;">第三步：Bean后置处理器的before方法</span>
- <span style="background-color:yellow;color:blue;font-weight:bolder;">第四步：初始化阶段</span>
  - @PostConstruct指定的初始化方法
  - InitializingBean指定的初始化方法
  - @Bean指定的初始化方法
- <span style="background-color:blue;color:yellow;font-weight:bolder;">第五步：Bean后置处理器的after方法</span>
- 第六步：正式使用
- <span style="background-color:yellow;color:blue;font-weight:bolder;">第七步：销毁阶段</span>
  - @PreDestroy指定的销毁方法
  - DisposableBean指定的销毁方法
  - @Bean指定的销毁方法

![image-20250909102934691](./assets/image-20250909102934691.png)

<br/>

## 3、代码测试

```java
@Component
public class Demo19BeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("○○○DemoLifeCycle[后置处理器][before init]○○○beanName = " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("○○○DemoLifeCycle[后置处理器][after init]○○○beanName = " + beanName);
        return bean;
    }
}
```

![image-20250726103412685](./assets/image-20250726103412685.png)

<br/>

## 4、重要提醒

Bean 的后置处理器，有以下两大特点：

- 针对 IoC 容器所有 bean 执行后置操作
- 允许对 bean 进行替换操作（偷梁换柱，狸猫换太子）

所以 bean 的后置处理器，有能力、有可能对整个项目有巨大的破坏力！！！

所以使用一定要慎重！！！