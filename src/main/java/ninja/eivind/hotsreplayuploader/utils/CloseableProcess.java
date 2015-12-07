package ninja.eivind.hotsreplayuploader.utils;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public class CloseableProcess extends Process implements AutoCloseable {

    public static CloseableProcess of(final Process process) {
        return new CloseableProcess(process);
    }

    private final Process base;

    private CloseableProcess(Process base) {
        this.base = base;
    }

    @Override
    public void close() {
        base.destroy();
    }

    @Override
    public OutputStream getOutputStream() {
        return base.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return base.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return base.getErrorStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return base.waitFor();
    }

    @Override
    public int exitValue() {
        return base.exitValue();
    }

    @Override
    public void destroy() {
        close();
    }
}
