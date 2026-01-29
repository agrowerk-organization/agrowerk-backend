package tech.agrowerk.application.dto.auth;

import lombok.Builder;
import org.springframework.http.ResponseCookie;
import tech.agrowerk.application.dto.user.UserInfoDto;

@Builder
public record LoginResult(
        ResponseCookie accessCookie,
        ResponseCookie refreshCookie,
        UserInfoDto userInfoDto,
        Long expiresIn
) {
}
