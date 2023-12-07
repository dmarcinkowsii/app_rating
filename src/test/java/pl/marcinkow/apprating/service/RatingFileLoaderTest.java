package pl.marcinkow.apprating.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.marcinkow.apprating.entity.AppRate;
import pl.marcinkow.apprating.entity.Application;
import pl.marcinkow.apprating.repository.AppRateRepository;
import pl.marcinkow.apprating.repository.ApplicationRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingFileLoaderTest {

    private static final String APP_RATING_FILE_PATH = "src/test/resources/app_rating_test.csv";
    private static final String EMPTY_APP_RATING_FILE_PATH = "src/test/resources/empty_app_rating_test.csv";

    @Mock
    private AppRateRepository appRateRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AppRateReportService appRateReportService;

    @InjectMocks
    private RatingFileLoader ratingFileLoader;

    @Test
    public void loadFile_shouldnNotLoadWhenFileDoesNotExist() {
        ratingFileLoader.loadFile();

        verify(applicationRepository, never()).save(any());
        verify(appRateRepository, never()).saveAll(any());
    }

    @Test
    public void processFile_shouldNotProcessEmptyFile() {
        ratingFileLoader.processFile(EMPTY_APP_RATING_FILE_PATH);

        verify(applicationRepository, never()).save(any());
        verify(appRateRepository).saveAll(Collections.emptyList());
    }
    @Test
    public void processFile_shouldGenerateReportWhenLastDayOfMonth() {
        LocalDate endOfMonthDay = LocalDate.of(2023, 1, 31);
        try (MockedStatic<LocalDate> topDateTimeUtilMock = Mockito.mockStatic(LocalDate.class)) {
            topDateTimeUtilMock.when(LocalDate::now).thenReturn(endOfMonthDay);

            ratingFileLoader.loadFile();
        }

        verify(appRateReportService, times(1)).createMonthlyReports();
    }


    @Test
    public void processFile_shouldProcessFileWithNewApps() {
        when(applicationRepository.findById(any()))
            .thenReturn(Optional.empty());
        final ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
        final ArgumentCaptor<List<AppRate>> appRateCaptor = ArgumentCaptor.forClass((Class) List.class);
        when(applicationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ratingFileLoader.processFile(APP_RATING_FILE_PATH);

        verify(applicationRepository, times(2)).save(applicationCaptor.capture());
        verify(appRateRepository, times(1)).saveAll(appRateCaptor.capture());
        List<Application> capturedApps = applicationCaptor.getAllValues();
        List<AppRate> capturedAppRates = appRateCaptor.getValue();
        assertThat(capturedApps.get(0)).usingRecursiveAssertion()
            .isEqualTo(Application.builder()
                .name("SciCalc")
                .id(UUID.fromString("9ed0a148-731b-46c9-a01d-cdd29a931cd1"))
                .build());
        assertThat(capturedApps.get(1)).usingRecursiveAssertion()
            .isEqualTo(Application.builder()
                .name("Mobile Notepad")
                .id(UUID.fromString("6ab3aa00-3c6a-11ee-be56-0242ac120002"))
                .build());
        assertThat(capturedAppRates).usingRecursiveAssertion()
            .isEqualTo(List.of(
                AppRate.builder()
                .rate(4.5)
                .ratingDate(LocalDate.now())
                .reviewerAge(47)
                .reviewerCountry("US")
                .application(Application.builder()
                    .name("SciCalc")
                    .id(UUID.fromString("9ed0a148-731b-46c9-a01d-cdd29a931cd1"))
                    .build())
                .build(),
                AppRate.builder()
                    .rate(5)
                    .ratingDate(LocalDate.now())
                    .reviewerAge(34)
                    .reviewerCountry("CN")
                    .application(Application.builder()
                        .name("Mobile Notepad")
                        .id(UUID.fromString("6ab3aa00-3c6a-11ee-be56-0242ac120002"))
                        .build())
                    .build()));
    }
}
