[toc]

# 单词表

| 单词      | 词性 | 发音                                               | 含义     |
| --------- | ---- | -------------------------------------------------- | -------- |
| bind      | 动词 | 英/baɪnd/ 美/baɪnd/                                | 绑定     |
| binding   | 名词 | 英/ˈbaɪndɪŋ/ 美/ˈbaɪndɪŋ/                          | 绑定     |
| field     | 名词 | 英/fiːld/ 美/fiːld/                                | 字段     |
| reject    | 动词 | 英/rɪˈdʒekt , ˈriːdʒekt/ 美/rɪˈdʒekt , ˈriːdʒekt/  | 拒绝     |
| rejected  | 动词 | 英/rɪˈdʒektɪd/ 美/rɪˈdʒektɪd/                      | 拒绝     |
| convert   | 动词 | 英/kənˈvɜːt , ˈkɒnvɜːt/ 美/kənˈvɜːrt , ˈkɑːnvɜːrt/ | 转换     |
| converter | 名词 | 英/kənˈvɜːtə(r)/ 美/kənˈvɜːrtər/                   | 转换器   |
| format    | 动词 | 英/ˈfɔːmæt/ 美/ˈfɔːrmæt/                           | 格式化   |
| formatter | 名词 | 英/ˈfɔːmætə/ 美/ˈfɔrˌmætər/                        | 格式化器 |

# Lecture10-特定功能-实验02-类型转换

# 一、自动类型转换

HTTP 协议是一个无类型的协议，我们在服务器端接收到请求参数等形式的数据时，本质上都是字符串类型。请看 javax.servlet.ServletRequest 接口中获取全部请求参数的方法：

```java
public Map<String, String[]> getParameterMap();
```

<br/>

而我们在实体类当中需要的类型是非常丰富的。对此，SpringMVC 对基本数据类型提供了自动的类型转换。例如：请求参数传入“100”字符串，我们实体类中需要的是 Integer 类型，那么 SpringMVC 会自动将字符串转换为 Integer 类型注入实体类

<br/>

# 二、日期和数值类型

提示：请大家自己添加getXxx()、setXxx()方法

```java
public class Product {
 
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date productDate;
 
    @NumberFormat(pattern = "###,###,###.###")
    private Double productPrice;
```

<br/>

# 三、转换失败后的处理方式

## 1、响应结果

### ①默认结果

![iamges](./assets/img010-1748570782160-7.png)

<br/>

![image-20250530153128924](./assets/image-20250530153128924.png)

<br/>

### ②BindingResult 接口

SpringMVC 将『把前端发送过来的数据，注入到 POJO 对象』这个操作称为<span style="color:blue;font-weight:bold;">『数据绑定』</span>，英文单词是 binding

数据类型的转换和格式化就发生在数据绑定的过程中

类型转换和格式化是密不可分的两个过程，很多带格式的数据必须明确指定格式之后才可以进行类型转换。最典型的就是日期类型

![iamges](./assets/img011.png)

BindingResult 接口和它的父接口 Errors 中定义了很多和数据绑定相关的方法，如果在数据绑定过程中发生了错误，那么通过这个接口类型的对象就可以获取到相关错误信息。

<br/>

## 2、用法举例

注意：BindingResult对象必须和Product对象紧挨着，中间不能声明别的参数，否则BindingResult无效

```java
@PostMapping("/demo/save/product")
public Result<Product> saveProduct(Product product, BindingResult bindingResult) {

    // 判断绑定过程中是否存在错误
    if (bindingResult.hasErrors()) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String fieldName = fieldError.getField();
            Object rejectedValue = fieldError.getRejectedValue();
            String defaultMessage = fieldError.getDefaultMessage();
            System.out.println(fieldName + " = " + rejectedValue);
            System.out.println("defaultMessage = " + defaultMessage);

            return Result.failed(400, fieldName + " 注入： " + rejectedValue + " 数据时，类型转换失败");
        }
    }

    return Result.ok(product);
}
```

<br/>

# 四、自定义类型转换器

## 1、提出问题

### ①创建实体类

![image-20250530154956892](./assets/image-20250530154956892.png)

```java
public class Worker {

    private Integer workerId;
    private String workerName;
    private Address address;
```

<br/>

![image-20250530155033174](./assets/image-20250530155033174.png)

```java
public class Address {

    private String province;
    private String city;
    private String street;
```

<br/>

### ②输入方式

我们想用一个字符串，给整个Address对象赋值：

![image-20250530155117288](./assets/image-20250530155117288.png)

<br/>

### ③SpringMVC响应

SpringMVC无法完成从字符串到Address对象的自动类型转换：

![image-20250530155145068](./assets/image-20250530155145068.png)

<br/>

## 2、创建自定义类型转换器

### ①创建自定义类型转换器类

Converter接口注意不要导错包：org.springframework.core.convert.converter.Converter

```java
@Component
public class AddressConverter implements Converter<String, Address> {

    @Override
    public Address convert(String source) {

        if (source == null || source.length() == 0) {
            throw new RuntimeException("源字符串不能为空！");
        }

        String[] split = source.split(",");
        String province = split[0];
        String city = split[1];
        String street = split[2];

        return new Address(province, city, street);
    }
}
```

<br/>

### ②注册自定义类型转换器类

```java
@Configuration
public class DemoConfig implements WebMvcConfigurer {

    @Autowired
    private AddressConverter addressConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(addressConverter);
    }
}
```

<br/>

# 五、@JsonFormat注解

## 1、应用场景

前端在请求体中以JSON形式发送数据，后端使用实体类对象加@RequestBody注解接收

此时，JSON数据中存在日期类型，那么我们的实体类中就可以使用@JsonFormat注解来指定这个日期类型数据的格式

<br/>

## 2、举例

### ①实体类

```java
public class Product {
 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date productDate;
    
    private String productName;
```

<br/>

### ②Controller方法

```java
@ResponseBody
@PostMapping("/demo/save/product")
public String saveProduct(@RequestBody Product product) {

    return "ok " + product.getProductDate();
}
```

<br/>

### ③PostMan测试

![image-20250602203816355](./assets/image-20250602203816355.png)
