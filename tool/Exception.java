public class Exception {
    public static void main(String[] args) {
         //1、希望达到的效果
        //        程序运行的过程中抛出异常，但是不把异常本身返回给前端让用户看到，而是给用户返回友好的提示信息
        //        后端负责视图的渲染：捕获到异常后，跳转到指定的视图页面
        //        前端负责视图的渲染：后端捕获到异常后，还是把 Result 对象封装为 JSON 数据返回给前端（Result.failed()）
        //
        //        另外，从代码实现的层面来说，我们希望针对异常的解析和返回相关数据的操作不需要在每一个 Controller 方法都写一遍，而是可以在全局范围内做统一设定
        //
        //    2、全局范围统一设定
        //        例如：UserLoginFailedException异常，在专门的类中设定这个异常返回指定的数据
        //        例如：UserLoginAccountAlreadyExistsException异常，在专门的类中设定这个异常返回指定的数据
        //        例如：从宏观上针对 Exception 异常返回一个统一的结果
        //        好处1：实现异常处理代码的复用
        //        好处2：异常处理代码集中到一起，便于统一管理
        //
        //    3、API用法总结（基于前端渲染页面模式）
        //        异常处理器类上标记 @RestControllerAdvice 注解
        //        异常处理方法上标记 @ExceptionHandler 注解
        //            value 属性指定要映射的异常类型
        //            被标记的方法上使用形参声明接收异常对象
        //            被标记方法的返回值作为响应体返回给前端
        //
        //    4、匹配规则
        //        对于捕获到的异常类型，映射的异常类型有精确匹配，优先采纳精确匹配
        //        如果没有精确匹配，则采纳范围上最接近的
        //
        //    5、实际开发时往往会根据业务需求，创建很多自定义异常，例如：
        //        UserLoginFailedException：用户登录失败异常
        //        LoginAccountAlreadyExitsException：登录账号已存在异常
        //        OrderDuplicateSubmitException：订单重复提交异常
        //        ……
        //        此时我们往往会把 Result.failed(code, message) 中需要的 code 和 message 封装到一个枚举类型中
        //        另外再写一个 Exception 或 Throwable 范围的全局映射
        //    例子：
        @RestControllerAdvice
        public class MyExceptionAdvice {

            /**
             * @ExceptionHandler 注解：表示在 SpringMVC 全局范围内，只要捕获到指定异常，就交给被标记的方法来处理
             * @param arithmeticException 当前捕获到的异常对象
             *               这里形参的声明，要么就写当前方法映射的异常类型，要么写该异常父类类型也可以，不能写它的子类（多态性）
             * @return 由于类上使用的是 @RestController，作为当前方法的返回值会被作为响应体返回给前端
             */
            @ExceptionHandler(value = ArithmeticException.class)
            public Result<Void> arithmeticExceptionHandler(ArithmeticException arithmeticException) {
                return Result.failed(99999, arithmeticException.getMessage());
            }

            @ExceptionHandler(value = IndexOutOfBoundsException.class)
            public Result<Void> indexOutOfBoundsExceptionHandler(IndexOutOfBoundsException indexOutOfBoundsException) {
                return Result.failed(88888, indexOutOfBoundsException.getMessage());
            }

        //    @ExceptionHandler(value = Exception.class)
        //    public Result<Void> exceptionHandler(Exception e) {
        //        return Result.failed(66666, e.getMessage());
        //    }

        }

    }
}