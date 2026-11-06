package ru.mirea.khusyainov.timetracker.application.analytics;

import java.util.List;

public final class RecommendationService {
    private final List<RecommendationStrategy> strategies;

    public RecommendationService(List<RecommendationStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public List<Recommendation> generate(AnalyticsInput input) {
        return strategies.stream()
                .map(strategy -> strategy.evaluate(input))
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
