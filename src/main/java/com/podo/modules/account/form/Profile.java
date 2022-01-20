package com.podo.modules.account.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class Profile {
    // 프로필(자기소개)
    @Length(max = 35)
    private String bio;

    // 앱사이트 URL
    @Length(max = 50)
    private String url;

    // 직업
    @Length(max = 50)
    private String occupation;

    // 살고있는 지역
    @Length(max = 50)
    private String location;

    // 프로필 이미지
    private String profileImage;

}
