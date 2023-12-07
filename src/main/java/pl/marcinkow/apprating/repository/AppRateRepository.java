package pl.marcinkow.apprating.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.marcinkow.apprating.entity.AppRate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppRateRepository extends JpaRepository<AppRate, Long> {

    List<AppRate> findByApplicationIdAndRatingDateBetween(UUID appId, LocalDate from, LocalDate until);

    @Query(value = "SELECT ar FROM AppRate ar LEFT JOIN FETCH ar.application WHERE (:minAge IS NULL OR ar.reviewerAge >= :minAge) " +
                "AND (:maxAge IS NULL OR ar.reviewerAge <= :maxAge)  AND ar.ratingDate BETWEEN :from AND :until")
    List<AppRate> findByReviewerAgeBetweenAndRateDateBetween(@Param("minAge") Integer minAge,
                                                                    @Param("maxAge") Integer maxAge,
                                                                    @Param("from") LocalDate from,
                                                                    @Param("until") LocalDate until);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value ="application")
    List<AppRate> findByRatingDateBetween(LocalDate from, LocalDate until);
}
