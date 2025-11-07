[toc]

| 单词                    | 词性      | 音标                        | 含义                                                         |
| ----------------------- | --------- | --------------------------- | ------------------------------------------------------------ |
| Definition              | n.        | /ˌdefɪˈnɪʃn/                | 定义（`BeanDefinition`的简称，封装Bean的元数据，如类信息、属性、作用域等，是Spring容器创建Bean的依据） |
| singleton               | n. / adj. | /ˈsɪŋɡltən/                 | 单例（指容器中仅存在一个实例的Bean，`singleton`作用域的Bean在容器启动时创建并缓存） |
| singleton Objects       | -         | /ˈsɪŋɡltən ˈɒbdʒekts/       | 单例对象缓存（Spring容器中存储已完全初始化的单例Bean的Map，是Bean的最终缓存容器） |
| early Singleton Objects | -         | /ˈɜːli ˈsɪŋɡltən ˈɒbdʒekts/ | 早期单例对象缓存（存储提前暴露的单例Bean实例，用于解决循环依赖，此时Bean可能尚未完全初始化） |
| singleton Factories     | -         | /ˈsɪŋɡltən ˈfæktriz/        | 单例工厂缓存（存储创建单例Bean的工厂对象，用于在需要时创建Bean并暴露早期引用以解决循环依赖） |
| Registry                | n.        | /ˈredʒɪstri/                | 注册中心（如`BeanDefinitionRegistry`，用于注册和管理`BeanDefinition`的接口） |
| allow                   | v.        | /əˈlaʊ/                     | 允许（指允许某种操作，如允许提前暴露单例引用以解决循环依赖） |
| Reference               | n.        | /ˈrefrəns/                  | 引用（指对象的引用，在循环依赖中通过暴露早期引用实现Bean之间的相互引用） |
| allow Early Reference   | -         | /əˈlaʊ ˈɜːli ˈrefrəns/      | 允许早期引用（Spring容器的配置项，控制是否允许提前暴露单例Bean的早期引用以解决循环依赖，默认为`true`） |

# Lecture31-面试-循环依赖

# 一、提出问题

如果两个bean A和B，在自动装配环节需要对方来完成装配，这就是循环依赖：

![image-20250527084331355](./assets/image-20250527084331355.png)

此时如果双方是基于构造器来注入属性的，那么这就是一个不可能完成的任务：

```java
public A(B b) {}

public B(A a) {}
```

所以要想把循环依赖的两个bean对象都创建出来，而且完成属性注入，那就必须把创建对象和设置属性分成两步来做：

- 调用无参构造器创建对象
- 调用setter或其它方式设置属性

<br/>

# 二、BeanFactory

## 1、总体说明

Spring IoC容器解决循环依赖问题，靠的是BeanFactory中的`三级缓存`机制

<br/>

## 2、BeanFactory中的五个重要属性

![image-20250527145631589](./assets/image-20250527145631589.png)

![image-20250527145657353](./assets/image-20250527145657353.png)

| 属性名                | 类型              | 含义和作用                                               |
| --------------------- | ----------------- | -------------------------------------------------------- |
| beanDefinitionMap     | ConcurrentHashMap | 存放每个bean创建的“图纸”                                 |
| beanDefinitionNames   | ArrayList         | 存放每个bean的id                                         |
| singletonObjects      | ConcurrentHashMap | 存放创建并初始化完成的bean，成品区<br />（一级缓存）     |
| earlySingletonObjects | ConcurrentHashMap | 存放创建好但尚未初始化的bean，半成品区<br />（二级缓存） |
| singletonFactories    | HashMap           | 存放创建bean的工厂，工厂区<br />（三级缓存）             |

<br/>

## 3、执行逻辑推演

假设 A 和 B 循环依赖（A 依赖 B，B 依赖 A），处理流程简化如下：

1. **创建 A 的过程**：
   - 实例化 A（生成空对象，未设置属性）。
   - 将 A 的对象工厂（`ObjectFactory`）放入三级缓存（singletonFactories），工厂的作用是在需要时生成 A 的早期引用。
   - 开始给 A 注入属性，发现依赖 B，于是暂停 A 的创建，转而去创建 B。
2. **创建 B 的过程**：
   - 实例化 B（生成空对象）。
   - 将 B 的对象工厂放入三级缓存（singletonFactories）。
   - 开始给 B 注入属性，发现依赖 A，此时从 A 的三级缓存中通过工厂获取 A 的早期引用（若 A 需要 AOP 代理，工厂会生成代理对象），并将 A 的早期引用从三级缓存移到二级缓存（earlySingletonObjects）。
   - 将 A 的早期引用注入 B 的属性中。
   - B 完成属性注入后，执行初始化方法（如`@PostConstruct`、`InitializingBean`等），成为**完全初始化的成品对象**。
3. **B 成为成品后的处理**：
   - B 完成初始化后，Spring 会将其从二级 / 三级缓存中移除，**放入一级缓存（singletonObjects）**，即 “成品区”。
   - 此时 B 在一级缓存中可用，后续其他对象需要 B 时，直接从一级缓存获取。
4. **A 的后续处理**：
   - B 放入一级缓存后，A 的属性注入（依赖 B）完成，继续执行 A 的初始化方法，成为成品。
   - 最终 A 也会被放入一级缓存，完成整个循环依赖的处理。

### ①没有半成品区

![image-20250527152045601](./assets/image-20250527152045601.png)

<br/>

### ②有半成品

<br/>

![image-20250722174543693](./assets/image-20250722174543693.png)

<br/>

![image-20250722174552003](./assets/image-20250722174552003.png)

<br/>

![image-20250722174558713](./assets/image-20250722174558713.png)

<Br/>

![image-20250722174605203](./assets/image-20250722174605203.png)

<br/>

## 4、源码

### ①警醒

通过实际程序探索源码时，构造一个循环依赖的场景：

- Department 中需要装配 Employee
- Employee 中需要装配 Department

上面两组件都放入 IoC 容器，那么 IoC 容器启动时会报错：

![image-20250728151927248](./assets/image-20250728151927248.png)

从这里我们看出：Spring 其实并不赞成我们使用循环依赖，如果程序中确实存在循环依赖的情况，那么首先要考虑的是重构代码，尽量避开循环依赖，因为此时非常容易出问题。

例如：Department 和 Employee 各自创建 toString() 方法

- Department 中打印 Employee
- Employee 中打印 Department

此时就会造成栈溢出：

![image-20250728152140088](./assets/image-20250728152140088.png)

<br/>

### ②修改配置

为了探索源码的执行流程，或者实际开发时必须使用循环依赖，那么可以打开 Spring 的相关默认配置：

```properties
spring.main.allow-circular-references=true
```

<br/>

### ③源码位置

#### [1]doGetBean()方法

![image-20250728160345699](./assets/image-20250728160345699.png)

- 类：org.springframework.beans.factory.support.AbstractBeanFactory
- 方法：doGetBean()

```java
/**
 * 获取指定名称的Bean实例，支持类型检查、参数传递以及是否仅用于类型检查的标志。
 *
 * @param name            要获取的Bean的名称（可能是别名）
 * @param requiredType    期望返回的Bean类型，如果为null则不进行类型检查
 * @param args            构造Bean时使用的参数数组，若为null表示使用默认构造方式
 * @param typeCheckOnly   是否仅为类型检查而调用该方法，若为true不会标记Bean已创建
 * @return                返回与给定名称和类型匹配的Bean实例
 * @throws BeansException 如果在获取Bean过程中发生异常
 */
protected <T> T doGetBean(String name, @Nullable Class<T> requiredType, @Nullable Object[] args, boolean typeCheckOnly) throws BeansException {
    // 解析原始Bean名称，处理可能存在的别名
    String beanName = this.transformedBeanName(name);
    
    // 尝试从单例缓存中获取共享实例
    // 补充：在getSingleton()方法中，会一次从一级缓存、二级缓存、三级缓存尝试获取对象
    Object sharedInstance = this.getSingleton(beanName);
    Object beanInstance;

    // 如果存在缓存实例且没有传入构造参数，则直接使用缓存
    if (sharedInstance != null && args == null) {
        if (this.logger.isTraceEnabled()) {
            // 根据当前Bean是否正在创建中输出不同日志信息
            if (this.isSingletonCurrentlyInCreation(beanName)) {
                this.logger.trace("Returning eagerly cached instance of singleton bean '" + beanName + "' that is not fully initialized yet - a consequence of a circular reference");
            } else {
                this.logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
            }
        }

        // 对原始Bean实例进行适配处理（如FactoryBean的情况）
        beanInstance = this.getObjectForBeanInstance(sharedInstance, name, beanName, (RootBeanDefinition)null);
    } else {
        // 检查原型Bean是否正在创建中，防止循环依赖问题
        if (this.isPrototypeCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }

        // 获取父级Bean工厂，尝试从父容器中查找Bean定义
        BeanFactory parentBeanFactory = this.getParentBeanFactory();
        if (parentBeanFactory != null && !this.containsBeanDefinition(beanName)) {
            // 如果当前容器没有该Bean定义，并且有父容器，则委托给父容器处理
            String nameToLookup = this.originalBeanName(name);
            if (parentBeanFactory instanceof AbstractBeanFactory) {
                AbstractBeanFactory abf = (AbstractBeanFactory)parentBeanFactory;
                return abf.doGetBean(nameToLookup, requiredType, args, typeCheckOnly);
            }

            // 处理带参数或指定类型的父容器查找逻辑
            if (args != null) {
                return parentBeanFactory.getBean(nameToLookup, args);
            }

            if (requiredType != null) {
                return parentBeanFactory.getBean(nameToLookup, requiredType);
            }

            return parentBeanFactory.getBean(nameToLookup);
        }

        // 如果不是仅做类型检查，则标记该Bean已被创建
        if (!typeCheckOnly) {
            this.markBeanAsCreated(beanName);
        }

        // 启动一个Bean实例化的监控步骤
        StartupStep beanCreation = this.applicationStartup.start("spring.beans.instantiate").tag("beanName", name);

        try {
            // 添加Bean类型标签到监控步骤中
            if (requiredType != null) {
                Objects.requireNonNull(requiredType);
                beanCreation.tag("beanType", requiredType::toString);
            }

            // 获取合并后的Bean定义并验证其有效性
            RootBeanDefinition mbd = this.getMergedLocalBeanDefinition(beanName);
            this.checkMergedBeanDefinition(mbd, beanName, args);

            // 处理depends-on依赖关系
            String[] dependsOn = mbd.getDependsOn();
            String[] prototypeInstance;
            if (dependsOn != null) {
                prototypeInstance = dependsOn;
                int var13 = dependsOn.length;

                for(int var14 = 0; var14 < var13; ++var14) {
                    String dep = prototypeInstance[var14];
                    // 防止循环依赖
                    if (this.isDependent(beanName, dep)) {
                        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                    }

                    // 注册依赖关系并提前初始化依赖Bean
                    this.registerDependentBean(dep, beanName);

                    try {
                        this.getBean(dep);
                    } catch (NoSuchBeanDefinitionException var31) {
                        throw new BeanCreationException(mbd.getResourceDescription(), beanName, "'" + beanName + "' depends on missing bean '" + dep + "'", var31);
                    }
                }
            }

            // 单例模式处理
            if (mbd.isSingleton()) {
                sharedInstance = this.getSingleton(beanName, () -> {
                    try {
                        return this.createBean(beanName, mbd, args);
                    } catch (BeansException var5) {
                        this.destroySingleton(beanName);
                        throw var5;
                    }
                });
                beanInstance = this.getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
            } 
            // 原型模式处理
            else if (mbd.isPrototype()) {
                prototypeInstance = null;

                Object prototypeInstance;
                try {
                    this.beforePrototypeCreation(beanName);
                    prototypeInstance = this.createBean(beanName, mbd, args);
                } finally {
                    this.afterPrototypeCreation(beanName);
                }

                beanInstance = this.getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
            } 
            // 自定义作用域处理
            else {
                String scopeName = mbd.getScope();
                if (!StringUtils.hasLength(scopeName)) {
                    throw new IllegalStateException("No scope name defined for bean '" + beanName + "'");
                }

                Scope scope = (Scope)this.scopes.get(scopeName);
                if (scope == null) {
                    throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                }

                try {
                    Object scopedInstance = scope.get(beanName, () -> {
                        this.beforePrototypeCreation(beanName);

                        Object var4;
                        try {
                            var4 = this.createBean(beanName, mbd, args);
                        } finally {
                            this.afterPrototypeCreation(beanName);
                        }

                        return var4;
                    });
                    beanInstance = this.getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                } catch (IllegalStateException var30) {
                    throw new ScopeNotActiveException(beanName, scopeName, var30);
                }
            }
        } catch (BeansException var32) {
            // 记录异常信息并清理失败状态
            beanCreation.tag("exception", var32.getClass().toString());
            beanCreation.tag("message", String.valueOf(var32.getMessage()));
            this.cleanupAfterBeanCreationFailure(beanName);
            throw var32;
        } finally {
            // 结束Bean实例化监控步骤
            beanCreation.end();
        }
    }

    // 最终对Bean实例进行类型适配后返回
    return this.adaptBeanInstance(name, beanInstance, requiredType);
}
```

<br/>

#### [2]getSingleton()

- 类：org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
- 方法：getSingleton()

```java
/**
 * 获取单例对象实例
 * 
 * @param beanName 单例bean的名称
 * @param allowEarlyReference 是否允许早期引用（提前暴露引用）
 * @return 返回指定名称的单例对象，如果不存在则返回null
 */
@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    // 从一级缓存中获取单例对象
    Object singletonObject = this.singletonObjects.get(beanName);
    
    // 如果一级缓存中没有且该bean正在创建中，则尝试从早期单例对象缓存中获取
    if (singletonObject == null && this.isSingletonCurrentlyInCreation(beanName)) {
        
        // 尝试从二级缓存获取对象
        singletonObject = this.earlySingletonObjects.get(beanName);
        
        // 如果二级缓存中也没有且允许早期引用，则通过工厂创建
        if (singletonObject == null && allowEarlyReference) {
            synchronized(this.singletonObjects) {
                // 双重检查锁定，再次从一级缓存获取
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    // 再次从早期单例对象缓存获取
                    singletonObject = this.earlySingletonObjects.get(beanName);
                    if (singletonObject == null) {
                        // 从三级缓存中获取工厂并创建对象
                        ObjectFactory<?> singletonFactory = (ObjectFactory)this.singletonFactories.get(beanName);
                        if (singletonFactory != null) {
                            singletonObject = singletonFactory.getObject();
                            // 将创建的对象放入二级缓存
                            this.earlySingletonObjects.put(beanName, singletonObject);
                            // 从三级缓存中移除已使用的工厂
                            this.singletonFactories.remove(beanName);
                        }
                    }
                }
            }
        }
    }

    return singletonObject;
}
```

<br/>

### ④双检查锁

- 同步代码块外部：判空检查（把锁外面不符合条件的线程排除，避免不必要的锁竞争浪费性能）
- 同步代码块内部：判空检查（在锁内确保创建的对象是单例的）
  第二个线程拿到锁进入同步代码块，需要先检查是否已经创建了对象，如果已经创建了对象就不能再重复创建，从而保证对象单实例

<br/>

# 三、话术

## 1、问法

- Spring IoC容器中遇到bean与bean之间循环依赖的问题怎么解决？
- Spring IoC容器中，说说你对三级缓存的理解

<br/>

## 2、回答

### ①第一部分：解释什么是循环依赖

首先，bean之间互相引用对方就会形成循环依赖

在这种情况下，既要创建对象，又要完成属性的设置和装配，而且是 A 和 B 两个对象都需要装配对方

Spring中其实并不赞成代码中出现循环依赖的结构，最好是避开这种写法

必须使用的话，需要通过配置打开对应设置的开关

<br/>

### ②第二部分：介绍三级缓存

Spring中解决办法是利用了BeanFactory中三级缓存的机制

- 一级缓存：singletonObjects属性（成品区，存放创建完成且装配完成的对象）
- 二级缓存：earlySingletonObjects属性（半成品区，存放创建完成但尚未装配的对象）
- 三级缓存：singletonFactories属性（工厂区，存放创建bean对象的工厂）

<br/>

### ③第三部分：执行流程

- 先创建 A 对象（空的）
- 需要给 A 对象设置 B 对象
- 需要创建 B 对象
  - 先创建空的 B 对象
  - 需要给 B 对象设置 A 对象
  - 把 A 对象放入半成品（A 工厂从工厂区移除）
  - 把 A 对象注入 B 对象中
  - B 对象创建并且设置完成（B 工厂从工厂区移除）
  - B 对象进入成品区

- 把 B 对象注入 A 对象中
- A 对象创建并且设置完成
- A 对象进入成品区
