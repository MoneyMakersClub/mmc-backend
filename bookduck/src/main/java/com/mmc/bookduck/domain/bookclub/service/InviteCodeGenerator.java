package com.mmc.bookduck.domain.bookclub.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InviteCodeGenerator {

    public String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public LocalDateTime defaultExpiry(int hours) {
        return LocalDateTime.now().plusHours(hours);
    }
}
