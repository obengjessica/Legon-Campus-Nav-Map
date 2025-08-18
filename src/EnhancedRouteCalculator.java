//// EnhancedRouteCalculator.java
//import java.util.*;
//
//public class EnhancedRouteCalculator extends RouteCalculator {
//    private RouteAnalyzer.TrafficAnalyzer trafficAnalyzer;
//    private RouteAnalyzer.DynamicProgrammingRouter dpRouter;
//
//    public EnhancedRouteCalculator(Map<String, Location> locations,
//                                   Map<String, Map<String, Double>> distanceMatrix,
//                                   Map<String, Map<String, Double>> timeMatrix) {
//        super(locations, distanceMatrix, timeMatrix);
//        this.trafficAnalyzer = new RouteAnalyzer.TrafficAnalyzer();
//        this.dpRouter = new RouteAnalyzer.DynamicProgrammingRouter();
//    }
//
//    // Enhanced route finding with multiple algorithms
//    public RouteComparisonResult findAllRoutes(String from, String to) {
//        RouteComparisonResult result = new RouteComparisonResult();
//
//        // Test all algorithms and compare performance
//        result.dijkstraRoutes = findRoutesDijkstra(from, to);
//        result.floydWarshallRoutes = findRoutesFloydWarshall(from, to);
//        result.astarRoutes = findRoutesAStar(from, to);
//        result.vogelRoutes = findRoutesVogel(from, to);
//        result.northwestRoutes = findRoutesNorthwestCorner(from, to);
//
//        // Dynamic Programming route
//        Route dpRoute = dpRouter.findOptimalRoute(from, to, locations, distanceMatrix, timeMatrix);
//        if (dpRoute != null) {
//            result.dpRoutes = Arrays.asList(dpRoute);
//        }
//
//        // Greedy route
//        Route greedyRoute = RouteAnalyzer.PathfindingAlgorithms.GreedyRouter
//                .findGreedyRoute(from, to, locations, distanceMatrix);
//        if (greedyRoute != null) {
//            result.greedyRoutes = Arrays.asList(greedyRoute);
//        }
//
//        // Critical Path Method route
//        List<String> criticalPath = RouteAnalyzer.PathfindingAlgorithms.CriticalPathMethod
//                .findCriticalPath(locations, timeMatrix, from, to);
//        if (!criticalPath.isEmpty()) {
//            double distance = calculatePathDistance(criticalPath);
//            double time = calculatePathTime(criticalPath);
//            Route cpmRoute = new Route(criticalPath, distance, time);
//            cpmRoute.algorithm = "Critical Path Method";
//            result.cpmRoutes = Arrays.asList(cpmRoute);
//        }
//
//        return result;
//    }
//
//    // Advanced landmark-based search with multiple options
//    @Override
//    public List<Route> findRoutesThroughLandmark(String from, String to, String landmarkType) {
//        List<Route> routes = new ArrayList<>();
//
//        // Enhanced landmark search using different strategies
//
//        // Strategy 1: Direct routes through specific landmark types
//        routes.addAll(findDirectLandmarkRoutes(from, to, landmarkType));
//
//        // Strategy 2: Multi-hop routes visiting multiple landmarks
//        routes.addAll(findMultiLandmarkRoutes(from, to, landmarkType));
//
//        // Strategy 3: Alternative routes near landmarks
//        routes.addAll(findNearLandmarkRoutes(from, to, landmarkType));
//
//        // Apply traffic-adjusted timing
//        for (Route route : routes) {
//            route.totalTime = calculateTrafficAdjustedTime(route);
//        }
//
//        // Sort and return top 3 routes
//        routes.sort(Comparator.comparing(r -> r.totalDistance));
//        return routes.subList(0, Math.min(3, routes.size()));
//    }
//
//    private List<Route> findDirectLandmarkRoutes(String from, String to, String landmarkType) {
//        List<Route> routes = new ArrayList<>();
//
//        for (Map.Entry<String, Location> entry : locations.entrySet()) {
//            String locationName = entry.getKey();
//            if (matchesLandmarkType(locationName, entry.getValue().type, landmarkType) &&
//                    !locationName.equals(from) && !locationName.equals(to)) {
//
//                List<String> path = Arrays.asList(from, locationName, to);
//                double distance = distanceMatrix.get(from).get(locationName) +
//                        distanceMatrix.get(locationName).get(to);
//                double time = timeMatrix.get(from).get(locationName) +
//                        timeMatrix.get(locationName).get(to);
//
//                Route route = new Route(path, distance, time);
//                route.landmarks.add(locationName);
//                route.algorithm = "Direct Landmark Route";
//                routes.add(route);
//            }
//        }
//
//        return routes;
//    }
//
//    private List<Route> findMultiLandmarkRoutes(String from, String to, String landmarkType) {
//        List<Route> routes = new ArrayList<>();
//        List<String> matchingLandmarks = new ArrayList<>();
//
//        // Find all matching landmarks
//        for (Map.Entry<String, Location> entry : locations.entrySet()) {
//            if (matchesLandmarkType(entry.getKey(), entry.getValue().type, landmarkType) &&
//                    !entry.getKey().equals(from) && !entry.getKey().equals(to)) {
//                matchingLandmarks.add(entry.getKey());
//            }
//        }
//
//        // Create routes visiting multiple landmarks (limit to 2 landmarks per route)
//        for (int i = 0; i < matchingLandmarks.size() && i < 2; i++) {
//            for (int j = i + 1; j < matchingLandmarks.size() && j < 3; j++) {
//                String landmark1 = matchingLandmarks.get(i);
//                String landmark2 = matchingLandmarks.get(j);
//
//                List<String> path = Arrays.asList(from, landmark1, landmark2, to);
//                double distance = distanceMatrix.get(from).get(landmark1) +
//                        distanceMatrix.get(landmark1).get(landmark2) +
//                        distanceMatrix.get(landmark2).get(to);
//                double time = timeMatrix.get(from).get(landmark1) +
//                        timeMatrix.get(landmark1).get(landmark2) +
//                        timeMatrix.get(landmark2).get(to);
//
//                Route route = new Route(path, distance, time);
//                route.landmarks.addAll(Arrays.asList(landmark1, landmark2));
//                route.algorithm = "Multi-Landmark Route";
//                routes.add(route);
//            }
//        }
//
//        return routes;
//    }
//
//    private List<Route> findNearLandmarkRoutes(String from, String to, String landmarkType) {
//        List<Route> routes = new ArrayList<>();
//
//        // Find locations near landmarks of the specified type
//        for (Map.Entry<String, Location> landmarkEntry : locations.entrySet()) {
//            if (matchesLandmarkType(landmarkEntry.getKey(), landmarkEntry.getValue().type, landmarkType)) {
//                Location landmark = landmarkEntry.getValue();
//
//                // Find locations within proximity of this landmark
//                for (Map.Entry<String, Location> nearbyEntry : locations.entrySet()) {
//                    String nearbyName = nearbyEntry.getKey();
//                    Location nearby = nearbyEntry.getValue();
//
//                    if (!nearbyName.equals(from) && !nearbyName.equals(to) &&
//                            !nearbyName.equals(landmarkEntry.getKey())) {
//
//                        double distanceToLandmark = calculateEuclideanDistance(nearby, landmark);
//
//                        // If within 100 units of the landmark, include in route
//                        if (distanceToLandmark < 100) {
//                            List<String> path = Arrays.asList(from, nearbyName, to);
//                            double distance = distanceMatrix.get(from).get(nearbyName) +
//                                    distanceMatrix.get(nearbyName).get(to);
//                            double time = timeMatrix.get(from).get(nearbyName) +
//                                    timeMatrix.get(nearbyName).get(to);
//
//                            Route route = new Route(path, distance, time);
//                            route.landmarks.add(landmarkEntry.getKey());
//                            route.algorithm = "Near-Landmark Route";
//                            routes.add(route);
//                        }
//                    }
//                }
//            }
//        }
//
//        return routes;
//    }
//
//    private boolean matchesLandmarkType(String locationName, LocationType locationType, String landmarkType) {
//        String lowerLandmarkType = landmarkType.toLowerCase();
//        String lowerLocationName = locationName.toLowerCase();
//
//        // Direct name matching
//        if (lowerLocationName.contains(lowerLandmarkType)) {
//            return true;
//        }
//
//        // Type-based matching
//        switch (lowerLandmarkType) {
//            case "bank":
//            case "banking":
//                return locationType == LocationType.BANKING || lowerLocationName.contains("bank");
//            case "library":
//                return lowerLocationName.contains("library");
//            case "cafeteria":
//            case "food":
//            case "dining":
//                return locationType == LocationType.DINING ||
//                        lowerLocationName.contains("cafeteria") ||
//                        lowerLocationName.contains("market");
//            case "hospital":
//            case "medical":
//                return locationType == LocationType.MEDICAL || lowerLocationName.contains("hospital");
//            case "hall":
//            case "residence":
//                return locationType == LocationType.RESIDENTIAL || lowerLocationName.contains("hall");
//            case "school":
//            case "academic":
//                return locationType == LocationType.ACADEMIC || lowerLocationName.contains("school");
//            case "sports":
//            case "recreation":
//                return locationType == LocationType.RECREATION ||
//                        lowerLocationName.contains("sports") ||
//                        lowerLocationName.contains("field");
//            default:
//                return false;
//        }
//    }
//
//    private double calculateEuclideanDistance(Location loc1, Location loc2) {
//        return Math.sqrt(Math.pow(loc2.x - loc1.x, 2) + Math.pow(loc2.y - loc1.y, 2));
//    }
//
//    private double calculateTrafficAdjustedTime(Route route) {
//        double totalTime = 0;
//
//        for (int i = 0; i < route.path.size() - 1; i++) {
//            String from = route.path.get(i);
//            String to = route.path.get(i + 1);
//            double baseTime = timeMatrix.get(from).get(to);
//            double adjustedTime = trafficAnalyzer.getTrafficAdjustedTime(from, to, baseTime);
//            totalTime += adjustedTime;
//        }
//
//        return totalTime;
//    }
//
//    // Method to demonstrate Critical Path Method application
//    public Route findCriticalPathRoute(String from, String to) {
//        List<String> criticalPath = RouteAnalyzer.PathfindingAlgorithms.CriticalPathMethod
//                .findCriticalPath(locations, timeMatrix, from, to);
//
//        if (!criticalPath.isEmpty()) {
//            double distance = calculatePathDistance(criticalPath);
//            double time = calculatePathTime(criticalPath);
//            Route route = new Route(criticalPath, distance, time);
//            route.algorithm = "Critical Path Method";
//
//            // Add landmarks along the critical path
//            for (String location : criticalPath) {
//                if (!location.equals(from) && !location.equals(to)) {
//                    route.landmarks.add(location);
//                }
//            }
//
//            return route;
//        }
//
//        return null;
//    }
//
//    // Comprehensive route analysis
//    public String generateRouteAnalysisReport(String from, String to) {
//        StringBuilder report = new StringBuilder();
//        report.append("=== COMPREHENSIVE ROUTE ANALYSIS REPORT ===\n\n");
//        report.append(String.format("From: %s\nTo: %s\n\n", from, to));
//
//        RouteComparisonResult allRoutes = findAllRoutes(from, to);
//
//        // Performance comparison
//        report.append("ALGORITHM PERFORMANCE COMPARISON:\n");
//        report.append("-".repeat(50)).append("\n");
//
//        Map<String, List<Route>> algorithmRoutes = new HashMap<>();
//        algorithmRoutes.put("Dijkstra", allRoutes.dijkstraRoutes);
//        algorithmRoutes.put("Floyd-Warshall", allRoutes.floydWarshallRoutes);
//        algorithmRoutes.put("A*", allRoutes.astarRoutes);
//        algorithmRoutes.put("Vogel", allRoutes.vogelRoutes);
//        algorithmRoutes.put("Northwest", allRoutes.northwestRoutes);
//        algorithmRoutes.put("Dynamic Programming", allRoutes.dpRoutes);
//        algorithmRoutes.put("Greedy", allRoutes.greedyRoutes);
//        algorithmRoutes.put("Critical Path", allRoutes.cpmRoutes);
//
//        for (Map.Entry<String, List<Route>> entry : algorithmRoutes.entrySet()) {
//            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
//                Route route = entry.getValue().get(0);
//                report.append(String.format("%-20s: Distance=%.2fm, Time=%.2fmin, Nodes=%d\n",
//                        entry.getKey(), route.totalDistance, route.totalTime, route.path.size()));
//            }
//        }
//
//        report.append("\nRECOMMENDED ROUTES:\n");
//        report.append("-".repeat(50)).append("\n");
//
//        // Combine all routes and find best options
//        List<Route> allRoutesList = new ArrayList<>();
//        for (List<Route> routeList : algorithmRoutes.values()) {
//            if (routeList != null) {
//                allRoutesList.addAll(routeList);
//            }
//        }
//
//        // Apply multi-criteria optimization
//        RouteAnalyzer.MultiCriteriaOptimizer.OptimizationCriteria criteria =
//                new RouteAnalyzer.MultiCriteriaOptimizer.OptimizationCriteria(0.4, 0.4, 0.2);
//
//        allRoutesList = RouteAnalyzer.MultiCriteriaOptimizer.optimizeRoutes(allRoutesList, criteria);
//
//        for (int i = 0; i < Math.min(3, allRoutesList.size()); i++) {
//            Route route = allRoutesList.get(i);
//            report.append(String.format("\nRoute %d (%s):\n", i + 1, route.algorithm));
//            report.append(String.format("  Path: %s\n", String.join(" → ", route.path)));
//            report.append(String.format("  Distance: %.2f meters\n", route.totalDistance));
//            report.append(String.format("  Estimated Time: %.2f minutes\n", route.totalTime));
//            report.append(String.format("  Traffic-Adjusted Time: %.2f minutes\n",
//                    calculateTrafficAdjustedTime(route)));
//
//            if (!route.landmarks.isEmpty()) {
//                report.append(String.format("  Notable Landmarks: %s\n",
//                        String.join(", ", route.landmarks)));
//            }
//
//            double score = RouteAnalyzer.MultiCriteriaOptimizer.calculateRouteScore(route, criteria);
//            report.append(String.format("  Optimization Score: %.3f\n", score));
//        }
//
//        return report.toString();
//    }
//
//    // Campus-specific route recommendations
//    public List<Route> getRecommendedRoutesForActivity(String from, String to, String activity) {
//        List<Route> routes = new ArrayList<>();
//
//        switch (activity.toLowerCase()) {
//            case "study":
//                routes.addAll(findRoutesThroughLandmark(from, to, "library"));
//                routes.addAll(findRoutesThroughLandmark(from, to, "academic"));
//                break;
//            case "dining":
//            case "food":
//                routes.addAll(findRoutesThroughLandmark(from, to, "cafeteria"));
//                routes.addAll(findRoutesThroughLandmark(from, to, "market"));
//                break;
//            case "banking":
//            case "money":
//                routes.addAll(findRoutesThroughLandmark(from, to, "bank"));
//                break;
//            case "exercise":
//            case "sports":
//                routes.addAll(findRoutesThroughLandmark(from, to, "sports"));
//                routes.addAll(findRoutesThroughLandmark(from, to, "recreation"));
//                break;
//            case "medical":
//            case "health":
//                routes.addAll(findRoutesThroughLandmark(from, to, "hospital"));
//                routes.addAll(findRoutesThroughLandmark(from, to, "medical"));
//                break;
//            default:
//                // General best routes
//                routes.addAll(findRoutesDijkstra(from, to));
//                break;
//        }
//
//        return routes;
//    }
//
//    // Time-based route optimization (considering class schedules)
//    public List<Route> findRoutesForTimeWindow(String from, String to, int hour) {
//        List<Route> routes = findRoutesDijkstra(from, to);
//
//        // Adjust routes based on time of day
//        for (Route route : routes) {
//            double timeFactor = getTimeOfDayFactor(hour);
//            route.totalTime *= timeFactor;
//
//            // Add time-specific landmarks
//            if (hour >= 12 && hour <= 14) { // Lunch time
//                route.landmarks.addAll(findNearbyDiningOptions(route.path));
//            } else if (hour >= 8 && hour <= 17) { // Class hours
//                route.landmarks.addAll(findNearbyAcademicBuildings(route.path));
//            }
//        }
//
//        return routes;
//    }
//
//    private double getTimeOfDayFactor(int hour) {
//        // Simulate traffic based on time of day
//        if (hour >= 8 && hour <= 9) return 1.3; // Morning rush
//        if (hour >= 12 && hour <= 13) return 1.4; // Lunch rush
//        if (hour >= 17 && hour <= 18) return 1.2; // Evening rush
//        if (hour >= 22 || hour <= 6) return 0.8; // Night time (less traffic)
//        return 1.0; // Normal traffic
//    }
//
//    private List<String> findNearbyDiningOptions(List<String> path) {
//        List<String> diningOptions = new ArrayList<>();
//        for (String location : path) {
//            Location loc = locations.get(location);
//            if (loc.type == LocationType.DINING) {
//                diningOptions.add(location);
//            }
//        }
//        return diningOptions;
//    }
//
//    private List<String> findNearbyAcademicBuildings(List<String> path) {
//        List<String> academicBuildings = new ArrayList<>();
//        for (String location : path) {
//            Location loc = locations.get(location);
//            if (loc.type == LocationType.ACADEMIC) {
//                academicBuildings.add(location);
//            }
//        }
//        return academicBuildings;
//    }
//
//    // Calculate traffic-adjusted time for a complete route
//    private double calculateTrafficAdjustedTime(Route route) {
//        double totalTime = 0;
//        for (int i = 0; i < route.path.size() - 1; i++) {
//            String from = route.path.get(i);
//            String to = route.path.get(i + 1);
//            double baseTime = timeMatrix.get(from).get(to);
//            totalTime += trafficAnalyzer.getTrafficAdjustedTime(from, to, baseTime);
//        }
//        return totalTime;
//    }
//}
//
//// Result container for algorithm comparison
//class RouteComparisonResult {
//    List<Route> dijkstraRoutes;
//    List<Route> floydWarshallRoutes;
//    List<Route> astarRoutes;
//    List<Route> vogelRoutes;
//    List<Route> northwestRoutes;
//    List<Route> dpRoutes;
//    List<Route> greedyRoutes;
//    List<Route> cpmRoutes;
//
//    public RouteComparisonResult() {
//        dijkstraRoutes = new ArrayList<>();
//        floydWarshallRoutes = new ArrayList<>();
//        astarRoutes = new ArrayList<>();
//        vogelRoutes = new ArrayList<>();
//        northwestRoutes = new ArrayList<>();
//        dpRoutes = new ArrayList<>();
//        greedyRoutes = new ArrayList<>();
//        cpmRoutes = new ArrayList<>();
//    }
//}
//
//// Campus Navigation Statistics
//class CampusNavigationStats {
//    private Map<String, Integer> locationPopularity = new HashMap<>();
//    private Map<String, Double> averageWalkingTimes = new HashMap<>();
//    private Map<String, List<String>> frequentRoutes = new HashMap<>();
//
//    public void recordRouteUsage(Route route) {
//        String routeKey = route.path.get(0) + "->" + route.path.get(route.path.size() - 1);
//        frequentRoutes.computeIfAbsent(routeKey, k -> new ArrayList<>()).add(route.algorithm);
//
//        // Update popularity
//        for (String location : route.path) {
//            locationPopularity.merge(location, 1, Integer::sum);
//        }
//
//        // Update average times
//        String key = route.path.get(0) + "->" + route.path.get(route.path.size() - 1);
//        averageWalkingTimes.put(key, route.totalTime);
//    }
//
//    public String generateUsageReport() {
//        StringBuilder report = new StringBuilder();
//        report.append("=== CAMPUS NAVIGATION USAGE STATISTICS ===\n\n");
//
//        // Most popular locations
//        report.append("MOST POPULAR LOCATIONS:\n");
//        locationPopularity.entrySet().stream()
//                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
//                .limit(10)
//                .forEach(entry -> report.append(String.format("  %s: %d visits\n",
//                        entry.getKey(), entry.getValue())));
//
//        report.append("\nFREQUENT ROUTES:\n");
//        frequentRoutes.entrySet().stream()
//                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
//                .limit(5)
//                .forEach(entry -> report.append(String.format("  %s: %d times\n",
//                        entry.getKey(), entry.getValue().size())));
//
//        return report.toString();
//    }
//}
//
//// Enhanced UI Controller for better user interaction
//class NavigationController {
//    private EnhancedRouteCalculator calculator;
//    private CampusNavigationStats stats;
//
//    public NavigationController(EnhancedRouteCalculator calculator) {
//        this.calculator = calculator;
//        this.stats = new CampusNavigationStats();
//    }
//
//    public String processNavigationRequest(String from, String to, String preferences) {
//        StringBuilder response = new StringBuilder();
//
//        // Parse preferences
//        boolean wantsFastest = preferences.toLowerCase().contains("fast");
//        boolean wantsShortest = preferences.toLowerCase().contains("short");
//        boolean wantsScenic = preferences.toLowerCase().contains("scenic");
//
//        if (wantsFastest) {
//            List<Route> routes = calculator.findRoutesDijkstra(from, to);
//            MergeSort.sortRoutesByTime(routes);
//            response.append("FASTEST ROUTE:\n");
//            if (!routes.isEmpty()) {
//                Route fastest = routes.get(0);
//                stats.recordRouteUsage(fastest);
//                response.append(formatRouteDescription(fastest));
//            }
//        } else if (wantsShortest) {
//            List<Route> routes = calculator.findRoutesDijkstra(from, to);
//            QuickSort.sortRoutesByDistance(routes);
//            response.append("SHORTEST ROUTE:\n");
//            if (!routes.isEmpty()) {
//                Route shortest = routes.get(0);
//                stats.recordRouteUsage(shortest);
//                response.append(formatRouteDescription(shortest));
//            }
//        } else if (wantsScenic) {
//            List<Route> routes = calculator.getRecommendedRoutesForActivity(from, to, "recreation");
//            response.append("SCENIC ROUTE:\n");
//            if (!routes.isEmpty()) {
//                Route scenic = routes.get(0);
//                stats.recordRouteUsage(scenic);
//                response.append(formatRouteDescription(scenic));
//            }
//        } else {
//            // Default: comprehensive analysis
//            response.append(calculator.generateRouteAnalysisReport(from, to));
//        }
//
//        return response.toString();
//    }
//
//    private String formatRouteDescription(Route route) {
//        StringBuilder desc = new StringBuilder();
//        desc.append(String.format("Path: %s\n", String.join(" → ", route.path)));
//        desc.append(String.format("Distance: %.2f meters\n", route.totalDistance));
//        desc.append(String.format("Walking Time: %.2f minutes\n", route.totalTime));
//        desc.append(String.format("Algorithm Used: %s\n", route.algorithm));
//
//        if (!route.landmarks.isEmpty()) {
//            desc.append(String.format("Points of Interest: %s\n",
//                    String.join(", ", route.landmarks)));
//        }
//
//        return desc.toString();
//    }
//
//    public CampusNavigationStats getStats() {
//        return stats;
//    }
//}