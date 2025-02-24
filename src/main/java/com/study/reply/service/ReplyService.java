package com.study.reply.service;

import com.study.board.entity.Board;
import com.study.board.repository.BoardRepository;
import com.study.board.service.BoardService;
import com.study.exception.BusinessLogicException;
import com.study.exception.ExceptionCode;
import com.study.member.entity.Member;
import com.study.member.service.MemberService;
import com.study.reply.entity.Reply;
import com.study.reply.repository.ReplyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReplyService {
    private final MemberService memberService;
    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;

    public void createReply(Reply reply) {
        adminLogIn(reply);

        Board findBoard = checkBoard(reply);
        replyRepository.save(reply);

        findBoard.setBoardStatus(Board.BoardStatus.QUESTION_ANSWERED);
        boardRepository.save(findBoard);
    }

    public void updateReply(Reply reply) {
        adminLogIn(reply);

        checkBoard(reply);

        Reply findReply = verifiedReply(reply);

        Optional.ofNullable(reply.getMessage())
                .ifPresent(findReply::setMessage);

        replyRepository.save(findReply);
    }

    public void deleteReply(long replyId, Reply reply) {
        adminLogIn(reply);

        replyRepository.deleteById(replyId);
    }

    private Reply verifiedReply(Reply reply) {
        Optional<Reply> findReply = replyRepository.findById(reply.getReplyId());
        Reply reply1 = findReply.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
        return reply1;
    }

    private Board checkBoard(Reply reply) {
        Board findboard = boardService.findVerifiedBoard(reply.getBoard().getBoardId());
        if (findboard.getBoardStatus().getStepNumber() == 3) {
            throw new BusinessLogicException(ExceptionCode.DELETED_BOARD);
        }
        return findboard;
    }

    public void adminLogIn(Reply reply) {
        Member findMember = memberService.findVerifiedMember(reply.getMember().getMemberId());
        if (findMember.getMemberId() != 1 || !findMember.getPassword().equals(reply.getPassword()))
            throw new BusinessLogicException(ExceptionCode.ADMIN_ACCESS_ONLY);
    }
}
