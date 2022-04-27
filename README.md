# 3scale to Okta REST Adapter

REST adapter to facilitate using Okta as the identity provider authorizing
requests to APIs managed by 3scale

## Set up

```bash
oc apply -f .openshift/01-imagestream.yaml -n YOURNAMESPACE
oc apply -f .openshift/02-buildconfig.yaml -n YOURNAMESPACE
oc apply -f .openshift/03-secret.yaml -n YOURNAMESPACE
oc apply -f .openshift/04-deploymentconfig.yaml -n YOURNAMESPACE
```

## Compile

```bash
./mvnw clean verify
```

## Deploy

```bash
oc start-build 3scale-okta-rest-adapter --from-file=target/threescale-okta-rest-adapter-1.0-SNAPSHOT.jar --follow -n YOURNAMESPACE
oc rollout latest dc/3scale-okta-rest-adapter -n YOURNAMESPACE
oc rollout status dc/3scale-okta-rest-adapter -n YOURNAMESPACE --watch
```

## Usage

Configure API Product to use OIDC with REST identity provider and URL of:

```
http://<ZYNC-CLIENT-ID>:<ZYNC-SECRET>@threescale-okta-rest-adapter.<YOURNAMESPACE>/
```
