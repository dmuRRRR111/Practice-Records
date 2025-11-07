[toc]

# 单词表

| 单词       | 词性         | 发音                                          | 含义                                                         |
| ---------- | ------------ | --------------------------------------------- | ------------------------------------------------------------ |
| validate   | 动词         | 英/ˈvælɪdeɪt/ 美/ˈvælɪdeɪt/                   | 验证、校验                                                   |
| validation | 名词         | 英/ˌvælɪˈdeɪʃən/ 美/ˌvæləˈdeɪʃən/             | 验证、校验                                                   |
| blank      | 名词         | 英/blæŋk/ 美/blæŋk/                           | 空白                                                         |
| decimal    | 名称、形容词 | 英/ˈdesɪml/ 美/ˈdesɪml/                       | 小数、小数的                                                 |
| digit      | 名词         | 英/ˈdɪdʒɪt/ 美/ˈdɪdʒɪt/                       | 数字；(从 0 到 9 的任何一个)                                 |
| positive   | 形容词       | 英/ˈpɒzətɪv/ 美/ˈpɑːzətɪv/                    | 积极乐观的；完全的；良好的；正数的；建设性的； 正电的；自信的；阳性的；拥护的；表示赞同的； 有绝对把握；证据确凿的；朝着成功的 |
| negative   | 形容词       | 英/ˈneɡətɪv/ 美/ˈneɡətɪv/                     | 负面的；消极的；负极的；否定的；有害的； 坏的；结果为阴性的（或否定的）；缺乏热情的；含有否定词的 |
| future     | 名词         | 英/ˈfjuːtʃə(r)/ 美/ˈfjuːtʃər/                 | 未来                                                         |
| past       | 名词         | 英/pɑːst/ 美/pæst/                            | 过去                                                         |
| present    | 名词         | 英/ˈpreznt , prɪˈzent/ 美/ˈpreznt , prɪˈzent/ | 目前；现在；礼物；礼品                                       |
| range      | 名词         | 英/reɪndʒ/ 美/reɪndʒ/                         | 范围                                                         |
| currency   | 名词         | 英/ˈkʌrənsi/ 美/ˈkɜːrənsi/                    | 货币；通货；通用；流行；流传                                 |
| logic      | 名词         | 英/ˈlɒdʒɪk/ 美/ˈlɑːdʒɪk/                      | 逻辑；逻辑学；                                               |

# Lecture11-特定功能-实验03-数据校验

在应用程序开发中，数据校验是一个非常重要的环节，在业务计算前必须通过数据校验把不符合规则的数据排除掉，才能保证业务计算的正确！

在 Spring 6 中，数据校验主要基于 **Jakarta Bean Validation 3.0** 规范，注解包名从 `javax.validation` 迁移至 `jakarta.validation`

<br/>


# 一、**Jakarta Bean Validation 核心注解**
| 注解                         | 作用描述                                                     |
| ---------------------------- | ------------------------------------------------------------ |
| `@NotNull`                   | 验证对象不为 `null`，但允许空字符串（如 `""`）。             |
| `@NotEmpty`                  | 验证字符串、集合、数组等不为 `null` 且长度大于 0（如 `""` 会校验失败）。 |
| `@NotBlank`                  | 验证字符串不为 `null` 且去除首尾空格后长度大于 0（如 `"  "` 会校验失败）。 |
| `@Size(min, max)`            | 验证字符串、集合、数组等的长度在指定范围内。                 |
| `@Min(value)`                | 验证数值类型（如 `int`、`Long`）不小于指定值。               |
| `@Max(value)`                | 验证数值类型不大于指定值。                                   |
| `@DecimalMin(value)`         | 验证 BigDecimal 或字符串表示的数值不小于指定值。             |
| `@DecimalMax(value)`         | 验证 BigDecimal 或字符串表示的数值不大于指定值。             |
| `@Digits(integer, fraction)` | 验证数值的整数位数和小数位数不超过指定范围。                 |
| `@Positive`                  | 验证数值为正数（不包括 0）。                                 |
| `@PositiveOrZero`            | 验证数值为正数或 0。                                         |
| `@Negative`                  | 验证数值为负数（不包括 0）。                                 |
| `@NegativeOrZero`            | 验证数值为负数或 0。                                         |
| `@Past`                      | 验证日期类型（如 `java.util.Date`）为过去的时间。            |
| `@PastOrPresent`             | 验证日期为过去或当前时间。                                   |
| `@Future`                    | 验证日期为未来的时间。                                       |
| `@FutureOrPresent`           | 验证日期为未来或当前时间。                                   |
| `@Pattern(regexp)`           | 验证字符串符合指定的正则表达式。                             |

<br/>

# 二、**Jakarta Bean Validation 扩展注解**

| 注解                | 作用描述                                                     |
| ------------------- | ------------------------------------------------------------ |
| `@Email`            | 验证字符串是合法的电子邮件格式。                             |
| `@URL`              | 验证字符串是合法的 URL 格式。                                |
| `@CreditCardNumber` | 验证字符串是合法的信用卡号码（通过 Luhn 算法校验）。         |
| `@Currency`         | 验证货币金额（需配合 `@Digits` 使用）。                      |
| `@Length(min, max)` | 验证字符串长度在指定范围内（与 `@Size` 类似，但仅适用于字符串）。 |
| `@Range(min, max)`  | 验证数值在指定范围内（包含边界值）。                         |

<br/>

# 三、**Spring 特有的校验注解**

| 注解         | 作用描述                                                     |
| ------------ | ------------------------------------------------------------ |
| `@Validated` | Spring 提供的注解，用于启用方法级别的校验（如校验方法参数或返回值）。 |
| `@Valid`     | Jakarta Bean Validation 提供的注解，用于级联校验（如校验对象的嵌套属性）。 |

<br/>

# 四、**示例代码**

## 1、增加依赖

在 SpringMVC 基础环境的基础上额外增加下面的依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

<br/>

## 2、在实体类设置校验规则

### ①实体类

```java
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class User {
    @NotNull(message = "ID 不能为空")
    private Long id;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度需在 2-20 之间")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Positive(message = "年龄必须为正数")
    @Max(value = 150, message = "年龄不能超过 150")
    private Integer age;

    @Past(message = "生日必须为过去的日期")
    private LocalDate birthday;

    @NotEmpty(message = "爱好不能为空")
    private List<String> hobbies;

    @Valid // 级联校验嵌套对象
    private Address address;
}

public class Address {
    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "街道不能为空")
    private String street;
}
```

<br/>

### ②Controller方法

```java
import com.atguigu.spring.entity.User;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DemoController {

    @RequestMapping("/save/user")
    public List<String> test01(@Valid @RequestBody User user, BindingResult result) {

        if (result.hasErrors()) {
            return result.getFieldErrors()
                    .stream()
                    .map(fieldError -> {
                        String fieldName = fieldError.getField();
                        Object rejectedValue = fieldError.getRejectedValue();
                        String defaultMessage = fieldError.getDefaultMessage();

                        return "字段名称：" + fieldName + "， 输入数据：" + rejectedValue + "， 提示信息：" + defaultMessage;
                    })
                    .collect(Collectors.toList());
        }

        return Arrays.asList(user.toString());
    }

}
```

<br/>

## 3、在Controller方法设置校验规则

- @Validated注解可以标记在类上或方法上
- 标记在类上：相当于每一个方法都进行了标记

```java
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/api")
public class MathController {
    @GetMapping("/divide")
    public int divide(@NotNull @Min(1) Integer dividend, 
                      @NotNull @Min(1) Integer divisor) {
        return dividend / divisor;
    }
}
```

此时的校验失败信息可以通过捕捉ConstraintViolationException异常来获取

> 注意：上述代码生效，需要基于 SpringBoot 较高版本，例如 3.3.12

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.12</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
```

<br/>

# 五、实体类分层

## 1、需求背景

在整合开发的环境下，表述层要给实体类上加校验规则注解，持久化层也需要加注解

例如MybatisPlus：

```java
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("t_user") // 对应数据库表名
public class User {

    @TableId(type = IdType.AUTO) // 主键注解及生成策略
    private Long id;

    @TableField("user_name") // 字段映射
    private String userName;

    @TableLogic // 逻辑删除字段
    private Integer deleted;

    @Version // 乐观锁版本号
    private Integer version;

    @TableField(fill = FieldFill.INSERT) // 自动填充
    private LocalDateTime createTime;

    // 省略构造方法、Getter 和 Setter
}
```

这就带来一个问题，不同分层的注解都标记在一个类上，代码就会混乱，不符合设计模式所提倡的单一职责原则

<br/>

## 2、具体分层方式

友情提示：实体类分层命名的设定，并不是语法层面的要求，而是团队内部人为规定的

| 分层名称                  | 中文名称     | 举例    | 功能                             |
| ------------------------- | ------------ | ------- | -------------------------------- |
| VO：View Object           | 视图对象     | UserVO  | 对接前端时使用的实体类对象       |
| DO：Data Object           | 数据对象     | UserDO  | 对接数据库时的实体类对象         |
| DTO：Data Transfer Object | 数据传输对象 | UserDTO | A模块向B模块传输数据时使用的对象 |
| BO：Business Object       | 业务对象     | UserBO  | 封装业务逻辑数据的实体类对象     |

<br/>

## 3、进一步拆分

以VO为例，考虑到请求和响应过程的区别，甚至会把VO分成两部分：

- 请求相关的放在request包下：com.atguigu.spring.entity.vo.req
- 响应相关的放在response包下：com.atguigu.spring.entity.vo.resp

举个例子：同样是User对象

- 请求部分的UserReqVO：包含密码
- 响应部分的UserRespVO：不包含密码（脱敏）

<br/>

另外，我们还可以针对不同情况来设定不同的数据校验规则：

- 新增操作时，不需要id字段的值必须存在：UserAddVO
- 更新操作时，要求id字段的值必须存在：UserUpdateVO
