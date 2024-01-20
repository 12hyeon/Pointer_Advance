package pointer.Pointer_Spring.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pointer.Pointer_Spring.friend.service.FriendService;
import pointer.Pointer_Spring.security.CurrentUser;
import pointer.Pointer_Spring.security.UserPrincipal;
import pointer.Pointer_Spring.user.dto.UserDto;
import pointer.Pointer_Spring.user.response.ResponseUser;
import pointer.Pointer_Spring.user.service.AuthService;
import pointer.Pointer_Spring.user.service.CloudinaryService;
import pointer.Pointer_Spring.user.service.UserService;
import pointer.Pointer_Spring.validation.ExceptionCode;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final AuthService authService;
    private final FriendService friendService;

    @GetMapping("/check")
    public ResponseEntity<Object> tokenCheck() {
        return new ResponseEntity<>(new UserDto.UserResponse(ExceptionCode.TOKEN_SUCCESS), HttpStatus.OK);
    }

    @PostMapping("/reissue") // token 재발급
    public ResponseEntity<Object> reissue(@CurrentUser UserPrincipal userPrincipal) {
        return new ResponseEntity<>(authService.reissue(userPrincipal), HttpStatus.OK);
    }

    @PostMapping("/agree") // 동의
    public ResponseEntity<Object> saveAgree(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.UserAgree agree) {
        return new ResponseEntity<>(authService.saveAgree(userPrincipal, agree), HttpStatus.OK);
    }

    @PostMapping("/marketing") // 마케팅 상태 변경
    public ResponseEntity<Object> updateMarketing(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.UserMarketing marketing) {
        return new ResponseEntity<>(authService.updateMarketing(userPrincipal, marketing), HttpStatus.OK);
    }

    @PostMapping("/id") // id 저장
    public ResponseEntity<Object> saveId(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.BasicUser info) {
        return new ResponseEntity<>(authService.saveId(userPrincipal, info), HttpStatus.OK);
    }

    @PostMapping("/check") // 중복 확인
    public ResponseEntity<Object> checkId(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserDto.BasicUser info) {
        return new ResponseEntity<>(authService.checkId(userPrincipal, info), HttpStatus.OK);
    }

    @GetMapping("/search") // 유저 검색 : 관계 포함
    public UserDto.UserInfoListResponse getUserInfoList(@CurrentUser UserPrincipal userPrincipal,
                                                        @RequestParam String keyword,
                                                        @RequestParam int lastPage) {
        return friendService.getUserInfoList(userPrincipal, keyword, lastPage);
    }

    @GetMapping("/get/points")
    public ResponseUser getPoint(@CurrentUser UserPrincipal userPrincipal){
        return userService.getPoints(userPrincipal.getId());
    }

/*    @PatchMapping ("/update/info")
    public ResponseUser updateUserInfo(@CurrentUser UserPrincipal userPrincipal,
                                       @RequestPart(value = "profile-image", required = false) MultipartFile profileImage,
                                       @RequestPart(value = "background-image", required = false) MultipartFile backgroundImage,
                                       @Valid @RequestPart("request") UserDto.UpdateUserInfoRequest updateUserInfoRequest, BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            throw new CustomException(ExceptionCode.INVALID_FORM);
        }
        Long userId = userPrincipal.getId();
        if(profileImage != null){
            cloudinaryService.uploadProfileImage(userId, profileImage);
        } else if(updateUserInfoRequest.isProfileImageDefaultChange()){
            cloudinaryService.changeDefaultProfileImage(userId);
        }

        if(backgroundImage != null){
            cloudinaryService.uploadBackgroundImage(userId, backgroundImage);
        }else if(updateUserInfoRequest.isBackgroundImageDefaultChange()){
            cloudinaryService.changeDefaultBackgroundImage(userId);
        }

        return userService.updateNm(userId, updateUserInfoRequest.getName());
    }*/

//    @PatchMapping("/update/id")
//    public ResponseUser updateUserId(@CurrentUser UserPrincipal userPrincipal, @Valid @RequestBody UserDto.UpdateIdRequest updateIdRequest, BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) {
//            throw new CustomException(ExceptionCode.INVALID_FORM);
//        }
//        return userService.updateId(userPrincipal.getId(), updateIdRequest.getId());
//    }

    @GetMapping(value = {"{targetUserId}/info", "/info"})
    public ResponseUser getUserInfo(@CurrentUser UserPrincipal userPrincipal, @PathVariable(required = false) Long targetUserId){
        return userService.getUserInfo(userPrincipal, targetUserId);
    }

    @PostMapping("/logout") // 로그아웃
    public ResponseEntity<Object> logout(@CurrentUser UserPrincipal userPrincipal) {
        return new ResponseEntity<>(authService.logout(userPrincipal), HttpStatus.OK);
    }

//    @PostMapping("/logout/apple")
//    public ResponseEntity<Object> logoutApple(@CurrentUser UserPrincipal userPrincipal) {
//
//        return new ResponseEntity<>(appleAuthService.logout(userPrincipal), HttpStatus.OK);
//    }

    @DeleteMapping("/resign") // 회원 탈퇴
    public ResponseEntity<Object> resign(@CurrentUser UserPrincipal userPrincipal) {
        return new ResponseEntity<>(authService.resign(userPrincipal), HttpStatus.OK);
    }
}
