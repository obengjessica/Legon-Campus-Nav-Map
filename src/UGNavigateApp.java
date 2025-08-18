// UGNavigateApp.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class UGNavigateApp extends JFrame {
    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 800;
    private static final int POINT_SIZE = 8;

    // Campus locations with realistic UG coordinates
    private Map<String, Location> locations = new HashMap<>();
    private Map<String, Map<String, Double>> distanceMatrix = new HashMap<>();
    private Map<String, Map<String, Double>> timeMatrix = new HashMap<>();
    private RouteCalculator routeCalculator;
    private PathfindingAlgorithms pathfinding;

    // UI Components
    private JComboBox<String> fromComboBox;
    private JComboBox<String> toComboBox;
    private JTextField landmarkField;
    private JTextArea resultArea;
    private JPanel mapPanel;
    private java.util.List<Route> currentRoutes;
    private JComboBox<String> sortOptionCombo;
    private JComboBox<String> algorithmCombo;

    public UGNavigateApp() {
        setTitle("UG Navigate - University of Ghana Campus Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        initializeLocations();
        initializeDistanceMatrix();
        routeCalculator = new RouteCalculator(locations, distanceMatrix, timeMatrix);
        pathfinding = new PathfindingAlgorithms();
        currentRoutes = new ArrayList<>();

        setupUI();
        setLocationRelativeTo(null);
    }

    private void initializeLocations() {
        locations = new HashMap<>();

        // Initialize UG campus locations with grid coordinates
        locations.put("Senate House", new Location("Senate House", 400, 200, LocationType.ADMINISTRATIVE));
        locations.put("Central Administration", new Location("Central Administration", 350, 150, LocationType.ADMINISTRATIVE));
        locations.put("John Evans Atta Mills Library", new Location("John Evans Atta Mills Library", 450, 250, LocationType.ACADEMIC));
        locations.put("Balme Library", new Location("Balme Library", 380, 280, LocationType.ACADEMIC));
        locations.put("Great Hall", new Location("Great Hall", 500, 180, LocationType.EVENT));

        // Halls of Residence
        locations.put("Legon Hall", new Location("Legon Hall", 200, 100, LocationType.RESIDENTIAL));
        locations.put("Akuafo Hall", new Location("Akuafo Hall", 150, 150, LocationType.RESIDENTIAL));
        locations.put("Commonwealth Hall", new Location("Commonwealth Hall", 250, 200, LocationType.RESIDENTIAL));
        locations.put("Volta Hall", new Location("Volta Hall", 300, 250, LocationType.RESIDENTIAL));
        locations.put("Achimota Hall", new Location("Achimota Hall", 180, 300, LocationType.RESIDENTIAL));

        // Academic Buildings
        locations.put("School of Engineering", new Location("School of Engineering", 600, 200, LocationType.ACADEMIC));
        locations.put("School of Law", new Location("School of Law", 550, 300, LocationType.ACADEMIC));
        locations.put("Department of Computer Science", new Location("Department of Computer Science", 650, 250, LocationType.ACADEMIC));
        locations.put("School of Business", new Location("School of Business", 520, 350, LocationType.ACADEMIC));
        locations.put("School of Medicine", new Location("School of Medicine", 700, 300, LocationType.ACADEMIC));

        // Services and Facilities
        locations.put("University Hospital", new Location("University Hospital", 750, 350, LocationType.MEDICAL));
        locations.put("Central Cafeteria", new Location("Central Cafeteria", 400, 320, LocationType.DINING));
        locations.put("Night Market", new Location("Night Market", 320, 380, LocationType.DINING));
        locations.put("Sports Complex", new Location("Sports Complex", 600, 400, LocationType.RECREATION));
        locations.put("University Field", new Location("University Field", 500, 450, LocationType.RECREATION));

        // Banks and Services
        locations.put("UG Branch GCB Bank", new Location("UG Branch GCB Bank", 430, 180, LocationType.BANKING));
        locations.put("University Post Office", new Location("University Post Office", 380, 170, LocationType.SERVICE));
        locations.put("Main Gate", new Location("Main Gate", 100, 200, LocationType.ENTRANCE));
        locations.put("East Gate", new Location("East Gate", 800, 250, LocationType.ENTRANCE));

        // Additional Academic Units
        locations.put("Institute of African Studies", new Location("Institute of African Studies", 480, 280, LocationType.ACADEMIC));
        locations.put("School of Performing Arts", new Location("School of Performing Arts", 350, 400, LocationType.ACADEMIC));
        locations.put("Noguchi Memorial Institute", new Location("Noguchi Memorial Institute", 720, 200, LocationType.RESEARCH));
    }

    private void initializeDistanceMatrix() {
        distanceMatrix = new HashMap<>();
        timeMatrix = new HashMap<>();

        // Calculate distances between all locations using Euclidean distance
        for (String loc1 : locations.keySet()) {
            distanceMatrix.put(loc1, new HashMap<>());
            timeMatrix.put(loc1, new HashMap<>());

            for (String loc2 : locations.keySet()) {
                if (loc1.equals(loc2)) {
                    distanceMatrix.get(loc1).put(loc2, 0.0);
                    timeMatrix.get(loc1).put(loc2, 0.0);
                } else {
                    Location l1 = locations.get(loc1);
                    Location l2 = locations.get(loc2);

                    // Calculate Euclidean distance and convert to campus scale (in meters)
                    double distance = Math.sqrt(Math.pow(l2.x - l1.x, 2) + Math.pow(l2.y - l1.y, 2)) * 2.5;

                    // Calculate time based on walking speed (5 km/h average) with traffic factors
                    double baseTime = distance / (5000.0 / 60.0); // minutes
                    double trafficFactor = getTrafficFactor(l1.type, l2.type);
                    double time = baseTime * trafficFactor;

                    distanceMatrix.get(loc1).put(loc2, distance);
                    timeMatrix.get(loc1).put(loc2, time);
                }
            }
        }
    }

    private double getTrafficFactor(LocationType type1, LocationType type2) {
        // Simulate traffic conditions based on location types
        if (type1 == LocationType.DINING || type2 == LocationType.DINING) return 1.3;
        if (type1 == LocationType.ACADEMIC || type2 == LocationType.ACADEMIC) return 1.1;
        if (type1 == LocationType.ADMINISTRATIVE || type2 == LocationType.ADMINISTRATIVE) return 1.2;
        return 1.0; // Normal traffic
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Control Panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Map Panel
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCampusMap(g);
            }
        };
        mapPanel.setPreferredSize(new Dimension(800, 600));
        mapPanel.setBackground(new Color(240, 248, 255));
        add(mapPanel, BorderLayout.CENTER);

        // Results Panel
        JPanel resultsPanel = createResultsPanel();
        add(resultsPanel, BorderLayout.EAST);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Route Planning"));

        // Location selectors
        fromComboBox = new JComboBox<>(locations.keySet().toArray(new String[0]));
        toComboBox = new JComboBox<>(locations.keySet().toArray(new String[0]));

        // Algorithm selector
        algorithmCombo = new JComboBox<>(new String[]{
                "Dijkstra's Algorithm", "Floyd-Warshall", "A* Search",
                "Vogel Approximation", "Northwest Corner"
        });

        // Sort options
        sortOptionCombo = new JComboBox<>(new String[]{
                "Sort by Distance", "Sort by Time", "Sort by Landmarks"
        });

        // Landmark search
        landmarkField = new JTextField(15);
        landmarkField.setToolTipText("Enter landmark type (e.g., Bank, Library, Cafeteria)");

        // Buttons
        JButton findRouteBtn = new JButton("Find Route");
        JButton searchLandmarkBtn = new JButton("Search by Landmark");
        JButton clearBtn = new JButton("Clear");

        // Add components
        panel.add(new JLabel("From:"));
        panel.add(fromComboBox);
        panel.add(new JLabel("To:"));
        panel.add(toComboBox);
        panel.add(new JLabel("Algorithm:"));
        panel.add(algorithmCombo);
        panel.add(findRouteBtn);

        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Landmark:"));
        panel.add(landmarkField);
        panel.add(searchLandmarkBtn);

        panel.add(Box.createHorizontalStrut(20));
        panel.add(sortOptionCombo);
        panel.add(clearBtn);

        // Event listeners
        findRouteBtn.addActionListener(e -> findOptimalRoute());
        searchLandmarkBtn.addActionListener(e -> searchByLandmark());
        clearBtn.addActionListener(e -> clearResults());

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Route Options"));
        panel.setPreferredSize(new Dimension(400, 600));

        resultArea = new JTextArea(30, 25);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void drawCampusMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw campus background
        g2d.setColor(new Color(245, 255, 245));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw grid for reference
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 0; i < getWidth(); i += 50) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 50) {
            g2d.drawLine(0, i, getWidth(), i);
        }

        // Draw connections between nearby locations
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        drawCampusConnections(g2d);

        // Draw all locations
        for (Location location : locations.values()) {
            drawLocation(g2d, location, false);
        }

        // Draw current routes
        if (!currentRoutes.isEmpty()) {
            drawRoutes(g2d);
        }
    }

    private void drawCampusConnections(Graphics2D g2d) {
        // Draw main campus pathways (simplified representation)
        String[][] connections = {
                {"Main Gate", "Central Administration"},
                {"Central Administration", "Senate House"},
                {"Senate House", "John Evans Atta Mills Library"},
                {"John Evans Atta Mills Library", "Central Cafeteria"},
                {"Commonwealth Hall", "Night Market"},
                {"School of Engineering", "Department of Computer Science"},
                {"University Hospital", "School of Medicine"},
                // Add more realistic campus connections
        };

        for (String[] connection : connections) {
            Location loc1 = locations.get(connection[0]);
            Location loc2 = locations.get(connection[1]);
            if (loc1 != null && loc2 != null) {
                g2d.drawLine(loc1.x, loc1.y, loc2.x, loc2.y);
            }
        }
    }

    private void drawLocation(Graphics2D g2d, Location location, boolean isHighlighted) {
        Color color = getLocationColor(location.type);
        if (isHighlighted) {
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(location.x - POINT_SIZE, location.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2);
        }

        g2d.setColor(color);
        g2d.fillOval(location.x - POINT_SIZE/2, location.y - POINT_SIZE/2, POINT_SIZE, POINT_SIZE);

        // Draw location name
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(location.name);
        g2d.drawString(location.name, location.x - textWidth/2, location.y - 15);
    }

    private Color getLocationColor(LocationType type) {
        switch (type) {
            case ACADEMIC: return Color.BLUE;
            case RESIDENTIAL: return Color.GREEN;
            case ADMINISTRATIVE: return Color.RED;
            case DINING: return Color.ORANGE;
            case RECREATION: return Color.MAGENTA;
            case MEDICAL: return Color.PINK;
            case BANKING: return Color.YELLOW;
            case SERVICE: return Color.CYAN;
            case ENTRANCE: return Color.BLACK;
            case RESEARCH: return Color.DARK_GRAY;
            case EVENT: return Color.LIGHT_GRAY;
            default: return Color.GRAY;
        }
    }

    private void drawRoutes(Graphics2D g2d) {
        if (currentRoutes.isEmpty()) return;

        Color[] routeColors = {Color.BLUE, Color.RED, Color.GREEN};
        int colorIndex = 0;

        for (Route route : currentRoutes) {
            g2d.setColor(routeColors[colorIndex % routeColors.length]);
            g2d.setStroke(new BasicStroke(3));

            for (int i = 0; i < route.path.size() - 1; i++) {
                Location from = locations.get(route.path.get(i));
                Location to = locations.get(route.path.get(i + 1));
                g2d.drawLine(from.x, from.y, to.x, to.y);

                // Draw arrow head
                drawArrowHead(g2d, from.x, from.y, to.x, to.y);
            }
            colorIndex++;
        }
    }

    private void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        int arrowLength = 10;
        double angle = Math.atan2(y2 - y1, x2 - x1);

        int arrowX1 = (int) (x2 - arrowLength * Math.cos(angle - Math.PI / 6));
        int arrowY1 = (int) (y2 - arrowLength * Math.sin(angle - Math.PI / 6));
        int arrowX2 = (int) (x2 - arrowLength * Math.cos(angle + Math.PI / 6));
        int arrowY2 = (int) (y2 - arrowLength * Math.sin(angle + Math.PI / 6));

        g2d.drawLine(x2, y2, arrowX1, arrowY1);
        g2d.drawLine(x2, y2, arrowX2, arrowY2);
    }

    private void findOptimalRoute() {
        String from = (String) fromComboBox.getSelectedItem();
        String to = (String) toComboBox.getSelectedItem();
        String algorithm = (String) algorithmCombo.getSelectedItem();

        if (from.equals(to)) {
            JOptionPane.showMessageDialog(this, "Source and destination cannot be the same!");
            return;
        }

        currentRoutes.clear();

        // Generate multiple route options using different algorithms
        switch (algorithm) {
            case "Dijkstra's Algorithm":
                currentRoutes = routeCalculator.findRoutesDijkstra(from, to);
                break;
            case "Floyd-Warshall":
                currentRoutes = routeCalculator.findRoutesFloydWarshall(from, to);
                break;
            case "A* Search":
                currentRoutes = routeCalculator.findRoutesAStar(from, to);
                break;
            case "Vogel Approximation":
                currentRoutes = routeCalculator.findRoutesVogel(from, to);
                break;
            case "Northwest Corner":
                currentRoutes = routeCalculator.findRoutesNorthwestCorner(from, to);
                break;
        }

        // Apply sorting
        sortRoutes();
        displayResults();
        mapPanel.repaint();
    }

    private void searchByLandmark() {
        String landmark = landmarkField.getText().trim().toLowerCase();
        if (landmark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a landmark type!");
            return;
        }

        String from = (String) fromComboBox.getSelectedItem();
        String to = (String) toComboBox.getSelectedItem();

        currentRoutes = routeCalculator.findRoutesThroughLandmark(from, to, landmark);
        sortRoutes();
        displayResults();
        mapPanel.repaint();
    }

    private void sortRoutes() {
        String sortOption = (String) sortOptionCombo.getSelectedItem();

        switch (sortOption) {
            case "Sort by Distance":
                QuickSort.sortRoutesByDistance(currentRoutes);
                break;
            case "Sort by Time":
                MergeSort.sortRoutesByTime(currentRoutes);
                break;
            case "Sort by Landmarks":
                currentRoutes.sort((r1, r2) -> Integer.compare(r2.landmarks.size(), r1.landmarks.size()));
                break;
        }
    }

    private void displayResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== UG NAVIGATE RESULTS ===\n\n");

        if (currentRoutes.isEmpty()) {
            sb.append("No routes found!\n");
        } else {
            for (int i = 0; i < Math.min(currentRoutes.size(), 3); i++) {
                Route route = currentRoutes.get(i);
                sb.append(String.format("ROUTE %d:\n", i + 1));
                sb.append(String.format("Path: %s\n", String.join(" → ", route.path)));
                sb.append(String.format("Distance: %.2f meters\n", route.totalDistance));
                sb.append(String.format("Time: %.2f minutes\n", route.totalTime));
                sb.append(String.format("Landmarks: %s\n",
                        route.landmarks.isEmpty() ? "None" : String.join(", ", route.landmarks)));
                sb.append("\n");
            }
        }

        resultArea.setText(sb.toString());
    }

    private void clearResults() {
        currentRoutes.clear();
        resultArea.setText("");
        mapPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new UGNavigateApp().setVisible(true);
        });
    }
}

// Supporting Classes

class Location {
    String name;
    int x, y;
    LocationType type;

    public Location(String name, int x, int y, LocationType type) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.type = type;
    }
}

enum LocationType {
    ACADEMIC, RESIDENTIAL, ADMINISTRATIVE, DINING, RECREATION,
    MEDICAL, BANKING, SERVICE, ENTRANCE, RESEARCH, EVENT
}

class Route {
    List<String> path;
    double totalDistance;
    double totalTime;
    List<String> landmarks;
    String algorithm;

    public Route(List<String> path, double distance, double time) {
        this.path = new ArrayList<>(path);
        this.totalDistance = distance;
        this.totalTime = time;
        this.landmarks = new ArrayList<>();
        this.algorithm = "";
    }
}

// Route Calculator Class implementing various algorithms
class RouteCalculator {
    private Map<String, Location> locations;
    private Map<String, Map<String, Double>> distanceMatrix;
    private Map<String, Map<String, Double>> timeMatrix;

    public RouteCalculator(Map<String, Location> locations,
                           Map<String, Map<String, Double>> distanceMatrix,
                           Map<String, Map<String, Double>> timeMatrix) {
        this.locations = locations;
        this.distanceMatrix = distanceMatrix;
        this.timeMatrix = timeMatrix;
    }

    public List<Route> findRoutesDijkstra(String from, String to) {
        // Dijkstra's shortest path implementation
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(
                Comparator.comparing(distances::get));

        // Initialize distances
        for (String location : locations.keySet()) {
            distances.put(location, location.equals(from) ? 0.0 : Double.MAX_VALUE);
        }
        pq.add(from);

        while (!pq.isEmpty()) {
            String current = pq.poll();

            if (current.equals(to)) break;

            for (String neighbor : distanceMatrix.get(current).keySet()) {
                double newDist = distances.get(current) + distanceMatrix.get(current).get(neighbor);
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }

        // Reconstruct path
        List<String> path = new ArrayList<>();
        String current = to;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }

        if (path.get(0).equals(from)) {
            double totalDistance = distances.get(to);
            double totalTime = calculatePathTime(path);
            Route route = new Route(path, totalDistance, totalTime);
            route.algorithm = "Dijkstra's Algorithm";
            return Arrays.asList(route);
        }

        return new ArrayList<>();
    }

    public List<Route> findRoutesFloydWarshall(String from, String to) {
        // Floyd-Warshall implementation for all-pairs shortest path
        Map<String, Integer> locationIndex = new HashMap<>();
        List<String> indexToLocation = new ArrayList<>(locations.keySet());

        for (int i = 0; i < indexToLocation.size(); i++) {
            locationIndex.put(indexToLocation.get(i), i);
        }

        int n = locations.size();
        double[][] dist = new double[n][n];
        int[][] next = new int[n][n];

        // Initialize distance matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    String loc1 = indexToLocation.get(i);
                    String loc2 = indexToLocation.get(j);
                    dist[i][j] = distanceMatrix.get(loc1).get(loc2);
                }
                next[i][j] = j;
            }
        }

        // Floyd-Warshall algorithm
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

        // Reconstruct path
        int fromIndex = locationIndex.get(from);
        int toIndex = locationIndex.get(to);
        List<String> path = new ArrayList<>();

        if (dist[fromIndex][toIndex] == Double.MAX_VALUE) {
            return new ArrayList<>();
        }

        int current = fromIndex;
        while (current != toIndex) {
            path.add(indexToLocation.get(current));
            current = next[current][toIndex];
        }
        path.add(to);

        double totalDistance = dist[fromIndex][toIndex];
        double totalTime = calculatePathTime(path);
        Route route = new Route(path, totalDistance, totalTime);
        route.algorithm = "Floyd-Warshall";

        return Arrays.asList(route);
    }

    public List<Route> findRoutesAStar(String from, String to) {
        // A* Search implementation with heuristic
        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> openSet = new PriorityQueue<>(
                Comparator.comparing(fScore::get));

        for (String location : locations.keySet()) {
            gScore.put(location, Double.MAX_VALUE);
            fScore.put(location, Double.MAX_VALUE);
        }

        gScore.put(from, 0.0);
        fScore.put(from, heuristic(from, to));
        openSet.add(from);

        while (!openSet.isEmpty()) {
            String current = openSet.poll();

            if (current.equals(to)) {
                List<String> path = reconstructPath(previous, current);
                double totalDistance = gScore.get(to);
                double totalTime = calculatePathTime(path);
                Route route = new Route(path, totalDistance, totalTime);
                route.algorithm = "A* Search";
                return Arrays.asList(route);
            }

            for (String neighbor : distanceMatrix.get(current).keySet()) {
                double tentativeGScore = gScore.get(current) + distanceMatrix.get(current).get(neighbor);

                if (tentativeGScore < gScore.get(neighbor)) {
                    previous.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, gScore.get(neighbor) + heuristic(neighbor, to));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    public List<Route> findRoutesVogel(String from, String to) {
        // Simplified Vogel Approximation Method adaptation for routing
        List<Route> routes = new ArrayList<>();

        // Generate alternative paths using penalty-based selection
        Set<String> visited = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        currentPath.add(from);

        vogelRecursive(from, to, visited, currentPath, routes);

        return routes.isEmpty() ? findRoutesDijkstra(from, to) : routes;
    }

    private void vogelRecursive(String current, String target, Set<String> visited,
                                List<String> path, List<Route> routes) {
        if (current.equals(target)) {
            double distance = calculatePathDistance(path);
            double time = calculatePathTime(path);
            Route route = new Route(path, distance, time);
            route.algorithm = "Vogel Approximation";
            routes.add(route);
            return;
        }

        if (routes.size() >= 3) return; // Limit to 3 routes

        visited.add(current);

        // Calculate penalties for each unvisited neighbor
        List<String> neighbors = new ArrayList<>();
        for (String neighbor : distanceMatrix.get(current).keySet()) {
            if (!visited.contains(neighbor)) {
                neighbors.add(neighbor);
            }
        }

        // Sort neighbors by distance + heuristic
        neighbors.sort((a, b) -> Double.compare(
                distanceMatrix.get(current).get(a) + heuristic(a, target),
                distanceMatrix.get(current).get(b) + heuristic(b, target)
        ));

        // Explore best neighbors
        for (String neighbor : neighbors.subList(0, Math.min(2, neighbors.size()))) {
            path.add(neighbor);
            vogelRecursive(neighbor, target, new HashSet<>(visited),
                    new ArrayList<>(path), routes);
            path.remove(path.size() - 1);
        }
    }

    public List<Route> findRoutesNorthwestCorner(String from, String to) {
        // Adapted Northwest Corner Method for route finding
        List<Route> routes = new ArrayList<>();

        // Direct route (northwest approach - most direct)
        List<String> directPath = Arrays.asList(from, to);
        double directDistance = distanceMatrix.get(from).get(to);
        double directTime = timeMatrix.get(from).get(to);
        Route directRoute = new Route(directPath, directDistance, directTime);
        directRoute.algorithm = "Northwest Corner (Direct)";
        routes.add(directRoute);

        // Alternative routes through intermediate points
        Location fromLoc = locations.get(from);
        Location toLoc = locations.get(to);

        // Find intermediate points that form a "northwest corner" pattern
        for (String intermediate : locations.keySet()) {
            if (!intermediate.equals(from) && !intermediate.equals(to)) {
                Location intLoc = locations.get(intermediate);

                // Check if intermediate point forms northwest pattern
                if ((intLoc.x <= Math.max(fromLoc.x, toLoc.x) && intLoc.x >= Math.min(fromLoc.x, toLoc.x)) ||
                        (intLoc.y <= Math.max(fromLoc.y, toLoc.y) && intLoc.y >= Math.min(fromLoc.y, toLoc.y))) {

                    List<String> altPath = Arrays.asList(from, intermediate, to);
                    double altDistance = distanceMatrix.get(from).get(intermediate) +
                            distanceMatrix.get(intermediate).get(to);
                    double altTime = timeMatrix.get(from).get(intermediate) +
                            timeMatrix.get(intermediate).get(to);

                    Route altRoute = new Route(altPath, altDistance, altTime);
                    altRoute.algorithm = "Northwest Corner (via " + intermediate + ")";
                    routes.add(altRoute);
                }
            }
        }

        // Sort and return top 3 routes
        routes.sort(Comparator.comparing(r -> r.totalDistance));
        return routes.subList(0, Math.min(3, routes.size()));
    }

    public List<Route> findRoutesThroughLandmark(String from, String to, String landmarkType) {
        List<Route> routes = new ArrayList<>();

        // Find locations that match the landmark type
        List<String> matchingLandmarks = new ArrayList<>();
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            String locationName = entry.getKey().toLowerCase();
            if (locationName.contains(landmarkType)) {
                matchingLandmarks.add(entry.getKey());
            }
        }

        // Generate routes through each matching landmark
        for (String landmark : matchingLandmarks) {
            if (!landmark.equals(from) && !landmark.equals(to)) {
                List<String> path = Arrays.asList(from, landmark, to);
                double distance = distanceMatrix.get(from).get(landmark) +
                        distanceMatrix.get(landmark).get(to);
                double time = timeMatrix.get(from).get(landmark) +
                        timeMatrix.get(landmark).get(to);

                Route route = new Route(path, distance, time);
                route.landmarks.add(landmark);
                routes.add(route);
            }
        }

        return routes;
    }

    private double heuristic(String from, String to) {
        // Euclidean distance heuristic for A*
        Location fromLoc = locations.get(from);
        Location toLoc = locations.get(to);
        return Math.sqrt(Math.pow(toLoc.x - fromLoc.x, 2) + Math.pow(toLoc.y - fromLoc.y, 2));
    }

    private List<String> reconstructPath(Map<String, String> previous, String current) {
        List<String> path = new ArrayList<>();
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return path;
    }

    private double calculatePathDistance(List<String> path) {
        double totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += distanceMatrix.get(path.get(i)).get(path.get(i + 1));
        }
        return totalDistance;
    }

    private double calculatePathTime(List<String> path) {
        double totalTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalTime += timeMatrix.get(path.get(i)).get(path.get(i + 1));
        }
        return totalTime;
    }
}

// Pathfinding Algorithms Class
class PathfindingAlgorithms {

    public static class CriticalPathMethod {
        public static List<String> findCriticalPath(Map<String, Location> locations,
                                                    Map<String, Map<String, Double>> timeMatrix,
                                                    String start, String end) {
            // Simplified Critical Path Method implementation
            Map<String, Double> earliestStart = new HashMap<>();
            Map<String, Double> latestStart = new HashMap<>();
            Map<String, String> predecessor = new HashMap<>();

            // Initialize
            for (String location : locations.keySet()) {
                earliestStart.put(location, location.equals(start) ? 0.0 : Double.MAX_VALUE);
                latestStart.put(location, Double.MAX_VALUE);
            }

            // Forward pass - calculate earliest start times
            PriorityQueue<String> queue = new PriorityQueue<>(
                    Comparator.comparing(earliestStart::get));
            queue.add(start);

            while (!queue.isEmpty()) {
                String current = queue.poll();

                for (String next : timeMatrix.get(current).keySet()) {
                    double newTime = earliestStart.get(current) + timeMatrix.get(current).get(next);
                    if (newTime < earliestStart.get(next)) {
                        earliestStart.put(next, newTime);
                        predecessor.put(next, current);
                        queue.remove(next);
                        queue.add(next);
                    }
                }
            }

            // Reconstruct critical path
            List<String> criticalPath = new ArrayList<>();
            String current = end;
            while (current != null) {
                criticalPath.add(0, current);
                current = predecessor.get(current);
            }

            return criticalPath;
        }
    }

    // Divide and Conquer approach for route optimization
    public static class DivideAndConquerRouter {
        public static List<Route> optimizeRoutes(List<Route> routes) {
            if (routes.size() <= 1) return routes;

            int mid = routes.size() / 2;
            List<Route> left = optimizeRoutes(routes.subList(0, mid));
            List<Route> right = optimizeRoutes(routes.subList(mid, routes.size()));

            return mergeOptimalRoutes(left, right);
        }

        private static List<Route> mergeOptimalRoutes(List<Route> left, List<Route> right) {
            List<Route> merged = new ArrayList<>();
            int i = 0, j = 0;

            while (i < left.size() && j < right.size()) {
                if (left.get(i).totalDistance <= right.get(j).totalDistance) {
                    merged.add(left.get(i++));
                } else {
                    merged.add(right.get(j++));
                }
            }

            while (i < left.size()) merged.add(left.get(i++));
            while (j < right.size()) merged.add(right.get(j++));

            return merged;
        }
    }

    // Greedy algorithm for quick route finding
    public static class GreedyRouter {
        public static Route findGreedyRoute(String from, String to,
                                            Map<String, Location> locations,
                                            Map<String, Map<String, Double>> distanceMatrix) {
            List<String> path = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            String current = from;
            path.add(current);
            visited.add(current);

            while (!current.equals(to)) {
                String next = null;
                double minDistance = Double.MAX_VALUE;

                for (String neighbor : distanceMatrix.get(current).keySet()) {
                    if (!visited.contains(neighbor)) {
                        double distance = distanceMatrix.get(current).get(neighbor);
                        if (distance < minDistance) {
                            minDistance = distance;
                            next = neighbor;
                        }
                    }
                }

                if (next == null) break; // No path found

                current = next;
                path.add(current);
                visited.add(current);

                // If we've visited too many nodes without reaching destination,
                // add direct path to destination
                if (path.size() > 5 && !current.equals(to)) {
                    path.add(to);
                    break;
                }
            }

            double totalDistance = 0;
            for (int i = 0; i < path.size() - 1; i++) {
                totalDistance += distanceMatrix.get(path.get(i)).get(path.get(i + 1));
            }

            Route route = new Route(path, totalDistance, totalDistance / 5.0); // Assuming 5 km/h walking speed
            route.algorithm = "Greedy";
            return route;
        }
    }
}

// Sorting Algorithms
class QuickSort {
    public static void sortRoutesByDistance(List<Route> routes) {
        if (routes.size() <= 1) return;
        quickSortRecursive(routes, 0, routes.size() - 1, "distance");
    }

    private static void quickSortRecursive(List<Route> routes, int low, int high, String criteria) {
        if (low < high) {
            int pi = partition(routes, low, high, criteria);
            quickSortRecursive(routes, low, pi - 1, criteria);
            quickSortRecursive(routes, pi + 1, high, criteria);
        }
    }

    private static int partition(List<Route> routes, int low, int high, String criteria) {
        double pivot = criteria.equals("distance") ? routes.get(high).totalDistance : routes.get(high).totalTime;
        int i = low - 1;

        for (int j = low; j < high; j++) {
            double value = criteria.equals("distance") ? routes.get(j).totalDistance : routes.get(j).totalTime;
            if (value < pivot) {
                i++;
                Collections.swap(routes, i, j);
            }
        }
        Collections.swap(routes, i + 1, high);
        return i + 1;
    }
}

class MergeSort {
    public static void sortRoutesByTime(List<Route> routes) {
        if (routes.size() <= 1) return;
        mergeSortRecursive(routes, 0, routes.size() - 1);
    }

    private static void mergeSortRecursive(List<Route> routes, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRecursive(routes, left, mid);
            mergeSortRecursive(routes, mid + 1, right);
            merge(routes, left, mid, right);
        }
    }

    private static void merge(List<Route> routes, int left, int mid, int right) {
        List<Route> leftList = new ArrayList<>(routes.subList(left, mid + 1));
        List<Route> rightList = new ArrayList<>(routes.subList(mid + 1, right + 1));

        int i = 0, j = 0, k = left;

        while (i < leftList.size() && j < rightList.size()) {
            if (leftList.get(i).totalTime <= rightList.get(j).totalTime) {
                routes.set(k++, leftList.get(i++));
            } else {
                routes.set(k++, rightList.get(j++));
            }
        }

        while (i < leftList.size()) routes.set(k++, leftList.get(i++));
        while (j < rightList.size()) routes.set(k++, rightList.get(j++));
    }
}