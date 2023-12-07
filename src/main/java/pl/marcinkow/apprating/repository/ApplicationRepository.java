package pl.marcinkow.apprating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.marcinkow.apprating.entity.Application;

import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

}
