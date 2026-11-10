package com.gymplatform.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.gymplatform.controller.error.ApiExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProblemContractTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var mapper = Jackson2ObjectMapperBuilder.json().featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @Test
    void invalidFieldsProduceProblemWithFieldErrors() throws Exception {
        mvc.perform(post("/test").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/test"))
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void unknownPropertiesAreRejected() throws Exception {
        mvc.perform(post("/test").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"ok\",\"tenant\":\"other\"}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unsupportedMethodAndContentTypeUseProblemFormat() throws Exception {
        mvc.perform(get("/test")).andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(post("/test").contentType(MediaType.TEXT_PLAIN).content("bad"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void unexpectedErrorsDoNotExposeInternalDetails() throws Exception {
        mvc.perform(post("/test/error")).andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("sensitive-internal-marker"))));
    }

    record Input(@NotBlank String name) { }

    @RestController
    static class TestController {
        @PostMapping(path = "/test", consumes = "application/json")
        Input input(@Valid @RequestBody Input input) { return input; }
        @PostMapping("/test/error")
        void error() { throw new IllegalStateException("sensitive-internal-marker"); }
    }
}
