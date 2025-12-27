package com.backend.mapper;

import com.backend.dto.FeedbackDTO;
import com.backend.model.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeedbackMapper {

    FeedbackDTO toDto(Feedback feedback);

    Feedback toEntity(FeedbackDTO feedbackDTO);
}