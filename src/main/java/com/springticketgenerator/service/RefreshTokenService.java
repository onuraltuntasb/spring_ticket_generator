package com.springticketgenerator.service;


import com.springticketgenerator.entity.RefreshToken;
import com.springticketgenerator.exception.ResourceNotFoundException;
import com.springticketgenerator.exception.TokenCustomException;
import com.springticketgenerator.repository.RefreshTokenRepository;
import com.springticketgenerator.repository.UserRepository;
import com.springticketgenerator.setup.TicketProperties;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    TicketProperties ticketProperties = new TicketProperties();

    private final String jwtRefreshExpirationSecond = ticketProperties.getJwtRefreshExpirationSecond();
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        //System.out.println("refreshTokenDurationMs expire when : " + jwtRefreshExpirationSecond);
        refreshToken.setUser(userRepository.findById(userId)
                                           .orElseThrow(() -> new ResourceNotFoundException(
                                                   "User not found in refresh token method!")));
        refreshToken.setExpiryDate(Instant.now().plusSeconds(Long.parseLong(jwtRefreshExpirationSecond)));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);

            throw new TokenCustomException(token.getToken(),
                                           "Refresh token was expired. Please make a new sign in request"
            );
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId)
                                                          .orElseThrow(() -> new ResourceNotFoundException(
                                                                  "User not found in refresh token service!")));
    }
}
