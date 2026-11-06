package ru.mirea.khusyainov.timetracker.application.analytics;

import java.util.Optional;

public final class FrequentPauseRecommendationStrategy implements RecommendationStrategy {
    private final int pauseThreshold;

    public FrequentPauseRecommendationStrategy(int pauseThreshold) {
        if (pauseThreshold < 1) throw new IllegalArgumentException("Порог пауз должен быть положительным");
        this.pauseThreshold = pauseThreshold;
    }

    @Override
    public Optional<Recommendation> evaluate(AnalyticsInput input) {
        if (input.pauseCount() < pauseThreshold) return Optional.empty();
        return Optional.of(new Recommendation(
                "Много прерываний",
                "Зафиксировано пауз: " + input.pauseCount() + ". Их общая длительность: " + input.pausedSeconds() + " сек.",
                "Проверьте причины пауз и попробуйте заранее выделить время на сообщения и бытовые дела."));
    }
}
