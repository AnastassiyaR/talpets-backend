package com.backend.mapper;

import com.backend.dto.UserLoginDTO;
import com.backend.dto.UserSignupDTO;
import com.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    UserLoginDTO toLoginDto(User user);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    User toUserFromLoginDto(UserLoginDTO dto);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    UserSignupDTO toSignupDto(User user);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    User toUserFromSignupDto(UserSignupDTO dto);
}