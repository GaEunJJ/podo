package com.podo.infra.mail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {
    // 받는 사람
    private String to;
    // 제목
    private String subject;
    // 메시지 내용
    private String message;

}
