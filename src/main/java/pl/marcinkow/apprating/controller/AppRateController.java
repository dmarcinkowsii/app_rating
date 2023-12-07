package pl.marcinkow.apprating.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.marcinkow.apprating.dto.AppRateAverageRspDto;
import pl.marcinkow.apprating.dto.AppRateTopRspDto;
import pl.marcinkow.apprating.enums.AgeGroup;
import pl.marcinkow.apprating.service.AppRateReportService;
import pl.marcinkow.apprating.service.AppRateService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController()
public class AppRateController {

    private static final String REQ_PARAM_DATE_PATTERN = "yyyyMMdd";

    private final AppRateService appRateService;
    private final AppRateReportService appRateReportService;

    public AppRateController(AppRateService appRateService, AppRateReportService appRateReportService) {
        this.appRateService = appRateService;
        this.appRateReportService = appRateReportService;
    }

    @GetMapping("/{applicationId}/avg")
    public AppRateAverageRspDto getAverage(
            @PathVariable UUID applicationId,
            @RequestParam("since") @DateTimeFormat(pattern = REQ_PARAM_DATE_PATTERN) LocalDate since,
            @RequestParam("until") @DateTimeFormat(pattern = REQ_PARAM_DATE_PATTERN) LocalDate until) {
        return appRateService.getAverage(applicationId, since, until);
    }

    @GetMapping("/top-apps/{ageGroup}")
    public List<AppRateTopRspDto> getTopApps(
            @PathVariable AgeGroup ageGroup,
            @RequestParam("since") @DateTimeFormat(pattern = REQ_PARAM_DATE_PATTERN) LocalDate since,
            @RequestParam("until") @DateTimeFormat(pattern = REQ_PARAM_DATE_PATTERN) LocalDate until) {
        return appRateService.getTopApps(ageGroup, since, until);
    }
}
