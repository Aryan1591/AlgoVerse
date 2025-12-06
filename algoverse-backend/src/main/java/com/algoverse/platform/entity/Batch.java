package com.algoverse.platform.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "batch")
@Data
public class Batch {
    @Id
    private String id;
    @Indexed(unique = true)
    private String batchName;
    private Instant batchCreatedAt;
    private Instant batchUpdatedAt;
    private String batchDescription;
    private boolean batchActive;
    private Integer totalUsers;
    private LocalDate startDate;          // when batch starts
    private LocalDate endDate;            // when batch ends

    public boolean isBatchActive() {
        return batchActive;
    }
}
