package com.yogpc.qp.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class Starter implements IDataProvider {
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
            .map(MapStreamSyntax.toEntry(Function.identity(), System.getProperties()::getProperty))
            .map(MapStreamSyntax.toAny((k, v) -> k + "=" + v))
            .forEach(LOGGER::info);
        LOGGER.info("---------- Env ----------");
        System.getenv().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(MapStreamSyntax.toAny((s, s2) -> s + "=" + s2))
            .forEach(LOGGER::info);
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                selectPackage("com.kotori316.test_qp"),
                selectPackage(Starter.class.getPackage().getName())
            )
            .build();

        Launcher launcher = LauncherFactory.create();

        // Register a listener of your choice
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        // Do something with the TestExecutionSummary.
        StringWriter stream = new StringWriter();
        summary.printTo(new PrintWriter(stream));
        LOGGER.info(stream.toString());
        List<Throwable> errors = summary.getFailures().stream()
            .map(TestExecutionSummary.Failure::getException).collect(Collectors.toList());
        errors.forEach(t -> LOGGER.fatal(MARKER, "Test failed.", t));
        if (Boolean.parseBoolean(System.getenv("GITHUB_ACTIONS")) && !errors.isEmpty()) {
            try (BufferedWriter w = Files.newBufferedWriter(Paths.get("..", "error-trace.txt"));
                 PrintWriter writer = new PrintWriter(w)) {
                errors.forEach(t -> t.printStackTrace(writer));
            } catch (IOException e) {
                LOGGER.error("File IO", e);
            }
        }
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
