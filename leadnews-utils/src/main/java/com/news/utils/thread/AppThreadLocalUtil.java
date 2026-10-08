package com.news.utils.thread;

public class AppThreadLocalUtil {
    private static final ThreadLocal<Integer> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserId(Integer userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    public static Integer getUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }
}
