package de.invesdwin.instrument;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.io.FileUtils;

@Immutable
public final class DynamicInstrumentationProperties {

    /**
     * Process specific temp dir that gets cleaned on exit.
     */
    public static final File TEMP_DIRECTORY;

    static {
        //CHECKSTYLE:OFF
        final String systemTempDir = System.getProperty("java.io.tmpdir");
        //CHECKSTYLE:ON
        TEMP_DIRECTORY = newTempDirectory(new File(systemTempDir));
    }

    private DynamicInstrumentationProperties() {
    }

    public static File newTempDirectory(final File baseDirectory) {
        final File tempDir = findEmptyTempDir(baseDirectory);
        try {
            FileUtils.forceMkdir(tempDir);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FileUtils.deleteQuietly(tempDir);
            }
        });
        return tempDir;
    }

    private static File findEmptyTempDir(final File baseDirectory) {
        File tempDir = new File(baseDirectory, ManagementFactory.getRuntimeMXBean().getName());
        int retry = 0;
        while (tempDir.exists() && !FileUtils.deleteQuietly(tempDir)) {
            //no permission to delete folder (maybe different user had this pid before), choose a different one
            tempDir = new File(tempDir.getAbsolutePath() + "_" + retry);
            retry++;
        }
        return tempDir;
    }

    public static String getProcessId() {
        final String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        final String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
        return pid;
    }
}
