package pl.marcinkow.apprating.service;

import org.springframework.stereotype.Service;
import pl.marcinkow.apprating.dto.AppRateAverageRspDto;
import pl.marcinkow.apprating.dto.AppRateTopRspDto;
import pl.marcinkow.apprating.entity.AppRate;
import pl.marcinkow.apprating.enums.AgeGroup;
import pl.marcinkow.apprating.repository.AppRateRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pl.marcinkow.apprating.util.StreamUtils.distinctByKey;

@Service
public class AppRateServiceImpl implements AppRateService {

    private final AppRateRepository appRateRepository;

    private static final int TOP_APPS_LIMIT_NUMBER = 100;

    public AppRateServiceImpl(AppRateRepository appRateRepository) {
        this.appRateRepository = appRateRepository;
    }

    @Override
    public AppRateAverageRspDto getAverage(final UUID applicationId, final LocalDate since, final LocalDate until) {
        List<AppRate> appRates = appRateRepository.findByApplicationIdAndRatingDateBetween(applicationId, since, until);

        return AppRateAverageRspDto
                .builder()
                .average(appRates.stream()
                        .mapToDouble(AppRate::getRate)
                        .average()
                        .orElse(Double.NaN))
                .build();
    }

    @Override
    public List<AppRateTopRspDto> getTopApps(final AgeGroup ageGroup, final LocalDate since, final LocalDate until) {
        List<AppRate> appRateList = appRateRepository.findByReviewerAgeBetweenAndRateDateBetween(ageGroup.getMinAge(), ageGroup.getMaxAge(), since, until);

        Map<UUID, AppRate> appIdToAppRateMap = appRateList
                .stream()
                .filter(distinctByKey(appRate -> appRate.getApplication().getId()))
                .collect(Collectors.toMap(appRate -> appRate.getApplication().getId(), Function.identity()));
        Map<UUID, Double> appIdToAverageRateMap = appRateList
                .stream()
                .collect(Collectors.groupingBy(
                        appRate -> appRate.getApplication().getId(),
                        Collectors.averagingDouble(AppRate::getRate)));

        return appIdToAverageRateMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_APPS_LIMIT_NUMBER)
                .map(stringDoubleEntry -> {
                    UUID appId = stringDoubleEntry.getKey();
                    String appName = appIdToAppRateMap.get(appId).getApplication().getName();

                    return AppRateTopRspDto.builder()
                            .appId(appId)
                            .name(appName)
                            .build();
                })
                .toList();
    }
}
