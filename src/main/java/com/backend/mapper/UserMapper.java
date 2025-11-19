package com.backend.mapper;

import com.backend.dto.UserSignupDTO;
import com.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUserFromSignupDto(UserSignupDTO dto);
}
