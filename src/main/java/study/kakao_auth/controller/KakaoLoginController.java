package study.kakao_auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import study.kakao_auth.service.KakaoService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        log.info("카카오로부터 받아온 인가 코드: {}", code);

        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}