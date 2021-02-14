package com.yogpc.qp.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jp.t2v.lab.syntax.MapStreamSyntax;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public final class Starter implements IDataProvider {
    private static final Starter INSTANCE = new Starter();
    private static final Logger LOGGER = LogManager.getLogger("QuarryPlus/TestExecutor");
    private static final Marker MARKER = MarkerManager.getMarker("QUARRYPLUS_TEST");

    public static Starter getInstance() {
        return INSTANCE;
    }

    public static void startTest() {
        LOGGER.info("Hello test");
        LOGGER.info("---------- System properties ----------");
        System.getProperties().stringPropertyNames().stream()
            .sorted()
            .filter(s -> !s.contains("path"))
            .map(MapStreamSyntax.toEntry(Function.identity(), System.getProperties()::getProperty))
            .map(MapStreamSyntax.toAny((k, v) -> k + "=" + v))
            .forEach(LOGGER::info);
        LOGGER.info("---------- Class Path ----------");
        Arrays.asList("java.class.path", "java.library.path", "sun.boot.class.path").forEach(s -> {
            LOGGER.info(s);
            String t = System.getProperty(s);
            if (t != null)
                Arrays.stream(t.split(System.getProperty("path.separator"))).sorted().distinct().forEach(LOGGER::info);
        });
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                selectPackage("com.kotori316.test_qp"),
                selectPackage(Starter.class.getPackage().getName())
            )
            .build();

        if (isInCI()) {
            try {
                Files.createFile(Paths.get("..", "test_started.txt"));
            } catch (IOException e) {
                LOGGER.error("File IO", e);
            }
        }

        Launcher launcher = LauncherFactory.create();

        // Register a summaryGeneratingListener of your choice
        SummaryGeneratingListener summaryGeneratingListener = new SummaryGeneratingListener();
        LoggingListener loggingListener = LoggingListener.forBiConsumer((t, s) -> LOGGER.info(s.get(), t));
        launcher.registerTestExecutionListeners(summaryGeneratingListener, loggingListener);

        LOGGER.info("---------- Starting Tests ----------");
        TestPlan plan = launcher.discover(request);
        if (!plan.containsTests()) {
            LOGGER.warn("Contains no tests.");
            return;
        }
        for (TestIdentifier root : plan.getRoots()) {
            for (TestIdentifier child : plan.getChildren(root)) {
                LOGGER.info("Test found: {}", child);
            }
        }
        launcher.execute(plan);

        TestExecutionSummary summary = summaryGeneratingListener.getSummary();
        // Do something with the TestExecutionSummary.
        StringWriter stream = new StringWriter();
        summary.printTo(new PrintWriter(stream));
        LOGGER.info(stream.toString());
        List<Throwable> errors = summary.getFailures().stream()
            .map(TestExecutionSummary.Failure::getException).collect(Collectors.toList());
        errors.forEach(t -> LOGGER.fatal(MARKER, "Test failed.", t));
        if (isInCI() && !errors.isEmpty()) {
            try (BufferedWriter w = Files.newBufferedWriter(Paths.get("..", "error-trace.txt"));
                 PrintWriter writer = new PrintWriter(w)) {
                errors.forEach(t -> t.printStackTrace(writer));
            } catch (IOException e) {
                LOGGER.error("File IO", e);
            }
        }
    }

    private static boolean isInCI() {
        return Boolean.parseBoolean(System.getenv("GITHUB_ACTIONS"));
    }

    @Override
    public void act(DirectoryCache cache) {
        startTest();
    }

    @Override
    public String getName() {
        return "QuarryPlus Test";
    }
}
