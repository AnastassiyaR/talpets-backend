package com.backend.mapper;


import com.backend.dto.CommentDTO;
import com.backend.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {
    CommentDTO toDto(Comment comment);
}
