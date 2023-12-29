package com.sparta.jamrello.domain.comment.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.given;

import com.sparta.jamrello.domain.card.repository.CardRepository;
import com.sparta.jamrello.domain.card.repository.entity.Card;
import com.sparta.jamrello.domain.comment.dto.CommentRequestDto;
import com.sparta.jamrello.domain.comment.repository.CommentRepository;
import com.sparta.jamrello.domain.comment.repository.entity.Comment;
import com.sparta.jamrello.domain.member.repository.MemberRepository;
import com.sparta.jamrello.domain.member.repository.entity.Member;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "test")
@SpringBootTest
class CommentServiceImplV1Test {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CommentServiceImplV1 commentServiceImplV1;

    private CommentRequestDto commentRequestDto;
    private Member member;
    private Card card;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
            .username("testUser")
            .password("password")
            .nickname("nickname")
            .email("email@email.com")
            .build()
        );
        card = cardRepository.save(Card.builder()
            .title("Test Card")
            .build());
        commentRequestDto = new CommentRequestDto("test comment");
        comment = commentRepository.save(
            Comment.createCommentOf(commentRequestDto.content(), member, card));
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        cardRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void createCommentTest_Success() {
        // Given
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
        given(cardRepository.findById(anyLong())).willReturn(Optional.of(card));
        given(commentRepository.save(any(Comment.class))).willAnswer(
            invocation -> invocation.getArgument(0));

        // When
        Comment result = commentServiceImplV1.createComment(1L, 1L, commentRequestDto);

        // Then
        assertNotNull(result);
//        verify(commentRepository).save(any(Comment.class));
    }
//
//    @Test
//    void createCommentTest_MemberNotFound() {
//        // Given
//        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());
//
//        // When & Then
//        assertThrows(BisException.class, () -> {
//            commentService.createComment(1L, 1L, commentRequestDto);
//        });
//    }
//
//    @Test
//    void createCommentTest_CardNotFound() {
//        // Given
//        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
//        given(cardRepository.findById(anyLong())).willReturn(Optional.empty());
//
//        // When & Then
//        assertThrows(BisException.class, () -> {
//            commentService.createComment(1L, 1L, commentRequestDto);
//        });
//    }
}
