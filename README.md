![스크린샷 2025-06-23 오후 5 38 21](https://github.com/user-attachments/assets/4caf8b36-85b7-45d8-bfae-fb4bcb7015c2)

> 카카오 공식 문서를 기반으로 로그인 연동 절차를 이해하고 적용하는 실습 코드입니다. <br>
> Spring Boot 3.5.3 환경에서의 구현을 기반으로 작성되었으며, 클라이언트는 Thymeleaf 템플릿 엔진을 사용해 구성했습니다.

> **https://developers.kakao.com/product/kakaoLogin**

<br>

## 카카오 로그인 이해하기
![kakaologin_process](https://github.com/user-attachments/assets/679b3a31-9fef-4166-9ac8-7c9f40ad051c)
> 카카오 로그인으로 서비스에 로그인하는 과정

<br>

### 1. 로그인 시작
- 사용자가 "카카오로 로그인" 버튼을 누르면, `client_id`와 `redirect_uri`가 포함된 인증 요청 URL로 이동합니다.

![스크린샷 2025-06-23 오후 6 27 20](https://github.com/user-attachments/assets/24a8e256-63b3-4db6-a5f9-61735f9c67a1)
```java
@Controller
@RequestMapping("/login")
public class KakaoLoginPageController {

    @Value("${kakao.client_id}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @GetMapping("/page")
    public String loginPage(Model model) {
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+client_id+"&redirect_uri="+redirect_uri;
        model.addAttribute("location", location);

        return "login";
    }
}
```
```html
<h1>카카오 로그인</h1>
<a th:href="${location}">
  <img src="/kakao_login_medium_narrow.png" alt="카카오 로그인 버튼">
</a>
```

### 2. 카카오 계정 인증
- 카카오 로그인 페이지로 리디렉션되며, 사용자는 자신의 카카오 계정으로 로그인합니다.
![스크린샷 2025-06-23 오후 6 18 39](https://github.com/user-attachments/assets/b9e8f4f9-88bc-4486-bff4-93b30cc55208)


### 3. 인가 코드 수신
- 로그인이 완료되면, 카카오는 미리 설정한 Redirect URI로 사용자 브라우저를 이동시키며 `code` 파라미터를 함께 전달합니다.  
- 이 `code`는 인증을 증명하는 인가 코드입니다.
```java
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
```

### 4. 토큰 교환
- 서버는 이 인가 코드를 카카오 토큰 발급 엔드포인트에 전달하여, 액세스 토큰과 리프레시 토큰을 발급받습니다.
- `https://kauth.kakao.com/oauth/token` URL로 POST 요청을 보내 토큰 발급 요청 (추후 토큰으로 사용자 정보 요청)
- 카카오 측 응답 파라미터를 참고하여 DTO 클래스 생성 후 매핑 필요

> https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token-response-body

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final WebClient kakaoAuthWebClient;

    @Value("${kakao.client_id}")
    private String clientId;

    public String getAccessTokenFromKakao(String code) {
        KakaoTokenResponseDto kakaoTokenResponseDto = kakaoAuthWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build(true))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        log.info("[KakaoService] access_token = {}", kakaoTokenResponseDto.getAccessToken());
        log.info("[KakaoService] refresh_token = {}", kakaoTokenResponseDto.getRefreshToken());

        // OpenID Connect를 활성화하면 카카오 로그인 시 사용자 인증 정보가 담긴 ID 토큰을 액세스 토큰과 함께 발급받을 수 있음.
        log.info("[KakaoService] id_token = {}", kakaoTokenResponseDto.getIdToken());
        log.info("[KakaoService] scope = {}", kakaoTokenResponseDto.getScope());

        return Objects.requireNonNull(kakaoTokenResponseDto).getAccessToken();
    }

}
```

### 5. 사용자 정보 요청
- 발급받은 액세스 토큰을 이용해 카카오 API에 사용자 정보를 요청할 수 있습니다.

### 6. 서비스 로그인 처리
- 가져온 사용자 정보를 기반으로 기존 회원 여부를 확인하고, 신규라면 회원가입, 기존 유저라면 로그인 처리를 합니다.
