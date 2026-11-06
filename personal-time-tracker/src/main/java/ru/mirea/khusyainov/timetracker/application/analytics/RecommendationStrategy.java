package ru.mirea.khusyainov.timetracker.application.analytics;

import java.util.Optional;

@FunctionalInterface
public interface RecommendationStrategy {
    Optional<Recommendation> evaluate(AnalyticsInput input);
}
