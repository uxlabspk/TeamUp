package com.codehuntspk.teamup.service;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LiveKitService {

    @Value("${livekit.api.key}")
    private String apiKey;

    @Value("${livekit.api.secret}")
    private String apiSecret;

    @Value("${livekit.url}")
    private String livekitUrl;

    public String generateToken(String roomName, String participantName) {
        try {
            AccessToken token = new AccessToken(apiKey, apiSecret);
            token.setName(participantName);
            token.setIdentity(participantName);

            // Grant permissions for video room
            token.addGrants(new RoomJoin(true), new RoomName(roomName));

            return token.toJwt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate LiveKit token", e);
        }
    }

    public String getLivekitUrl() {
        return livekitUrl;
    }
}

