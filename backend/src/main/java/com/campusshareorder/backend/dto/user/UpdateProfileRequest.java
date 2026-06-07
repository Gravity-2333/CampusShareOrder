package com.campusshareorder.backend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 20, message = "昵称长度不能超过20")
    private String nickname;

    @Size(max = 50, message = "联系方式长度不能超过50")
    private String contactInfo;

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo == null ? null : contactInfo.trim();
    }
}
