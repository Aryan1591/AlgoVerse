package com.algoverse.platform.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_profile")
@Data
public class UserProfile {
    @Id
    private String id;
    @Indexed(unique = true)
    private String authId; // reference to AuthAccount.id
    private String displayName;
    @Indexed(unique = true)
    private String leetCodeUserName;
    private boolean active = true;
    private MemberShip memberShip;
    private Stats stats;
    private Instant createdAt;
    private Instant updatedAt;
    private SyncStatus syncStatus;
    private Instant lastSyncedAt;
    private Integer currentStreak;
    private Integer maxStreak;
    @Indexed
    private String batchId;  // reference to batch
}
