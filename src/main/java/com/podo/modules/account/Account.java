package com.podo.modules.account;

import com.podo.modules.tag.Tag;
import com.podo.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")   // 메서드 생성시 아이디만 사용(연관관계 복잡해질때 무한루프-> 스택오버플로우 발생할 수 있어서).
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    // 이메일 인증 계정 확인
    private boolean emailVerified;

    // 이메일 검증 토큰값
    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    // 가입 날짜
    private LocalDateTime joinedAt;

    // 프로필(자기소개)
    private String bio;

    // 앱사이트 URL
    private String url;

    // 직업
    private String occupation;

    // 살고있는 지역
    private String location;

    // 프로필 이미지
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    // 클럽 만들어진걸 이메일로 받을것인지
    private boolean clubCreatedByEmail;

    // 클럽 만들어진걸 웹으로 받을것인지
    private boolean clubCreatedByWeb = true;

    // 가입신청 결과를 이메일로 받을것인지
    private boolean clubEnrollmentResultByEmail;

    // 가입신청 결과를 웹으로 받을것인지
    private boolean clubEnrollmentResultByWeb = true;

    // 바뀐정보 이메일로 받을것인지
    private boolean clubUpdatedByEmail;

    // 바뀐정보 웹으로 받을것인지
    private boolean clubUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void geterateEmailCheckToken() {

        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        // 가입날짜
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        // 1시간 이전에 토큰만들어야 보낼 수 있음
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    private boolean crewCreatedByEmail;

    private boolean crewCreatedByWeb = true;

    private boolean crewUpdatedByEmail;

    private boolean crewUpdatedByWeb = true;

    private boolean crewEnrollmentResultByEmail;

    private boolean crewEnrollmentResultByWeb = true;
}
