package com.example.dlqexample;

import com.example.model.entities.Member;
import com.example.model.enums.MemberStatus;
import com.example.model.request.ApproveDocumentRequest;
import com.example.model.response.ApproveDocumentResponse;
import com.example.utils.DLQEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;


public class DLQUnitTest {
    static final DelayQueue<DLQEntry<Long>> COMMON_DLQ = new DelayQueue<>();
    private static final Map<Long, Long> mapRetryApproveForMemberId = new HashMap<>();
    private static final long MAX_TIME_RETRY_APPROVE_DOCUMENT = 5;

    public static void main(String[] args) throws InterruptedException {
        Member member = Member.builder()
                .id(1)
                .fullName("nguyen van thang")
                .username("thangnv")
                .identityNo(12313233L)
                .status(MemberStatus.NEW)
                .build();
        approveDocument(member);
        while (!COMMON_DLQ.isEmpty()) {
            var entry = COMMON_DLQ.take();
            entry.apply();
        }
    }

    public static void approveDocument(Member member) {
        member.setStatus(MemberStatus.WAIT_FOR_APPROVE);

        var approveDocumentRequest = new ApproveDocumentRequest();
        approveDocumentRequest.setMemberId(member.getId());
        approveDocumentRequest.setRequestId(UUID.randomUUID().toString());

        ApproveDocumentResponse response = null;
        try {
            response = ApproveDocumentResponse.builder()
                    .isSuccess(false)
                    .isError(true)
                    .message("Fail")
                    .body("Token expired!")
                    .build();
        } catch (Exception exception) {
            System.out.printf("Exception happen when integrating Digital Document Service '%s'", exception.getMessage());
        }

        if (Objects.nonNull(response)) {
            if (response.isSuccess()) {
                System.out.println("Success");
            } else {
                handleFailApproveDocumentInDigitalService(member, response);
            }
        }
    }

    private static void handleFailApproveDocumentInDigitalService(Member member, ApproveDocumentResponse response) {
        long retryTime = mapRetryApproveForMemberId.getOrDefault(member.getId(), 1L);
        if (!mapRetryApproveForMemberId.containsKey(member.getId())) {
            mapRetryApproveForMemberId.put(member.getId(), retryTime);
        } else {
            if (mapRetryApproveForMemberId.get(member.getId()) == MAX_TIME_RETRY_APPROVE_DOCUMENT) {
                System.out.printf("member id '%s' is retried max '%s' times to approve document in Digital Document Management Service %n",
                        member.getId(), MAX_TIME_RETRY_APPROVE_DOCUMENT);

//                         save log response in db
                mapRetryApproveForMemberId.remove(mapRetryApproveForMemberId.get(member.getId()));
                return;
            }
            mapRetryApproveForMemberId.put(member.getId(), retryTime + 1);
        }

        System.out.printf("member id '%s' is retried '%s' times fail to approve document in Digital Document Service with error '%s' \n",
                member.getId(),
                mapRetryApproveForMemberId.get(member.getId()),
                Objects.isNull(response.getBody()) ? "Error in response from Digital Document Service" : response.getBody().toString());
        addToDLQ(member.getId(), 5, TimeUnit.SECONDS, () -> approveDocument(member));
    }

    public static void addToDLQ(Long requestId, long delay, TimeUnit timeUnit, Runnable runnable) {
        COMMON_DLQ.add(new DLQEntry<>(requestId, delay, timeUnit, runnable));
    }
}
