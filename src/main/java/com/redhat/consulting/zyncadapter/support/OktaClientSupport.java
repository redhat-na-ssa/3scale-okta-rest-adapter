package com.redhat.consulting.zyncadapter.support;

import com.okta.sdk.client.Client;
import com.okta.sdk.resource.ResourceException;
import com.okta.sdk.resource.application.*;
import com.okta.sdk.resource.authorization.server.AuthorizationServer;
import com.redhat.consulting.zyncadapter.mappers.ScopeGrantMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OktaClientSupport {

    private static final Logger log = LoggerFactory.getLogger(OktaClientSupport.class);

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private static final Set<String> DEFAULT_SCOPES = new HashSet<>(Arrays.asList(
        "openid"

        // Okta flat rejects adding these... No idea why:
        /*
        ,
        "profile",
        "email",
        "address",
        "phone",
        "offline_access",
        "device_sso"
         */
    ));

    private final Client oktaClient;
    private final String orgUrl;
    private final ScopeGrantMapper scopeGrantMapper;

    @Autowired
    public OktaClientSupport(@Autowired Client oktaClient,
                             @Value("${okta.client.org-url}") String orgUrl,
                             @Autowired ScopeGrantMapper scopeGrantMapper) {
        this.oktaClient = oktaClient;
        this.orgUrl = orgUrl;
        this.scopeGrantMapper = scopeGrantMapper;
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

    public void finalizeClient(OpenIdConnectApplication application) {

        grantScopes(application, DEFAULT_SCOPES);

    }

    private void grantScopes(OpenIdConnectApplication application, Collection<String> scopes) {
        // Start with adding all scopes
        Set<String> scopesToAdd = new HashSet<>(scopes);

        if (scopesToAdd.isEmpty()) {
            // Short circuit and exit early if no scopes need to be added
            return;
        }

        // Remove scopes that have already been added to the application
        application.listScopeConsentGrants().stream()
            .map(OAuth2ScopeConsentGrant::getScopeId)
            .forEach(scopesToAdd::remove);

        if (scopesToAdd.isEmpty()) {
            // Short circuit and exit early if all scopes are already granted
            return;
        }

        // Filter to only attempt creation of scopes that exist
        Set<String> availableScopeNames = getAvailableScopes();
        CollectionUtils.filter(scopesToAdd, availableScopeNames::contains);

        for (String scopeName : scopesToAdd) {
            OAuth2ScopeConsentGrant newGrant = scopeGrantMapper.createGrant(orgUrl, scopeName);

            try {
                log.debug("Granting scope to client ID {}: org-url={}, scope name={}", application.getCredentials().getOAuthClient().getClientId(), orgUrl, scopeName);
                application.grantConsentToScope(newGrant);
            } catch (ResourceException e) {
                if ("E0000001".equals(e.getCode())) {
                    log.warn("Scope refused for client ID {}: org-url={}, scope name={}, causes={}", application.getCredentials().getOAuthClient().getClientId(), orgUrl, scopeName, e.getCauses());
                    continue;
                }

                throw e;
            }
        }
    }

    /**
     * Retrieve all the scopes available on this organization.
     *
     * @return the unique set of scope names
     * @apiNote Okta SDK automatically uses production grade in-memory
     * caching, so there shouldn't be a big impact to calculating this set on
     * each call. If configuring a wider/clustered deployment, the Okta SDK
     * should be set to configured with a cluster-aware caching mechanism
     * rather than each instance using default in-memory caching.
     */
    private Set<String> getAvailableScopes() {
        Set<String> availableScopes = new HashSet<>();

        for (AuthorizationServer authServer : oktaClient.listAuthorizationServers()) {
            for (OAuth2Scope scope : authServer.listOAuth2Scopes()) {
                availableScopes.add(scope.getName());
            }
        }

        return availableScopes;
    }

}
