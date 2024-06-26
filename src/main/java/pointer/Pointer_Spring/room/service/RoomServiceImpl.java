package pointer.Pointer_Spring.room.service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pointer.Pointer_Spring.config.BaseEntity;
import pointer.Pointer_Spring.friend.domain.Friend;
import pointer.Pointer_Spring.friend.repository.FriendRepository;
import pointer.Pointer_Spring.question.domain.Question;
import pointer.Pointer_Spring.question.repository.QuestionRepository;
import pointer.Pointer_Spring.report.repository.ReportRepository;
import pointer.Pointer_Spring.room.util.Base62Util;
import pointer.Pointer_Spring.security.UserPrincipal;
import pointer.Pointer_Spring.user.domain.User;
import pointer.Pointer_Spring.user.repository.ImageRepository;
import pointer.Pointer_Spring.user.repository.UserRepository;
import pointer.Pointer_Spring.room.domain.Room;
import pointer.Pointer_Spring.room.domain.RoomMember;
import pointer.Pointer_Spring.room.dto.RoomDto;
import pointer.Pointer_Spring.room.dto.RoomDto.CreateRequest;
import pointer.Pointer_Spring.room.dto.RoomDto.DetailResponse;
import pointer.Pointer_Spring.room.dto.RoomDto.InviteRequest;
import pointer.Pointer_Spring.room.dto.RoomDto.*;
import pointer.Pointer_Spring.room.dto.RoomMemberDto.*;
import pointer.Pointer_Spring.room.repository.RoomMemberRepository;
import pointer.Pointer_Spring.room.repository.RoomRepository;
import pointer.Pointer_Spring.room.response.ResponseMemberRoom;
import pointer.Pointer_Spring.room.response.ResponseRoom;
import pointer.Pointer_Spring.user.service.AuthService;
import pointer.Pointer_Spring.validation.CustomException;
import pointer.Pointer_Spring.validation.ExceptionCode;
import pointer.Pointer_Spring.vote.domain.VoteHistory;
import pointer.Pointer_Spring.vote.repository.VoteRepository;


@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final FriendRepository friendRepository;
    private final VoteRepository voteRepository;
    private final QuestionRepository questionRepository;
    private final ReportRepository reportRepository;
    private final ImageRepository imageRepository;
    private final AuthService authService;
    private final Base62Util base62Util;

    private final String FIRST_QUESTION = "첫인상이 좋았던 사람을 지목해주세요";

    private final Integer STATUS = 1;
    private final Integer PAGE_COUNT = 30;

    @Transactional
    @Override//질문 생성 시 마다 room updateAt도 같이 시간 update하기
    public ResponseRoom getRoomList(UserPrincipal userPrincipal, String kwd, HttpServletRequest request) {//검색 추가
        List<RoomDto.ListRoom> roomListDto = new ArrayList<>();
        Long userId = userPrincipal.getId();
        if (kwd == null) {
            roomListDto = roomMemberRepository.findAllByUserUserIdAndRoom_StatusEqualsAndStatusOrderByRoom_UpdatedAtAsc(userId,  STATUS, STATUS)
                    .stream()
                    .map(roomMember -> {
                        Room room = roomMember.getRoom();
                        Question latestQuestion = room.getQuestions().stream()
                                .max(Comparator.comparing(BaseEntity::getUpdatedAt))
                                .orElseThrow(()-> new CustomException(ExceptionCode.QUESTION_NOT_FOUND));//updatedAt 기준

                        String msgForTopUserNm = getTopUserNm(room, latestQuestion.getId());

                        boolean isVoted = voteRepository.existsByMemberIdAndQuestionIdAndStatus(userId, latestQuestion.getId(), STATUS);
                        Optional<Question> topQuestion = questionRepository.findTopByRoomRoomIdAndStatusOrderByIdDesc(room.getRoomId(), STATUS);
                        LocalDateTime questionCreateDate = LocalDateTime.now();
                        if(topQuestion.isPresent()){
                            Question question = topQuestion.get();
                            questionCreateDate = question.getCreatedAt().plusDays(1);
                        }

                        return new ListRoom(roomMember, latestQuestion, msgForTopUserNm, isVoted, questionCreateDate);
                    })
                    .sorted(Comparator.comparing(room -> room.getLimitedAt(), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } else {
            roomListDto = roomMemberRepository.findAllByUserUserIdAndRoom_StatusEqualsAndStatusOrderByRoom_UpdatedAtAsc(userId, STATUS, STATUS).stream()
                    .filter(roomMember -> {
                        Room room = roomMember.getRoom();
                        Optional<Question> latestQuestion = room.getQuestions().stream()
                                .max(Comparator.comparing(BaseEntity::getUpdatedAt));//updatedAt 기준
                        return roomMember.getPrivateRoomNm().contains(kwd) || latestQuestion.map(question -> question.getQuestion().contains(kwd)).orElse(false);
                    })
//                    .map(RoomDto.ListRoom::new).toList()
                    .map(roomMember -> {
                        Room room = roomMember.getRoom();
                        Question latestQuestion = room.getQuestions().stream()
                                .max(Comparator.comparing(BaseEntity::getUpdatedAt))
                                .orElseThrow(()-> new CustomException(ExceptionCode.QUESTION_NOT_FOUND));//updatedAt 기준

                        String msgForTopUserNm = getTopUserNm(room, latestQuestion.getId());

                        boolean isVoted = voteRepository.existsByMemberIdAndQuestionIdAndStatus(userId, latestQuestion.getId(), STATUS);
                        Optional<Question> o = questionRepository.findTopByRoomRoomIdAndStatusOrderByIdDesc(room.getRoomId(), STATUS);
                        LocalDateTime questionCreateDate = LocalDateTime.now();
                        if(o.isPresent()){
                            Question question = o.get();
                            questionCreateDate = question.getCreatedAt().plusDays(1);
                        }

                        return new ListRoom(roomMember, latestQuestion, msgForTopUserNm, isVoted, questionCreateDate);
                    })
                    .sorted(Comparator.comparing(room -> room.getLimitedAt(), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }
        return new ResponseRoom(ExceptionCode.ROOM_FOUND_OK, new ListResponse(roomListDto));
    }
    private String getTopUserNm(Room room, Long latestQuestionId){
        List<RoomMember> roomMembers = roomMemberRepository.findAllByRoom(room);
        int maxVote = 0;
        RoomMember topMem = roomMembers.get(0);
        for (RoomMember roomMem : roomMembers) {
            User member = roomMem.getUser();
            //QuestionIdAndCandidateId로 찾아서 updatedAt으로 최신순 sort해서 첫번째 UPDAtdAt끼리 비교 후 반환
            int votedCnt = voteRepository.countByQuestionIdAndCandidateId(latestQuestionId, member.getUserId());
            if(votedCnt>maxVote){
                topMem = roomMem;
                maxVote = votedCnt;
            }else if(maxVote != 0 & votedCnt == maxVote){
                VoteHistory newVote = voteRepository.findTopByQuestionIdAndCandidateIdOrderByUpdatedAtDesc(latestQuestionId, member.getUserId());
                VoteHistory topVote = voteRepository.findTopByQuestionIdAndCandidateIdOrderByUpdatedAtDesc(latestQuestionId, topMem.getUser().getUserId());
                topMem = newVote.getUpdatedAt().isAfter(topVote.getUpdatedAt()) ? roomMem : topMem;
            }
        }
        //투표자가 없는 경우도 고려
        String msgForTopUserNm = maxVote == 0 ? null : topMem.getUser().getName();
        return msgForTopUserNm;
    }

    public ResponseRoom getRoom(Long targetUserId, Long roomId) {//질문, 투표 등까지 같이 가져오기[합친 후에]
        Room foundRoom = roomRepository.findById(roomId).orElseThrow(
            () -> {
                throw new CustomException(ExceptionCode.ROOM_NOT_FOUND);
            }
        );
        Question latestQuestion = foundRoom.getQuestions().stream()
                .max(Comparator.comparing(BaseEntity::getUpdatedAt)).orElseThrow(
                        () -> {
                            throw new CustomException(ExceptionCode.QUESTION_NOT_FOUND);
                        }
                );

        List<RoomMemberResopnose> roomMemberResopnoseList = roomMemberRepository.findAllByRoomAndStatus(foundRoom,STATUS).stream()
                .map(RoomMemberResopnose::new).collect(Collectors.toList());
        String targetUserPrivateRoomNm = roomMemberRepository.findByRoom_RoomIdAndUser_UserIdAndStatus(foundRoom.getRoomId(), targetUserId, STATUS)
                .orElseThrow(()->new CustomException(ExceptionCode.ROOMMEMBER_NOT_EXIST)).getPrivateRoomNm();
        return new ResponseRoom(ExceptionCode.ROOM_FOUND_OK, new DetailResponse(foundRoom, targetUserPrivateRoomNm, latestQuestion, roomMemberResopnoseList, latestQuestion.getCreatedAt().plusDays(1)));
    }


    //validation 정해지면 ( 룸 이름 중복 가능)
    public ResponseRoom createRoom(UserPrincipal userPrincipal, CreateRequest createRoomDto, HttpServletRequest request) {
        isValidRoomNmLength(createRoomDto.getRoomNm());

        User foundUser = userRepository.findById(userPrincipal.getId()).orElseThrow(//이후 토큰으로 변경 필요
            () -> {
                throw new CustomException(ExceptionCode.USER_NOT_FOUND);
            }
        );
        Integer roomLimit = foundUser.getRoomLimit();
        if(roomLimit > 30){//방 입장 가능 횟수 제한
            return new ResponseRoom(ExceptionCode.ROOM_CREATE_OVER_LIMIT);
        }

        Room createdRoom = new Room(foundUser.getUserId(), createRoomDto.getRoomNm());
        roomRepository.saveAndFlush(createdRoom);
        Room savedRoom = roomRepository.findById(createdRoom.getRoomId()).orElseThrow(
                () -> {
                    throw new CustomException(ExceptionCode.ROOM_CREATE_FAIL);
                }
        );
        RoomMember roomMember = new RoomMember(savedRoom, foundUser);
        roomMemberRepository.save(roomMember);

        foundUser.updateRoomLimit(roomLimit + 1); //user의 roomlimit + 1

        //createdRoom.setInvitation(createLink(createdRoom));

        Question question = Question.builder()
                .room(savedRoom)
                .creatorId(foundUser.getUserId())
                .question(FIRST_QUESTION)
                .build();

        questionRepository.save(question);

        List<RoomMemberResopnose> roomMemberResopnoseList = roomMemberRepository.findAllByRoomAndStatus(savedRoom, STATUS).stream()
                .map(RoomMemberResopnose::new).collect(Collectors.toList());

        Optional<Question> o = questionRepository.findTopByRoomRoomIdAndStatusOrderByIdDesc(savedRoom.getRoomId(), STATUS);
        LocalDateTime questionCreateDate = LocalDateTime.now();
        if(o.isPresent()){
            Question topQuestion = o.get();
            questionCreateDate = topQuestion.getCreatedAt().plusDays(1);
        }
        CreateResponse createResponse = new CreateResponse( new DetailResponse(savedRoom, question, roomMemberResopnoseList, questionCreateDate));
        return new ResponseRoom(ExceptionCode.ROOM_CREATE_SUCCESS, createResponse);
    }
    private String createCode(Room room) {
        return room.getCode();
        //room.getRoomId() + passwordEncoder.encode(room.getName()); // 임시로 암호화
    }

    @Override
    public ResponseMemberRoom updateRoomNm(UserPrincipal userPrincipal, ModifyRoomNmRequest modifyRoomNmRequestDto){//validation정해지면 ( 룸 이름 중복 가능)
        RoomMember roomMember = roomMemberRepository
                .findByRoom_RoomIdAndUser_UserIdAndStatus(modifyRoomNmRequestDto.getRoomId(), userPrincipal.getId(), 1)
                .orElseThrow(() -> new CustomException(ExceptionCode.ROOMMEMBER_NOT_EXIST));

        String roomNm = roomRepository.findById(modifyRoomNmRequestDto.getRoomId()).get().getName();
        String privateRoomNm = modifyRoomNmRequestDto.getPrivateRoomNm();
        if(privateRoomNm == null || privateRoomNm.isEmpty()) {
            roomNm = roomRepository.findById(modifyRoomNmRequestDto.getRoomId()).get().getName();
        } else if (isValidRoomNmLength(privateRoomNm)) {
            roomNm = privateRoomNm;
        }

        roomMember.updatePrivateRoomNm(roomNm);
        return new ResponseMemberRoom(ExceptionCode.ROOMNAME_VERIFY_OK);
    }

    private boolean isValidRoomNmLength(String roomNm) {
        if (roomNm.length() <= 15 && roomNm.length() >= 1) {
            return true;
        } else {
            throw new CustomException(ExceptionCode.ROOM_NAME_INVALID);
        }
    }



    @Override
    public ResponseRoom exitRoom(Long roomId, UserPrincipal userPrincipal){//남은 사람 있는지 확인
        User foundUser = userRepository.findById(userPrincipal.getId()).orElseThrow(()->new CustomException(ExceptionCode.USER_NOT_FOUND));
        RoomMember roomMember = roomMemberRepository.findByRoom_RoomIdAndUser_UserIdAndStatus(roomId, foundUser.getUserId(), 1).orElseThrow(()->new CustomException(ExceptionCode.ROOMMEMBER_NOT_EXIST));

        foundUser.updateRoomLimit(foundUser.getRoomLimit() - 1);
        Room room = roomRepository.findById(roomId).orElseThrow(()->new CustomException(ExceptionCode.ROOM_NOT_FOUND));//roomMember에서 roomId로도 비교하기 때문에 무조건 존재해야 넘어옴
        room.updateMemberNum(room.getMemberNum()-1);
        if(room.getMemberNum()<=0){
            questionRepository.deleteAllByRoomId(room.getRoomId());
            voteRepository.deleteAllByRoomId(room.getRoomId());
            roomMemberRepository.deleteAllByRoom_RoomId(room.getRoomId());
            reportRepository.deleteAllByRoom_RoomId(room.getRoomId());
            roomRepository.delete(room);
        }
        roomMember.setStatus(0);
        return new ResponseRoom(ExceptionCode.ROOM_EXIT_SUCCESS);
    }

    @Override
    public ResponseRoom inviteMembers(InviteRequest inviteDto) {

        Room foundRoom = roomRepository.findById(inviteDto.getRoomId()).orElseThrow(
                () -> {
                    throw new CustomException(ExceptionCode.ROOM_NOT_FOUND);
                }
        );

        synchronized (foundRoom) {
            Integer totalRoomMemberNum = foundRoom.getMemberNum() + inviteDto.getUserFriendIdList().size();

            if (totalRoomMemberNum > 50) {//초대 가능 인원 수 제한
                throw new CustomException(ExceptionCode.ROOMMEMBER_OVER_LIMIT);
            } // 룸 자체적으로 초대 가능한지
            foundRoom.updateMemberNum(totalRoomMemberNum);
        }

            //RoomMember저장
        List<RoomMember> roomMembers;
        roomMembers = inviteDto.getUserFriendIdList().stream()
                .map((userFriendId) -> {
                    RoomMember roomMember = roomMemberRepository.findByRoom_RoomIdAndUser_UserIdAndStatus
                            (inviteDto.getRoomId(), userFriendId, 0).orElse(null);
                    if (roomMember != null) {
                        roomMember.setStatus(1);
                        return null;
                    } else {
                        User foundUser = userRepository.findById(userFriendId).orElseThrow( // 이미 fetchpage에서 status 0인거로 골라옴
                                () -> new CustomException(ExceptionCode.USER_NOT_FOUND));
                        foundUser.updateRoomLimit(foundUser.getRoomLimit() + 1);
                        return new RoomMember(foundRoom, foundUser);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            roomMemberRepository.saveAllAndFlush(roomMembers);
        }catch (DataIntegrityViolationException e){
            throw new CustomException(ExceptionCode.ROOMMEMBER_ALREADY);
        }


        return new ResponseRoom(ExceptionCode.ROOM_INVITATION_SUCCESS);
    }

    //이미 초대된 멤버 get(getRoomMember)
    @Override
    public ResponseRoom getInviteMembers(Long roomId){
        List<RoomMember> roomMember = roomMemberRepository.findAllByRoom_RoomIdAndStatusEquals(roomId, 1);
        List<RoomMemberResopnose> roomMemberResopnoseList = roomMember.stream()
                .map(RoomMemberResopnose::new).collect(Collectors.toList());
        return new ResponseRoom(ExceptionCode.ROOMMEMBER_GET_SUCCESS, roomMemberResopnoseList);
    }

    //초대 가능 여부 리스트 보내기 - 여기서 해당 유저가 초대 가능한 지 따짐
    @Override
    public ResponseRoom isInviteMembersList(UserPrincipal userPrincipal, Long roomId, Integer currentPage, int pageSize, String kwd, HttpServletRequest request){
        Long userId = userPrincipal.getId();
        if(!roomMemberRepository.existsByUserUserIdAndRoomRoomIdAndStatus(userId, roomId, 1)){//초대하려는 유저가 룸 멤버가 아닐 때
            throw new CustomException(ExceptionCode.ROOMMEMBER_NOT_EXIST);
        }
        //pageSize는 상수로
        List<Friend> friendList = fetchPagesOffset(userId, currentPage, pageSize, kwd);//status가 1인 것만 가져옴

        //roomMember에 존재하는가 + 30개 방 개수가 넘지 않았는가
        //List<User> userList =  friendList.stream().map(friend -> userRepository.findById(friend.getUserFriendId()).get()).collect(Collectors.toList());
        List<IsInviteMember> roomMemberResponoseList = new ArrayList<>();

        for(Friend f : friendList){
            Long userFriendId = f.getUserFriendId();

            IsInviteMember isInviteMember = new IsInviteMember(userRepository.findById(userFriendId).get(), f);
            if(roomMemberRepository.existsByUserUserIdAndRoomRoomIdAndStatus(userFriendId, roomId, STATUS)){
                isInviteMember.updateIsInvite(false, IsInviteMember.Reason.ALREADY);
            } else if (userRepository.findById(f.getUserFriendId()).get().getRoomLimit()>30) {
                isInviteMember.updateIsInvite(false, IsInviteMember.Reason.OVERLIMIT);
            }
            roomMemberResponoseList.add(isInviteMember);
            //User user = userRepository.findById(f.getUserFriendId());
        }

//        Comparator<IsInviteMember> comparator = Comparator.comparing(IsInviteMember::getNickNm);
//        Collections.sort(roomMemberResponoseList, comparator);//이름 순 정렬
        return new ResponseRoom( ExceptionCode.INVITATION_LIST_GET_SUCCESS ,currentPage, roomMemberResponoseList);
    }


    private List<Friend> fetchPagesOffset(Long userId, int currentPage, int pageSize, String kwd){
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.by("friendName"));
        //System.out.println(pageRequest);
        return friendRepository.findAllByUserUserIdAndRelationshipAndStatusAndFriendNameContaining(userId, Friend.Relation.SUCCESS, 1,kwd, pageRequest);
    }



    private List<Friend> fetchPagesCursor(Long userId, Long userFriendId, String cursorNm, int pageSize, String kwd) {//이름 중복시 처리
        Sort sort = Sort.by("friendName").ascending();
        Pageable pageable = PageRequest.of(0, pageSize, sort);
        if (cursorNm != null) {
            return friendRepository.findAllByFriendNameAfterAndUserUserIdAndRelationshipAndStatusAndFriendNameContainingOrderByFriendNameAsc(userFriendId, cursorNm, userId, Friend.Relation.SUCCESS, 1, kwd, pageable);
        }

        return friendRepository.findAllByUserUserIdAndRelationshipAndStatus(userId, Friend.Relation.SUCCESS, 1, pageable);
    }


    private static UUID generateCode() {
        return UUID.randomUUID();
    }

    private String checkAndGetCode(Long roomId) throws NoSuchAlgorithmException {
        Room room = roomRepository.findById(roomId).orElseThrow(()-> new CustomException(ExceptionCode.ROOM_NOT_FOUND));
        String uuidToken = room.getCode();
        String code;
        if(uuidToken == null || uuidToken.isEmpty() || room.getDeadline().isBefore(LocalDateTime.now())){

            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime deadline = currentDateTime.plusHours(1);
            System.out.println(currentDateTime);
            System.out.println(deadline);
            room.updateDeadline(deadline);

            uuidToken = roomId.toString() + "-" + generateCode().toString();
//            uuidToken = generateInvitationKey(30);
            code = base62Util.urlEncoder(uuidToken);
            room.updateCode(uuidToken);
        }else{
            code = base62Util.urlEncoder(uuidToken);
        }
        System.out.println(code);
        System.out.println(uuidToken);
        return code;
    }
    @Override
    @Transactional
    public ResponseRoom getRealIDCode(UserPrincipal userPrincipal, String code) throws NoSuchAlgorithmException {
        String IdCode = base62Util.urlDecoder(code);
        System.out.println(code);
        System.out.println(IdCode);

        Room room = roomRepository.findByCode(IdCode).orElseThrow(
                ()->new CustomException(ExceptionCode.ROOM_NOT_FOUND)
        );
        User user = userRepository.findByUserIdAndStatus(userPrincipal.getId(), STATUS).orElseThrow(
                ()->new CustomException(ExceptionCode.USER_NOT_FOUND)
        );
        
        if(!roomMemberRepository.existsByUserUserIdAndRoomRoomIdAndStatus(user.getUserId(), room.getRoomId(), STATUS)){
            System.out.println(roomMemberRepository.existsByUserUserIdAndRoomRoomIdAndStatus(room.getRoomId(), user.getUserId(), STATUS));
            inviteMembers(new InviteRequest(room.getRoomId(), List.of(user.getUserId())));
            return new ResponseRoom(ExceptionCode.ROOM_INVITATION_SUCCESS, getRoom(userPrincipal.getId(), room.getRoomId()));
        }else{
            throw new CustomException(ExceptionCode.ROOMMEMBER_ALREADY);
        }
    }


    @Override
    public ResponseRoom findLink(Long userId, Long roomId) throws NoSuchAlgorithmException {
        if(!roomMemberRepository.existsByUserUserIdAndRoomRoomIdAndStatus(userId, roomId, STATUS)){
            throw new CustomException(ExceptionCode.ROOMMEMBER_NOT_EXIST);
        }
        final String URL_PREFIX = "http://pointer2024.com/invitation-link/";

        String url = URL_PREFIX + checkAndGetCode(roomId);
        return new ResponseRoom(ExceptionCode.INVITATION_GET_OK, url);
    }

}
