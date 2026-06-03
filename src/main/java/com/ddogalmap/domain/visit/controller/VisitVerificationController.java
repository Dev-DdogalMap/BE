package com.ddogalmap.domain.visit.controller;

import com.ddogalmap.domain.visit.dto.request.VisitVerificationRequest;
import com.ddogalmap.domain.visit.dto.response.VisitVerificationResponse;
import com.ddogalmap.domain.visit.service.VisitVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visit")
public class VisitVerificationController {

    private final VisitVerificationService visitVerificationService;

    @PostMapping("/visit-verification")
    public ResponseEntity<VisitVerificationResponse> verifyVisit(
            Authentication authentication,
            @RequestBody VisitVerificationRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        VisitVerificationResponse response =
                visitVerificationService.verifyVisit(userId, request);

        return ResponseEntity.ok(response);
    }
}