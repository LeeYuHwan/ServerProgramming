package com.practice.demo.polling;

import com.practice.demo.controller.LongPollingExController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.AsyncListener;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LongPollingExControllerTest {
    MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LongPollingExController()).build();
    }

    @Test
    public void givenUserNameParams_whenRequestAuthentication_thenIsOk() throws Exception {

        mockMvc.perform(get("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());
    }

    @Test
    public void givenUserNameParams_whenRequestAuthentication_thenPoolSizeOne() throws Exception {

        mockMvc.perform(get("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        mockMvc.perform(get("/pool-size"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    public void givenUserNameParams_whenAuthenticate_thenIsOk() throws Exception {

        mockMvc.perform(post("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk());
    }

    @Test
    public void givenRequestAuthenticationAuthenticate_whenAsyncDispatch_thenReturnTrue() throws Exception {

        // given
        MvcResult result = mockMvc.perform(get("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(post("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk());

        // when, then
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void givenRequestAuthenticationAuthenticate_whenAsyncDispatch_thenPoolSizeZero() throws Exception {

        // given
        mockMvc.perform(get("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(post("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(status().isOk());

        // when, then
        mockMvc.perform(get("/pool-size"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    public void givenRequestAuthentication_whenTimeout_thenIs5xxServerError() throws Exception {

        // given
        MvcResult result = mockMvc.perform(get("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // when
        MockAsyncContext ctx = (MockAsyncContext) result.getRequest().getAsyncContext();
        for (AsyncListener listener : ctx.getListeners()) {
            listener.onTimeout(null);
        }

        // then
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void givenRequestAuthentication_whenTimeout_thenPoolSizeZero() throws Exception {

        // given
        MvcResult result = mockMvc.perform(get("/auth")
                        .param("order", "OrderByDoha"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // when
        MockAsyncContext ctx = (MockAsyncContext) result.getRequest().getAsyncContext();
        for (AsyncListener listener : ctx.getListeners()) {
            listener.onTimeout(null);
        }

        // then
        mockMvc.perform(get("/pool-size"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}
