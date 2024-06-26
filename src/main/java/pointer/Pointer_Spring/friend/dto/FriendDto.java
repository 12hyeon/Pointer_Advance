package pointer.Pointer_Spring.friend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import pointer.Pointer_Spring.config.ResponseType;
import pointer.Pointer_Spring.friend.domain.Friend;
import pointer.Pointer_Spring.user.domain.User;
import pointer.Pointer_Spring.validation.ExceptionCode;

import java.util.List;


public class FriendDto {

    @Getter
    public static class RoomFriendListResponse extends ResponseType {

        List<FriendRoomInfoList> friendList;
        Long total;
        int currentPage;

        public RoomFriendListResponse(ExceptionCode exceptionCode, List<FriendRoomInfoList> friendList, Long total, int currentPage) {
            super(exceptionCode);
            this.total = total;
            this.friendList = friendList;
            this.currentPage = currentPage;
        }
    }

    @Getter
    public static class FriendInfoListResponse {

        ExceptionCode exceptionCode;
        List<FriendInfoList> friendInfoList;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String name;
        Long total;
        int currentPage;

        @JsonCreator
        public FriendInfoListResponse(@JsonProperty("exceptionCode") ExceptionCode exceptionCode,
                                      @JsonProperty("name") String name,
                                      @JsonProperty("total") Long total,
                                      @JsonProperty("friendInfoList") List<FriendInfoList> friendInfoList,
                                      @JsonProperty("currentPage") int currentPage) {
            this.exceptionCode = exceptionCode;
            this.total = total;
            this.name = name;
            this.friendInfoList = friendInfoList;
            this.currentPage = currentPage;
        }

        public FriendInfoListResponse(ExceptionCode exceptionCode, List<FriendInfoList> friendInfoList) {
            this.exceptionCode = exceptionCode;
            this.friendInfoList = friendInfoList;
        }
    }

    @Getter
    public static class FriendRoomInfoListResponse extends ResponseType {

        List<FriendInfoList> friendInfoList;
        String name;
        Long total;
        int currentPage;

        public FriendRoomInfoListResponse(ExceptionCode exceptionCode,  String name, Long total, List<FriendInfoList> friendInfoList, int currentPage) {
            super(exceptionCode);
            this.total = total;
            this.name = name;
            this.friendInfoList = friendInfoList;
            this.currentPage = currentPage;
        }
        public FriendRoomInfoListResponse(ExceptionCode exceptionCode, List<FriendInfoList> friendInfoList) {
            super(exceptionCode);
            this.friendInfoList = friendInfoList;
        }
    }

    @Data
    public static class FriendList {
        Long friendId;
        String id;
        String friendName;
        String file;

        public FriendList(User user) {
            this.friendId = user.getUserId();
            this.id = user.getId();
            this.friendName = user.getName();
        }

        public FriendList setFile(String file) {
            this.file = file;
            return this;
        }
    }

    @Data
    public static class FriendInfoList {

        private Long friendId;
        private String id;
        private String friendName;
        private String file;
        private int relationship;

        @JsonCreator
        public FriendInfoList(@JsonProperty("friendId") Long friendId,
                              @JsonProperty("id") String id,
                              @JsonProperty("friendName") String friendName,
                              @JsonProperty("file") String file,
                              @JsonProperty("relationship") int relationship) {
            this.friendId = friendId;
            this.id = id;
            this.friendName = friendName;
            this.file = file;
            this.relationship = relationship;
        }

        public FriendInfoList(User user, Friend.Relation relationship) {
            this.friendId = user.getUserId();
            this.id = user.getId();
            this.friendName = user.getName();
            this.relationship = relationship.ordinal();
        }

        public FriendInfoList setFile(String file) {
            this.file = file;
            return this;
        }
    }

    @Data
    public static class FriendRoomInfoList {

        Long friendId;
        String id;
        String friendName;
        String file;
        int status; //초대 가능한지

        public FriendRoomInfoList(User user, int status) {
            this.friendId = user.getUserId();
            this.id = user.getId();
            this.friendName = user.getName();
            this.status = status;
        }

        public FriendRoomInfoList setFile(String file) {
            this.file = file;
            return this;
        }
    }


    @Data
    public static class FriendUserDto {
        //private Long userId;
        // pageable
        private int lastPage;

    }

    @Data
    public static class FindFriendDto {
        private Long userId;
        private String keyword;
        // pageable
        private int lastPage;

    }

    @Data
    public static class FindFriendFriendDto {
        private Long userId;
        private String keyword;
        // pageable
        private int lastPage;

        public FindFriendFriendDto(Long userId, int lastPage) {
            this.userId = userId;
            this.lastPage = lastPage;
        }
    }

    @Data
    public static class UserDto {
        private User user;
    }

    @Data
    public static class RequestFriendDto {
        //private Long userId;
        private Long memberId;
    }
}

