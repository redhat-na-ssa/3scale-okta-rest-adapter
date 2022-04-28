package com.redhat.consulting.zyncadapter.mappers;

import com.okta.sdk.resource.application.OAuth2ScopeConsentGrant;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {
        OktaClientObjectFactory.class
    })
public interface ScopeGrantMapper {

    // Note 1: OAuth2ScopeConsentGrant#scopeId is actually scope name, not ID
    // Note 2: OAuth2ScopeConsentGrant#issuer is actually org-url, not issuer URL
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "issuer", source = "orgUrl")
    @Mapping(target = "scopeId", source = "scopeName")
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "embedded", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "resourceHref", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "userId", ignore = true)
    OAuth2ScopeConsentGrant createGrant(String orgUrl, String scopeName);

}
