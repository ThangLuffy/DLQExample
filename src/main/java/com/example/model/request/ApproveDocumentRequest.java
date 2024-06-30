package com.example.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApproveDocumentRequest {
    private String requestId;
    private Long memberId;
}
