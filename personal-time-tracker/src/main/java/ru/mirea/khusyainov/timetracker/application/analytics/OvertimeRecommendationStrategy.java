package ru.mirea.khusyainov.timetracker.application.analytics;

import java.util.Optional;

public final class OvertimeRecommendationStrategy implements RecommendationStrategy {
    private final double threshold;

    public OvertimeRecommendationStrategy(double threshold) {
        if (threshold < 0) throw new IllegalArgumentException("Порог не может быть отрицательным");
        this.threshold = threshold;
    }

    @Override
    public Optional<Recommendation> evaluate(AnalyticsInput input) {
        if (input.plannedSeconds() == 0 || input.actualSeconds() <= input.plannedSeconds() * (1 + threshold)) {
            return Optional.empty();
        }
        long difference = input.actualSeconds() - input.plannedSeconds();
        return Optional.of(new Recommendation(
                "Плановое время регулярно занижено",
                "Фактическое время по задаче «" + input.taskTitle() + "» превышает план на " + difference + " сек.",
                "Сравните несколько завершённых сеансов и скорректируйте плановую длительность."));
    }
}
