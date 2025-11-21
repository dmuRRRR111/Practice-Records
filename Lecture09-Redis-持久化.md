[toc]

| 单词         | 音标          | 含义                                                         |
| ------------ | ------------- | ------------------------------------------------------------ |
| fork         | /fɔːk/        | 分叉；派生（Redis中`fork()`系统调用用于创建子进程，执行RDB持久化时通过fork子进程避免阻塞主进程，子进程负责写入快照文件） |
| compression  | /kəmˈpreʃn/   | 压缩（指通过算法减小数据体积，Redis在RDB或AOF持久化时可配置压缩选项，如`rdbcompression yes`启用RDB文件压缩，节省存储空间） |
| checksum     | /ˈtʃeksəm/    | 校验和（用于验证数据完整性的数值，Redis可对RDB文件计算checksum，加载时校验确保文件未损坏，如`rdbchecksum yes`开启校验） |
| percentage   | /pəˈsentɪdʒ/  | 百分比（Redis配置中用于表示比例阈值，如内存淘汰策略中`maxmemory-eviction-tenacity`设置淘汰尝试的百分比） |
| backup       | /ˈbækʌp/      | 备份（指数据的副本存储，Redis通过RDB或AOF文件实现数据备份，用于灾难恢复，如定期备份RDB文件防止数据丢失） |
| analyzed     | /ˈænəlaɪzd/   | 已分析的（指Redis对数据或文件进行解析处理，如启动时分析AOF文件完整性，或通过工具分析RDB文件结构） |
| truncated    | /trʌŋˈkeɪtɪd/ | 被截断的（指文件因异常终止而不完整，如Redis检测到AOF文件被截断时，可通过`aof-load-truncated yes`尝试加载并修复） |
| Successfully | /səkˈsesfəli/ | 成功地（Redis操作结果提示，如日志中“Successfully wrote RDB file”表示RDB文件写入成功） |
| shrink       | /ʃrɪŋk/       | 收缩；缩减（指减小文件或内存占用，如Redis通过AOF重写（rewrite）收缩AOF文件体积，去除冗余命令） |

# Lecture07-Redis-持久化

# 一、概述

## 1、需求

作为一款内存数据库，Redis工作时数据都保存在内存中，一旦停电或重启，数据都会丢失

所以持久化就是一个很自然的需求

Redis提供了两种持久化机制：

- RDB（Redis DataBase）：默认开启，定时数据快照
- AOF（Append Only File）：指令日志文件

<br/>

## 2、RDB

RDB持久化是一种周期性将Redis数据集快照保存到磁盘的机制

它会创建一个二进制文件（以`.rdb`为扩展名），其中包含了当前数据库中的所有键值对的快照

RDB持久化有以下特点：

- 快速恢复：RDB文件是一个快照，恢复时可以快速加载整个数据集，适合用于备份和灾难恢复。
- 紧凑的文件格式：RDB文件采用二进制格式，文件相对较小，节省存储空间。
- 高性能：由于RDB是周期性执行的快照操作，可以提供很好的性能，不会对数据库的读写操作产生额外的负担。
- 可配置的触发机制：可以通过配置触发RDB持久化的方式，如根据时间间隔、写操作次数或者同时满足两者等。

<br/>

## 3、AOF

AOF持久化通过将Redis的写操作追加到一个日志文件（Append-Only File）中来记录数据库状态的持久化方式

AOF文件以文本方式保存 Redis 数据库的操作命令，它可以通过重新执行这些命令来还原数据集

AOF持久化有以下特点：

- 高数据完整性：通过记录每个写操作命令，可以将数据库的状态完全还原。
- 恢复方式灵活：可以选择完全根据AOF文件来还原数据库状态，也可以选择在启动时将AOF文件的内容重放到内存数据库中。
- 默认是追加模式：在默认情况下，Redis以追加模式将写操作追加到AOF文件中，即使文件很大，也不会对系统性能产生明显影响。
- 文件体积相对较大：由于AOF文件保存了系统的写操作历史，相比RDB文件，AOF文件的体积通常要大。
- 可能会有较高的写入延迟：由于每个写操作都需要追加到AOF文件，如果AOF文件较大，可能会导致写入延迟增加。

<br/>

# 二、RDB

## 1、执行流程

### ①总体流程

Redis会单独创建（fork）一个子进程来进行持久化，也就是说持久化操作是异步的

子进程会先将数据写入到一个临时文件中，写入之后，再用这个临时文件替换上次持久化好的文件

整个过程中，主进程是不进行任何I/O操作的，这就确保了极高的性能

如果需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那RDB方式要比AOF方式更加的高效

RDB的缺点是最后一次持久化后的数据可能丢失（服务器宕机，最后一次不会执行持久化，正常关闭会执行持久化）

<br/>

### ②Fork子进程

- fork() 是 Linux 系统的一个操作，作用是复制进程

-   Fork的作用是复制一个与当前进程一样的进程
    新进程的所有数据（变量、环境变量、程序计数器等） 数值都和原进程一致，但是是一个全新的进程，并作为原进程的子进程
- 在Linux程序中，fork()会产生一个和父进程完全相同的子进程，但子进程在此后会通过执行exec系统调用执行持久化
- 出于效率考虑，Linux中引入了“写时复制技术”
-   一般情况父进程和子进程会共用同一段物理内存，只有进程空间的各段的内容要发生变化时，才会将父进程的内容复制一份给子进程。

<br/>

### ③流程图

![image-20250613220115516](./assets/image-20250613220115516.png)

<br/>

## 2、相关配置

### ①总体说明

| 配置项                      | 作用说明                                 |
| --------------------------- | ---------------------------------------- |
| dbfilename                  | RDB持久化文件名                          |
| dir                         | Redis工作目录，RDB持久化文件的保存目录   |
| save                        | 自动保存快照时间间隔                     |
| stop-writes-on-bgsave-error | 如果bgsave失败则停止Redis写操作，建议yes |
| rdbcompression              | 是否进行压缩存储                         |
| rdbchecksum                 | 是否执行持久化文件完整性检查             |

<br/>

### ②细节：save

#### [1]持久化时间间隔配置

较低版本 Redis 中默认配置如下：

```properties
save 900 1      # 在900秒（15分钟）内，如果至少有1个键发生改变，则执行RDB持久化
save 300 10     # 在300秒（5分钟）内，如果至少有10个键发生改变，则执行RDB持久化
save 60 10000   # 在60秒内，如果至少有10,000个键发生改变，则执行RDB持久化
```

当前版本 Redis 中配置如下：

```properties
save 3600 1 300 100 60 10000
```

<br/>

![image-20250614141858244](./assets/image-20250614141858244.png)

![image-20250614142008675](./assets/image-20250614142008675.png)

![image-20250614142103334](./assets/image-20250614142103334.png)

<br/>

#### [2]禁用RDB

```properties
save ""
```

<br/>

### ③细节：rdbcompression

对于存储到磁盘中的快照，可以设置是否进行压缩存储

如果是的话，Redis 会采用 LZF 算法进行压缩

如果你不想消耗 CPU 来进行压缩的话，可以设置为关闭此功能

<br/>

### ④细节：rdbchecksum

在存储快照后，还可以让 Redis 使用 CRC64 算法来进行数据校验，但是这样做会增加大约 10% 的性能消耗

如果希望获取到最大的性能提升，可以关闭此功能

<br/>

## 3、相关命令

### ①save

由主线程执行持久化保存，主线程会被阻塞，直至保存操作完成——同步操作

<br/>

### ②bgsave

创建子进程执行持久化保存，异步非阻塞，持久化期间主线程仍然可以响应其它命令

<br/>

### ③flushall

清空全库，16个数据库中的数据全部删除，执行flushall命令，也会产生dump.rdb文件，但里面是空的，无意义

<br/>

### ④shutdown

关闭Redis服务器端进程，此前会执行一次持久化保存

<br/>

### ⑤总结RDB持久化触发时机

- 符合 save 配置的规则
- 关闭 Redis 服务器进程之前
- 手动执行命令：
  - save
  - bgsave
  - flushall（危险）

<br/>

## 4、手动备份

把*.rdb文件视为数据备份文件执行数据备份：

- 备份操作：把*.rdb文件复制到数据盘
- 恢复操作：把*.rdb文件复制到Redis工作目录下，Redis启动时会自动加载

<br/>

## 5、RDB（Redis Database）持久化的优势与劣势


### ①优势
1. 文件紧凑，恢复速度快
   - RDB生成的快照文件是二进制格式，体积小，便于备份和传输。  
   - 恢复数据时，直接读取文件加载到内存，速度显著快于AOF日志重放。

2. 对性能影响较小
   - RDB通过fork子进程执行快照，主线程无需阻塞（除fork瞬间），适合高并发场景。  
   - 相比AOF的实时写入，RDB的周期性触发对Redis整体性能影响更低。

3. 适合全量数据备份
   - 可用于生成某个时间点的完整数据副本，便于数据归档、容灾恢复或只读实例部署。

4. 内存占用优化
   - RDB在生成快照时会压缩数据（如整数类型使用更小的存储空间），减少磁盘占用。

5. 配置简单灵活
   - 可通过`save`参数自定义触发条件（时间间隔+写操作数），适配不同业务场景。


### ②劣势
1. 数据丢失风险  
   - 两次RDB之间的数据无法持久化（例如配置`save 60 100`，若Redis崩溃，可能丢失最近1分钟内的操作）。

2. fork 耗时与内存压力  
   - 生成RDB时需fork子进程，当数据量较大（如数GB内存）时，fork可能导致主线程短暂阻塞。  
   - 子进程会复制父进程内存空间，可能引发内存翻倍问题（尤其是在容器环境中）。

3. 无法实时持久化  
   - 不支持实时数据持久化，不适用于对数据一致性要求极高的场景（如金融交易）。

4. 格式不兼容问题  
   - RDB文件格式随Redis版本更新可能变化，低版本Redis无法读取高版本生成的RDB文件。

5. 增量更新效率低  
   - 每次RDB都会生成完整快照，即使只有少量数据变更，也需重新写入整个文件。


### ③应用场景建议
- 推荐场景：
  缓存系统、非核心数据存储、允许少量数据丢失但要求快速恢复的业务。
- 不推荐场景：
  金融交易、实时数据记录、对数据一致性要求极高的关键业务（建议结合AOF使用）。

<br/>

# 三、AOF

## 1、简介

Append Only File 以日志的形式来记录每个写操作（增量保存），将Redis执行过的所有写指令记录下来(读操作不记录)， 只许追加文件但不可以改写文件，Redis启动之初会读取该文件重新构建数据，换言之，Redis 重启的话就根据日志文件的内容将写指令从前到后执行一次以完成数据的恢复工作

<br/>

## 2、执行流程

（1）客户端的请求写命令会被append追加到AOF缓冲区内；

（2）AOF缓冲区根据AOF持久化策略\[always,everysec,no]将操作sync同步到磁盘的AOF文件中；

（3）AOF文件大小超过重写策略或手动重写时，会对AOF文件rewrite重写，压缩AOF文件容量；

（4）Redis服务重启时，会重新load加载AOF文件中的写操作达到数据恢复的目的；

![image-20250613225558523](./assets/image-20250613225558523.png)

<br/>

## 3、相关配置

### ①总体说明

| 配置项名称                  | 作用                      |
| --------------------------- | ------------------------- |
| appendfilename              | AOF文件名                 |
| appenddirname               | AOF文件所在目录名称       |
| appendonly                  | 开启或禁用AOF             |
| appendfsync                 | 持久化操作执行频率        |
| no-appendfsync-on-rewrite   | AOF重写时是否执行文件同步 |
| auto-aof-rewrite-percentage | 触发AOF重写的百分比阈值   |
| auto-aof-rewrite-min-size   | 触发AOF重写的文件大小阈值 |

<br/>

### ②细节：文件名

```properties
# The base name of the append only file.
#
# Redis 7 and newer use a set of append-only files to persist the dataset
# and changes applied to it. There are two basic types of files in use:
#
# - Base files, which are a snapshot representing the complete state of the
#   dataset at the time the file was created. Base files can be either in
#   the form of RDB (binary serialized) or AOF (textual commands).
# - Incremental files, which contain additional commands that were applied
#   to the dataset following the previous file.
#
# In addition, manifest files are used to track the files and the order in
# which they were created and should be applied.
#
# Append-only file names are created by Redis following a specific pattern.
# The file name's prefix is based on the 'appendfilename' configuration
# parameter, followed by additional information about the sequence and type.
#
# For example, if appendfilename is set to appendonly.aof, the following file
# names could be derived:
#
# - appendonly.aof.1.base.rdb as a base file.
# - appendonly.aof.1.incr.aof, appendonly.aof.2.incr.aof as incremental files.
# - appendonly.aof.manifest as a manifest file.
```

在Redis 7中，持久化机制在传统RDB（快照）和AOF（ Append-Only File ）的基础上进行了优化，通过细分为基础文件（Base File）、增量文件（Incremental File） 和清单文件（Manifest File），进一步提升了持久化的效率（减少IO开销）、恢复速度和数据可靠性。这三种文件类型的具体含义和作用如下：


#### 1. 基础文件（Base File）：全量数据的“基准快照”  
基础文件是Redis持久化的全量数据载体，类似于传统RDB或AOF重写后的“完整数据快照”，包含某一时刻Redis实例的全部有效数据（键值对、过期时间、数据结构等）。  

- 特点：体积较大（包含全量数据），生成频率较低（通常由手动触发或按配置的“全量持久化周期”生成），是增量文件的“基准参考”。  
- 作用：作为数据恢复的“基础底本”，后续的增量文件均基于此基础文件记录数据变更，避免了频繁生成全量快照的高IO成本。  
- 格式：可基于RDB格式（二进制，紧凑高效）或AOF重写格式（文本，可读性强），具体由Redis配置决定。  


#### 2. 增量文件（Incremental File）：基础文件之后的“变更日志”  
增量文件是记录基础文件生成之后Redis数据变更的“增量日志”，包含新写入、修改、删除的键值对，以及过期键清理、数据结构更新等操作。  

- 特点：体积较小（仅记录增量变更），生成频率高（实时或按短周期追加），依赖基础文件存在（单独无法恢复完整数据）。  
- 作用：在基础文件的基础上，实时记录数据变化，既保证了数据的“近实时持久化”，又避免了频繁全量写入的性能损耗。  
- 与传统AOF的区别：传统AOF是“独立的增量日志”（从实例启动开始记录所有变更），而Redis 7的增量文件是“基于基础文件的增量”（仅记录基础文件之后的变更），更便于按“基础文件+增量文件”的组合进行数据分片和备份。  


#### 3. 清单文件（Manifest File）：持久化文件的“元数据管理清单”  
清单文件是记录基础文件和增量文件元信息的“管理目录”，相当于持久化文件的“索引和说明书”，包含以下关键信息：  

- 基础文件的路径、生成时间、校验和（用于验证文件完整性）、数据版本；  
- 增量文件的序列（按生成时间排序，确保恢复时的顺序正确）、路径、生成时间、与基础文件的关联关系；  
- 整个持久化数据集的“全局版本号”（用于识别数据一致性）、过期文件的清理策略等。  

- 作用：Redis启动恢复数据时，通过读取清单文件可快速定位“最新的基础文件”和“对应的增量文件序列”，按正确顺序加载（先加载基础文件，再按序列应用增量文件），避免因文件混乱（如增量文件顺序错误、基础文件损坏）导致的数据恢复失败。  
- 格式：通常为文本格式（如JSON或自定义格式），便于解析和人工查看。  


#### 4. 三者的配合关系  
Redis 7的持久化流程通过这三种文件的配合实现高效数据持久化和恢复：  
1. 生成基础文件（全量快照）→ 2. 持续生成增量文件（记录基础文件后的变更）→ 3. 清单文件实时更新基础文件和增量文件的元数据；  
2. 数据恢复时：先通过清单文件找到最新的基础文件→加载基础文件恢复全量数据→按清单中增量文件的序列依次应用增量变更→最终恢复完整数据。  


这种细分设计的核心优势是：平衡了“全量持久化的可靠性”和“增量持久化的高效性”，同时通过清单文件简化了多文件的管理，尤其适合大规模Redis实例（数据量大、变更频繁）的持久化需求。

#### 5. 文件名举例

Redis会按照特定模式生成追加文件的名称。文件名前缀基于`appendfilename`配置参数，后面附加序列和类型的额外信息。

例如，若`appendfilename`设置为`appendonly.aof`，可能生成以下文件名：
- `appendonly.aof.1.base.rdb` 作为基础文件
- `appendonly.aof.1.incr.aof`、`appendonly.aof.2.incr.aof` 作为增量文件
- `appendonly.aof.manifest` 作为清单文件

<br/>

### ③细节：持久化频率

`appendfsync` 控制Redis将AOF日志写入磁盘的频率，直接影响数据安全性和性能。以下是三种取值及其功能：

| 取值     | 功能描述                                                     | 数据安全性                                              | 性能                        | 适用场景                                   |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------- | --------------------------- | ------------------------------------------ |
| always   | 每次写操作都同步到磁盘（调用fsync）。                        | 最高（几乎不丢数据，仅系统崩溃可能丢失1条未完成命令）。 | 最低（每次写都需磁盘I/O）。 | 对数据完整性要求极高的场景（如金融交易）。 |
| everysec | 每秒同步一次（默认值）。Redis使用后台线程执行fsync，主线程继续处理新命令。 | 较高（最多丢失1秒内的数据）。                           | 中等（每秒1次磁盘I/O）。    | 平衡性能与安全性的通用场景。               |
| no       | 由操作系统决定何时同步（通常是缓冲区满时）。                 | 最低（可能丢失上次同步后所有数据）。                    | 最高（无额外磁盘I/O）。     | 允许大量数据丢失的缓存场景。               |

关键说明：

1. always：  
   - 确保每次写操作都持久化到磁盘，但频繁磁盘IO会显著降低性能（吞吐量可能下降50%以上）。  
   - 仅在极端需要数据完整性的场景下使用。

2. everysec：  
   - Redis主线程将写操作追加到AOF缓冲区后立即返回，由后台线程每秒执行一次fsync。  
   - 即使系统崩溃，最多丢失1秒内的数据，是性能与安全的最佳平衡点。

3. no：  
   - 写操作仅写入操作系统内核缓冲区，由系统决定何时刷盘（可能30秒或更久）。  
   - Redis崩溃时可能丢失大量数据，仅适用于缓存且数据可重建的场景。

性能测试参考：

- always：QPS（每秒查询率）可能从数万降至数千。  
- everysec：性能影响通常在10%以内，适合大多数生产环境。  
- no：性能接近无AOF持久化的情况。

建议生产环境优先使用everysec，除非有特殊需求。

<br/>

## 4、相关操作

### ①开启AOF

修改配置后重启：

```properties
appendonly yes
```

<br/>

### ②损坏文件修复

#### [1]搞破坏

故意把持久化文件改错：

```text
INCRBY
$6
number
$3
100aaaaaaaaaaaa
```

<br/>

#### [2]重启Redis

Redis启动时需要读取 AOF 持久化文件，遇到错误的命令就会启动失败

> 2952:M 14 Jun 2025 14:49:12.427 # Bad file format reading the append only file appendonly.aof.1.incr.aof: make a backup of your AOF file, then use ./redis-check-aof --fix <filename.manifest>

<br/>

#### [3]执行修复

这个修复操作并不是把错误的命令改成正确的命令，而是把持久化文件从错误命令开始，后面全删掉，保证Redis可以正常启动而已

```shell
[root@a bin]# ./redis-check-aof --fix /usr/local/redis/appendonlydir/appendonly.aof.1.incr.aof
Start checking Old-Style AOF
0x              ee: Expected \r\n, got: 6161
AOF analyzed: filename=/usr/local/redis/appendonlydir/appendonly.aof.1.incr.aof, size=255, ok_up_to=206, ok_up_to_line=47, diff=49
This will shrink the AOF /usr/local/redis/appendonlydir/appendonly.aof.1.incr.aof from 255 bytes, with 49 bytes, to 206 bytes
Continue? [y/N]: y
Successfully truncated AOF /usr/local/redis/appendonlydir/appendonly.aof.1.incr.aof
[root@a bin]#
```

<br/>

## 5、Redis AOF（Append Only File）持久化机制的优势与劣势


### ①核心优势
| 优势类型       | 详细说明                                                     | 应用场景价值                                    |
| -------------- | ------------------------------------------------------------ | ----------------------------------------------- |
| 数据安全性高   | - 可配置`appendfsync`为`always`，每次写操作都同步到磁盘，几乎不丢失数据<br>- 相比RDB（可能丢失数分钟数据），更适合对数据完整性要求高的场景 | 金融交易、实时订单系统等不能容忍数据丢失的场景  |
| 日志可读性强   | - AOF文件以文本格式存储Redis命令（如`SET key value`）<br>- 可直接查看、编辑或修复文件（如删除错误命令） | 便于运维排查问题，或手动恢复部分数据            |
| 增量持久化     | - 仅追加写操作到文件末尾，无需像RDB一样每次全量生成快照<br>- 写入操作是顺序IO，性能稳定（尤其机械硬盘场景） | 适合写操作频繁的场景，避免RDB全量快照的阻塞问题 |
| 混合持久化支持 | - Redis 4.0+支持AOF混合模式（前半部分RDB快照+后半部分增量命令）<br>- 兼顾RDB的恢复速度和AOF的安全性 | 缩短实例重启时的恢复时间，平衡性能与安全        |
| 重写机制优化   | - 自动/手动触发AOF重写（BGREWRITEAOF），删除冗余命令（如多次SET同一key）<br>- 重写过程不阻塞主线程（后台执行） | 避免AOF文件无限增长，释放磁盘空间               |


### ②主要劣势
| 劣势类型     | 详细说明                                                     | 潜在影响                               |
| ------------ | ------------------------------------------------------------ | -------------------------------------- |
| 文件体积较大 | - 记录所有写命令（如`INCR`操作会记录多次），相比RDB二进制格式体积更大<br>- 即使相同数据，AOF文件可能是RDB的数倍大小 | 占用更多磁盘空间，备份和传输耗时更长   |
| 恢复速度较慢 | - 重启时需逐条执行AOF命令重建数据<br>- 当文件很大时（如GB级），恢复时间可能长达数分钟 | 影响服务重启效率，尤其大规模集群故障时 |
| 性能损耗     | - `appendfsync=always`时，每次写操作都需磁盘IO，可能降低50%以上吞吐量<br>- AOF重写期间会消耗额外CPU和磁盘资源 | 高并发场景下可能成为性能瓶颈           |
| 兼容性问题   | - 不同Redis版本的命令格式可能变化，旧版本AOF文件可能无法在新版本直接使用<br>- 部分复杂命令（如LUA脚本）记录后可能无法完全还原 | 版本升级时需谨慎测试AOF恢复兼容性      |
| 重写阻塞风险 | - 虽然BGREWRITEAOF是后台线程执行，但极端情况下（如超大文件重写）可能导致短暂主线程阻塞<br>- 重写期间若写入量极高，可能导致AOF文件持续增长 | 高负载场景下需监控重写耗时和频率       |


### ③应用建议
- 优先选择AOF的场景：  
  数据不可丢失（如金融、支付系统）、写操作频繁、需要可查日志的业务。

- 谨慎使用AOF的场景：  
  纯缓存场景（数据可重建）、对重启恢复速度要求极高、磁盘空间紧张且写入量极大的业务。

- 最佳实践：  
  1. 生产环境建议配置`appendfsync=everysec`，平衡安全与性能  
  2. 启用AOF混合持久化（`aof-use-rdb-preamble yes`）  
  3. 通过`auto-aof-rewrite-percentage`和`auto-aof-rewrite-min-size`控制文件增长  
  4. 定期备份AOF文件，并测试恢复流程  

AOF与RDB并非互斥，实际应用中常结合使用：RDB用于定期全量备份，AOF用于实时增量持久化，以兼顾数据安全性和恢复效率。

<br/>

# 四、Redis持久化方案选择指南

在选择Redis持久化方案时，需综合考虑数据安全性、性能要求、恢复速度和运维成本。以下是具体建议：


## 1、核心方案对比
| 方案           | 数据安全性               | 性能影响             | 恢复速度               | 磁盘占用         | 适用场景                       |
| -------------- | ------------------------ | -------------------- | ---------------------- | ---------------- | ------------------------------ |
| RDB            | 低（可能丢失分钟级数据） | 低（定期fork子进程） | 极快（直接加载二进制） | 小（二进制压缩） | 纯缓存、允许部分数据丢失的场景 |
| AOF (everysec) | 中（最多丢失1秒数据）    | 中（每秒1次fsync）   | 较慢（重放命令）       | 大（文本格式）   | 多数生产环境（平衡安全与性能） |
| AOF (always)   | 高（几乎不丢数据）       | 高（每次写都fsync）  | 较慢（重放命令）       | 大（文本格式）   | 金融交易、关键数据存储         |
| RDB+AOF混合    | 高（结合两者优势）       | 中（兼顾两者开销）   | 较快（RDB快照+增量）   | 较大（两种文件） | 需要极致安全且快速恢复的场景   |


## 2、场景化选择建议
### ①纯缓存场景（数据可丢失）
- 方案：禁用持久化 或 仅RDB（配置宽松的save参数，如`save 3600 1`）
- 理由：  
  
  - 缓存数据可从源头重建，追求极致性能
  - RDB的周期性持久化对性能影响最小
- 配置示例：  
  ```
  save 3600 1  
  appendonly no  
  ```

### ②一般生产环境（平衡安全与性能）
- 方案：AOF（appendfsync=everysec） + RDB（定期备份）
- 理由：  
  - AOF的每秒同步提供足够安全性（最多丢失1秒数据）  
  - RDB用于冷备和快速恢复（如集群重建）  
- 配置示例：  
  ```
  save 900 1  
  appendonly yes  
  appendfsync everysec  
  aof-use-rdb-preamble yes  # 启用混合持久化  
  ```

### ③关键数据存储（不容许数据丢失）
- 方案：AOF（appendfsync=always） + 定期RDB备份
- 理由：  
  - 每次写操作都同步到磁盘，确保数据完整性  
  - RDB作为额外保障，应对AOF文件损坏等极端情况  
- 配置示例：  
  ```
  save 3600 1  
  appendonly yes  
  appendfsync always  
  aof-use-rdb-preamble yes  
  ```

### ④大规模集群（追求快速恢复）
- 方案：RDB + AOF混合持久化（`aof-use-rdb-preamble yes`）
- 理由：  
  - 重启时先加载RDB快照（快速恢复大部分数据），再执行增量AOF命令  
  - 大幅缩短大规模集群的整体恢复时间  
- 配置示例：  
  ```
  save 300 10  
  appendonly yes  
  aof-use-rdb-preamble yes  
  aof-rewrite-threads 4  # 多线程加速AOF重写（Redis 7+）  
  ```


## 3、特殊场景处理
1. 大内存实例（>10GB）：  
   - 避免频繁RDB快照（fork耗时过长），可配置`save 3600 1`  
   - 启用AOF多线程重写（`aof-rewrite-threads 4`）  

2. 写密集型业务：  
   - 使用AOF（appendfsync=everysec）而非always，减少磁盘IO压力  
   - 定期执行AOF重写，避免文件过大  

3. 异地容灾：  
   - 定期将RDB文件同步至异地机房（体积小，传输快）  
   - 考虑Redis Sentinel或Cluster的多节点部署  


## 4、配置检查清单
1. 性能敏感型业务：  
   ```
   save 3600 1       # 宽松RDB触发条件  
   appendonly yes  
   appendfsync everysec  
   aof-rewrite-percentage 100  
   aof-rewrite-min-size 1GB  # 避免小文件频繁重写  
   ```

2. 安全敏感型业务：  
   ```
   save 900 1  
   appendonly yes  
   appendfsync always  
   aof-rewrite-incremental-fsync yes  # 重写时增量fsync  
   ```

3. 云环境/容器部署：  
   ```
   dir /data/redis  # 明确指定数据目录（避免容器重启后路径丢失）  
   save 300 10  
   appendonly yes  
   aof-use-rdb-preamble yes  
   ```


## 5、监控与运维建议
1. 定期检查：  
   - 使用`INFO persistence`查看RDB/AOF状态（lastsave、aof_current_size等）  
   - 监控磁盘空间（尤其是AOF文件增长）  

2. 性能优化：  
   - 避免在高峰期执行手动RDB/AOF操作  
   - 使用`BGREWRITEAOF`而非`REWRITEAOF`（避免阻塞主线程）  

3. 灾备测试：  
   - 每月至少演练一次从RDB/AOF恢复数据的流程  
   - 备份AOF文件时，同步保存manifest文件（Redis 7+的MP-AOF）  

通过合理选择和配置持久化方案，可在满足业务需求的同时，确保Redis的高性能和数据安全性。

<br/>

# 五、学习建议

根据不同知识点的不同优先级，合理分配时间、精力、资源：

![image-20250811135251146](./assets/image-20250811135251146.png)