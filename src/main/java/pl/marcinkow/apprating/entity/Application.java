package pl.marcinkow.apprating.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "APPLICATIONS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    @Id
    @Column(name = "ID")
    private UUID id;

    @Column(name = "NAME")
    @NotNull
    private String name;
}
