package pl.marcinkow.apprating.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.marcinkow.apprating.entity.AppRate;
import pl.marcinkow.apprating.repository.AppRateRepository;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pl.marcinkow.apprating.util.StreamUtils.distinctByKey;

@Service
public class AppRateReportServiceImpl implements AppRateReportService {

    private final Logger LOG = LoggerFactory.getLogger(AppRateReportService.class);
    private final AppRateRepository appRateRepository;
    private final CsvReportGenerator csvReportGenerator;

    private static final String[] REPORT_HEADERS = new String[]{"app_name", "app_uuid", "rating_this_month", "rating_previous_month"};
    private final String reportsPath;

    public AppRateReportServiceImpl(AppRateRepository appRateRepository, CsvReportGenerator csvReportGenerator, @Value("${fileLoaderPath}") String fileLoaderPath) {
        this.appRateRepository = appRateRepository;
        this.csvReportGenerator = csvReportGenerator;
        this.reportsPath = fileLoaderPath + "reports\\";
    }

    public void createMonthlyReports() {
        LOG.info("Creating monthly report.");

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        List<AppRate> currentMonthAppRates = appRateRepository.findByRatingDateBetween(currentMonth.atDay(1), currentMonth.atEndOfMonth());
        Map<UUID, Double> currentMonthRatesMap = currentMonthAppRates
                .stream()
                .collect(Collectors.groupingBy(appRate -> appRate.getApplication().getId(), Collectors.averagingDouble(AppRate::getRate)));
        Map<UUID, Double> previousMonthRatesMap = appRateRepository.findByRatingDateBetween(previousMonth.atDay(1), previousMonth.atEndOfMonth())
                .stream()
                .collect(Collectors.groupingBy(appRate -> appRate.getApplication().getId(), Collectors.averagingDouble(AppRate::getRate)));
        Map<UUID, AppRate> appIdToAppRateMap = currentMonthAppRates
                .stream()
                .filter(distinctByKey(appRate -> appRate.getApplication().getId()))
                .collect(Collectors.toMap(appRate -> appRate.getApplication().getId(), Function.identity()));

        Map<UUID, Double> appIdToRateDifferenceMap = new HashMap<>();

        currentMonthRatesMap.forEach((appId, currentMonthRate) -> {
            Double previousMonthRate = previousMonthRatesMap.get(appId);
            if (previousMonthRate == null) {
                return;
            }
            appIdToRateDifferenceMap.put(appId, currentMonthRate - previousMonthRate);
        });

        List<Map.Entry<UUID, Double>> trendingApps = appIdToRateDifferenceMap.entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 0)
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(100)
            .toList();

        List<Map.Entry<UUID, Double>> appsWithIssues = appIdToRateDifferenceMap.entrySet()
            .stream()
            .filter(entry -> entry.getValue() <= -0.3)
            .toList();

        String reportDate = currentMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

        csvReportGenerator.generateReport("trending100apps-" + reportDate + ".csv", REPORT_HEADERS, reportsPath, getReportRows(trendingApps, currentMonthRatesMap, previousMonthRatesMap, appIdToAppRateMap));
        csvReportGenerator.generateReport("apps-with-issues-" + reportDate + ".csv", REPORT_HEADERS, reportsPath, getReportRows(appsWithIssues, currentMonthRatesMap, previousMonthRatesMap, appIdToAppRateMap));

        LOG.info("Creating monthly report has been finished.");
    }

    private List<String[]> getReportRows(List<Map.Entry<UUID, Double>> appsEntryList, Map<UUID, Double> currentMonthRatesMap, Map<UUID, Double> previousMonthRatesMap, Map<UUID, AppRate> appIdToAppRateMap) {
        List<String[]> reportRows = new ArrayList<>();

        appsEntryList.forEach(appNameDifferenceEntry -> {
            UUID appId = appNameDifferenceEntry.getKey();
            String[] row = new String[]{appIdToAppRateMap.get(appId).getApplication().getName(),
                String.valueOf(appId),
                String.valueOf(currentMonthRatesMap.get(appId)),
                String.valueOf(previousMonthRatesMap.get(appId))};
            reportRows.add(row);
        });

        return reportRows;
    }
}
