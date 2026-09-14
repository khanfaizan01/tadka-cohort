package com.tadka.domain.common;

public final class ResultT<T> {
    private final boolean success;
    private final T value;
    private final String error;

    private ResultT(boolean success, T value, String error) {
        this.success = success;
        this.value = value;
        this.error = error;
    }

    public static <T> ResultT<T> success(T value) { return new ResultT<>(true, value, ""); }
    public static <T> ResultT<T> failure(String error) { return new ResultT<>(false, null, error); }
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public T getValue() { return value; }
    public String getError() { return error; }
}
