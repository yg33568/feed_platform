package com.feed.common;

public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public Result(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }
    public static <T> Result<T> success(T data){
        return new Result<>(200,data,"success");
    }
    //系统错误，不关心状态码
    public static <T> Result<T> error(String message){
        return new Result<>(500,null,message);
    }
    //限流(429)、未登录(401)、无权限(403)
    public static <T> Result<T> error(Integer code,String message){
        return new Result<>(code,null,message);
    }

    // getter 和 setter
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
