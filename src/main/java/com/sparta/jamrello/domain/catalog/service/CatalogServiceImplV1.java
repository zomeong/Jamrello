package com.sparta.jamrello.domain.catalog.service;

import static com.sparta.jamrello.global.exception.ErrorCode.NOT_FOUND_BOARD;
import static com.sparta.jamrello.global.exception.ErrorCode.NOT_FOUND_CATALOG;
import static com.sparta.jamrello.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static com.sparta.jamrello.global.exception.ErrorCode.POSITION_OVER;
import static com.sparta.jamrello.global.exception.ErrorCode.YOUR_NOT_INVITED_BOARD;

import com.sparta.jamrello.domain.board.entity.Board;
import com.sparta.jamrello.domain.board.repository.BoardRepository;
import com.sparta.jamrello.domain.catalog.dto.CatalogPositionRequestDto;
import com.sparta.jamrello.domain.catalog.dto.CatalogRequestDto;
import com.sparta.jamrello.domain.catalog.dto.CatalogResponseDto;
import com.sparta.jamrello.domain.catalog.repository.CatalogRepository;
import com.sparta.jamrello.domain.catalog.repository.entity.Catalog;
import com.sparta.jamrello.domain.member.repository.MemberRepository;
import com.sparta.jamrello.domain.member.repository.entity.Member;
import com.sparta.jamrello.domain.memberBoard.entity.MemberBoard;
import com.sparta.jamrello.domain.memberBoard.entity.MemberBoardRoleEnum;
import com.sparta.jamrello.domain.memberBoard.repository.MemberBoardRepository;
import com.sparta.jamrello.global.exception.BisException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogServiceImplV1 implements CatalogService {

    private final MemberBoardRepository memberBoardRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CatalogRepository catalogRepository;


    @Override
    public CatalogResponseDto createCatalog(Long boardId, Long memberId,
            CatalogRequestDto requestDto) {

        Board board = findBoardWithCatalog(boardId);

        Optional<MemberBoard> memberBoard = memberBoardRepository.findByMemberIdAndBoardId(
                memberId, boardId);

        if (!memberBoard.isPresent()) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        if (memberBoard.get().getRole().equals(MemberBoardRoleEnum.NOT_INVITED_MEMBER)) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        Long position = (long) (board.getCatalogList().size() + 1);
        Catalog catalog = Catalog.createCatalog(board, requestDto, position);
        Catalog savedCatalog = catalogRepository.save(catalog);
        savedCatalog.addCatalogInBoard();

        return Catalog.createCatalogResponseDto(savedCatalog);
    }

    @Override
    public CatalogResponseDto updateCatalogTitle(Long boardId, Long memberId, Long catalogId,
            CatalogRequestDto requestDto) {

        existsBoard(boardId);

        Optional<MemberBoard> memberBoard = memberBoardRepository.findByMemberIdAndBoardId(
                memberId, boardId);

        if (!memberBoard.isPresent()) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        if (memberBoard.get().getRole().equals(MemberBoardRoleEnum.NOT_INVITED_MEMBER)) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        Catalog catalog = findCatalog(catalogId);
        catalog.updateCatalogTitle(requestDto);
        Catalog savedCatalog = catalogRepository.save(catalog);

        return Catalog.createCatalogResponseDto(savedCatalog);
    }


    @Override
    @Transactional
    public void updateCatalogStatus(Long boardId, Long memberId, Long catalogId) {

        existsBoard(boardId);

        Optional<MemberBoard> memberBoard = memberBoardRepository.findByMemberIdAndBoardId(
                memberId, boardId);
        if (!memberBoard.isPresent()) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        if (memberBoard.get().getRole().equals(MemberBoardRoleEnum.NOT_INVITED_MEMBER)) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        Catalog catalog = findCatalog(catalogId);
        catalog.changeStatus();

        catalogRepository.save(catalog);
    }

    @Override
    @Transactional
    public void deleteCatalog(Long boardId, Long memberId, Long catalogId) {

        existsBoard(boardId);

        Optional<MemberBoard> memberBoard = memberBoardRepository.findByMemberIdAndBoardId(
                memberId, boardId);
        if (!memberBoard.isPresent()) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }

        if (memberBoard.get().getRole().equals(MemberBoardRoleEnum.NOT_INVITED_MEMBER)) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }
        Catalog catalog = findCatalog(catalogId);
        Long currentPos = catalog.getPosition();

        catalogRepository.decreasePositionBeforeDelete(boardId, currentPos);

        catalogRepository.delete(catalog);
    }

    @Override
    @Transactional
    public void updateCatalogPos(Long boardId, Long memberId, Long catalogId,
            CatalogPositionRequestDto requestDto) {

        Board board = findBoardWithCatalog(boardId);

        if (requestDto.pos() > board.getCatalogList().size() || requestDto.pos() < 1) {
            throw new BisException(POSITION_OVER);
        }

        Optional<MemberBoard> memberBoard = memberBoardRepository.findByMemberIdAndBoardId(
                memberId, boardId);
        if (!memberBoard.isPresent()) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }
        if (memberBoard.get().getRole().equals(MemberBoardRoleEnum.NOT_INVITED_MEMBER)) {
            throw new BisException(YOUR_NOT_INVITED_BOARD);
        }
        Catalog catalog = findCatalog(catalogId);

        Long currentPos = catalog.getPosition();
        Long changedPos = (requestDto.pos());

        if (changedPos > currentPos) {
            catalogRepository.decreasePositionBeforeUpdate(boardId, currentPos,
                    changedPos); // 바꿀 위치가 현재 위치보다 크다면 ? between current+1 ~ changed 까지의 모든 것들 /2
        } else {
            catalogRepository.increasePositionBeforeUpdate(boardId, currentPos,
                    changedPos); // 바꿀 위치가 현재 위치보다 작다면 ? between current-1 ~ changed
        }

        catalogRepository.updateCatalogPosition(catalogId, changedPos);
    }

    private Board findBoardWithCatalog(Long boardId) {
        return boardRepository.findBoardWithCatalog(boardId).orElseThrow(
                () -> new BisException(NOT_FOUND_BOARD)
        );
    }

    private void existsBoard(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new BisException(NOT_FOUND_BOARD);
        }
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(
                () -> new BisException(NOT_FOUND_BOARD)
        );
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new BisException(NOT_FOUND_MEMBER)
        );
    }

    private Catalog findCatalog(Long catalogId) {
        return catalogRepository.findById(catalogId).orElseThrow(
                () -> new BisException(NOT_FOUND_CATALOG)
        );
    }
}
