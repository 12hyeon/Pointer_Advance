package pointer.Pointer_Spring.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pointer.Pointer_Spring.security.CurrentUser;
import pointer.Pointer_Spring.security.UserPrincipal;
import pointer.Pointer_Spring.user.dto.UserDto;
import pointer.Pointer_Spring.user.response.ResponseUser;
import pointer.Pointer_Spring.user.service.UserService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserController2 {

    private final UserService userService;

    @PatchMapping("/update/name") // 이름 수정 : 중복 가능
    public ResponseUser updateUserName(@CurrentUser UserPrincipal userPrincipal,
                                     @Valid @RequestBody UserDto.UpdateNameRequest updateNameRequest) {
        return userService.updateNmBatch(userPrincipal.getId(), updateNameRequest.getName());
    }
}
