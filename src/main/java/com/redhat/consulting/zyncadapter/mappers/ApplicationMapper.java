package com.redhat.consulting.zyncadapter.mappers;

import com.okta.sdk.resource.application.OpenIdConnectApplication;
import com.okta.sdk.resource.application.OpenIdConnectApplicationType;
import com.redhat.threescale.zyncadapter.rest.model.ClientUpdatePayload;
import com.redhat.threescale.zyncadapter.rest.model.GrantTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.EnumSet;
import java.util.List;

@Mapper(uses = {
    OktaClientObjectFactory.class
})
public interface ApplicationMapper {

    @Mapping(target = "label", source = "clientName")
    @Mapping(target = "credentials.OAuthClient.clientId", source = "clientId")
    @Mapping(target = "credentials.OAuthClient.clientSecret", source = "clientSecret")
    @Mapping(target = "settings.OAuthClient.applicationType", source = "grantTypes")
    @Mapping(target = "settings.OAuthClient.grantTypes", source = "grantTypes")
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

        if (grantTypes.contains(GrantTypeEnum.CLIENT_CREDENTIALS)) {
            // Okta only allows client credentials for SERVICE app types
            return OpenIdConnectApplicationType.SERVICE;
        } else if (grantTypes.contains(GrantTypeEnum.PASSWORD)) {
            // Okta only allows password grant type for NATIVE app types
            return OpenIdConnectApplicationType.NATIVE;
        }

        // Otherwise, WEB is the most lenient app type
        return OpenIdConnectApplicationType.WEB;
    }

}
