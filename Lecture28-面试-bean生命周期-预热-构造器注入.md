[toc]

# Lecture28-面试-bean生命周期-预热-构造器注入

# 一、提出问题

在执行装配的同时，执行某些操作——需要编写代码

但是@Autowired、@Resource注解都只能执行装配，不能编写代码

<br/>

解决方案：

- 使用构造器注入
- 使用setter方法注入

<br/>

# 二、测试构造器注入

## 1、创建组件

![image-20250523154956249](./assets/image-20250523154956249.png)

```java
@Controller
public class BananaController {

    private BananaService bananaService;

    // 这里只创建有参的构造器，故意不提供无参构造器，逼着框架只能通过这个有参构造器创建当前类的对象
    // 然后就是又进一步逼着框架必须传入这里所需要的参数
    // 那么框架到哪去找这个参数类型的对象呢？还是 IoC 容器中
    public BananaController(BananaService bananaService) {
        this.bananaService = bananaService;
    }

    public void doSth() {
        System.out.println("bananaService = " + bananaService);
    }
}
```

<br/>

```java
@Service
public class BananaService {
}
```

<br/>

## 2、语法说明

- 使用构造器注入，不需要写@Autowired或@Resource注解
- 如果需要指定bean的名称，就在构造器参数位置加上

```java
public BananaController(@Qualifier("aaa") BananaService bananaService) {
	this.bananaService = bananaService;
}
```

- 如果额外提供了无参构造器，那么有参构造器将不会被调用，装配操作也不会被执行

```java
@Controller
public class BananaController {

    private BananaService bananaService;

    public BananaController(@Qualifier("aaa") BananaService bananaService) {
        this.bananaService = bananaService;
    }

    public BananaController() {
		// 框架会优先调用无参构造器
    }

    public void doSth() {
        // 此时 bananaService 的值是 null
        System.out.println("bananaService = " + bananaService 的值是 null);
    }
}
```

