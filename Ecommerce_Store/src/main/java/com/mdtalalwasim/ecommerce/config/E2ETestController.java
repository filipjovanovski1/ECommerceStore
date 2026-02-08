package com.mdtalalwasim.ecommerce.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("e2e")
@RestController
@RequestMapping("/e2e")
public class E2ETestController {

    private final DataSource dataSource;

    public E2ETestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("data-e2e.sql"));
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to reset e2e database", ex);
        }
    }
}