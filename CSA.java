import java.io.*;
import java.lang.reflect.Array;
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
    public static final int MAX_STATIONS  = 100000;

    Timetable timetable;
    Connection in_connection[];
    int earliest_arrival[];

    int arrival_station;
    List<List<Connection>> possibleRoute = new ArrayList<>();

    CSA(BufferedReader in) {
        timetable = new Timetable(in);
    }

    void main_loop(int arrival_station) {
        int earliest = Integer.MAX_VALUE;
        for (Connection connection: timetable.connections) {
            if (connection.departure_timestamp >= earliest_arrival[connection.departure_station] &&
                    connection.arrival_timestamp <= earliest_arrival[connection.arrival_station]) {
                earliest_arrival[connection.arrival_station] = connection.arrival_timestamp;
                in_connection[connection.arrival_station] = connection;

                if(connection.arrival_station == arrival_station) {
                    earliest = Math.min(earliest, connection.arrival_timestamp);
                }
            } else if(connection.arrival_timestamp > earliest) {
                return;
            }
        }
    }

    void print_result(List<Connection> route_solution) {
        if(route_solution == null) {
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
     * build the list of connections for the trip
     * @param arrival_station
     * @return
     */
    public List<Connection> buildTripSolution(int arrival_station){
        //if there is no solution then return null
        if(in_connection[arrival_station] == null) {
            return null;
        } else {
            List<Connection> route = new ArrayList<Connection>();
            // We have to rebuild the route from the arrival station
            Connection last_connection = in_connection[arrival_station];
            while (last_connection != null) {
                route.add(last_connection);
                last_connection = in_connection[last_connection.departure_station];
            }
            return route;
        }
    }

    void compute(int departure_station, int arrival_station, int departure_time) {
        in_connection = new Connection[MAX_STATIONS];
        earliest_arrival = new int[MAX_STATIONS];
        for(int i = 0; i < MAX_STATIONS; ++i) {
            in_connection[i] = null;
            earliest_arrival[i] = Integer.MAX_VALUE;
        }
        earliest_arrival[departure_station] = departure_time;

        if (departure_station <= MAX_STATIONS && arrival_station <= MAX_STATIONS) {
            main_loop(arrival_station);
        }
        print_result(buildTripSolution(arrival_station));
    }

    void compute_least_connection_route(int departure_station, int arrival_station, int departure_time){
        List<Connection> final_list = new ArrayList<>();
        this.arrival_station = arrival_station;

        permute(timetable.connections, final_list, departure_station, departure_time);
        if(!possibleRoute.isEmpty()){
            for(List<Connection> route : possibleRoute){
                print_result(route);
                return;
            }

        } else {
            print_result(null);
        }
    }

    void permute(List<Connection> to_permute, List<Connection> final_list, int station, int timestamp){
        if(final_list.size() != 0) {
            if(final_list.get(final_list.size()-1).arrival_station == this.arrival_station){
                possibleRoute.add(final_list);
                return;
            }

        }

        for(int i = 0; i < to_permute.size(); i++){
            if(to_permute.get(i).departure_timestamp < timestamp){
                to_permute.remove(i);
            }
        }
        for(int i = 0; i < to_permute.size(); i++){
            if(to_permute.get(i).departure_station != station  || to_permute.get(i).departure_timestamp < timestamp ){
                continue;
            }

            List<Connection> final_cpy = new ArrayList<>(final_list);
            List<Connection> to_perm_cpy = new ArrayList<>(to_permute);
            final_cpy.add(to_perm_cpy.remove(i));

            permute(to_perm_cpy, final_cpy, final_cpy.get(final_cpy.size()-1).arrival_station, final_cpy.get(final_cpy.size()-1).arrival_timestamp);
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
        Connection[] connections = new Connection[csa.timetable.connections.size()];
        connections = csa.timetable.connections.toArray(connections);

        String line;

        try {
            line = in.readLine();
            while (!line.isEmpty()) {
                String[] tokens = line.split(" ");
                csa.compute_least_connection_route(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));

                line = in.readLine();
            }

        } catch (IOException e) {
            System.out.println("Something went wrong while reading the parameters: " + e.getMessage());
        }



    }
}
