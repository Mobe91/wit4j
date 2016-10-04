package com.codelawine.wit4j;

import javax.ws.rs.client.ClientBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 04.10.2016.
 */
public class WitConfiguration {

    private final String witAccessToken;
    private final String witApiVersion;
    private final Map<String, Action> actions;
    private final String witUrl;
    private final ClientBuilder providedClientBuilder;
    private final int maxSteps;

    private WitConfiguration(String witAccessToken, String witApiVersion, Map<String, Action> actions, String witUrl, ClientBuilder providedClientBuilder, int maxSteps) {
        this.witAccessToken = witAccessToken;
        this.witApiVersion = witApiVersion;
        this.actions = actions;
        this.witUrl = witUrl;
        this.providedClientBuilder = providedClientBuilder;
        this.maxSteps = maxSteps;
    }

    public String getWitAccessToken() {
        return witAccessToken;
    }

    public String getWitApiVersion() {
        return witApiVersion;
    }

    public Map<String, Action> getActions() {
        return actions;
    }

    public String getWitUrl() {
        return witUrl;
    }

    public ClientBuilder getProvidedClientBuilder() {
        return providedClientBuilder;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public static WitConfigurationBuilder newBuilder() {
        return new WitConfigurationBuilder();
    }

    public static class WitConfigurationBuilder {

        private static final String DEFAULT_WIT_URL = "https://api.wit.ai";
        private static final int DEFAULT_MAX_STEPS = 5;

        private String witAccessToken;
        private String witApiVersion;
        private final Map<String, Action> actions = new HashMap<>();
        private String witUrl;
        private ClientBuilder providedClientBuilder;
        private Integer maxSteps;

        private WitConfigurationBuilder() {
        }

        public WitConfigurationBuilder withWitAccessToken(String witAccessToken) {
            this.witAccessToken = witAccessToken;
            return this;
        }

        public WitConfigurationBuilder withWitApiVersion(String witApiVersion) {
            this.witApiVersion = witApiVersion;
            return this;
        }

        public WitConfigurationBuilder registerAction(Action action) {
            actions.put(action.getName(), action);
            return this;
        }

        public WitConfigurationBuilder withWitUrl(String witUrl) {
            this.witUrl = witUrl;
            return this;
        }

        /**
         * Can be used to pass vendor dependant ClientBuilder to customize implementation
         * specific details like e.g. the underlying HTTP client.
         * @param providedClientBuilder
         * @return
         */
        public WitConfigurationBuilder withProvidedClientBuilder(ClientBuilder providedClientBuilder) {
            this.providedClientBuilder = providedClientBuilder;
            return this;
        }

        public WitConfigurationBuilder withMaxSteps(Integer maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        public WitConfiguration build() {
            // check required configuration parameters
            if (witAccessToken == null) {
                throw new NullPointerException("witAccessToken");
            }
            if (witApiVersion == null) {
                throw new NullPointerException("witApiVersion");
            }
            if (witApiVersion == null) {
                throw new NullPointerException("witApiVersion");
            }
            validateActions(actions);

            String effectiveWitUrl = witUrl == null ? DEFAULT_WIT_URL : witUrl;
            int effectiveMaxSteps = maxSteps == null ? DEFAULT_MAX_STEPS : maxSteps;

            return new WitConfiguration(witAccessToken, witApiVersion, actions, effectiveWitUrl, providedClientBuilder, effectiveMaxSteps);
        }

        private static void validateActions(Map<String, Action> actions) {
            if (!actions.containsKey(Wit.SEND_ACTION)) {
                throw new RuntimeException("The '" + Wit.SEND_ACTION + "' action is missing.");
            }

            for (String actionName : actions.keySet()) {
                if ("say".equals(actionName) ||
                        "merge".equals(actionName) ||
                        "error".equals(actionName)) {
                    throw new RuntimeException("The '" + actionName + "' action has been deprecated.");
                }
            }
        }

    }
}
