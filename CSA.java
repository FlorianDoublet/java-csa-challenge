import java.io.*;
import java.util.*;

class Connection {
    int departure_station, arrival_station;
    int departure_timestamp, arrival_timestamp;

    // Connection constructor
    Connection(String line) {
        line.trim();
        String[] tokens = line.split(" ");

        departure_station = Integer.parseInt(tokens[0]);
        arrival_station = Integer.parseInt(tokens[1]);
        departure_timestamp = Integer.parseInt(tokens[2]);
        arrival_timestamp = Integer.parseInt(tokens[3]);
    }
};

class Timetable {
    List<Connection> connections;

    // Timetable constructor: reads all the connections from stdin
    Timetable(BufferedReader in) {
        connections = new ArrayList<Connection>();
        String line;
        try {
            line = in.readLine();

            while (!line.isEmpty()) {
                connections.add( new Connection(line) );
                line = in.readLine();
            }
        } catch( Exception e) {
            System.out.println("Something went wrong while reading the data: " + e.getMessage());
        }
    }
};

public class CSA {

    Timetable timetable;

    //for least connections problem
    int arrival_station;
    int minNumberOfConnection = Integer.MAX_VALUE;
    int earlieastValue = Integer.MAX_VALUE;
    List<Connection> routeWithLeastConnections = new ArrayList<>();

    CSA(BufferedReader in) {
        timetable = new Timetable(in);
    }


    void print_result(List<Connection> route_solution) {
        if(route_solution == null || route_solution.isEmpty()) {
            System.out.println("NO_SOLUTION");
        } else {
            //Collections.reverse(route_solution);
            for (Connection connection : route_solution) {
                System.out.println(connection.departure_station + " " + connection.arrival_station + " " +
                        connection.departure_timestamp + " " + connection.arrival_timestamp);
            }
        }
        System.out.println("");
        System.out.flush();
    }


    /**
     * Compute and print the solution with the least connection, and if there is several solution
     * take the one which arrive the earlier and with the later departure
     * @param departure_station the station departure
     * @param arrival_station the station we want to reach
     * @param departure_time the minimum time for the departure
     */
    public void compute_least_connection_route(int departure_station, int arrival_station, int departure_time){
        List<Connection> final_list = new ArrayList<>();
        //init our arrival station
        this.arrival_station = arrival_station;
        //permute to get our solution
        permute(timetable.connections, final_list, departure_station, departure_time);
        //then print it
        print_result(routeWithLeastConnections);
    }

    /**
     * Semi-recursive method to compute all the possible route solution
     * potentially very costly but optimized to only compute potential minimum route solution and not all the possible route
     * @param to_permute
     * @param final_list
     * @param station
     * @param timestamp
     */
    void permute(List<Connection> to_permute, List<Connection> final_list, int station, int timestamp){

        if(final_list.size() != 0 && final_list.get(final_list.size()-1).arrival_station == this.arrival_station){
            //here we know that our final list contain a route which start from the departure station and
            //arrive at the arrival station
            if(final_list.size() < minNumberOfConnection){
                //we find a smallest route so we update all the important parameter as numer of connection and the arrival timestamp
                minNumberOfConnection = final_list.size()-1;
                earlieastValue = final_list.get(final_list.size()-1).arrival_timestamp;
                //save our solution
                routeWithLeastConnections = final_list;

            } else {
                //it means the the route as the same number of connection than the previous solution
                computeBestMinimumRoute(final_list);
            }
            return;
        }

        //to save memory and calculus we remove all the connection with a later departure than our timestamp
        //because it mean that we never could reach them in future connection
        if(minNumberOfConnection < final_list.size()) return;
        for(int i = 0; i < to_permute.size(); i++){
            if(to_permute.get(i).departure_timestamp < timestamp){
                to_permute.remove(i);
                i--;
            }
        }

        //for each connection in our "to permute" list
        for(int i = 0; i < to_permute.size(); i++){
            //if the departure station of the scanned connection isn't the arrival of the previous one we don't have to compute for solutions
            if(to_permute.get(i).departure_station != station ){
                continue;
            }

            //create copy of arrays
            //we have to make copy to don't proceed to operation like deletion for the future compute
            List<Connection> final_cpy = new ArrayList<>(final_list);
            List<Connection> to_perm_cpy = new ArrayList<>(to_permute);
            //add remove a scanned connection into the permutation copy list, to add it into the final copy list
            final_cpy.add(to_perm_cpy.remove(i));
            //permute with the copies and the new information to the latest added connection into the final copy list
            permute(to_perm_cpy, final_cpy, final_cpy.get(final_cpy.size()-1).arrival_station, final_cpy.get(final_cpy.size()-1).arrival_timestamp);
        }

    }

    /**
         * Will test if the concurrent route is better than the existing best one
         * and if it is replace the older route by the concurrent
     * @param concurrentRoute The route that we gonna test to see if it's a better route than the current best solution
     */
    public void computeBestMinimumRoute(List<Connection> concurrentRoute){
        //the first most important parameter is the arrival time
        if(concurrentRoute.get(concurrentRoute.size()-1).arrival_timestamp != earlieastValue){
            if(concurrentRoute.get(concurrentRoute.size()-1).arrival_timestamp < earlieastValue){
                earlieastValue = concurrentRoute.get(concurrentRoute.size()-1).arrival_timestamp;
                routeWithLeastConnections = concurrentRoute;
            } else {
                return;
            }
        } else if (concurrentRoute.get(0).departure_timestamp > routeWithLeastConnections.get(0).departure_timestamp){
            //here the arrival time is the same, but we can still leave later
            routeWithLeastConnections = concurrentRoute;
        }else{
            //if the departure time and the arrival time is the same, we gonna compute the best by the smallest time travel
            //it's the less important priority for the best route
            int valConcurrent = 0;
            int valSolution = 0;
            for(int i = 0; i < concurrentRoute.size(); i++){
                valConcurrent += concurrentRoute.get(i).arrival_timestamp - concurrentRoute.get(i).departure_timestamp;
                valSolution += routeWithLeastConnections.get(i).arrival_timestamp - routeWithLeastConnections.get(i).departure_timestamp;
            }
            if(valConcurrent < valSolution){
                //then update the solution if the concurrent is better
                routeWithLeastConnections = concurrentRoute;
            }
        }
    }


    public static void main(String[] args) {
        BufferedReader in = null;
        if(args.length > 0){
            try {
                in = new BufferedReader(new FileReader(args[0]));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            in = new BufferedReader(new InputStreamReader(System.in));
        }

        CSA csa = new CSA(in);

        String line;

        try {
            line = in.readLine();
            while (!line.isEmpty()) {
                String[] tokens = line.split(" ");
                //compute for the least connection route
                csa.compute_least_connection_route(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                line = in.readLine();
            }

        } catch (IOException e) {
            System.out.println("Something went wrong while reading the parameters: " + e.getMessage());
        }


    }
}
