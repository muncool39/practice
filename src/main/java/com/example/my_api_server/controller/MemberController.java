package com.example.my_api_server.controller;

import ch.qos.logback.core.util.StringUtil;
import com.example.my_api_server.service.MemberDBService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberDBService memberService;

    @PostMapping
    public Long signup(@Validated @RequestBody MemberSignupDto dto) {
        if (StringUtil.isNullOrEmpty(dto.email()) || StringUtil.isNullOrEmpty(dto.password())) {
            throw new RuntimeException("email or password가 비면 안됩니다.");
        }
        Long memberId = null;
        try {
            memberId = memberService.signUp(dto.email(), dto.password());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return memberId;
    }

    @GetMapping("/test")
    public void test() {
        memberService.tx1();
    }

//    @GetMapping("/{id}")
//    public Member findMember(@PathVariable Long id) {
//        return memberService.findMember(id);
//    }
}





















