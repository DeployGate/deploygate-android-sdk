package com.deploygate.sdk.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FakeLogcat extends Process {
    private final int suspendAtLine;
    private final List<String> generatedLines;
    private final InputStream in;
    private boolean isDestroyed;

    public FakeLogcat(
            int lines
    ) {
        this(lines, -1);
    }

    public FakeLogcat(
            final int lines,
            final int suspendAtLine
    ) {
        if (lines < suspendAtLine) {
            throw new IllegalArgumentException(String.format(Locale.US, "suspendAtLine (%d) must be less than lines (%d)", suspendAtLine, lines));
        }

        this.suspendAtLine = suspendAtLine;
        this.generatedLines = new ArrayList<>();
        this.in = new InputStream() {
            private int lineLength = UUID.randomUUID().toString().length();
            private int available = lineLength * lines + lines - 1;

            private String currentLine;
            private int index = 0;

            @Override
            public int read() throws IOException {
                if (isDestroyed) {
                    throw new IOException("already destroyed");
                }

                if (currentLine == null) {
                    index = 0;
                    currentLine = UUID.randomUUID().toString();
                    generatedLines.add(currentLine + '\n');
                }

                if (suspendAtLine > 0 && generatedLines.size() >= suspendAtLine) {
                    return 0;
                }

                final int read;

                if (currentLine.length() <= index) {
                    if (lines == generatedLines.size()) {
                        return -1;
                    }

                    index = 0;
                    currentLine = UUID.randomUUID().toString();
                    generatedLines.add(currentLine + '\n');
                    read = '\n';
                } else {
                    read = currentLine.charAt(index++);
                }

                if (available > 0) {
                    available -= read;
                }

                return read;
            }

            @Override
            public int available() throws IOException {
                if (isDestroyed) {
                    throw new IOException("already destroyed");
                }

                return Math.max(available, 0);
            }
        };
    }

    public List<String> getGeneratedLines() {
        return Collections.unmodifiableList(generatedLines);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new RuntimeException("output stream is not supported");
    }

    @Override
    public InputStream getInputStream() {
        if (isDestroyed) {
            throw new IllegalStateException("process is already destroyed");
        }

        return in;
    }

    @Override
    public InputStream getErrorStream() {
        throw new RuntimeException("output stream is not supported");
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        if (!isDestroyed) {
            try {
                if (in.available() == 0) {
                    return 0;
                }
            } catch (IOException ignore) {
            }

            throw new IllegalThreadStateException("not yet destroyed");
        }

        return 0;
    }

    @Override
    public void destroy() {
        if (isDestroyed) {
            return;
        }

        isDestroyed = true;

        try {
            in.close();
        } catch (IOException ignore) {
        }
    }
}
