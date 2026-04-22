package com.chesstune.core.controller;

import com.chesstune.core.dto.UserProfileResponse;
import com.chesstune.core.entity.UpsolveTask;
import com.chesstune.core.repository.UpsolveTaskRepository;
import com.chesstune.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User profile and leaderboard endpoints.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UpsolveTaskRepository upsolveTaskRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/{id}/upsolve-tasks")
    public ResponseEntity<List<UpsolveTask>> getUpsolveTasks(@PathVariable Long id) {
        return ResponseEntity.ok(upsolveTaskRepository.findByUserId(id));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserProfileResponse>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }
}
