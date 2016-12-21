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

    private final Path file = Paths.get("test-file-name.txt");

    private String timeStamp = "1 2 2000 3000\n" +
            "1 2 4000 7200\n" +
            "1 3 4500 6000\n" +
            "1 3 5000 6000\n" +
            "3 4 6000 6200\n" +
            "4 5 6300 7000\n" +
            "3 2 6500 7000\n" +
            "3 2 6700 7000\n" +
            "2 5 8000 9000\n";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
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
    public void testEarliestLeastConnectionsRoute() throws IOException {

        String query = "1 5 3000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        //the earliest should be 1 3 / 3 4 / 4 5 and arrival timestamp 7000
        //but we want here the route with the minimum number of connection
        assertEquals(2, res.length);
        assertEquals("1 2 4000 7200", res[0]);
        assertEquals("2 5 8000 9000", res[1]);
    }


    @Test
    public void testEarliestLeastConnectionsRouteWithTheLaterDeparture() throws IOException {

        timeStamp = "1 2 2000 3000\n" +
                "1 2 4000 7200\n" +
                "2 5 8000 9000\n";

        String query = "1 5 3000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        assertEquals(2, res.length);
        //the earliest departure is 1 2 2000 3000 but we don't want it
        assertEquals("1 2 4000 7200", res[0]);
        assertEquals("2 5 8000 9000", res[1]);

    }

    @Test
    public void testTheMoreComfortableSolutionWithEqualsArrivalAndDepartureTime() throws IOException {

        timeStamp = "1 4 2000 3000\n" +
                "1 2 2000 6000\n" +
                "4 3 5000 6000\n" +
                "2 3 6000 7000\n" +
                "3 5 8000 9000\n";

        String query = "1 5 2000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        assertEquals(3, res.length);
        assertEquals("1 4 2000 3000", res[0]);
        assertEquals("4 3 5000 6000", res[1]);
        assertEquals("3 5 8000 9000", res[2]);

        //a less confortable route but with the same depature and arrival timestamp should be
        //1 2 2000 6000
        //2 3 6000 7000
        //3 5 8000 9000
        //because the trip time IN vehicle is longer

    }

    @Test
    public void testNoSolution() throws IOException {

        String query = "1 5 6000\n";
        String fileContent = timeStamp + "\n" + query + "\r";

        Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
        String[] args = {file.getFileName().toString()};

        CSA.main(args);

        String[] res = outContent.toString().split("\n");
        assertEquals(1, res.length);
        assertEquals("NO_SOLUTION", res[0]);

    }




}
