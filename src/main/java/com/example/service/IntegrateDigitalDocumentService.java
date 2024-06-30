package com.example.service;

import com.example.model.request.ApproveDocumentRequest;
import com.example.model.response.ApproveDocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IntegrateDigitalDocumentService {

    //   create some unit test to test DLQ processor
    public ApproveDocumentResponse approveDocument(ApproveDocumentRequest request) {
        return null;
    }

    public ApproveDocumentResponse approveDocumentSuccess(ApproveDocumentRequest request) {
        return ApproveDocumentResponse.builder()
                .isSuccess(true)
                .isError(false)
                .message("success")
                .body(String.format("Approve document with request id '%s' for member id '%s'",
                        request.getRequestId(),
                        request.getMemberId()))
                .build();
    }

    public ApproveDocumentResponse approveDocumentFail(ApproveDocumentRequest request) {
        return ApproveDocumentResponse.builder()
                .isSuccess(false)
                .isError(true)
                .message("Fail")
                .body(String.format("System error happen when approving document with request id '%s' for member id '%s'",
                        request.getRequestId(),
                        request.getMemberId()))
                .build();
    }

    public ApproveDocumentResponse approveDocumentFailWithTokenExpired(ApproveDocumentRequest request) {
        return ApproveDocumentResponse.builder()
                .isSuccess(false)
                .isError(true)
                .message("Fail")
                .body("Token expired!")
                .build();
    }

    public String login(String username, String password) {
//        login in Digital Document Service
//        to get access token
        var accessToken = "123";
        return accessToken;
    }
}
