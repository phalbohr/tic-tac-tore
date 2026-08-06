package com.tictactore.service;

import com.tictactore.dto.NotificationLogDto;
import com.tictactore.dto.PushSubscriptionRequest;

import com.tictactore.model.Match;
import com.tictactore.model.User;

import java.util.List;
import java.util.UUID;

public interface PushNotificationService {

    void subscribe(UUID userId, PushSubscriptionRequest request);

    void unsubscribe(UUID userId, String endpoint);

    void sendConfirmationRequest(Match match, List<User> opponents, boolean isDuplicateWarning);

    void sendRejectionNotification(Match match, User creator, String rejectionReason);

    void sendPartialConfirmationNotification(Match match, List<User> remainingOpponents, String firstConfirmerName);

    List<NotificationLogDto> getUserNotifications(UUID userId);
}

