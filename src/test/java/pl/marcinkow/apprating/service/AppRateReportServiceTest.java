package pl.marcinkow.apprating.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.marcinkow.apprating.entity.AppRate;
import pl.marcinkow.apprating.entity.Application;
import pl.marcinkow.apprating.repository.AppRateRepository;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppRateReportServiceTest {

    @Mock
    private AppRateRepository appRateRepository;

    @Mock
    private CsvReportGenerator csvReportGenerator;

    @InjectMocks
    private AppRateReportServiceImpl appRateReportService;

    @Test
    public void createMonthlyReports_shouldGenerateEmptyReportsWhenRatesDoNotExist() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        when(appRateRepository.findByRatingDateBetween(currentMonth.atDay(1), currentMonth.atEndOfMonth()))
            .thenReturn(Collections.emptyList());
        when(appRateRepository.findByRatingDateBetween(previousMonth.atDay(1), previousMonth.atEndOfMonth()))
            .thenReturn(Collections.emptyList());
        final ArgumentCaptor<List<String[]>> rowsCaptor = ArgumentCaptor.forClass((Class) List.class);
        final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
        String reportDate = currentMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

        appRateReportService.createMonthlyReports();

        verify(csvReportGenerator, times(2)).generateReport(fileNameCaptor.capture(), any(), any(), rowsCaptor.capture());
        List<List<String[]>> rowsList = rowsCaptor.getAllValues();
        List<String> fileNames = fileNameCaptor.getAllValues();
        assertThat(rowsList.get(0)).isEmpty();
        assertThat(rowsList.get(1)).isEmpty();
        assertThat(fileNames.get(0)).isEqualTo("trending100apps-" + reportDate + ".csv");
        assertThat(fileNames.get(1)).isEqualTo("apps-with-issues-" + reportDate + ".csv");
    }

    @Test
    public void createMonthlyReports_shouldGenerateEmptyReports() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        when(appRateRepository.findByRatingDateBetween(currentMonth.atDay(1), currentMonth.atEndOfMonth()))
            .thenReturn(List.of(AppRate.builder()
                .application(Application.builder()
                    .id(UUID.fromString("6ab3aa00-3c6a-11ee-be56-0242ac120002"))
                    .name("App1")
                    .build())
                .rate(3.0)
                .build()));
        when(appRateRepository.findByRatingDateBetween(previousMonth.atDay(1), previousMonth.atEndOfMonth()))
            .thenReturn(List.of(AppRate.builder()
                .application(Application.builder()
                    .id(UUID.fromString("6ab3aa00-3c6a-11ee-be56-0242ac120002"))
                    .name("App1")
                    .build())
                .rate(3.2)
                .build()));
        final ArgumentCaptor<List<String[]>> rowsCaptor = ArgumentCaptor.forClass((Class) List.class);

        appRateReportService.createMonthlyReports();

        verify(csvReportGenerator, times(2)).generateReport(any(), any(), any(), rowsCaptor.capture());
        List<List<String[]>> rowsList = rowsCaptor.getAllValues();
        assertThat(rowsList.get(0)).isEmpty();
        assertThat(rowsList.get(1)).isEmpty();
    }

    @Test
    public void createMonthlyReports_shouldGenerateReports() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        when(appRateRepository.findByRatingDateBetween(currentMonth.atDay(1), currentMonth.atEndOfMonth()))
            .thenReturn(List.of(
                AppRate.builder()
                .application(Application.builder()
                    .id(UUID.fromString("6ab3aa00-3c6a-11ee-be56-0242ac120002"))
                    .name("App1")
                    .build())
                .rate(3.0)
                .build(),
                AppRate.builder()
                    .application(Application.builder()
                        .id(UUID.fromString("9ed0a148-731b-46c9-a01d-cdd29a931cd1"))
                        .name("App2")
                        .build())
                    .rate(4.5)
                    .build()));
        when(appRateRepository.findByRatingDateBetween(previousMonth.atDay(1), previousMonth.atEndOfMonth()))
            .thenReturn(List.of(
                AppRate.builder()
                    .application(Application.builder()
                        .id(UUID.fromString("6ab3aa00-3c6a-11ee-be56-0242ac120002"))
                        .name("App1")
                        .build())
                    .rate(4.0)
                    .build(),
                AppRate.builder()
                    .application(Application.builder()
                        .id(UUID.fromString("9ed0a148-731b-46c9-a01d-cdd29a931cd1"))
                        .name("App2")
                        .build())
                    .rate(3.0)
                    .build()));
        final ArgumentCaptor<List<String[]>> rowsCaptor = ArgumentCaptor.forClass((Class) ArrayList.class);

        appRateReportService.createMonthlyReports();

        verify(csvReportGenerator, times(2)).generateReport(any(), any(), any(), rowsCaptor.capture());
        List<List<String[]>> rowsList = rowsCaptor.getAllValues();
        assertThat(rowsList.get(0).size()).isEqualTo(1);
        assertThat(rowsList.get(1).size()).isEqualTo(1);
        assertThat(rowsList.get(0).get(0)).usingRecursiveAssertion()
            .isEqualTo(new String[]{"App2", "9ed0a148-731b-46c9-a01d-cdd29a931cd1", "4.5", "3.0"});
        assertThat(rowsList.get(1).get(0)).usingRecursiveAssertion()
            .isEqualTo(new String[]{"App1", "6ab3aa00-3c6a-11ee-be56-0242ac120002", "3.0", "4.0"});
    }
}
