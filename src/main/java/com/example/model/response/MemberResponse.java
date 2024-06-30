package com.example.model.response;

import com.example.model.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {
    private long id;

    private String username;

    private String fullName;

    private Long identityNo;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime modifiedAt;

    private String modifiedBy;

    private String approvedBy;

    private MemberStatus status;

    private int version;
}
