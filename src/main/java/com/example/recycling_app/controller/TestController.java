package com.example.recycling_app.controller;

import com.example.recycling_app.domain.Test;
import com.example.recycling_app.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("test")
public class TestController {
    Test test;
    private final TestService testService;

    @GetMapping("/get")
    public ResponseEntity<List<Test>> getUser() throws Exception{
        List<Test> list = testService.getUsers();
        return ResponseEntity.ok(list);
    }
}
