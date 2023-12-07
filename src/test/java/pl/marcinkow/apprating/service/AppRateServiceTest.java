package pl.marcinkow.apprating.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.marcinkow.apprating.dto.AppRateAverageRspDto;
import pl.marcinkow.apprating.dto.AppRateTopRspDto;
import pl.marcinkow.apprating.entity.AppRate;
import pl.marcinkow.apprating.entity.Application;
import pl.marcinkow.apprating.enums.AgeGroup;
import pl.marcinkow.apprating.repository.AppRateRepository;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppRateServiceTest {

    @InjectMocks
    private AppRateServiceImpl appRateService;

    @Mock
    private AppRateRepository appRateRepository;

    @Test
    public void getAverage_shouldReturnAverageRate() {
        AppRate appRate1 = AppRate.builder()
                .application(Application.builder()
                    .id(UUID.fromString("1e7895dc-0b37-4bf3-a06f-1753ff5a38a4"))
                    .name("App1")
                    .build())
                .ratingDate(LocalDate.now())
                .rate(4.0)
                .build();
        AppRate appRate2 = AppRate.builder()
                .application(Application.builder()
                        .id(UUID.fromString("1e7895dc-0b37-4bf3-a06f-1753ff5a38a4"))
                        .name("App1")
                        .build())
                .ratingDate(LocalDate.now())
                .rate(5.0)
                .build();
        when(appRateRepository.findByApplicationIdAndRatingDateBetween(any(), any(), any()))
                .thenReturn(List.of(appRate1, appRate2));

        AppRateAverageRspDto average = appRateService.getAverage(UUID.randomUUID(), LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(average.getAverage()).isEqualTo(4.5);
    }

    @Test
    public void getAverage_shouldReturnNanIfAppDoesNotExist() {
        when(appRateRepository.findByApplicationIdAndRatingDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        AppRateAverageRspDto average = appRateService.getAverage(UUID.randomUUID(), LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(Double.isNaN(average.getAverage())).isTrue();
    }

    @Test
    public void getTopApps_shouldReturnEmptyList() {
        when(appRateRepository.findByReviewerAgeBetweenAndRateDateBetween(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<AppRateTopRspDto> topApps = appRateService.getTopApps(AgeGroup.AGE_GROUP_2, LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(topApps).isEmpty();
    }

    @Test
    public void getTopApps_shouldReturn100apps() {
        List<AppRate> appRateList = new ArrayList<>();
                appRateList.addAll(prepareRandomAppRateList(50, "bestApp_", LocalDate.now(), 5.0));
                appRateList.addAll(prepareRandomAppRateList(50, "worstApp_", LocalDate.now(), 1.0));
                appRateList.addAll(prepareRandomAppRateList(50, "mediumApp_",LocalDate.now(), 3.0));
        when(appRateRepository.findByReviewerAgeBetweenAndRateDateBetween(any(), any(), any(), any()))
                .thenReturn(appRateList);

        List<AppRateTopRspDto> topApps = appRateService.getTopApps(AgeGroup.AGE_GROUP_2, LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(topApps).isNotEmpty();
        assertThat(topApps.size()).isEqualTo(100);
        assertThat(topApps.stream().filter(apps -> apps.getName().startsWith("worstApp_")).toList()).isEmpty();
        assertThat(topApps.stream().filter(apps -> apps.getName().startsWith("bestApp_")).toList().size()).isEqualTo(50);
        assertThat(topApps.stream().filter(apps -> apps.getName().startsWith("mediumApp_")).toList().size()).isEqualTo(50);
    }

    private List<AppRate> prepareRandomAppRateList(int appsNumber, String appNamePrefix, LocalDate rateDate, double rate) {
        List<AppRate> list = new ArrayList();
        for (int i=0; i<appsNumber; i++) {
            list.add(AppRate.builder()
                    .application(Application.builder()
                            .id(UUID.randomUUID())
                            .name(appNamePrefix + i)
                            .build())
                    .ratingDate(rateDate)
                    .reviewerAge(30)
                    .reviewerCountry("PL")
                    .rate(rate)
                    .build());
        }
        return list;
    }
}
