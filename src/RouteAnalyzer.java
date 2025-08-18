// RouteAnalyzer.java
import java.util.*;

public class RouteAnalyzer {

    // Dynamic Programming approach for optimal route caching
    public static class DynamicProgrammingRouter {
        private Map<String, Map<String, Route>> memo = new HashMap<>();

        public Route findOptimalRoute(String from, String to,
                                      Map<String, Location> locations,
                                      Map<String, Map<String, Double>> distanceMatrix,
                                      Map<String, Map<String, Double>> timeMatrix) {

            String key = from + "->" + to;
            if (memo.containsKey(from) && memo.get(from).containsKey(to)) {
                return memo.get(from).get(to);
            }

            // Base case: direct route
            if (distanceMatrix.get(from).containsKey(to)) {
                List<String> directPath = Arrays.asList(from, to);
                double distance = distanceMatrix.get(from).get(to);
                double time = timeMatrix.get(from).get(to);
                Route directRoute = new Route(directPath, distance, time);

                // Memoize result
                memo.computeIfAbsent(from, k -> new HashMap<>()).put(to, directRoute);
                return directRoute;
            }

            // Recursive case: find optimal intermediate point
            Route bestRoute = null;
            double minCost = Double.MAX_VALUE;

            for (String intermediate : locations.keySet()) {
                if (!intermediate.equals(from) && !intermediate.equals(to)) {
                    Route route1 = findOptimalRoute(from, intermediate, locations, distanceMatrix, timeMatrix);
                    Route route2 = findOptimalRoute(intermediate, to, locations, distanceMatrix, timeMatrix);

                    if (route1 != null && route2 != null) {
                        double totalCost = route1.totalDistance + route2.totalDistance;
                        if (totalCost < minCost) {
                            minCost = totalCost;

                            // Combine paths
                            List<String> combinedPath = new ArrayList<>(route1.path);
                            combinedPath.addAll(route2.path.subList(1, route2.path.size()));

                            bestRoute = new Route(combinedPath,
                                    route1.totalDistance + route2.totalDistance,
                                    route1.totalTime + route2.totalTime);
                            bestRoute.algorithm = "Dynamic Programming";
                        }
                    }
                }
            }

            // Memoize and return result
            if (bestRoute != null) {
                memo.computeIfAbsent(from, k -> new HashMap<>()).put(to, bestRoute);
            }
            return bestRoute;
        }
    }

    // Traffic Analysis System
    public static class TrafficAnalyzer {
        private Map<String, Double> trafficLevels = new HashMap<>();

        public TrafficAnalyzer() {
            initializeTrafficData();
        }

        private void initializeTrafficData() {
            // Simulate traffic levels based on location popularity
            trafficLevels.put("Central Cafeteria", 0.8);
            trafficLevels.put("John Evans Atta Mills Library", 0.7);
            trafficLevels.put("Night Market", 0.9);
            trafficLevels.put("Central Administration", 0.6);
            trafficLevels.put("Main Gate", 0.8);
            trafficLevels.put("University Hospital", 0.5);
            trafficLevels.put("Sports Complex", 0.4);

            // Default traffic level for other locations
            for (String location : trafficLevels.keySet()) {
                trafficLevels.putIfAbsent(location, 0.3);
            }
        }

        public double getTrafficFactor(String location) {
            return trafficLevels.getOrDefault(location, 0.3);
        }

        public double getTrafficAdjustedTime(String from, String to, double baseTime) {
            double fromTraffic = getTrafficFactor(from);
            double toTraffic = getTrafficFactor(to);
            double avgTraffic = (fromTraffic + toTraffic) / 2.0;
            return baseTime * (1.0 + avgTraffic);
        }
    }

    // Performance metrics for different algorithms
    public static class AlgorithmPerformanceAnalyzer {

        public static class PerformanceMetrics {
            String algorithmName;
            long executionTime;
            int pathLength;
            double totalDistance;
            double totalTime;
            int nodesExplored;

            public PerformanceMetrics(String algorithmName) {
                this.algorithmName = algorithmName;
            }

            @Override
            public String toString() {
                return String.format("%s: Execution=%.2fms, Path=%d nodes, Distance=%.2fm, Time=%.2fmin, Explored=%d",
                        algorithmName, executionTime/1000000.0, pathLength, totalDistance, totalTime, nodesExplored);
            }
        }

        public static PerformanceMetrics analyzeAlgorithmPerformance(String algorithmName,
                                                                     Runnable algorithmExecution,
                                                                     Route result) {
            PerformanceMetrics metrics = new PerformanceMetrics(algorithmName);

            long startTime = System.nanoTime();
            algorithmExecution.run();
            long endTime = System.nanoTime();

            metrics.executionTime = endTime - startTime;

            if (result != null) {
                metrics.pathLength = result.path.size();
                metrics.totalDistance = result.totalDistance;
                metrics.totalTime = result.totalTime;
                metrics.nodesExplored = result.path.size(); // Simplified metric
            }

            return metrics;
        }
    }

    // Binary Search for finding routes by criteria
    public static class RouteSearcher {

        public static List<Route> searchRoutesByDistance(List<Route> sortedRoutes,
                                                         double minDistance, double maxDistance) {
            List<Route> results = new ArrayList<>();

            int start = binarySearchLowerBound(sortedRoutes, minDistance);
            int end = binarySearchUpperBound(sortedRoutes, maxDistance);

            for (int i = start; i <= end && i < sortedRoutes.size(); i++) {
                results.add(sortedRoutes.get(i));
            }

            return results;
        }

        private static int binarySearchLowerBound(List<Route> routes, double targetDistance) {
            int left = 0, right = routes.size() - 1;
            int result = routes.size();

            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (routes.get(mid).totalDistance >= targetDistance) {
                    result = mid;
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }

            return result;
        }

        private static int binarySearchUpperBound(List<Route> routes, double targetDistance) {
            int left = 0, right = routes.size() - 1;
            int result = -1;

            while (left <= right) {
                int mid = left + (right - left) / 2;
                if (routes.get(mid).totalDistance <= targetDistance) {
                    result = mid;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }

            return result;
        }

        // Linear search for landmark-based routes
        public static List<Route> searchRoutesByLandmark(List<Route> routes, String landmarkType) {
            List<Route> results = new ArrayList<>();

            for (Route route : routes) {
                for (String location : route.path) {
                    if (location.toLowerCase().contains(landmarkType.toLowerCase())) {
                        results.add(route);
                        break;
                    }
                }
            }

            return results;
        }
    }

    // Route optimization using multiple criteria
    public static class MultiCriteriaOptimizer {

        public static class OptimizationCriteria {
            double distanceWeight = 0.5;
            double timeWeight = 0.3;
            double landmarkWeight = 0.2;
            boolean avoidTraffic = true;

            public OptimizationCriteria(double distanceWeight, double timeWeight, double landmarkWeight) {
                this.distanceWeight = distanceWeight;
                this.timeWeight = timeWeight;
                this.landmarkWeight = landmarkWeight;
            }
        }

        public static double calculateRouteScore(Route route, OptimizationCriteria criteria) {
            // Normalize scores (assuming max distance is 2000m and max time is 30 minutes)
            double normalizedDistance = route.totalDistance / 2000.0;
            double normalizedTime = route.totalTime / 30.0;
            double normalizedLandmarks = Math.min(route.landmarks.size() / 3.0, 1.0);

            // Calculate weighted score (lower is better for distance/time, higher is better for landmarks)
            return (criteria.distanceWeight * normalizedDistance) +
                    (criteria.timeWeight * normalizedTime) -
                    (criteria.landmarkWeight * normalizedLandmarks);
        }

        public static List<Route> optimizeRoutes(List<Route> routes, OptimizationCriteria criteria) {
            routes.sort((r1, r2) -> Double.compare(
                    calculateRouteScore(r1, criteria),
                    calculateRouteScore(r2, criteria)
            ));
            return routes;
        }
    }

    // Route alternatives generator
    public static class AlternativeRouteGenerator {

        public static List<Route> generateAlternatives(String from, String to,
                                                       Map<String, Location> locations,
                                                       Map<String, Map<String, Double>> distanceMatrix,
                                                       Map<String, Map<String, Double>> timeMatrix) {
            List<Route> alternatives = new ArrayList<>();

            // Method 1: Routes through academic buildings
            alternatives.addAll(generateRoutesThrough(from, to, LocationType.ACADEMIC,
                    locations, distanceMatrix, timeMatrix));

            // Method 2: Routes through dining facilities
            alternatives.addAll(generateRoutesThrough(from, to, LocationType.DINING,
                    locations, distanceMatrix, timeMatrix));

            // Method 3: Scenic routes through recreational areas
            alternatives.addAll(generateRoutesThrough(from, to, LocationType.RECREATION,
                    locations, distanceMatrix, timeMatrix));

            return alternatives;
        }

        private static List<Route> generateRoutesThrough(String from, String to, LocationType type,
                                                         Map<String, Location> locations,
                                                         Map<String, Map<String, Double>> distanceMatrix,
                                                         Map<String, Map<String, Double>> timeMatrix) {
            List<Route> routes = new ArrayList<>();

            for (Map.Entry<String, Location> entry : locations.entrySet()) {
                if (entry.getValue().type == type &&
                        !entry.getKey().equals(from) &&
                        !entry.getKey().equals(to)) {

                    String intermediate = entry.getKey();
                    List<String> path = Arrays.asList(from, intermediate, to);

                    double distance = distanceMatrix.get(from).get(intermediate) +
                            distanceMatrix.get(intermediate).get(to);
                    double time = timeMatrix.get(from).get(intermediate) +
                            timeMatrix.get(intermediate).get(to);

                    Route route = new Route(path, distance, time);
                    route.landmarks.add(intermediate);
                    routes.add(route);
                }
            }

            return routes;
        }
    }
}

