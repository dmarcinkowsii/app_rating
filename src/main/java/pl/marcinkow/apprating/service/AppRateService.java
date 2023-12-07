package pl.marcinkow.apprating.service;

import pl.marcinkow.apprating.dto.AppRateAverageRspDto;
import pl.marcinkow.apprating.dto.AppRateTopRspDto;
import pl.marcinkow.apprating.enums.AgeGroup;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppRateService {
    AppRateAverageRspDto getAverage(UUID applicationId, LocalDate since, LocalDate until);

    List<AppRateTopRspDto> getTopApps(AgeGroup ageGroup, LocalDate since, LocalDate until);
}
