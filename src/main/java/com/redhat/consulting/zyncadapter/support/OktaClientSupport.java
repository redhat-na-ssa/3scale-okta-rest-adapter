package com.redhat.consulting.zyncadapter.support;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.application.Application;
import com.okta.sdk.resource.application.ApplicationCredentialsOAuthClient;
import com.okta.sdk.resource.application.OAuthApplicationCredentials;
import com.okta.sdk.resource.application.OpenIdConnectApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OktaClientSupport {

    private final Client oktaClient;

    @Autowired
    public OktaClientSupport(Client oktaClient) {
        this.oktaClient = oktaClient;
    }

    public Application lookupClient(String clientId) {
        // This sucks. Why doesn't Okta let us filter server-side by client ID?
        return oktaClient.listApplications().stream()
            .filter(application -> {
                if (!OpenIdConnectApplication.class.isAssignableFrom(application.getClass())) {
                    return false;
                }

                OpenIdConnectApplication openIdConnectApplication = (OpenIdConnectApplication) application;

                return clientId.equals(
                    Optional.of(openIdConnectApplication)
                        .map(OpenIdConnectApplication::getCredentials)
                        .map(OAuthApplicationCredentials::getOAuthClient)
                        .map(ApplicationCredentialsOAuthClient::getClientId)
                        .orElse(null));
            })
            .findAny()
            .orElse(null);
    }

}
