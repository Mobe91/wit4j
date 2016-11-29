# Wit4j

Wit4j is a java client/SDK for [Wit.ai](https://wit.ai) quite analogous to the Node.js client under https://github.com/wit-ai/node-wit.
 
## Getting Started

Wit4j uses the JAX-RS 2.0 client API and you need to add a proper implementation dependency to use it. The following RESTEASY setup has been tested:

```
<dependency>
    <groupId>com.codelawine.wit4j</groupId>
    <artifactId>wit4j-core</artifactId>
    <version>LATEST</version>
</dependency>
<dependency>
    <groupId>org.jboss.resteasy</groupId>
    <artifactId>resteasy-client</artifactId>
    <version>3.0.14.Final</version>
</dependency>
```

Use it like this:

```java
public class Wit4jExample {
    private static final String WIT_API_VERSION = "20160823";
 
    private final Wit wit;
    
    public Wit4jExample() {
        WitConfiguration witConfig = WitConfiguration.newBuilder()
                    // register custom actions
                    .registerAction(new MySendAction())
                    .registerAction(new DoStuffAction())
                    .withWitAccessToken(config.getWitAccessToken())
                    .withWitApiVersion(WIT_API_VERSION)
                    .build();

        // create wit.ai client
        wit = new Wit(config.getWitAccessToken(), WIT_API_VERSION, actions);
    }
    
    public void onMessage(String senderId, String message) {
        Session session = Sessions.getOrCreate(senderId);
        session.setContext(wit.runActions(session.getId(), message, session.getContext()));
    }
}

public class MySendAction implements Action {

    // e.g. Facebook Messenger
    private SomeMessengerClient messengerClient;

    @Override
    public String getName() {
        return Wit.SEND_ACTION;
    }
    
    @Override
    public JsonObject perform(Object... params) {
        WitRequest witRequest = (WitRequest) params[0];
        WitResponse witResponse = (WitResponse) params[1];
        
        Session session = Sessions.byId(witRequest.getSessionId());
        
        messengerClient.sendMessage(session.getSenderId(), witResponse.getMessage());
        return null;
    }
}

public class DoStuffAction implements Action {

    @Override
    public String getName() {
        return "doStuff";
    }
    
    @Override
    public JsonObject perform(Object... params) {
        WitRequest witRequest = (WitRequest) params[0];
        
        Session session = Sessions.byId(witRequest.getSessionId());
        
        // retrieve previous context if needed
        JsonObject context = witRequest.getContext();
        
        // return new context
        return Json.createObjectBuilder()
            .add("didStuff", true)
            .build();
    }
}

public class Sessions {
    // some basic session management
}

```

## Session Management

A facility for session management is currently not provided by wit4j but it is easy to implement a very basic in-memory session management yourself.
In case of CDI you could utilize an `@ApplicationScoped` bean.
```java
@ApplicationScoped
public class Sessions {
   private Map<String, Session> sessionsById = new ConcurrentHashMap<>();
   public Session getOrCreateSession(String senderId) {
        Session session = sessionsById.get(senderId);
        if (session == null) {
            session = new Session(senderId);
            Session existingSession = sessionsById.putIfAbsent(senderId, session);
            session = existingSession == null ? session : existingSession;
        }
        return session;
    }
}
```
The `Session` implementation is also up to you. A minimum version should be capable of storing 

- an identifier for the sender this session is attached to and 

- the current wit.ai context as JSON

Here is an example:
```java
public class Session {

    private final String senderId;
    private JsonObject context;

    public Session(String senderId) {
        this.senderId= senderId;
    }

    public String getSenderId() {
        return senderId;
    }

    public JsonObject getContext() {
        return context;
    }

    public void setContext(JsonObject context) {
        this.context = context;
    }
}
```