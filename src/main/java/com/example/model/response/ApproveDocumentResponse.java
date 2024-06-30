package com.example.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApproveDocumentResponse {
    private boolean isError;
    private boolean isSuccess;
    private Object body;
    private String message;
}
