package study.kakao_auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 외부 API에서 예상하지 못한 필드가 포함돼도 오류 없이 무시하고 역직렬화를 수행
public class KakaoTokenResponseDto {

    // 역직렬화 이유 : 카카오 API 같은 외부 서비스에서 토큰 정보를 JSON 형식으로 받으면, 자바 객체로 변환해야함
    // 역직렬화를 통해 KakaoTokenResponseDto 객체에 데이터가 채워짐
    @JsonProperty("token_type")
    public String tokenType;
    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("id_token")
    public String idToken;
    @JsonProperty("expires_in")
    public Integer expiresIn;
    @JsonProperty("refresh_token")
    public String refreshToken;
    @JsonProperty("refresh_token_expires_in")
    public Integer refreshTokenExpiresIn;
    @JsonProperty("scope")
    public String scope;

}
