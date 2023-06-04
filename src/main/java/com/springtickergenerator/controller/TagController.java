package com.springtickergenerator.controller;

import com.springtickergenerator.entity.Tag;
import com.springtickergenerator.entity.User;
import com.springtickergenerator.exception.ResourceNotFoundException;
import com.springtickergenerator.model.payload.dto.EventDTO;
import com.springtickergenerator.repository.TagRepository;
import com.springtickergenerator.repository.UserRepository;
import com.springtickergenerator.security.JwtUtils;
import com.springtickergenerator.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagRepository tagRepository;
    private final TagService tagService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    @PostMapping("/save")
    public ResponseEntity<?> saveTag(@Valid @RequestBody Tag tag, @RequestHeader(name = "Authorization") String token) {

        if (token == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);

        userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user not found with this email :" + email));

        String auth = jwtUtils.getAuthorityClaim(token);

        if (auth.equals("ROLE_ADMIN")) {
            return ResponseEntity.ok().body(tagRepository.save(tag));
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action!");
        }

    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTag(@Valid @RequestBody Tag tag, @RequestParam(value = "tag-id") Long tagId,
                                       @RequestHeader(name = "Authorization") String token) {

        if (token == null || tagId == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);

        userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user not found with this email :" + email));

        String auth = jwtUtils.getAuthorityClaim(token);

        if (auth.equals("ROLE_ADMIN")) {
            return ResponseEntity.ok().body(tagService.updateTag(tag, tagId));
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action!");
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteTag(@RequestParam(value = "tag-id") Long tagId,
                                       @RequestHeader(name = "Authorization") String token) {

        if (token == null || tagId == null) {
            return ResponseEntity.badRequest().body("Bad request!");
        }

        String email = jwtUtils.extractUsername(token);

        userRepository.findUserByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("user not found with this email :" + email));

        String auth = jwtUtils.getAuthorityClaim(token);

        if (auth.equals("ROLE_ADMIN")) {
            tagService.deleteTag(tagId);
            return ResponseEntity.ok().body("success");
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action!");
        }


    }

}
