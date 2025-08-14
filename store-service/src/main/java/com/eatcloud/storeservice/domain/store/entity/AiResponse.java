package com.eatcloud.storeservice.domain.store.entity;

import com.eatcloud.storeservice.global.timeData.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;


import java.util.UUID;

@Entity
@Table(name = "p_ai_responses")
@Getter
@Setter
public class AiResponse extends BaseTimeEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ai_response_id")
    private UUID aiResponseId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

}