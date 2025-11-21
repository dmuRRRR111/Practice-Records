[toc]

| 单词 | 音标    | 含义                                                         |
| ---- | ------- | ------------------------------------------------------------ |
| Lua  | /ˈluːə/ | 卢阿（一种轻量级脚本语言，Redis支持通过`EVAL`命令执行Lua脚本，实现复杂原子操作，避免多命令执行的并发问题） |
| call | /kɔːl/  | 调用（Redis Lua脚本中`redis.call()`函数用于调用Redis命令，如`redis.call('GET', 'key')`在脚本中执行GET命令，获取键值） |

# Lecture06-Redis-Lua脚本

# 一、需求背景

## 1、原子性操作

使用Lua脚本可以把多条命令封装到一个脚本中

执行Lua脚本时相当于仅执行一条命令，这样在Redis单线程执行脚本时，就能保证原子性

相比较通过事务实现原子性：

- **Lua 脚本**：适合复杂逻辑、高并发场景，通过一次请求实现强原子性。
- **事务**：适合简单批量操作，通过`WATCH`提供乐观锁能力，但原子性较弱。

<br/>

## 2、性能开销

Lua脚本把多个操作封装到一起，避免执行多条命令时，多次来回往返的网络请求开销，更节约时间

<br/>

## 3、灵活性

有些功能Redis本身没有提供，需要通过自己编写Lua脚本实现

<br/>

# 二、简介

## 1、什么是Lua脚本

Lua 是一个小巧的[脚本语言](http://baike.baidu.com/item/脚本语言)，Lua脚本可以很容易的被C/C++ 代码调用，也可以反过来调用C/C++的函数

Lua并没有提供强大的库，一个完整的Lua解释器不过200k

所以Lua不适合作为开发独立应用程序的语言，而是作为嵌入式脚本语言

很多应用程序、游戏使用LUA作为自己的嵌入式脚本语言，以此来实现可配置性、可扩展性

这其中包括魔兽争霸地图、魔兽世界、博德之门、愤怒的小鸟等众多游戏插件或外挂。

![](./assets/g1e6OrgrfS.png)

<br/>

## 2、Lua脚本的优势

1. **原子性执行**：整个脚本作为单个命令执行，避免并发干扰  
2. **减少网络开销**：将多次往返操作封装为单次请求  
3. **支持复杂逻辑**：内置条件判断、循环和函数调用  
4. **脚本缓存复用**：通过SHA1哈希值重用脚本，降低传输成本  
5. **提高代码可维护性**：集中管理Redis操作逻辑  
6. **原子条件操作**：实现"检查并设置"的原子性逻辑  
7. **降低死锁风险**：减少锁竞争，简化并发控制  
8. **无缝集成Redis**：直接操作Redis数据结构（SET/HASH/ZSET等）  
9. **性能优化**：在服务端执行逻辑，减少客户端计算负担  
10. **简化分布式系统设计**：替代复杂的事务和锁机制

<br/>

# 三、Lua脚本开发

## 1、创建Lua脚本

- 外部调用 Lua 脚本时可以传入：
  - 多个key
  - 多个参数
- KEYS[下标]：从多个key中取下标对应的那一个，下标从1开始
- ARGV[下标]：从多个参数中取下标对应的那一个，下标从1开始
- redis.call()：执行Redis命令

![image-20250613204318160](./assets/image-20250613204318160.png)

```java
local current = redis.call('GET', KEYS[1])
if current == ARGV[1]
then redis.call('SET', KEYS[1], ARGV[2])
    return true
end
return false
```

<br/>

## 2、创建Lua脚本对应的bean

- RedisScript：Lua 脚本封装的 Java 类型
- 泛型：Lua 脚本最终返回值的类型
- 调用RedisScript.of()方法创建RedisScript对象
  - 参数1：加载Lua脚本文件的Resource对象
  - 参数2：Lua脚本返回值类型

![image-20250613210751033](./assets/image-20250613210751033.png)

```java
@Bean
public RedisScript<Boolean> redisScript() {
    ClassPathResource resource = new ClassPathResource("lua/test.lua");
    return RedisScript.of(resource, Boolean.class);
}
```

<br/>

## 3、测试

```java
@SpringBootTest
public class DemoRedisScriptTest {

    @Resource
    private RedisScript<Boolean> redisScript;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Test
    public void test() {
        Boolean executeResult = redisTemplate.execute(redisScript, Arrays.asList("number"), "555", "666");
        System.out.println(executeResult?"已设置新值":"未设置新值");
    }

}
```

