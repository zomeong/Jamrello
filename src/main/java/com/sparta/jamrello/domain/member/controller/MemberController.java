package com.sparta.jamrello.domain.member.controller;

import com.sparta.jamrello.domain.member.dto.DeleteMemberRequestDto;
import com.sparta.jamrello.domain.member.dto.EmailRequestDto;
import com.sparta.jamrello.domain.member.dto.MemberResponseDto;
import com.sparta.jamrello.domain.member.dto.SignupRequestDto;
import com.sparta.jamrello.domain.member.dto.UpdateMemberRequestDto;
import com.sparta.jamrello.domain.member.repository.entity.Member;
import com.sparta.jamrello.domain.member.service.MemberServiceImpl;
import com.sparta.jamrello.global.annotation.AuthUser;
import com.sparta.jamrello.global.constant.ResponseCode;
import com.sparta.jamrello.global.dto.BaseResponse;
import com.sparta.jamrello.global.exception.ErrorCode;
import com.sparta.jamrello.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

  private final MemberServiceImpl memberService;

  @PostMapping("/email")
  public ResponseEntity<BaseResponse> authenticationEmail (
      @Valid @RequestBody EmailRequestDto emailRequestDto,
      BindingResult bindingResult
  ) {
    // Validation 예외처리
    List<FieldError> fieldErrors = bindingResult.getFieldErrors();
    if (fieldErrors.size() > 0) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
      }
      return ResponseEntity.status(ErrorCode.INVALID_VALUE.getStatus()).body(
          BaseResponse.of(
              ErrorCode.INVALID_VALUE.getMsg(),
              ErrorCode.INVALID_VALUE.getStatus().value(),
              emailRequestDto
          ));
    }

    memberService.sendCodeToEmail(emailRequestDto);
    return ResponseEntity.ok().body(BaseResponse.of(ResponseCode.SEND_MAIL, emailRequestDto));
  }

  @PostMapping("/signup")
  public ResponseEntity<BaseResponse> signupMember (
      @Valid @RequestBody SignupRequestDto signupRequestDto,
      BindingResult bindingResult
  ) {
    // Validation 예외처리
    List<FieldError> fieldErrors = bindingResult.getFieldErrors();
    if (fieldErrors.size() > 0) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
      }
      return ResponseEntity.status(ErrorCode.INVALID_VALUE.getStatus()).body(
          BaseResponse.of(
              ErrorCode.INVALID_VALUE.getMsg(),
              ErrorCode.INVALID_VALUE.getStatus().value(),
              ""
          ));
    }

    memberService.signup(signupRequestDto);
    return ResponseEntity.status(201).body(BaseResponse.of(ResponseCode.SIGNUP, ""));
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<BaseResponse> getMember (
      @PathVariable("memberId") Long memberId,
      @AuthenticationPrincipal UserDetailsImpl userDetails
  ) {
    MemberResponseDto memberResponseDto = memberService.getProfile(memberId, userDetails);
    return ResponseEntity.status(ResponseCode.GET_MY_PROFILE.getHttpStatus()).body(
        BaseResponse.of(ResponseCode.GET_MY_PROFILE, memberResponseDto)
    );
  }

  @PutMapping("/{memberId}")
  public ResponseEntity<BaseResponse> updateMember (
      @PathVariable("memberId") Long memberId,
      @RequestBody UpdateMemberRequestDto updateMemberRequestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      BindingResult bindingResult
  ) {
    // Validation 예외처리
    List<FieldError> fieldErrors = bindingResult.getFieldErrors();
    if (fieldErrors.size() > 0) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
      }
      return ResponseEntity.status(ErrorCode.INVALID_VALUE.getStatus()).body(
          BaseResponse.of(
              ErrorCode.INVALID_VALUE.getMsg(),
              ErrorCode.INVALID_VALUE.getStatus().value(),
              ""
          ));
    }

    MemberResponseDto memberResponseDto = memberService.updateMember(memberId, updateMemberRequestDto, userDetails);
    return ResponseEntity.status(ResponseCode.UPDATE_MY_PROFILE.getHttpStatus()).body(
        BaseResponse.of(ResponseCode.UPDATE_MY_PROFILE, memberResponseDto)
    );
  }

  @DeleteMapping("/{memberId}")
  public ResponseEntity<BaseResponse> deleteMember (
      @PathVariable("memberId") Long memberId,
      @RequestBody DeleteMemberRequestDto deleteMemberRequestDto,
      @AuthUser Member loginMember
  ) {
    memberService.deleteMember(memberId, deleteMemberRequestDto, loginMember);
    return ResponseEntity.status(ResponseCode.DELETE_USER.getHttpStatus()).body(
        BaseResponse.of(ResponseCode.DELETE_USER, "")
    );
  }




}
