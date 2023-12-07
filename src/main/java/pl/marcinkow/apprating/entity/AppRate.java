package pl.marcinkow.apprating.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "APP_RATES")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "application", attributeNodes = @NamedAttributeNode(value = "application"))
public class AppRate {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_key")
    @SequenceGenerator(name = "seq_key", sequenceName = "APP_RATES_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Application application;

    private double rate;

    private int reviewerAge;

    private String reviewerCountry;

    private LocalDate ratingDate;
}
