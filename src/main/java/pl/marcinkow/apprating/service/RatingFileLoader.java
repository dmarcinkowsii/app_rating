package pl.marcinkow.apprating.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.marcinkow.apprating.entity.AppRate;
import pl.marcinkow.apprating.entity.Application;
import pl.marcinkow.apprating.repository.AppRateRepository;
import pl.marcinkow.apprating.repository.ApplicationRepository;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
public class RatingFileLoader {
    private final Logger LOG = LoggerFactory.getLogger(RatingFileLoader.class);

    private final static String CSV_EXTENSION = ".csv";
    private final static String LOADING_FILE_PREFIX = "app_rating-";

    private final String LOADING_FILE_PATH;

    private final AppRateRepository appRateRepository;
    private final ApplicationRepository applicationRepository;
    private final AppRateReportService appRateReportService;

    public RatingFileLoader(AppRateRepository appRateRepository, ApplicationRepository applicationRepository, @Value("${fileLoaderPath}") String filePath, AppRateReportService appRateReportService) {
        this.appRateRepository = appRateRepository;
        this.applicationRepository = applicationRepository;
        this.LOADING_FILE_PATH = filePath;
        this.appRateReportService = appRateReportService;
    }

    @Scheduled(cron = "0 0 20 * * *")
    @PostConstruct
    public void loadFile() {
        LOG.info("Started loading rate file process.");
        String fileName = LOADING_FILE_PREFIX + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + CSV_EXTENSION;
        String fileToLoadPath = LOADING_FILE_PATH + fileName;

        if (fileToLoadExists(fileToLoadPath)) {
            processFile(fileToLoadPath);
            LOG.info("Loading file processed has been finished successfully.");

        } else {
            LOG.error("Couldn't find file " + fileName + ".");
        }

        if (LocalDate.now().isEqual(LocalDate.now().with(lastDayOfMonth()))) {
            appRateReportService.createMonthlyReports();
        }
    }

    protected void processFile(final String fileToLoadPath) {
        List<AppRate> appRateList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(fileToLoadPath))) {
            List<String[]> csvRows = reader.readAll();
            csvRows.stream().skip(1).forEach(fields -> {
                AppRate appRate = AppRate.builder()
                    .application(getOrCreateApp(UUID.fromString(fields[1]), fields[0]))
                    .rate(Double.parseDouble(fields[2]))
                    .reviewerAge(Integer.parseInt(fields[3]))
                    .reviewerCountry(fields[4])
                    .ratingDate(LocalDate.now())
                    .build();

                appRateList.add(appRate);
            });
        } catch (IOException | CsvException e) {
            LOG.error("An exception has occurred during processing file.", e);
            return; // założyłem, że nie ma się zapisywać w ogóle nic z pliku w przypadku błędnego rekordu.
        }

        appRateRepository.saveAll(appRateList);
    }

    private boolean fileToLoadExists(final String fileToLoadPath) {
        return new File(fileToLoadPath).exists();
    }

    private Application getOrCreateApp(final UUID appId, final String appName) {
        return applicationRepository.findById(appId)
            .orElse(applicationRepository.save(Application.builder() // Założyłem, że mogą przychodzić nowe aplikacje
                .id(appId)
                .name(appName)
                .build()));
    }
}
