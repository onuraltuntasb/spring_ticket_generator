package com.springticketgenerator.controller;

import com.springticketgenerator.entity.Tag;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.repository.TagRepository;
import com.springticketgenerator.repository.UserRepository;
import com.springticketgenerator.security.JwtUtils;
import com.springticketgenerator.service.TagService;
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
    public ResponseEntity<?> saveTag(@Valid @RequestBody Tag tag,
                                     @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                     String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        userRepository.findUserByEmail(email)
                      .orElseThrow(() -> new ResourceNotFoundException("user not found with this email :" + email));

        String token = egAuthCookie.substring(15, egAuthCookie.indexOf("email"));

        String auth = jwtUtils.getAuthorityClaim(token);

        if (auth.equals("ROLE_ADMIN")) {
            return ResponseEntity.ok().body(tagRepository.save(tag));
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }

    @GetMapping("/all")
    public ResponseEntity<?> getTags(
            @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull") String egAuthCookie) {

        return ResponseEntity.ok(tagRepository.findAll());

    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTag(@Valid @RequestBody Tag tag, @RequestParam(value = "tag-id") Long tagId,
                                       @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                       String egAuthCookie) {


        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        String token = egAuthCookie.substring(15, egAuthCookie.indexOf("email"));

        String auth = jwtUtils.getAuthorityClaim(token);

        userRepository.findUserByEmail(email)
                      .orElseThrow(() -> new ResourceNotFoundException("user not found with this email :" + email));

        if (auth.equals("ROLE_ADMIN")) {
            return ResponseEntity.ok().body(tagService.updateTag(tag, tagId));
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteTag(@Valid @RequestBody Tag tag, @RequestParam(value = "tag-id") Long tagId,
                                       @CookieValue(name = "eg-auth-cookie", defaultValue = "emptyOrNull")
                                       String egAuthCookie) {

        String email = egAuthCookie.substring(egAuthCookie.indexOf("email") + 6);

        String token = egAuthCookie.substring(15, egAuthCookie.indexOf("email"));

        String auth = jwtUtils.getAuthorityClaim(token);


        userRepository.findUserByEmail(email)
                      .orElseThrow(() -> new ResourceNotFoundException("user not found with this email :" + email));

        if (auth.equals("ROLE_ADMIN")) {
            tagService.deleteTag(tagId);
            return ResponseEntity.ok().body("success");
        } else {
            return ResponseEntity.badRequest().body("You are not allowed to this action");
        }

    }

}
