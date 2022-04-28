# 3scale to Okta REST Adapter

REST adapter to facilitate using Okta as the identity provider authorizing
requests to APIs managed by 3scale

## Installation

### Set up

```bash
oc apply -f .openshift/01-imagestream.yaml -n YOURNAMESPACE
oc apply -f .openshift/02-buildconfig.yaml -n YOURNAMESPACE
oc apply -f .openshift/03-secret.yaml -n YOURNAMESPACE
oc apply -f .openshift/04-deploymentconfig.yaml -n YOURNAMESPACE
oc apply -f .openshift/05-service.yaml -n YOURNAMESPACE
```

### Compile

```bash
./mvnw clean verify
```

### Deploy

```bash
oc start-build 3scale-okta-rest-adapter --from-file=target/threescale-okta-rest-adapter-1.0-SNAPSHOT.jar --follow -n YOURNAMESPACE
oc rollout latest dc/3scale-okta-rest-adapter -n YOURNAMESPACE
oc rollout status dc/3scale-okta-rest-adapter -n YOURNAMESPACE --watch
```

### Usage

Configure API Product to use OIDC with REST identity provider and URL of:

```
http://<ZYNC-CLIENT-ID>:<ZYNC-SECRET>@threescale-okta-rest-adapter.<YOURNAMESPACE>.svc/
```

Where `<ZYNC-CLIENT-ID>` and `<ZYNC-SECRET>` are the client ID and client secret
for a client that has already been defined in the Okta environment.

## Implementation Notes

### OAuth Clients are Okta Applications

Okta maintains OAuth/OIDC clients as part of the Okta "Application" list.
Because of this, applications created by this integration app may need to have
users or groups assigned to them before they can be used to create tokens.

### Grant Type Restrictions

By default, Okta imposes a strict limitation on the grant types allowed for
each "application type". This application attempts to automatically select
the Okta application type (web, native, service) depending upon the grant types
requested by 3scale.

### Scope Grants

Okta's management API is very unclear on scope grants to applications. On
the one hand, it seems like the intent is for generalized scopes to be
configured at the Authorization Server level, but on the other, almost all the
default scopes are specific to managing aspects of Okta configuration.

In addition, the API for interacting with scopes seems very immature. For
example: to grant a scope to an application, the request must include both
`issuer` and `scopeId` -- however these are misleading labels. The `issuer` is
not the Authorization Server's issuer value; instead, it is the general
organization URL for the whole Okta environment. Similarly, the key `scopeId`
is actually to be set with a value of the scope name and not the scope's ID.

Beyond the misleading labeling, Okta rejects scope grants with very little
detail for the reason: it states only `scopeId: 'scopeId' is invalid`. This
makes setting scopes for Service app types (`client_credentials`) almost
impossible.

Instead of setting scopes programmatically, it is easier to just define a scope
on the Okta Authorization Server and set it as a "default" scope. Then clients
will be able to generate tokens.

### Caching

The Okta Java SDK automatically comes with in-memory caching configured for
production use. This is useful for individual deployments of apps like this.

When using the SDK in clustered deployment configurations, Okta recommends
changing the SDK's cache to use a proper clustered cache mechanism.

## Other Notes

A free developer account for Okta can be created at
https://developer.okta.com/signup/

This is useful for developing enhancements to this integration app
