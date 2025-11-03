package com.bookrecommend.book_recommend_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "recsys")
public class RecsysProperties {

    /**
     * Key of the model that should be active when the application starts.
     */
    private String defaultModel;

    /**
     * Registry of all available recommendation models keyed by identifier.
     * LinkedHashMap is used to preserve declaration order from YAML.
     */
    private Map<String, Model> models = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class Model {
        private String label;
        private String baseUrl;
        private boolean supportsOnlineLearning;
    }
}
