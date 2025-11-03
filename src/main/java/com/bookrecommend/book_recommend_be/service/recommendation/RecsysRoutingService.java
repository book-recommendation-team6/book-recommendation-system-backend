package com.bookrecommend.book_recommend_be.service.recommendation;

import com.bookrecommend.book_recommend_be.config.RecsysProperties;
import com.bookrecommend.book_recommend_be.dto.recommendation.RecommendationModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class RecsysRoutingService {

    private final Map<String, RecsysProperties.Model> models;
    private final AtomicReference<String> activeModelKey = new AtomicReference<>();

    public RecsysRoutingService(RecsysProperties properties) {
        if (properties.getModels() == null || properties.getModels().isEmpty()) {
            throw new IllegalStateException("No recommender models configured under 'recsys.models'");
        }
        this.models = Collections.unmodifiableMap(properties.getModels());

        String initialKey = properties.getDefaultModel();
        if (!StringUtils.hasText(initialKey) || !models.containsKey(initialKey)) {
            initialKey = models.keySet().iterator().next();
        }
        activeModelKey.set(initialKey);
        log.info("Active recommendation model initialized to '{}'", initialKey);
    }

    public String getActiveModelKey() {
        return activeModelKey.get();
    }

    public RecsysProperties.Model getActiveModel() {
        return models.get(activeModelKey.get());
    }

    public String getActiveBaseUrl() {
        RecsysProperties.Model model = getActiveModel();
        if (model == null || !StringUtils.hasText(model.getBaseUrl())) {
            throw new IllegalStateException("Active recommender model does not have a base URL configured");
        }
        return model.getBaseUrl();
    }

    public RecommendationModelInfo activateModel(String modelKey) {
        if (!models.containsKey(modelKey)) {
            throw new IllegalArgumentException("Unknown recommender model: " + modelKey);
        }
        activeModelKey.set(modelKey);
        log.info("Switched active recommender model to '{}'", modelKey);
        return buildModelInfo(modelKey, models.get(modelKey), true);
    }

    public List<RecommendationModelInfo> getAvailableModels() {
        List<RecommendationModelInfo> infos = new ArrayList<>(models.size());
        String current = activeModelKey.get();
        models.forEach((key, model) -> infos.add(buildModelInfo(key, model, key.equals(current))));
        return infos;
    }

    public RecommendationModelInfo getActiveModelInfo() {
        String current = activeModelKey.get();
        RecsysProperties.Model model = models.get(current);
        if (model == null) {
            return null;
        }
        return buildModelInfo(current, model, true);
    }

    private RecommendationModelInfo buildModelInfo(String key, RecsysProperties.Model model, boolean active) {
        RecommendationModelInfo info = new RecommendationModelInfo();
        info.setKey(key);
        info.setLabel(model.getLabel());
        info.setBaseUrl(model.getBaseUrl());
        info.setSupportsOnlineLearning(model.isSupportsOnlineLearning());
        info.setActive(active);
        return info;
    }
}
