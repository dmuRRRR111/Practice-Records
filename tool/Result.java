package com.atguigu.entity;

/**
 * 我们封装 Result 这个类，让后端所有响应数据都按照这个格式返回
 * 为什么能涵盖所有响应结果的数据结构？
 * 因为响应无非是成功和失败：
 *      失败：不管增删改查什么操作，都返回 code 和 message
 *      成功：
 *          增删改成功，返回 code 和 message（OK）
 *          查询成功，返回 code 和 message（OK）还有数据
 * 友情提示：这个类不需要自己写，用的时候可以复制粘贴，理解就行
 *
 * code 属性：不是 HTTP 响应状态码，而是我们根据项目的实际情况自定义的
 * message 属性：返回响应的消息说明
 *      成功：OK
 *      失败：错误消息
 * data 属性：查询结果数据
 *
 * @param <T>
 */
public class Result<T> {

    private int code;
    private T data;
    private String message;

    /**
     * 增删改操作成功
     * @return
     * @param <T>
     */
    public static <T> Result<T> ok() {
        return new Result<>(2000, null, "ok");
    }

    /**
     * 查询操作成功
     * @param data 查询结果，作为响应数据的一部分返回给前端
     * @return
     * @param <T>
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(2000, data, "ok");
    }

    /**
     * 任何操作失败
     * @param code 由开发人员指定一个错误码
     * @param message 当前故障情况的说明信息
     * @return
     * @param <T>
     */
    public static <T> Result<T> failed(int code, String message) {
        return new Result<>(code, null, message);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Result() {
    }
}