import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by florian on 20/12/16.
 */
public class TestCSA {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private final Path file = Paths.get("test-file-name.txt");

    private final String timeStamp = "1 3 2000 10000\n" +
            "1 2 3600 7200\n" +
            "1 2 3900 7200\n" +
            "2 3 4000 5000\n" +
            "1 5 4500 7000\n" +
            "1 3 5000 6000\n" +
            "3 4 6500 7000\n" +
            "3 4 6700 7000\n" +
            "3 2 7000 8000\n" +
            "3 2 7000 9000\n" +
            "2 3 8000 9000\n";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    @After
    public void cleanUpFile() throws IOException {
        Files.delete(file);
    }

    @Test
    public void testSimpleRoute() throws IOException {

        String query = "3 2 6000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        assertEquals(1, res.length);
        assertEquals("3 2 7000 8000", res[0]);
    }

    @Test
    public void testSimpleRouteArriveSameTimeButLeaveLatter() throws IOException {

        String query = "1 2 3000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        assertEquals(1, res.length);
        assertEquals("1 2 3900 7200", res[0]);
    }

    @Test
    public void testMultipleArriveAtSameTimeButLeaveLatter() throws IOException {

        String query = "1 4 3000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        assertEquals(2, res.length);
        assertEquals("1 3 5000 6000", res[0]);
        assertEquals("3 4 6700 7000", res[1]);

    }


}
