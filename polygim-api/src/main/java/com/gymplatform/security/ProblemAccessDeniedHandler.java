package com.gymplatform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymplatform.controller.error.ProblemFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;
    public ProblemAccessDeniedHandler(ObjectMapper mapper) { this.mapper = mapper; }
    @Override public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), ProblemFactory.create(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Acceso denegado", "No tenés permiso para esta operación.", request.getRequestURI()));
    }
}
