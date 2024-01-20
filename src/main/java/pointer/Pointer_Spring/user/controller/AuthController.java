package pointer.Pointer_Spring.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pointer.Pointer_Spring.report.repository.BlockedUserRepository;
import pointer.Pointer_Spring.user.apple.service.AppleAuthServiceImpl;
import pointer.Pointer_Spring.user.domain.User;
import pointer.Pointer_Spring.user.dto.*;
import pointer.Pointer_Spring.user.repository.ImageRepository;
import pointer.Pointer_Spring.user.repository.UserRepository;
import pointer.Pointer_Spring.user.service.AuthServiceImpl;
import pointer.Pointer_Spring.validation.ExceptionCode;

import java.util.Optional;

@RestController("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authServiceImpl;
    //private final PasswordEncoder passwordEncoder; // test
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final ImageRepository imageRepository;
    private final AuthenticationManager authenticationManager;
    private final AppleAuthServiceImpl appleAuthService;

    @Transactional
    @PostMapping("/test")
    public ResponseEntity<Object> test(@RequestBody KakaoRequestDto signUpRequest) {

        String password = "1111"; // 오류
        KakaoRequestDto kakaoDto = new KakaoRequestDto(signUpRequest.getId(), signUpRequest.getEmail(), signUpRequest.getName(), "test");

        Optional<User> findUser = userRepository.findByEmailAndStatus(kakaoDto.getEmail(),1);
        User user;
        ExceptionCode exception;

        if (findUser.isEmpty()) {
            if (blockedUserRepository.existsByEmail(kakaoDto.getEmail())) {
                return new ResponseEntity<>(new UserDto.UserResponse(ExceptionCode.SIGNUP_LIMITED_ID), HttpStatus.OK);
            }
            user = authServiceImpl.signup(kakaoDto, User.SignupType.KAKAO.name()+kakaoDto.getEmail(), password);

            exception = ExceptionCode.SIGNUP_CREATED_OK;
        } else if (findUser.get().getType().equals(User.SignupType.APPLE)) { // email 중복
            return new ResponseEntity<>(new UserDto.UserResponse(ExceptionCode.SIGNUP_DUPLICATED_EMAIL), HttpStatus.OK);
        }
        else {
            user = findUser.get();
            exception = ExceptionCode.SIGNUP_COMPLETE;
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        "1111" // password
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        TokenDto tokenDto = authServiceImpl.createToken(authentication, user.getUserId());
        user.setToken(tokenDto.getRefreshToken());
        tokenDto.setUserId(user.getUserId());
        tokenDto.setExceptionCode(exception);
        userRepository.save(user);

        return new ResponseEntity<>(tokenDto, HttpStatus.OK);
    }

    @GetMapping("/kakao")
    public Object kakaoLogin(@RequestParam String code) {
        String accessToken = authServiceImpl.getKakaoAccessToken(code, false);
        return new ResponseEntity<>(authServiceImpl.kakaoCheck(accessToken), HttpStatus.OK);
    }

    @GetMapping("/kakao/web")
    public Object webKakaoLogin(@RequestParam String code) {
        String accessToken = authServiceImpl.getKakaoAccessToken(code, true);
        return new ResponseEntity<>(authServiceImpl.webKakaoCheck(accessToken), HttpStatus.OK);
    }

    // real
    @PostMapping("/login") // kakao social login
    public ResponseEntity<Object> login(@RequestBody TokenRequest tokenRequest) {
        return new ResponseEntity<>(authServiceImpl.kakaoCheck(tokenRequest.getAccessToken()), HttpStatus.OK);
    }

    @PostMapping("/login/web")
    public Object webKakaoLogin(@RequestBody TokenRequest tokenRequest) {
        return new ResponseEntity<>(authServiceImpl.webKakaoCheck(tokenRequest.getAccessToken()), HttpStatus.OK);
    }

    @PostMapping("/login/apple")
    public ResponseEntity<Object> loginApple(@RequestBody AppleLoginRequest appleLoginRequest) {
        return new ResponseEntity<>(appleAuthService.login(appleLoginRequest.getIdentityToken()),  HttpStatus.OK);
    }
}
