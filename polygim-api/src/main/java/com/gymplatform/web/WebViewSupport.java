package com.gymplatform.web;

import com.gymplatform.controller.dto.PageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

final class WebViewSupport {
    private WebViewSupport() { }
    static <T> List<T> all(IntFunction<PageResponse<T>> load) {
        var values = new ArrayList<T>();
        int number = 0;
        PageResponse<T> page;
        do { page = load.apply(number++); values.addAll(page.content()); } while (number < page.totalPages());
        return List.copyOf(values);
    }
}
