import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class BusTrips {
    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = LocalTime.now();
        String dayOfWeek = now.getDayOfWeek().name().toLowerCase();
        
        //for testing
        //LocalTime currentTime = LocalTime.of(14, 30, 45);

        String busStopId = args[0];
        int maxBusCount = Integer.parseInt(args[1]);
        String timeType = args[2].toLowerCase();

        String busStop = getBusStopName(busStopId);

        if (isServiceRunningToday(dayOfWeek)) {
            HashMap<String, String> busTrips = getListOfBuses(busStopId, currentTime);

            Map<String, String> sortedBusTrips = busTrips.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> LocalTime.parse(entry.getValue())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

            
            if (sortedBusTrips.isEmpty()) {
                System.out.println("There are no buses coming to your stop within 2 hours from now.");
            } else {
                int busCount = 0;
                System.out.println("Buses for stop " + busStop + " within 2 hours from now:");
                for (Map.Entry<String, String> entry : sortedBusTrips.entrySet()) {
                    if (busCount == maxBusCount) {
                        break;
                    }
                                        
                    if (timeType.equals("relative")) {
                        Duration duration = Duration.between(currentTime, LocalTime.parse(entry.getValue()));
                        System.out.println(entry.getKey() + ": " + duration.toMinutes() +"min ");
                        
                    } else {
                        System.out.println(entry.getKey() + ": " + LocalTime.parse(entry.getValue()).format(DateTimeFormatter.ofPattern("HH:mm")));
                    } 
                    busCount++;

                    
                }
    
            }            
            
        } else {
            System.out.println("No service today.");
        }      

    }

    private static boolean isServiceRunningToday(String dayOfWeek) {
        try (BufferedReader br = new BufferedReader(new FileReader("./data/calendar.txt"))) {
            

            String line;
            String[] headers = br.readLine().split(",");
            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columnMap.put(headers[i], i);
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                
                int dayIndex = columnMap.get(dayOfWeek);
                if (values[dayIndex].equals("1")) {
                    
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getBusStopName(String busStopId) {
        try (BufferedReader br = new BufferedReader(new FileReader("./data/stops.txt"))) {
            String line;
            String[] headers = br.readLine().split(",");
            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columnMap.put(headers[i], i);
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                
                if (values[0].equals(busStopId)) {
                    return values[columnMap.get("stop_name")]; 
                }
            }
            return null; 
        } catch (IOException e) {
            e.printStackTrace();
            return null; 
        }
    }


    private static HashMap<String, String> getListOfBuses(String busStopId, LocalTime currentTime) {
        HashMap<String, String> busTrips = new HashMap<>();

        LocalTime maxTime = currentTime.plusHours(2);

        try (BufferedReader br = new BufferedReader(new FileReader("./data/stop_times.txt"))) {
            String line;
            String[] headers = br.readLine().split(",");
            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columnMap.put(headers[i], i);
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String[] parts = values[0].split("_");
                String busId = parts[2];

                String stopId = values[columnMap.get("stop_id")];
                String arrivalTime = values[columnMap.get("arrival_time")];
                
                if (stopId.equals(busStopId) && LocalTime.parse(arrivalTime).isAfter(currentTime) && LocalTime.parse(arrivalTime).isBefore(maxTime)) {
                    busTrips.put(busId, arrivalTime);
                    
                } 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return busTrips;
    }
}
