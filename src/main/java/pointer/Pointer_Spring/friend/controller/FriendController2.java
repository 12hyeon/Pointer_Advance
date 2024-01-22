package pointer.Pointer_Spring.friend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pointer.Pointer_Spring.friend.dto.FriendDto;
import pointer.Pointer_Spring.friend.service.FriendService;
import pointer.Pointer_Spring.security.CurrentUser;
import pointer.Pointer_Spring.security.UserPrincipal;

@RestController
@RequestMapping("/api/v2/friends")
@RequiredArgsConstructor
public class FriendController2 {

    private final FriendService friendService;

    @GetMapping("/search") // 친구 목록 조회
    public FriendDto.FriendInfoListResponse getUserFriendList(@CurrentUser UserPrincipal userPrincipal,
                                                              @RequestParam Long userId,
                                                              @RequestParam String keyword,
                                                              @RequestParam int lastPage) {
        return friendService.getUserFriendList2(userPrincipal, userId, keyword, lastPage);
    }

}
