package com.mmc.bookduck.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 도메인
    // 타입(상태 코드, "메시지");

    // Default
    ERROR(400, "요청 처리에 실패했습니다."),

    // 400 Bad Request
    // 입력 에러
    INVALID_INPUT_FORMAT(400, "유효하지 않은 형식입니다."),
    INVALID_INPUT_LENGTH(400, "입력 길이가 잘못되었습니다."),
    INVALID_INPUT_VALUE(400, "입력값이 잘못되었습니다."),
    MISSING_PARAMETER(400, "필수 파라미터가 누락되었습니다."),
    INVALID_UNLOCK_CONDITION(400, "해제 조건이 잘못되었습니다."),
    // enum 값이 잘못됨
    INVALID_ENUM_VALUE(400, "enum 값이 잘못되었습니다."),


    // 401 Unauthorized
    // 로그인 상태여야 하는 요청
    NOT_AUTHENTICATED(401, "로그인 상태가 아닙니다."),
    // 권한이 없는 요청을 보냄
    UNAUTHORIZED_REQUEST(401,"권한이 없습니다."),
    // 소셜 로그인이 정상적으로 이루어지지 않음
    OAUTH2_LOGIN_FAILED(401, "소셜 로그인에 실패했습니다."),
    // 유효하지 않은 토큰
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    // 만료된 토큰
    EXPIRED_ACCESS_TOKEN(401,"만료된 액세스 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(401, "만료된 리프레시 토큰입니다."),
    // 액세스 토큰이 만료되지 않은 상황에서 재발급받으려는 경우
    ACCESS_TOKEN_NOT_EXPIRED(401,"액세스 토큰이 아직 만료되지 않았습니다."),
    // 쿠키에 리프레시 토큰이 들어있지 않은 경우
    NO_COOKIE(401, "쿠키에 값이 존재하지 않습니다."),
    USER_STATUS_IS_NOT_ACTIVE(401, "계정이 활성 상태가 아닙니다."),

    // 404 Not Found
    // 각 리소스를 찾지 못함
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    FRIEND_REQUEST_NOT_FOUND(404, "친구 요청을 찾을 수 없습니다."),
    FRIEND_NOT_FOUND(404, "친구를 찾을 수 없습니다."),
    BOOKINFO_NOT_FOUND(404, "책 정보를 찾을 수 없습니다."),
    USERBOOK_NOT_FOUND(404, "사용자의 책을 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(404, "감상평을 찾을 수 없습니다."),
    REVIEW_HEART_NOT_FOUND(404, "리뷰의 좋아요를 찾을 수 없습니다."),
    EXCERPT_NOT_FOUND(404, "발췌를 찾을 수 없습니다."),
    EXCERPT_HEART_NOT_FOUND(404, "발췌의 좋아요를 찾을 수 없습니다."),
    ALARM_NOT_FOUND(404, "알림을 찾을 수 없습니다."),
    FOLDER_NOT_FOUND(404, "폴더를 찾을 수 없습니다."),
    BADGE_NOT_FOUND(404, "뱃지를 찾을 수 없습니다."),
    SKIN_NOT_FOUND(404, "스킨을 찾을 수 없습니다."),
    GENRE_NOT_FOUND(404, "장르를 찾을 수 없습니다."),


    // 409 Conflict
    // 중복 리소스 생성 시도
    USER_ALREADY_EXISTS(409, "이미 존재하는 사용자입니다."),
    FRIEND_REQUEST_ALREADY_EXISTS(409, "이미 친구 요청이 존재합니다."),
    FRIEND_ALREADY_EXISTS(409, "이미 친구를 맺었습니다."),
    EMAIL_ALREADY_REGISTERED(400, "해당 이메일을 사용하는 다른 소셜 로그인 방법으로 가입되어 있습니다."),
    BOOK_ALREADY_EXISTS(409, "이미 등록된 책입니다."),
    USERBOOK_ALREADY_EXISTS(409, "이미 사용자의 책이 등록되어 있습니다."),
    REVIEW_ALREADY_EXISTS(409, "이미 존재하는 감상평입니다."),
    EXCERPT_ALREADY_EXISTS(409, "이미 존재하는 발췌입니다."),
    ALARM_ALREADY_EXISTS(409, "이미 존재하는 알림입니다."),
    FOLDER_ALREADY_EXISTS(409, "이미 존재하는 폴더입니다."),
    BADGE_ALREADY_EXISTS(409, "이미 존재하는 뱃지입니다."),
    SKIN_ALREADY_EXISTS(409, "이미 존재하는 스킨입니다."),
    SKIN_ALREADY_EQUIPPED(409, "이미 장착한 스킨입니다."),

    // 500 Internal Server Error
    // 외부 API 사용 도중 에러
    REDIS_CONNECTION_ERROR(500, "서버에서 Redis 연결 중 문제가 발생했습니다."),
    EXTERNAL_API_ERROR(500, "외부 API 사용 중 문제가 발생했습니다."),
    //JSON
    JSON_PARSING_ERROR(500, "API JSON에서 정보를 파싱하는 중 문제가 발생했습니다."),
    // Oauth2, JWT
    ILLEGAL_REGISTRATION_ID(500, "잘못된 registrationId입니다."),
    DATABASE_ERROR(500, "데이터베이스 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.")
    ;

    private final int status;
    private final String message;

}
