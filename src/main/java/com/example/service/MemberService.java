package com.example.service;

import com.example.model.entities.Member;
import com.example.model.enums.MemberStatus;
import com.example.model.request.MemberCreateRequest;
import com.example.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member create(MemberCreateRequest request) {
//        TODO validate username, identityNo already exists in table member
        var member = Member.builder()
                .fullName(request.getFullName())
                .identityNo(request.getIdentityNo())
                .username(request.getUsername())
                .status(MemberStatus.NEW)
                .build();
        return memberRepository.save(member);
    }

    public Member approve(Long id) {
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) {
//       TODO handle entity not found exception
        }
        assert member != null;
        member.setStatus(MemberStatus.WAIT_FOR_APPROVE);
        return memberRepository.save(member);
    }

    public Member approveMemberSuccess(Member member) {
        member.setStatus(MemberStatus.APPROVED);
        member.setModifiedAt(LocalDateTime.now());
        return memberRepository.save(member);
    }

    //    revert member after approve in digital document service over max retry
    public void revertMemberAfterApproveDocumentFail(Member member) {
        member.setStatus(MemberStatus.NEW);
        memberRepository.save(member);
    }
}
