package com.englishassistant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Prints JVM info at startup — demonstrates JVM awareness in interviews.
 *
 * Interview talking points:
 * - Heap memory: -Xms (initial) vs -Xmx (max), why set them equal in prod?
 * - GC: Which collector? (JDK 21 default = G1)
 * - Metaspace: Stores class metadata, separate from heap.
 */
@Component
public class JvmInfoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JvmInfoRunner.class);

    @Override
    public void run(String... args) {
        Runtime rt = Runtime.getRuntime();

        long maxHeapMB  = rt.maxMemory()  / 1024 / 1024;
        long totalHeapMB = rt.totalMemory() / 1024 / 1024;
        long freeHeapMB  = rt.freeMemory()  / 1024 / 1024;
        long usedMB = totalHeapMB - freeHeapMB;
        int cores = rt.availableProcessors();

        log.info("╔══════════════════════════════╗");
        log.info("║  Available processors : {}   ║", cores);
        log.info("║  Max heap (Xmx)       : {} MB║", maxHeapMB);
        log.info("║  Used heap            : {} MB║", usedMB);
        log.info("║  GC collector : G1 (JDK21)   ║");
        log.info("╚══════════════════════════════╝");
    }
}
