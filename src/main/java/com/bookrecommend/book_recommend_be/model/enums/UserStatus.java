package com.bookrecommend.book_recommend_be.model.enums;

import com.bookrecommend.book_recommend_be.model.User;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BANNED;

    public static UserStatus fromUser(User user) {
        if (user.isBan()) {
            return BANNED;
        }
        return user.isActivate() ? ACTIVE : INACTIVE;
    }
}
