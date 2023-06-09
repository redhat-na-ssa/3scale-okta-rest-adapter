package com.redhat.consulting.zyncadapter.mappers;

import com.okta.sdk.resource.application.OAuthResponseType;
import com.okta.sdk.resource.application.OpenIdConnectApplication;
import com.okta.sdk.resource.application.OpenIdConnectApplicationType;
import com.redhat.threescale.zyncadapter.rest.model.ClientUpdatePayload;
import com.redhat.threescale.zyncadapter.rest.model.GrantTypeEnum;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {
        OktaClientObjectFactory.class
    })
public interface ApplicationMapper {

    @Mapping(target = "label", source = "clientName")
    @Mapping(target = "credentials.OAuthClient.clientId", source = "clientId")
    @Mapping(target = "credentials.OAuthClient.clientSecret", source = "clientSecret")
    @Mapping(target = "credentials.OAuthClient.tokenEndpointAuthMethod", constant = "CLIENT_SECRET_BASIC")
    @Mapping(target = "credentials.OAuthClient.autoKeyRotation", constant = "false")
    @Mapping(target = "settings.OAuthClient.applicationType", source = "grantTypes")
    @Mapping(target = "settings.OAuthClient.grantTypes", source = "grantTypes")
    @Mapping(target = "settings.OAuthClient.responseTypes", source = "grantTypes")
    @Mapping(target = "settings.OAuthClient.redirectUris", source = "redirectUris")
    @Mapping(target = "accessibility", ignore = true)
    @Mapping(target = "embedded", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "licensing", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "resourceHref", ignore = true)
    @Mapping(target = "signOnMode", ignore = true)
    @Mapping(target = "visibility", ignore = true)
    void updateApplication(@MappingTarget OpenIdConnectApplication application, ClientUpdatePayload client);

    /**
     * Determine Okta application type based upon grant types requested.
     * <p>
     * <a href="https://developer.okta.com/docs/reference/api/apps/#add-oauth-2-0-client-application">More info here</a>
     *
     * @param input the list of grant types requested; if more are requested
     *              than Okta allows for the determined application type, Okta
     *              will throw an exception
     * @return the application type that was determined
     */
    default OpenIdConnectApplicationType mapApplicationType(List<GrantTypeEnum> input) {
        EnumSet<GrantTypeEnum> grantTypes = EnumSet.copyOf(input);

        if (grantTypes.contains(GrantTypeEnum.PASSWORD)
            && !grantTypes.contains(GrantTypeEnum.CLIENT_CREDENTIALS)) {
            // Native is the only app type that is listed as allowing password,
            // but Okta explicitly disallows client_credentials for it.
            // Also note, Okta docs display that NATIVE requires
            // authorization_code, but that's not actually the case
            return OpenIdConnectApplicationType.NATIVE;
        }

        if (grantTypes.contains(GrantTypeEnum.AUTHORIZATION_CODE)) {
            // WEB allows everything except password. Even though the docs
            // don't list client_credentials, it is allowed and even has UI
            // to select it on their web interface
            return OpenIdConnectApplicationType.WEB;
        }

        // Similarly, SERVICE allows password and implicit grants...
        return OpenIdConnectApplicationType.SERVICE;
    }

    default List<OAuthResponseType> mapResponseType(List<GrantTypeEnum> input) {
        return input.stream()
            .map(Mappers
                .getMapper(GrantTypeToResponseTypeMapper.class)
                ::mapGrantToResponse)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

}
