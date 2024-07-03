package com.example.processor;

import com.example.model.entities.Member;
import com.example.model.request.ApproveDocumentRequest;
import com.example.model.response.ApproveDocumentResponse;
import com.example.service.IntegrateDigitalDocumentService;
import com.example.service.MemberService;
import com.example.utils.DLQUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberProcessor {
    private final Map<Long, Long> mapRetryApproveForMemberId = new HashMap<>();
    private static final long MAX_TIME_RETRY_APPROVE_DOCUMENT = 5;
    private final MemberService memberService;
    private final IntegrateDigitalDocumentService integrateDigitalDocumentService;

    public void approveDocument(Long id) {
        var member = memberService.approve(id);
        var approveDocumentRequest = new ApproveDocumentRequest();
        approveDocumentRequest.setMemberId(member.getId());
        approveDocumentRequest.setRequestId(UUID.randomUUID().toString());

        ApproveDocumentResponse response = null;
        try {
            response = integrateDigitalDocumentService.approveDocument(approveDocumentRequest);
        } catch (Exception exception) {
            log.error("Exception happen when integrating Digital Document Service '{}'", exception.getMessage());
            memberService.revertMemberAfterApproveDocumentFail(member);
        }

        if (Objects.nonNull(response)) {
            if (response.isSuccess()) {
                memberService.approveMemberSuccess(member);
            } else {
                handleFailApproveDocumentInDigitalService(member, response);
            }
        }
    }

    private void handleFailApproveDocumentInDigitalService(Member member, ApproveDocumentResponse response) {
        long retryTime = mapRetryApproveForMemberId.getOrDefault(member.getId(), 1L);
        if (!mapRetryApproveForMemberId.containsKey(member.getId())) {
            mapRetryApproveForMemberId.put(member.getId(), retryTime);
        } else {
            if (mapRetryApproveForMemberId.get(member.getId()) == MAX_TIME_RETRY_APPROVE_DOCUMENT) {
                log.error("member id '{}' is retried '{}' times to approve document in Digital Document Management Service",
                        member.getId(), MAX_TIME_RETRY_APPROVE_DOCUMENT);

//                         save log response in db
                mapRetryApproveForMemberId.remove(mapRetryApproveForMemberId.get(member.getId()));
                memberService.revertMemberAfterApproveDocumentFail(member);
                return;
            }
            mapRetryApproveForMemberId.put(member.getId(), retryTime + 1);
        }

        log.error("member id'{}' is retried '{}' times fail to approve document in Digital Document Service with error '{}'",
                member.getId(),
                mapRetryApproveForMemberId.get(member.getId()),
                Objects.isNull(response.getBody()) ? "Error in response from Digital Document Service" : response.getBody().toString());
        DLQUtil.addToDLQ(member.getId(), 5, TimeUnit.MINUTES, () -> approveDocument(member.getId()));
    }
}
