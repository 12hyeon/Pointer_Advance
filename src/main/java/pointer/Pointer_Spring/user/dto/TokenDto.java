package pointer.Pointer_Spring.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenDto {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
