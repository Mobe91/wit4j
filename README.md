# Wit4j

Wit4j is a java client for [Wit.ai](https://wit.ai) quite analogous to the Node.js client under https://github.com/wit-ai/node-wit.
 
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

```
public class Wit4jExample {
    private static final String WIT_API_VERSION = "20160823";
 
    private final Wit wit;
    
    public Wit4jExample() {
        // register custom actions
        Set<Action> actions = new HashSet<>();
        actions.add(new MySendAction());
        actions.add(new DoStuffAction());
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
