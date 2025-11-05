# Lecture29-面试-bean生命周期-预热-setter注入

# 一、创建组件

![image-20250523155430571](./assets/image-20250523155430571.png)

```java
@Controller
public class OrangeController {

    private OrangeService orangeService;

    @Autowired
    public void setOrangeService(OrangeService orangeService) {
        this.orangeService = orangeService;
    }

    public void doSth() {
        System.out.println("orangeService = " + orangeService);
    }
}
```

<br/>

```java
@Service
public class OrangeService {
}
```

<br/>

# 二、测试

> orangeService = com.atguigu.spring.demo10.OrangeService@37d00a23