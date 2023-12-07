package pl.marcinkow.apprating.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AppRateTopRspDto {
    private String name;
    private UUID appId;
}
