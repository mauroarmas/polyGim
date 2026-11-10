package com.gymplatform.controller.error;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ProblemFactory {
    private ProblemFactory() { }

    public static ProblemDetail create(HttpStatus status, String code, String title, String detail, String path) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("urn:gym-api:problem:" + code.toLowerCase(java.util.Locale.ROOT).replace('_', '-')));
        problem.setTitle(title);
        problem.setInstance(URI.create(path));
        problem.setProperty("code", code);
        return problem;
    }
}
