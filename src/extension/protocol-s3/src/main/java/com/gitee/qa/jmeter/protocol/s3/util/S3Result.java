package com.gitee.qa.jmeter.protocol.s3.util;

import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.S3ResponseMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * S3操作结果包装类
 * 用于统一返回操作结果、错误信息和数据
 *
 * @param <T> 数据类型
 */
public class S3Result<T> {

    private boolean success;
    private String errorMessage;
    private T data;
    private SdkHttpResponse sdkHttpResponse;
    private S3ResponseMetadata responseMetadata;

//    private S3Result(boolean success, String errorMessage, T data) {
//        this.success = success;
//        this.errorMessage = errorMessage;
//        this.data = data;
//    }

    private S3Result(boolean success, String errorMessage, T data,
                     SdkHttpResponse sdkHttpResponse, S3ResponseMetadata responseMetadata) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = data;
        this.sdkHttpResponse = sdkHttpResponse;
        this.responseMetadata = responseMetadata;
    }

    /**
     * 创建成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> S3Result<T> success(T data) {
        return new S3Result<>(true, null, data, null, null);
    }

    /**
     * 创建成功结果（带响应信息）
     *
     * @param data             数据
     * @param sdkHttpResponse  SDK HTTP响应信息
     * @param responseMetadata 响应元数据
     * @param <T>              数据类型
     * @return 成功结果
     */
    public static <T> S3Result<T> success(T data,
                                          SdkHttpResponse sdkHttpResponse,
                                          S3ResponseMetadata responseMetadata) {
        return new S3Result<>(true, null, data, sdkHttpResponse, responseMetadata);
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @param <T>          数据类型
     * @return 失败结果
     */
    public static <T> S3Result<T> failure(String errorMessage) {
        return new S3Result<>(false, errorMessage, null, null, null);
    }

    /**
     * 创建失败结果（带异常）
     *
     * @param e   异常
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> S3Result<T> failure(Exception e) {
        return new S3Result<>(false, e.getMessage(), null, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }

    public SdkHttpResponse getSdkHttpResponse() {
        return sdkHttpResponse;
    }

    public S3ResponseMetadata getResponseMetadata() {
        return responseMetadata;
    }

    public String getSdkHttpResponseStatusCode() {
        if (sdkHttpResponse == null) {
            return "null";
        } else {
            return String.valueOf(sdkHttpResponse.statusCode());
        }
    }

    public String getSdkHttpResponseStatusText() {
        if (sdkHttpResponse == null) {
            return "null";
        } else {
            return sdkHttpResponse.statusText().get();
        }
    }

    @Override
    public String toString() {
        return "S3Result{" +
                "success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", data=" + data +
                ", sdkHttpResponse=" + sdkHttpResponse +
                ", responseMetadata=" + responseMetadata +
                '}';
    }
}
