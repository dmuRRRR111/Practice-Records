[toc]

| 单词             | 音标                 | 含义                                                         |
| ---------------- | -------------------- | ------------------------------------------------------------ |
| master           | /ˈmɑːstə(r)/         | 主节点（Redis主从架构中的主节点，负责处理写操作并同步数据到从节点，是数据的主要来源，如配置中的`mymaster`指定主节点名称） |
| slave            | /sleɪv/              | 从节点（旧称，现多称为replica，指同步主节点数据的节点，默认只读，分担读压力，主节点故障时可升级为新主节点） |
| replica          | /ˈreplɪkə/           | 副本；从节点（Redis中替代slave的术语，指同步主节点数据的副本节点，配置中`replicaof`用于指定主节点地址） |
| ephemeral        | /ɪˈfemərəl/          | 短暂的；临时的（指存储周期短的数据，如从节点上写入的临时数据，主从同步时可能被覆盖） |
| resync           | /ˌriːˈsɪŋk/          | 重新同步（指从节点与主节点重新建立数据同步，如网络中断后`PSYNC`命令实现增量或全量数据重同步） |
| misconfiguration | /ˌmɪskənˌfɪɡəˈreɪʃn/ | 配置错误（指错误的配置设置，如客户端误写入从节点可能因配置错误导致数据不一致） |
| read-only        | /ˌriːd ˈəʊnli/       | 只读的（Redis从节点默认属性，禁止写入操作，通过`replica-read-only yes`配置，保障数据一致性） |
| untrusted        | /ˌʌnˈtrʌstɪd/        | 不可信的（指未验证的客户端，从节点虽只读但仍暴露管理命令，不建议直接暴露给不可信网络） |
| administrative   | /ədˌmɪnɪˈstreɪtɪv/   | 管理的（指用于系统管理的命令，如`CONFIG`、`DEBUG`等，从节点默认开放，需谨慎暴露） |
| rename-command   | /ˌriːneɪm kəˈmɑːnd/  | 重命名命令（Redis配置项，用于隐藏危险命令，如`rename-command CONFIG ""`禁用CONFIG命令，提升安全性） |
| sentinel         | /ˈsentɪnl/           | 哨兵（Redis高可用组件，`sentinel monitor mymaster`配置用于监控主节点，自动实现故障转移） |
| lettuce          | /ˈletɪs/             | 生菜（Redis的Java客户端，`client-type: lettuce`指定使用Lettuce客户端，支持连接池和哨兵模式） |
| pool             | /puːl/               | 连接池（管理数据库连接的缓存池，`lettuce.pool`配置控制连接池大小、等待时间等，提升性能） |
| max-active       | /mæks ˈæktɪv/        | 最大活跃连接数（连接池配置项，`max-active: 8`表示同时允许的最大连接数，防止连接过多耗尽资源） |
| max-idle         | /mæks ˈaɪdl/         | 最大空闲连接数（连接池配置项，`max-idle: 5`表示保持的最大空闲连接数，避免频繁创建连接） |
| max-wait         | /mæks weɪt/          | 最大等待时间（连接池配置项，`max-wait: 100`表示获取连接的最长等待毫秒数，超时则抛出异常） |
| nodes            | /nəʊdz/              | 节点（指哨兵集群的节点地址列表，`nodes`配置指定多个哨兵地址，实现高可用的哨兵服务发现） |
| dependency       | /dɪˈpendənsi/        | 依赖（指项目中引入的库，如`commons-pool2`是Lettuce连接池的依赖，提供连接池基础功能） |
| ReadFrom         | /riːd frəm/          | 读取策略（Lettuce客户端的读数据来源策略，如`REPLICA_PREFERRED`优先从从节点读取，分担主节点压力） |

# Lecture08-Redis-主从复制

# 一、引入

## 1、结构

![image-20250614153242947](./assets/image-20250614153242947.png)

<br/>

## 2、好处

- 主从实例分工协作，提高运行效率
- 数据多份存储，避免单点故障，让数据丢失的风险进一步下降

> 单点故障：服务器只有一个实例，一旦宕机，就全部丢失数据、且无法对外提供服务

<br/>

# 二、搭建

## 1、思路

### ①生产环境

让每一个服务器上，运行一个单独的Redis服务器实例

<br/>

### ②现有条件

所有Redis服务器实例都运行在同一个VMWare虚拟机上（权宜之计）

也就是说我们需要在一个VMWare虚拟机上启动三个Redis进程：

![image-20250614154041155](./assets/image-20250614154041155.png)

<br/>

## 2、准备

```shell
# 创建专属目录
mkdir /usr/local/redis_replication

# 复制原始配置文件
cp /opt/redis-7.0.10/redis.conf /usr/local/redis_replication/
```

创建三个配置文件，并编辑内容：

![image-20250614125237548](./assets/image-20250614125237548.png)

```properties
include /usr/local/redis_replication/redis.conf
daemonize yes
dir /usr/local/redis_replication
logfile redis1000.log
dbfilename dump1000.rdb
pidfile redis1000.pid
protected-mode no
bind 0.0.0.0
port 1000
```

<br/>

```properties
include /usr/local/redis_replication/redis.conf
daemonize yes
dir /usr/local/redis_replication
logfile redis2000.log
dbfilename dump2000.rdb
pidfile redis2000.pid
protected-mode no
bind 0.0.0.0
port 2000
```

<br/>

```properties
include /usr/local/redis_replication/redis.conf
daemonize yes
dir /usr/local/redis_replication
logfile redis3000.log
dbfilename dump3000.rdb
pidfile redis3000.pid
protected-mode no
bind 0.0.0.0
port 3000
```

<br/>

## 3、启动

```shell
/usr/local/redis/bin/redis-server /usr/local/redis_replication/redis1000.conf
/usr/local/redis/bin/redis-server /usr/local/redis_replication/redis2000.conf
/usr/local/redis/bin/redis-server /usr/local/redis_replication/redis3000.conf
```

<br/>

## 4、建立主从关系

### ①查看主从关系

命令：INFO replication

●未建立主从关系时，打印信息举例：

```shell
127.0.0.1:1000> INFO replication
# Replication
role:master
connected_slaves:0
```

<br/>

●已建立主从关系的从机信息举例：

```shell
127.0.0.1:2000> INFO replication
# Replication
role:slave
master_host:127.0.0.1
master_port:1000
master_link_status:up
master_last_io_seconds_ago:2
```

<br/>

●已建立主从关系的主机信息举例：

```shell
127.0.0.1:1000> INFO replication
# Replication
role:master
connected_slaves:2
slave0:ip=127.0.0.1,port=2000,state=online,offset=126,lag=0
slave1:ip=127.0.0.1,port=3000,state=online,offset=126,lag=1
```

<br/>

### ②设置主从关系

```shell
# 建立
SLAVEOF 127.0.0.1 1000

# 取消
SLAVEOF no one
```

<br/>

## 5、测试

- 正常的数据同步√
- 从服务器能否写入数据？不能，从服务器默认是只读的

```shell
127.0.0.1:2000> set number 100000
(error) READONLY You can't write against a read only replica.
```

```properties
# You can configure a replica instance to accept writes or not. Writing against
# a replica instance may be useful to store some ephemeral data (because data
# written on a replica will be easily deleted after resync with the master) but
# may also cause problems if clients are writing to it because of a
# misconfiguration.
#
# Since Redis 2.6 by default replicas are read-only.
#
# Note: read only replicas are not designed to be exposed to untrusted clients
# on the internet. It's just a protection layer against misuse of the instance.
# Still a read only replica exports by default all the administrative commands
# such as CONFIG, DEBUG, and so forth. To a limited extent you can improve
# security of read only replicas using 'rename-command' to shadow all the
# administrative / dangerous commands.
replica-read-only yes
```

<br/>

- 从服务器宕机再启动后，宕机期间的新增数据能否同步？可以，但是需要重新手动建立主从关系
- 主服务器宕机期间，从服务器是否可以执行写操作？不行，哪怕主服务器宕机了，从服务器还是从服务器
- 主服务器宕机再启动后，主从关系会如何变化？不会变化

<br/>

# 三、原理

-   Slave启动成功连接到master后会发送一个sync命令
-   Master接到命令启动后台的存盘进程，同时收集所有接收到的用于修改数据集命令， 在后台进程执行完毕之后，master将传送整个数据文件到slave，以完成一次完全同步
-   全量复制：slave服务在接收到数据库文件数据后，将其存盘并加载到内存中。
-   增量复制：Master继续将新的所有收集到的修改命令依次传给slave，完成同步
-   但是只要是重新连接master，就会执行一次全量复制

![](./assets/图片_ARh2hxM4BZ.png)

<br/>

# 四、哨兵模式

## 1、需求

上面我们搭建的主从复制结构，需要手动维护

- 主服务器宕机后，从服务器不能写入新数据，相当于整个结构不能执行写操作
- 从服务器宕机再重启后，需要手动重新设置主从关系

我们希望这个过程可以尽量自动化的完成

<Br/>

## 2、配置

### ①创建哨兵实例所需配置文件

- 文件名：sentinel.conf
- 文件位置：/usr/local/redis_replication/
- 文件内容：

```properties
# mymaster是我们给主服务器起的名字
sentinel monitor mymaster 127.0.0.1 1000 1
```

<br/>

### ②相关概念

![image-20250811114858702](./assets/image-20250811114858702.png)

生产环境下最好启动多个哨兵实例，监控主从复制结构中的每一个实例，监控方式就是给每个实例发送心跳检查

心跳检查机制有一点小问题：没有收到对方返回的响应数据包，并不一定是对方宕机，也有可能是网络不通导致的

所以单独一个哨兵实例，判断某个被监控实例宕机，比较“主观”

所以我们需要多个哨兵都判断被监控实例宕机，再最终确定这个实例确实宕机了

- 主观下线：单独一个哨兵判断某个实例宕机
- 客观下线：多个哨兵判断某个实例宕机

多个是多少个呢？

![image-20250614162746724](./assets/image-20250614162746724.png)

- master：需要进行主观下线、客观下线的判定
- slave：只做主观下线的判定

<br/>

### ③启动哨兵

```shell
/usr/local/redis/bin/redis-sentinel /usr/local/redis_replication/sentinel.conf --sentinel
```

<br/>

### ④测试

- slave宕机

> +sdown slave 127.0.0.1:3000 127.0.0.1 3000 @ mymaster 127.0.0.1 1000
>
> +reboot slave 127.0.0.1:3000 127.0.0.1 3000 @ mymaster 127.0.0.1 1000
>
> -sdown slave 127.0.0.1:3000 127.0.0.1 3000 @ mymaster 127.0.0.1 1000
>
> +convert-to-slave slave 127.0.0.1:3000 127.0.0.1 3000 @ mymaster 127.0.0.1 1000

<br/>

- master宕机：哨兵负责在从服务器中选举一个新的主服务器

![image-20250614163821279](./assets/image-20250614163821279.png)

<br/>

# 五、对接RedisTemplate

## 1、修改哨兵启动时的 IP 地址

- 前面的配置：127.0.0.1
- 修改后的地址：192.168.100.100

```properties
sentinel monitor mymaster 192.168.100.100 1000 1
```

<br/>

## 2、配置连接信息

```yaml
spring:
  data:
    redis:
      client-type: lettuce
      lettuce:
        pool:
          enabled: true
          max-active: 8
          max-idle: 5
          max-wait: 100
      sentinel:
        # 哨兵名称
        master: mymaster
        # 哨兵地址，集群模式下继续配置多个
        nodes:
          - 192.168.100.100:26379
```

要使用lettuce的连接池，那么就需要额外导入一个依赖：

```xml
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
            <version>2.11.1</version> <!-- 版本号可以根据需要调整 -->
        </dependency>
```

<br/>

## 3、配置主从节点访问策略

```java
/**
 * 配置主和从节点访问策略
 * - MASTER：从主节点读取
 * - MASTER_PREFERRED：优先从master节点读取，master不可用才读取replica
 * - REPLICA：从slave（replica）节点读取
 * - REPLICA_PREFERRED：优先从slave（replica）节点读取，所有的slave都不可用才读取master
 * @return
 */
@Bean
public LettuceClientConfigurationBuilderCustomizer clientConfigurationBuilderCustomizer(){
    //设置访问策略值
    return clientConfigurationBuilder -> clientConfigurationBuilder.readFrom(ReadFrom.REPLICA_PREFERRED);
}
```

