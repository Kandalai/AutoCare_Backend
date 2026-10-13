package com.autocare.dto;

import com.autocare.model.User;

public record UserSummaryResponse(Long id, String username, String email, String role) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
