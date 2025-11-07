[toc]

# 单词表

| 单词     | 词性   | 发音        | 含义   |
| -------- | ------ | ----------- | ------ |
| Original | 形容词 | /əˈrɪdʒənl/ | 原始的 |

# Lecture13-特定功能-实验05-文件上传

# 一、MultipartFile

在Spring框架里，MultipartFile接口主要用于处理HTTP请求中的文件上传操作。下面为你列出MultipartFile接口里各个方法的名称及其作用：

| 方法名称                       | 方法作用                                                     |
| ------------------------------ | ------------------------------------------------------------ |
| `String getName()`             | 获取表单中文件字段的名称，比如在HTML表单里<input type="file" name="fileField">，这个方法返回的就是"fileField"。 |
| `String getOriginalFilename()` | 获取上传文件的原始文件名，像"example.txt"。要是客户端没有提供原始文件名，就可能返回null。 |
| `String getContentType()`      | 获取文件的内容类型，例如"image/jpeg"或者"application/pdf"。如果无法确定类型，就会返回null。 |
| `boolean isEmpty()`            | 检查上传的文件是否为空。要是文件没有内容或者根本没选文件，就会返回true。 |
| `long getSize()`               | 获取文件的大小，单位是字节。                                 |
| `byte[] getBytes()`            | 以字节数组的形式获取文件内容。要是文件很大，这种方式可能会占用大量内存，所以要谨慎使用。 |
| `InputStream getInputStream()` | 获取一个用于读取文件内容的输入流。                           |
| `void transferTo(File dest)`   | 把上传的文件转存到指定的目标文件。这个方法既可以直接保存文件，也能在必要时先将文件写入临时位置。 |
| `void transferTo(Path dest)`   | （从Spring 5.0开始支持）把上传的文件转存到指定的目标路径。   |

<br/>

# 二、接收文件上传的Controller方法

> 前端如果使用表单来上传文件，有三点需要注意：
>
> - form标签必须把enctype属性设置为：multipart/form-data
> - form标签的method属性必须设置为：post
> - 通过在input标签中把type属性设置为file，创建文件上传框

```java
@ResponseBody
@PostMapping("/demo/save/file/upload")
public String saveFileUpload(@RequestPart("picture") MultipartFile file) throws IOException {
    String inputName = file.getName();
    System.out.println("inputName = " + inputName);

    String originalFilename = file.getOriginalFilename();
    System.out.println("originalFilename = " + originalFilename);

    long size = file.getSize();
    System.out.println("size = " + size);

    String contentType = file.getContentType();
    System.out.println("contentType = " + contentType);

    byte[] bytes = file.getBytes();
    System.out.println("bytes = " + bytes);

    InputStream inputStream = file.getInputStream();
    System.out.println("inputStream = " + inputStream);
    return "ok";
}
```

<br/>

# 三、PostMan上传文件

![image-20250602175441338](./assets/image-20250602175441338.png)

<br/>

![image-20250602175544485](./assets/image-20250602175544485.png)

<br/>

![image-20250602175607984](./assets/image-20250602175607984.png)

<br/>

# 四、多文件上传

## 1、同名

![image-20250915105924664](./assets/image-20250915105924664.png)

```java
@RequestMapping("/upload/multi/file/same/name")
public String uploadMultiFileSameName(@RequestPart("picture") List<MultipartFile> pictureList) {

    for (MultipartFile multipartFile : pictureList) {
        if (!multipartFile.isEmpty()) {
            String originalFilename = multipartFile.getOriginalFilename();
            System.out.println("originalFilename = " + originalFilename);
        }
    }

    return "ok";
}
```

<br/>

## 2、不同名

![image-20250915110245613](./assets/image-20250915110245613.png)

```java
@RequestMapping("/upload/multi/file/different/name")
public String uploadMultiFileDifferentName(
        @RequestPart("picture") MultipartFile picture,
        @RequestPart("image") MultipartFile image) {

    String originalFilename = picture.getOriginalFilename();
    System.out.println("originalFilename = " + originalFilename);

    originalFilename = image.getOriginalFilename();
    System.out.println("originalFilename = " + originalFilename);

    return "ok";
}
```

<br/>

# 五、文件上传后的三种去向

## 1、本地转存（杜绝）

![img016](./assets/img016-1748858266441-7.png)

<br/>

### ①实现方式

#### [1]创建保存文件的目录

![img017](./assets/img017-1748858304954-10.png)

这个目录如果是空目录，那么服务器部署运行时很容易会忽略这个目录。为了避免这个问题，在这个目录下随便创建一个文件，随便写点内容即可

<br/>

#### [2]编写转存代码

下面是负责处理文件上传请求的 handler 方法的转存部分：

```Java
……
 
// 1、准备好保存文件的目标目录
// ①File 对象要求目标路径是一个物理路径（在硬盘空间里能够直接找到文件的路径）
// ②项目在不同系统平台上运行，要求能够自动兼容、适配不同系统平台的路径格式
//      例如：Window系统平台的路径是 D:/aaa/bbb 格式
//      例如：Linux系统平台的路径是 /ttt/uuu/vvv 格式
//      所以我们需要根据『不会变的虚拟路径』作为基准动态获取『跨平台的物理路径』
// ③虚拟路径：浏览器通过 Tomcat 服务器访问 Web 应用中的资源时使用的路径
String destFileFolderVirtualPath = "/head-picture";
 
// ④调用 ServletContext 对象的方法将虚拟路径转换为真实物理路径
String destFileFolderRealPath = servletContext.getRealPath(destFileFolderVirtualPath);
 
// 2、生成保存文件的文件名
// ①为了避免同名的文件覆盖已有文件，不使用 originalFilename，所以需要我们生成文件名
// ②我们生成文件名包含两部分：文件名本身和扩展名
// ③声明变量生成文件名本身
String generatedFileName = UUID.randomUUID().toString().replace("-","");
 
// ④根据 originalFilename 获取文件的扩展名
String fileExtname = originalFilename.substring(originalFilename.lastIndexOf("."));
 
// ⑤拼装起来就是我们生成的整体文件名
String destFileName = generatedFileName + "" + fileExtname;
 
// 3、拼接保存文件的路径，由两部分组成
//      第一部分：文件所在目录
//      第二部分：文件名
String destFilePath = destFileFolderRealPath + "/" + destFileName;
 
// 4、创建 File 对象，对应文件具体保存的位置
File destFile = new File(destFilePath);
 
// 5、执行转存
picture.transferTo(destFile);
 
……
```

<br/>

### ②缺陷

- Web 应用重新部署时通常都会清理旧的构建结果，此时用户以前上传的文件会被删除，导致数据丢失。
- 项目运行很长时间后，会导致上传的文件积累非常多，体积非常大，从而拖慢 Tomcat 运行速度。
- 当服务器以集群模式运行时，文件上传到集群中的某一个实例，其他实例中没有这个文件，就会造成数据不一致。
- 不支持动态扩容，一旦系统增加了新的硬盘或新的服务器实例，那么上传、下载时使用的路径都需要跟着变化，导致 Java 代码需要重新编写、重新编译，进而导致整个项目重新部署。

![img018](./assets/img018-1748858340873-12.png)

<br/>

## 2、文件服务器（采纳）

### ①总体机制

![img019](./assets/img019-1748858394298-14.png)

<br/>

### ②好处

- 不受 Web 应用重新部署影响
- 在应用服务器集群环境下不会导致数据不一致
- 针对文件读写进行专门的优化，性能有保障
- 能够实现动态扩容

![img020](./assets/img020-1748858423995-16.png)

<br/>

### ③文件服务器类型

- 第三方平台：
  - 阿里的 OSS 对象存储服务
  - 七牛云
- 自己搭建服务器：FastDFS 、MinIO等

<br/>

## 3、上传到其他模块（极少）

这种情况肯定出现在分布式架构中，常规业务功能不会这么做，采用这个方案的一定是特殊情况，这种情况极其少见。

![img021](./assets/img021-1748858452928-18.png)

<br/>

在 MultipartFile 接口中有一个对应的方法：

```Java
/**
 * Return a Resource representation of this MultipartFile. This can be used
 * as input to the {@code RestTemplate} or the {@code WebClient} to expose
 * content length and the filename along with the InputStream.
 * @return this MultipartFile adapted to the Resource contract
 * @since 5.1
 */
default Resource getResource() {
  return new MultipartFileResource(this);
}
```

注释中说：这个 Resource 对象代表当前 MultipartFile 对象，输入给 RestTemplate 或 WebClient。而 RestTemplate 或 WebClient 就是用来在 Java 程序中向服务器端发出请求的组件
