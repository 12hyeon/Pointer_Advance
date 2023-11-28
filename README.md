# POINTER : 10대를 위한 커뮤니티 서비스
![image](https://github.com/12hyeon/Pointer_Advance/assets/67951802/6d7a0b6d-777d-4eb6-ad8f-cdbad1ea4e53)


## 목차
- [개요](#개요)
- [사용기술](#사용기술)
- [API](#API)
- [ERD](#ERD)
- [TIL 및 회고](#프로젝트-관리-및-회고
  )


## 개요

POINTER는 10대를 위한 소통 플랫폼입니다. <br>
이 어플리케이션은 '본인이 생성한 집단'의 사람들끼리 소통할 수 있는 기능을 제공하며, 투표 기능을 메인으로 합니다.<br>
모든 멤버가 투표를 완료 후, 투표 결과를 포인트를 통해 투표한 사람의 힌트 확인이 가능합니다.<br>
<br/>

![image](https://github.com/12hyeon/Pointer_Advance/assets/67951802/9edaa87f-332c-4074-af4b-bdf3a6e18c70)


## 사용기술

### 개발환경

[![Java](https://img.shields.io/badge/java-007396?&logo=java&logoColor=white)](https://www.java.com)
[![Spring](https://img.shields.io/badge/spring-6DB33F?&logo=spring&logoColor=white)](https://spring.io)
[![Spring Boot](https://img.shields.io/badge/Spring_boot-6DB33F?&logo=Spring%20boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/gradle-02303A?&logo=gradle&logoColor=white)](https://gradle.org)
[![Spring JPA](https://img.shields.io/badge/Spring%20JPA-6DB33F?&logo=Spring%20JPA&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![IntelliJ IDEA](https://img.shields.io/badge/intellijidea-000000?&logo=intellij%20idea&logoColor=white)](https://www.jetbrains.com/idea)
[![Postman](https://img.shields.io/badge/postman-FF6C37?&logo=postman&logoColor=white)](https://www.postman.com)
<br>

![MySQL Connector/J](https://img.shields.io/badge/MySQL%20Connector%2FJ-8.0.33-lightgrey)
![JWT API](https://img.shields.io/badge/JWT--API-0.11.2-blue)
![OAuth2 Libraries](https://img.shields.io/badge/OAuth2%20Libraries-3.1.0-informational)

### 배포환경
<img src="https://img.shields.io/badge/aws-232F3E?&logo=amazonaws&logoColor=white"> <img src="https://img.shields.io/badge/ec2-FF9900?&logo=amazonec2&logoColor=white">
<img src="https://img.shields.io/badge/rds-527FFF?&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/beanstalk-232F3E?&logo=awselasticbeanstalk&logoColor=white"> <img src="https://img.shields.io/badge/github-181717?&logo=github&logoColor=white">

### 협업 도구
<img src="https://img.shields.io/badge/discord-4A154B?&logo=discord&logoColor=white"> <img src="https://img.shields.io/badge/notion-000000?&logo=notion&logoColor=white"> <img src="https://img.shields.io/badge/zep-25c3d1?&logo=zep&logoColor=white">
</br>

### 프로젝트 작업 관리
  ![image](https://github.com/12hyeon/Pointer_Advance/assets/67951802/1d90d0b2-3cbe-4dda-9acd-0906c1e46fd9)
</br>

## API

| API Type | Http Method| URL                         | Description    |
|----------|-------------|-----------------------------|----------------|
| **User API** | POST | `/api/v1/users/check`                   | 회원 아이디 중복 확인      |
| **User API** | POST | `/api/v1/users/id`                   | 회원 아이디 저장      | 
| **User API** | POST | `/api/v1/users/agree`                   | 약관 동의      | 
| **User API** | POST | `/api/v1/users/reissue`                   | 토큰 재발급      | 
| **User API**| PATCH | `/api/v1/users/marketing`                  | 마케팅 정보 수정           |
| **User API**| DELETE | `/api/v1/users/resign`                  | 회원 탈퇴           |
| **Auth API**| POST | `/api/v1/auth/login`                  | 카카오 소셜 로그인           |
| **User API**| GET | `/api/v1/users/search`          | 유저 검색    |
| **Friend API**| GET | `/api/v1/friends/search`          | 친구 검색    |
| **Friend API**| POST | `/api/v1/friends/request`          | 친구 요청    |
| **Friend API**| PUT | `/api/v1/friends/block/search`          | 요청 취소    |
| **Friend API**| POST | `/api/v1/friends/accept`          | 친구 수락    |
| **Friend API**| PUT | `/api/v1/friends`          | 친구 취소    |
| **Friend API**| POST | `/api/v1/friends/refuse`          | 친구 거절    |
| **Friend API**| POST | `/api/v1/friends/block`          | 친구 차단    |
| **Friend API**| PUT | `/api/v1/friends/block`          | 친구 차단 해체   |
| **Room API**| GET | `/api/v1/rooms/{room-id}/friends`          | 초대 가능한 친구 목록   |
| **Vote API**| GET | `/api/v1/votes/check/{question-id}`          | 투표 여부 조회   |
| **Point API**| GET | `/api/v1/points`          | 포인트 알림 조회   |
| **Point API**| POST | `/api/v1/points/{point-id}`          | 포인트 차감   |
</br>

## ERD
![image](https://github.com/12hyeon/Pointer_Advance/assets/67951802/674666e1-b8d1-40bc-a2ee-1c168298e2cd)
</br>

## 프로젝트-관리-및-회고

- 도메인 설계 : 친구 관련 도메인에서는 각 유저별로 상대와 관계를 정의함으로써 다양한 형태의 관계를 정의할 수 있도록 확장성을 고려해 설계하였습니다. 그에 따라 후에 PM의 요청에 따라 차단이나 정의되지 않은 관계에 대한 정보도 빠르고 통일성 있는 방식으로 제공할 수 있었습니다.
- 사용자 인증 : Oauth를 통한 카카오 소셜로그인을 구현함으로써 사용자 정보의 안전성을 보장하는 경험을 할 수 있었습니다.
