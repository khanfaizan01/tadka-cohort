package com.tadka.domain.common;

public final class Result {
    private final boolean success;
    private final String error;

    private Result(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public static Result success() { return new Result(true, ""); }
    public static Result failure(String error) { return new Result(false, error); }
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public String getError() { return error; }
}
