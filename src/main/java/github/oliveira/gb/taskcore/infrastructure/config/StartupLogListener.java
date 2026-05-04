package github.oliveira.gb.taskcore.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupLogListener {

    @EventListener(ApplicationReadyEvent.class )
    public void startupFinished(){
        log.info("""
        ========================================================
        SYSTEM STARTED SUCCESSFULLY. Awaiting requests.
        ========================================================
        """);    }
}
