public class Converter {
    public static void main(String[] args) {

        /*
        日期类型转换
         */
        //(1)以请求参数形式发送数据
        //     指定日期时间类型格式注解：@DateTimeFormat(pattern = "日期时间格式")
        //     指定数值类型格式注解：@NumberFormat(pattern = "数值格式")
        //(2)以JSON请求体形式发送数据（★记住）
        //     指定数据格式注解：@JsonFormat
        //     额外说明：@JsonFormat不光管数据输入，还可以管数据输出
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date productDate;

        @NumberFormat(pattern = "###,###,###.###")
        private Double productPrice;


        /*
        BindingResult
         */
        //(1)数据绑定的概念：SpringMVC 把前端输入的数据封装到实体类中，这个过程就是数据的绑定
        //(2)作用：SpringMVC 框架在底层把数据绑定过程中出现的错误封装得到的对象
        //(3)API：
        //    hasErrors() 返回布尔值：
        //         true：说明整个绑定过程中有错误
        //         false：说明整个绑定过程中没有错误
        //    getFieldErrors() 返回所有错误字段以及错误信息，返回一个 FieldError 组成的 List 集合
        //         FieldError可以通过调用 getField() 方法获取错误字段名称
        //         FieldError可以通过调用 getDefaultMessage() 方法获取该字段的错误信息
        //(4)注意：在形参列表中，BindingResult 必须紧跟在实体类对象后，中间不能有任何其它形参声明，否则 BindingResult 失效
        @PostMapping("/converter/date/number")
        public String converterDateNumberOld(Product product, BindingResult bindingResult) {

            // 判断数据绑定过程中是否发生了错误
            if (bindingResult.hasErrors()) {
                // 如果发生了错误，那么就获取错误信息
                // 每一个 FieldError 对象代表一个字段上的错误信息
                // 所有字段的错误信息就封装为了一个 List 集合
                List<FieldError> fieldErrors = bindingResult.getFieldErrors();

                for (FieldError fieldError : fieldErrors) {
                    // fieldName：出错字段的名称
                    String fieldName = fieldError.getField();
                    System.out.println("fieldName = " + fieldName);

                    // defaultMessage：出错字段的具体出错原因信息
                    String defaultMessage = fieldError.getDefaultMessage();
                    System.out.println("defaultMessage = " + defaultMessage);
                }
            }

            return "ok " + product.toString();
        }

//    @PostMapping("/converter/date/number")
        public Result<Product> converterDateNumberNew(Product product, BindingResult bindingResult) {

            if (bindingResult.hasErrors()) {

                // Optional 也是 JDK 1.8 的新特性，它是用来帮我们把判空代码变得更简洁、优雅的语法糖
                Optional<String> optional = bindingResult.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                        .reduce((prev, next) -> prev + "●" + next);

                // optional 里面封装的数据不确定是否为 null
                // 如果我们肯定 optional 里面封装的数据非空，就调用 get() 方法获取被封装的数据
                // 如果不确定 optional 里面封装的数据是否为空，就调用 orElse() 方法，此时如果封装数据非空就返回数据本身；否则返回实参出传入的备用值
                String summaryMessage = optional.orElse("没有有效的提示信息");

                return Result.failed(55555, summaryMessage);
            }

            return Result.ok(product);
        }

        /*
        创建自定义类型转换器类
         */
        //(1)应用场景：
        //    开发人员自定义类型：由开发人员提供一个类型转换器
        //    String -----> Address
        //    "广东,深圳,黄田" ------> Address(province,city,street)
        //
        //(2)操作步骤
        //    ●声明一个类实现 org.springframework.core.convert.converter.Converter<S, T> 接口
        //        泛型 S：Source 源类型
        //        泛型 T：Target 目标类型
        //        注意：这个自定义类型转换器需要放入 IoC 容器
        //    ●实现 T convert(S source); 抽象方法
        //         在方法体中编写具体的代码执行类型转换
        //         传入的参数：需要被转换的源类型数据
        //         返回值：转换之后的目标类型结果数据
        //    ●在配置类中注册自定义的类型转换器
        //         把自定义类型转换器从 IoC 容器中装配到当前配置类
        //         要求配置类实现 WebMvcConfigurer 接口
        //         实现接口的 addFormatters()
        //创建自定义类型转换器类converter.Converter
        //Converter接口注意不要导错包：org.springframework.core.convert.converter.Converter
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

        //在配置类中注册自定义类型转换器类
        @Configuration
        public class DemoConfig implements WebMvcConfigurer {

            @Autowired
            private AddressConverter addressConverter;

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(addressConverter);
            }
        }


    }
}