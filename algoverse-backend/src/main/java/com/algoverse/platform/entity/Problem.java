package com.algoverse.platform.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "problems")
@Data
public class Problem {

    @Id
    private String id;
    @Indexed(unique = true)
    private String title;
    private String problemUrl;
    private String titleSlug;
    private Set<String> topics;
    private Category category;
}
