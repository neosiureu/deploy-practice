package edu.kh.deploy;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
class BoomOnStart {
    @PostConstruct
    void boom() {
        throw new IllegalStateException("test: dies while starting");
    }
}