<!-- markdownlint-disable MD033 -->
<!-- x-hide-in-docs-start -->
<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/open-feature/community/0e23508c163a6a1ac8c0ced3e4bd78faafe627c7/assets/logo/horizontal/white/openfeature-horizontal-white.svg" />
    <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/open-feature/community/0e23508c163a6a1ac8c0ced3e4bd78faafe627c7/assets/logo/horizontal/black/openfeature-horizontal-black.svg" />
    <img align="center" alt="OpenFeature Logo">
  </picture>
</p>

<h2 align="center">OpenFeature Java SDK</h2>

<!-- x-hide-in-docs-end -->
<!-- The 'github-badges' class is used in the docs -->
<p align="center" class="github-badges">
  <a href="https://github.com/open-feature/spec/releases/tag/v0.7.0">
    <img alt="Specification" src="https://img.shields.io/static/v1?label=specification&message=v0.7.0&color=yellow&style=for-the-badge" />
  </a>
  <!-- x-release-please-start-version -->

  <a href="https://github.com/open-feature/java-sdk/releases/tag/v1.14.1">
    <img alt="Release" src="https://img.shields.io/static/v1?label=release&message=v1.14.1&color=blue&style=for-the-badge" />
  </a>  

  <!-- x-release-please-end -->
  <br/>
  <a href="https://javadoc.io/doc/dev.openfeature/sdk">
    <img alt="Javadoc" src="https://javadoc.io/badge2/dev.openfeature/sdk/javadoc.svg" />
  </a>
  <a href="https://maven-badges.herokuapp.com/maven-central/dev.openfeature/sdk">
    <img alt="Maven Central" src="https://maven-badges.herokuapp.com/maven-central/dev.openfeature/sdk/badge.svg" />
  </a>
  <a href="https://codecov.io/gh/open-feature/java-sdk">
    <img alt="Codecov" src="https://codecov.io/gh/open-feature/java-sdk/branch/main/graph/badge.svg?token=XMS9L7PBY1" />
  </a>
  <a href="https://bestpractices.coreinfrastructure.org/projects/6241">
    <img alt="CII Best Practices" src="https://bestpractices.coreinfrastructure.org/projects/6241/badge" />
  </a>
</p>
<!-- x-hide-in-docs-start -->

[OpenFeature](https://openfeature.dev) is an open specification that provides a vendor-agnostic, community-driven API for feature flagging that works with your favorite feature flag management tool.

<!-- x-hide-in-docs-end -->
## 🚀 Quick start

### Requirements

- Java 8+ (compiler target is 1.8)

Note that this library is intended to be used in server-side contexts and has not been evaluated for use on mobile devices.

### Install

#### Maven

<!-- x-release-please-start-version -->
```xml
<dependency>
    <groupId>dev.openfeature</groupId>
    <artifactId>sdk</artifactId>
    <version>1.14.1</version>
</dependency>
```
<!-- x-release-please-end-version -->

If you would like snapshot builds, this is the relevant repository information:

```xml
<repositories>
    <repository>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <id>sonatype</id>
        <name>Sonatype Repository</name>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
</repositories>
```

#### Gradle

<!-- x-release-please-start-version -->
```groovy
dependencies {
    implementation 'dev.openfeature:sdk:1.14.1'
}
```
<!-- x-release-please-end-version -->

### Usage

```java
public void example(){

    // flags defined in memory
    Map<String, Flag<?>> myFlags = new HashMap<>();
    myFlags.put("v2_enabled", Flag.builder()
        .variant("on", true)
        .variant("off", false)
        .defaultVariant("on")
        .build());

    // configure a provider
    OpenFeatureAPI api = OpenFeatureAPI.getInstance();
    api.setProviderAndWait(new InMemoryProvider(myFlags));

    // create a client
    Client client = api.getClient();

    // get a bool flag value
    boolean flagValue = client.getBooleanValue("v2_enabled", false);
}
```

### API Reference

See [here](https://javadoc.io/doc/dev.openfeature/sdk/latest/) for the Javadocs.

## 🌟 Features

| Status | Features                                                            | Description                                                                                                                                                   |
| ------ |---------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ✅     | [Providers](#providers)                                             | Integrate with a commercial, open source, or in-house feature management tool.                                                                                |
| ✅     | [Targeting](#targeting)                                             | Contextually-aware flag evaluation using [evaluation context](https://openfeature.dev/docs/reference/concepts/evaluation-context).                            |
| ✅     | [Hooks](#hooks)                                                     | Add functionality to various stages of the flag evaluation life-cycle.                                                                                        |
| ✅     | [Tracking](#tracking)                                               | Associate user actions with feature flag evaluations.                                                                                                         |
| ✅     | [Logging](#logging)                                                 | Integrate with popular logging packages.                                                                                                                      |
| ✅     | [Domains](#domains)                                                 | Logically bind clients with providers.                                                                                                                        |
| ✅     | [Eventing](#eventing)                                               | React to state changes in the provider or flag management system.                                                                                             |
| ✅     | [Shutdown](#shutdown)                                               | Gracefully clean up a provider during application shutdown.                                                                                                   |
| ✅     | [Transaction Context Propagation](#transaction-context-propagation) | Set a specific [evaluation context](https://openfeature.dev/docs/reference/concepts/evaluation-context) for a transaction (e.g. an HTTP request or a thread). |   
| ✅     | [Extending](#extending)                                             | Extend OpenFeature with custom providers and hooks.                                                                                                           |

<sub>Implemented: ✅ | In-progress: ⚠️ | Not implemented yet: ❌</sub>

### Providers

[Providers](https://openfeature.dev/docs/reference/concepts/provider) are an abstraction between a flag management system and the OpenFeature SDK.
Look [here](https://openfeature.dev/ecosystem?instant_search%5BrefinementList%5D%5Btype%5D%5B0%5D=Provider&instant_search%5BrefinementList%5D%5Btechnology%5D%5B0%5D=Java) for a complete list of available providers.
If the provider you're looking for hasn't been created yet, see the [develop a provider](#develop-a-provider) section to learn how to build it yourself.

Once you've added a provider as a dependency, it can be registered with OpenFeature like this:
   
#### Synchronous  
  
To register a provider in a blocking manner to ensure it is ready before further actions are taken, you can use the `setProviderAndWait` method as shown below:     
  
```java
    OpenFeatureAPI api = OpenFeatureAPI.getInstance();
    api.setProviderAndWait(new MyProvider());
```  
  
#### Asynchronous    

To register a provider in a non-blocking manner, you can use the `setProvider` method as shown below:  

```java
    OpenFeatureAPI.getInstance().setProvider(new MyProvider());
```    

In some situations, it may be beneficial to register multiple providers in the same application.
This is possible using [domains](#domains), which is covered in more detail below.

### Targeting

Sometimes, the value of a flag must consider some dynamic criteria about the application or user, such as the user's location, IP, email address, or the server's location.
In OpenFeature, we refer to this as [targeting](https://openfeature.dev/specification/glossary#targeting).
If the flag management system you're using supports targeting, you can provide the input data using the [evaluation context](https://openfeature.dev/docs/reference/concepts/evaluation-context).

```java
// set a value to the global context
OpenFeatureAPI api = OpenFeatureAPI.getInstance();
Map<String, Value> apiAttrs = new HashMap<>();
apiAttrs.put("region", new Value(System.getEnv("us-east-1")));
EvaluationContext apiCtx = new ImmutableContext(apiAttrs);
api.setEvaluationContext(apiCtx);

// set a value to the client context
Map<String, Value> clientAttrs = new HashMap<>();
clientAttrs.put("region", new Value(System.getEnv("us-east-1")));
EvaluationContext clientCtx = new ImmutableContext(clientAttrs);
Client client = api.getInstance().getClient();
client.setEvaluationContext(clientCtx);

// set a value to the invocation context
Map<String, Value> requestAttrs = new HashMap<>();
requestAttrs.put("email", new Value(session.getAttribute("email")));
requestAttrs.put("product", new Value("productId"));
String targetingKey = session.getId();
EvaluationContext reqCtx = new ImmutableContext(targetingKey, requestAttrs);

boolean flagValue = client.getBooleanValue("some-flag", false, reqCtx);
```

### Hooks

[Hooks](https://openfeature.dev/docs/reference/concepts/hooks) allow for custom logic to be added at well-defined points of the flag evaluation life-cycle
Look [here](https://openfeature.dev/ecosystem?instant_search%5BrefinementList%5D%5Btype%5D%5B0%5D=Hook&instant_search%5BrefinementList%5D%5Btechnology%5D%5B0%5D=Java) for a complete list of available hooks.
If the hook you're looking for hasn't been created yet, see the [develop a hook](#develop-a-hook) section to learn how to build it yourself.

Once you've added a hook as a dependency, it can be registered at the global, client, or flag invocation level.

```java
  // add a hook globally, to run on all evaluations
  OpenFeatureAPI api = OpenFeatureAPI.getInstance();
  api.addHooks(new ExampleHook());
  
  // add a hook on this client, to run on all evaluations made by this client
  Client client = api.getClient();        
  client.addHooks(new ExampleHook());
  
  // add a hook for this evaluation only
  Boolean retval = client.getBooleanValue(flagKey, false, null,
          FlagEvaluationOptions.builder().hook(new ExampleHook()).build());
```

### Tracking

The [tracking API](https://openfeature.dev/specification/sections/tracking/) allows you to use OpenFeature abstractions to associate user actions with feature flag evaluations.
This is essential for robust experimentation powered by feature flags. Note that, unlike methods that handle feature flag evaluations, calling `track(...)` may throw an `IllegalArgumentException` if an empty string is passed as the `trackingEventName`.

```java
OpenFeatureAPI api = OpenFeatureAPI.getInstance();
api.getClient().track("visited-promo-page", new MutableTrackingEventDetails(99.77).add("currency", "USD"));
```

### Logging

The Java SDK uses SLF4J. See the [SLF4J manual](https://slf4j.org/manual.html) for complete documentation.
Note that in accordance with the OpenFeature specification, the SDK doesn't generally log messages during flag evaluation.

#### Logging Hook

The Java SDK includes a `LoggingHook`, which logs detailed information at key points during flag evaluation, using SLF4J's structured logging API.
This hook can be particularly helpful for troubleshooting and debugging; simply attach it at the global, client or invocation level and ensure your log level is set to "debug".

See [hooks](#hooks) for more information on configuring hooks.

### Domains

Clients can be assigned to a domain.
A domain is a logical identifier which can be used to associate clients with a particular provider.
If a domain has no associated provider, the global provider is used.  

```java
FeatureProvider scopedProvider = new MyProvider();

// registering the default provider
OpenFeatureAPI.getInstance().setProvider(LocalProvider());
// registering a provider to a domain
OpenFeatureAPI.getInstance().setProvider("my-domain", new CachedProvider());

// A client bound to the default provider
Client clientDefault = OpenFeatureAPI.getInstance().getClient();
// A client bound to the CachedProvider provider
Client domainScopedClient = OpenFeatureAPI.getInstance().getClient("my-domain");
```

Providers for domains can be set in a blocking or non-blocking way. 
For more details, please refer to the [providers](#providers) section.  

### Eventing

Events allow you to react to state changes in the provider or underlying flag management system, such as flag definition changes, provider readiness, or error conditions.
Initialization events (`PROVIDER_READY` on success, `PROVIDER_ERROR` on failure) are dispatched for every provider.
Some providers support additional events, such as `PROVIDER_CONFIGURATION_CHANGED`.

Please refer to the documentation of the provider you're using to see what events are supported.

```java
// add an event handler to a client
Client client = OpenFeatureAPI.getInstance().getClient();
client.onProviderConfigurationChanged((EventDetails eventDetails) -> {
    // do something when the provider's flag settings change
});

// add an event handler to the global API
OpenFeatureAPI.getInstance().onProviderStale((EventDetails eventDetails) -> {
    // do something when the provider's cache goes stale
});
```

### Shutdown

The OpenFeature API provides a close function to perform a cleanup of all registered providers.
This should only be called when your application is in the process of shutting down.

```java
// shut down all providers
OpenFeatureAPI.getInstance().shutdown();
```

### Transaction Context Propagation
Transaction context is a container for transaction-specific evaluation context (e.g. user id, user agent, IP).
Transaction context can be set where specific data is available (e.g. an auth service or request handler) and by using the transaction context propagator it will automatically be applied to all flag evaluations within a transaction (e.g. a request or thread).
By default, the `NoOpTransactionContextPropagator` is used, which doesn't store anything.
To register a `ThreadLocal` context propagator, you can use the `setTransactionContextPropagator` method as shown below.
```java
// registering the ThreadLocalTransactionContextPropagator
OpenFeatureAPI.getInstance().setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());
```
Once you've registered a transaction context propagator, you can propagate the data into request-scoped transaction context.

```java
// adding userId to transaction context
OpenFeatureAPI api = OpenFeatureAPI.getInstance();
Map<String, Value> transactionAttrs = new HashMap<>();
transactionAttrs.put("userId", new Value("userId"));
EvaluationContext transactionCtx = new ImmutableContext(transactionAttrs);
api.setTransactionContext(transactionCtx);
```
Additionally, you can develop a custom transaction context propagator by implementing the `TransactionContextPropagator` interface and registering it as shown above.

## Extending

### Develop a provider

To develop a provider, you need to create a new project and include the OpenFeature SDK as a dependency.
This can be a new repository or included in [the existing contrib repository](https://github.com/open-feature/java-sdk-contrib) available under the OpenFeature organization.
You’ll then need to write the provider by implementing the `FeatureProvider` interface exported by the OpenFeature SDK.

```java
public class MyProvider implements FeatureProvider {
    @Override
    public Metadata getMetadata() {
        return () -> "My Provider";
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        // start up your provider
    }

    @Override
    public void shutdown() {
        // shut down your provider
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        // resolve a boolean flag value
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        // resolve a string flag value
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
        // resolve an int flag value
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
        // resolve a double flag value
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
        // resolve an object flag value
    }
}
```

If you'd like your provider to support firing events, such as events for when flags are changed in the flag management system, extend `EventProvider`.

```java
class MyEventProvider extends EventProvider {
    @Override
    public Metadata getMetadata() {
        return () -> "My Event Provider";
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        // emit events when flags are changed in a hypothetical REST API
        this.restApiClient.onFlagsChanged(() -> {
            ProviderEventDetails details = ProviderEventDetails.builder().message("flags changed in API!").build();
            this.emitProviderConfigurationChanged(details);
        });
    }

    @Override
    public void shutdown() {
        // shut down your provider
    }

    // remaining provider methods...
}
```

Providers no longer need to manage their own state, this is done by the SDK itself. If desired, the state of a provider 
can be queried through the client that uses the provider.

```java
OpenFeatureAPI.getInstance().getClient().getProviderState();
```

> Built a new provider? [Let us know](https://github.com/open-feature/openfeature.dev/issues/new?assignees=&labels=provider&projects=&template=document-provider.yaml&title=%5BProvider%5D%3A+) so we can add it to the docs!

### Develop a hook

To develop a hook, you need to create a new project and include the OpenFeature SDK as a dependency.
This can be a new repository or included in [the existing contrib repository](https://github.com/open-feature/java-sdk-contrib) available under the OpenFeature organization.
Implement your own hook by conforming to the `Hook interface`.

```java
class MyHook implements Hook {

    @Override
    public Optional before(HookContext ctx, Map hints) {
        // code that runs before the flag evaluation
    }

    @Override
    public void after(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        // code that runs after the flag evaluation succeeds
    }

    @Override
    public void error(HookContext ctx, Exception error, Map hints) {
        // code that runs when there's an error during a flag evaluation
    }

    @Override
    public void finallyAfter(HookContext ctx, FlagEvaluationDetails details, Map hints) {
        // code that runs regardless of success or error
    }
};
```

> Built a new hook? [Let us know](https://github.com/open-feature/openfeature.dev/issues/new?assignees=&labels=hook&projects=&template=document-hook.yaml&title=%5BHook%5D%3A+) so we can add it to the docs!

<!-- x-hide-in-docs-start -->
## ⭐️ Support the project

- Give this repo a ⭐️!
- Follow us on social media:
  - Twitter: [@openfeature](https://twitter.com/openfeature)
  - LinkedIn: [OpenFeature](https://www.linkedin.com/company/openfeature/)
- Join us on [Slack](https://cloud-native.slack.com/archives/C0344AANLA1)
- For more, check out our [community page](https://openfeature.dev/community/)

## 🤝 Contributing

Interested in contributing? Great, we'd love your help! To get started, take a look at the [CONTRIBUTING](CONTRIBUTING.md) guide.

### Thanks to everyone who has already contributed

<a href="https://github.com/open-feature/java-sdk/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=open-feature/java-sdk" alt="Pictures of the folks who have contributed to the project" />
</a>

Made with [contrib.rocks](https://contrib.rocks).
<!-- x-hide-in-docs-end -->
