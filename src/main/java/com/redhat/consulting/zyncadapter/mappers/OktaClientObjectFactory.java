package com.redhat.consulting.zyncadapter.mappers;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.Resource;
import com.okta.sdk.resource.application.OpenIdConnectApplication;
import org.mapstruct.TargetType;
import org.springframework.stereotype.Component;

@Component
public class OktaClientObjectFactory {

    private final Client oktaClient;

    public OktaClientObjectFactory(Client oktaClient) {
        this.oktaClient = oktaClient;
    }

    public <T extends Resource> T createResource(@TargetType Class<T> clazz) {
        return oktaClient.instantiate(clazz);
    }

    public OpenIdConnectApplication createApplication() {
        return createResource(OpenIdConnectApplication.class);
    }

}
