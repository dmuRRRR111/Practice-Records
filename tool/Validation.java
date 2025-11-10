public class Validation {

    //在 web 环境的基础上，额外增加下面依赖：
    //            <dependency>
    //                <groupId>org.springframework.boot</groupId>
    //                <artifactId>spring-boot-starter-validation</artifactId>
    //            </dependency>

    /**
     * 常用Validation注解
     */
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

    //把当前数据标记为需要执行校验
    //        (1)针对实体类对象，使用 @Valid 注解
    //            saveUser(@Valid @RequestBody User user, BindingResult bindingResult)
    //
    //            @Valid
    //            private Address address;
    //        (2)针对单个入参
    //            可以在方法上标记 @Validated 注解
    //            可以在类型上标记 @Validated 注解
    //           例子：
    //            @Validated
    //            @GetMapping("/divide")
    //            public Result<Integer> divide(@Positive(message = "被除数必须是正数") Integer dividend,
    //                                          @Positive(message = "除数必须是正数") Integer divisor){
    //            Integer divResult = dividend / divisor;
    //            return Result.ok(divResult);}
    //
    //实体类分层命名
    //VO：View Object   视图对象   UserVO   对接前端时使用的实体类对象
    //DO: Data Object   数据对象   UserDO   对接数据库时的实体类对象(数据访问层使用的实体类对象)
    //DTO：Data Transfer Object   数据传输对象   UserDTO   A模块向B模块传输数据时使用的对象(接口层使用的实体类对象)
    //BO：Business Object   业务对象   UserBO   封装业务逻辑数据的实体类对象(业务逻辑层使用的实体类对象)
}


}
