[toc]

| 单词        | 词性 | 音标               | 含义                                                         |
| ----------- | ---- | ------------------ | ------------------------------------------------------------ |
| FactoryBean | n.   | /ˈfæktri biːn/     | 工厂Bean（Spring中的特殊Bean，实现`FactoryBean`接口的类用于自定义Bean的创建逻辑，`getObject()`方法返回实际的Bean对象） |
| isSingleton | adj. | /ɪz ˈsɪŋɡltən/     | 是单例的（`FactoryBean`接口方法，返回`boolean`值表示该工厂创建的Bean是否为单例，`true`表示单例，`false`表示原型） |
| SqlSession  | n.   | /ˌes kjuː ˈselɪʃn/ | SQL会话（MyBatis中的核心接口，代表与数据库的一次会话，用于执行SQL语句、获取映射器等） |
| invalidate  | v.   | /ɪnˈvælɪdeɪt/      | 使无效（如`SqlSession.invalidate()`方法用于使当前会话失效，清除缓存并关闭会话） |
| Enable      | v.   | /ɪˈneɪbl/          | 启用（Spring中用于开启特定功能的注解前缀，如`@EnableAspectJAutoProxy`开启AOP代理，`@EnableTransactionManagement`开启事务管理） |
| obtain      | v.   | /əbˈteɪn/          | 获取（指获取对象或资源，如`SqlSessionFactory.obtainSqlSession()`用于获取SqlSession实例） |

# Lecture26-面试-FactoryBean

# 一、简介

Spring为整合各种不同的第三方框架，提出了一个统一规范

- 第三方框架（或其它技术），创建对象的过程各有不同
- 所以Spring设计了FactoryBean接口，统一的把创建对象的过程进行了封装

```java
public interface FactoryBean<T> {
    String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";

    @Nullable
    T getObject() throws Exception;

    @Nullable
    Class<?> getObjectType();

    default boolean isSingleton() {
        return true;
    }
}
```

| 方法名          | 作用                                              |
| --------------- | ------------------------------------------------- |
| getObject()     | 返回最终创建完成的对象<br />把这个对象存入IoC容器 |
| getObjectType() | 返回对象的类型                                    |
| isSingleton()   | 对象是否为单例，默认是单例的                      |

<br/>

# 二、代码测试

## 1、创建代表产品的类

```java
@Data
public class BiYaDiCar {

    private String type;
    private String carColor;
    private Double price;

}
```

<br/>

## 2、创建代表工厂的类

```java
// 工厂类需要加入 IoC 容器
// 实现 FactoryBean 接口，泛型使用产品的类型
@Component
public class BiYaDiFactory implements FactoryBean<BiYaDiCar> {

    // Spring 把这个方法的返回值放入 IoC 容器
    @Override
    public BiYaDiCar getObject() throws Exception {

        // 创建 BiYaDiCar 对象
        BiYaDiCar biYaDiCar = new BiYaDiCar();

        biYaDiCar.setCarColor("blue");
        biYaDiCar.setType("秦");
        biYaDiCar.setPrice(100000d);

        // 返回 BiYaDiCar 对象
        return biYaDiCar;
    }

    @Override
    public Class<?> getObjectType() {
        return BiYaDiCar.class;
    }
}
```

<br/>

## 3、测试

查看运行结果发现：根据工厂bean id获取到的反而是产品的对象

理解：Java类如果实现了FactoryBean接口，那么放入IoC容器的就不是这个类本身，而是它getObject()方法返回值，所以getBean()得到的就是不是工厂本身，而是产品对象

```java
// 从 IoC 容器中获取代表工厂的类的 bean
Object product = ioc.getBean("biYaDiFactory");

// 实际运行结果：factory = BiYaDiCar(type=秦, carColor=blue, price=100000.0)
System.out.println("product = " + product);

// 在 bean id 前附加 & 符号，就可以获取到工厂本身的对象
Object factory = ioc.getBean("&biYaDiFactory");
System.out.println("factory = " + factory);
```

<br/>

# 三、整合第三方技术举例

往往整合第三方技术，创建对象的过程会非常复杂（动不动就几百行、上千行代码），不仅工作量巨大，而是学习成本非常高

为了解决这个问题，Spring 封装了 FactoryBean 这个接口把创建对象的繁琐细节屏蔽了，程序员不必关系整合第三方技术相关细节

- 整合Mybatis需要用到SqlSessionFactoryBean
- 工厂生产的产品：SqlSessionFactory对象
- Mybatis会使用SqlSessionFactory对象生产SqlSession对象
- SqlSessionFactoryBean负责Mybatis和Spring的整合

```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {
    public SqlSessionFactory getObject() throws Exception {
        if (this.sqlSessionFactory == null) {
            this.afterPropertiesSet();
        }

        return this.sqlSessionFactory;
    }

    public Class<? extends SqlSessionFactory> getObjectType() {
        return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
    }
}
```

<br/>

# 四、彩蛋：Lombok故障解决

## 1、方案一：配置注解处理器

![image-20250524111544263](./assets/image-20250524111544263.png)

<br/>

## 2、方案二：IDEA 清空缓存重启

![image-20250524111610547](./assets/image-20250524111610547.png)

![image-20250524111634603](./assets/image-20250524111634603.png)