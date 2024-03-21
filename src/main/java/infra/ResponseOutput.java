package infra;

import java.io.OutputStream;
import java.util.function.Supplier;

public interface ResponseOutput extends Supplier<OutputStream> {
}
