package com.redhat.consulting.zyncadapter.mappers;

import com.okta.sdk.resource.application.OAuthResponseType;
import com.redhat.threescale.zyncadapter.rest.model.GrantTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper
public interface GrantTypeToResponseTypeMapper {

    @ValueMapping(target = "CODE", source = "AUTHORIZATION_CODE")
    @ValueMapping(target = "TOKEN", source = "IMPLICIT")
    @ValueMapping(target = "<NULL>", source = "CLIENT_CREDENTIALS")
    @ValueMapping(target = "<NULL>", source = "PASSWORD")
    OAuthResponseType mapGrantToResponse(GrantTypeEnum input);

}
