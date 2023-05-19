import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.IOException;
import java.util.Scanner;

public class scandal {
    public static int totalEmails;
    public static Map<String, Set<String>> mailGraph = new HashMap<>();

    /**
     * Constructs the mail graph by processing files in the given folder.
     *
     * @param folder - the folder containing the email files
     */
    public static void fileGraph(final File folder) {
        String line;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                fileGraph(fileEntry);
            } 
            else {
                try {
                    BufferedReader bufferedreader = new BufferedReader(new FileReader(fileEntry.getPath()));
                    String sent = null;
                    String received = null;
                    while ((line = bufferedreader.readLine()) != null) {
                        if (line.startsWith("From: ")) {
                            sent = extractEmail(line);
                        }
                        if (line.startsWith("To: ")) {
                            String[] arr = line.split(" ");
                            for (int i = 1; i < arr.length; i++) {
                                received = extractEmail(arr[i]);
                                if (sent != null && received != null) {
                                    addEdge(mailGraph, sent, received);
                                }
                            }
                        }
                        if (line.startsWith("Cc: ")) {
                            String[] arr = line.split(" ");
                            for (int i = 1; i < arr.length; i++) {
                                received = extractEmail(arr[i]);
                                if (received != null && sent != null) {
                                    addEdge(mailGraph, sent, received);
                                }
                            }
                        }
                    }
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Extracts email addresses from the input string.
     *
     * @param input - the input string containing an email address
     * @return - the extracted email address, or null if no email address is found
     */
    public static String extractEmail(String input) {
        Matcher matcher = Pattern.compile("([a-zA-Z0-9.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9._-]+)").matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Adds an edge between sender and receiver in the adjacency map.
     *
     * @param adjacencyMap - the adjacency map representing the mail graph
     * @param sender - the sender's email address
     * @param receiver - the receiver's email address
     */
    public static void addEdge(Map<String, Set<String>> adjacencyMap, String sender, String receiver) {
        if (!adjacencyMap.containsKey(sender)) {
            adjacencyMap.put(sender, new HashSet<>());
        }
        if (!adjacencyMap.containsKey(receiver)) {
            adjacencyMap.put(receiver, new HashSet<>());
        }
        adjacencyMap.get(sender).add(receiver);
        adjacencyMap.get(receiver).add(sender);
    }

    /**
     * Prints the adjacency map representing the mail graph.
     *
     * @param adjacencyMap the adjacency map representing the mail graph
     */
    public static void printAdjacencyMap(Map<String, Set<String>> adjacencyMap) {
        for (String node : adjacencyMap.keySet()) {
            Set<String> connections = adjacencyMap.get(node);
            System.out.print(node + ": ");
            for (String connect : connections) {
                System.out.print(connect + " ");
            }
            System.out.println();
        }
    }

    /**
     * Performs a depth-first search (DFS) to find email connectors in the mail graph.
     *
     * @param adjacencyMap - the adjacency map representing the mail graph
     * @param vert - the current vertex being processed
     * @param parent - the parent vertex of the current vertex
     * @param dfsnum - the map to store DFS numbers of vertices
     * @param back - the map to store back numbers of vertices
     * @param connectors - the set to store email connectors
     * @param dfsCount - the current DFS count
     */
    private static void dfs(Map<String, Set<String>> adjacencyMap, String vert, String parent, Map<String, Integer> dfsnum, Map<String, Integer> back, Set<String> connectors, int dfsCount) {
        dfsnum.put(vert, dfsCount);
        back.put(vert, dfsCount);
        dfsCount++;
        int childCount = 0;
        boolean isConnector = false;
        Set<String> connections = adjacencyMap.get(vert);
        if (connections != null) {
            for (String connect : connections) {
                if (connect.equals(parent)) {
                    continue;
                }
                if (!dfsnum.containsKey(connect)) {
                    dfs(adjacencyMap, connect, vert, dfsnum, back, connectors, dfsCount);
                    childCount++;
                    if (dfsnum.get(vert) <= back.get(connect)) {
                        isConnector = true;
                    } 
                    else {
                        back.put(vert, Math.min(back.get(vert), back.get(connect)));
                    }
                } 
                else {
                    back.put(vert, Math.min(back.get(vert), dfsnum.get(connect)));
                }
            }
        }
        if ((parent != null && isConnector) || (parent == null && childCount > 1)) {
            connectors.add(vert);
        }
    }

    /**
     * Finds email connectors in the mail graph and optionally writes them to a file.
     *
     * @param adjacencyMap - the adjacency map representing the mail graph
     * @param outputFileName - the name of the output file to write connectors (null to only print)
     */
    public static void findConnectors(Map<String, Set<String>> adjacencyMap, String outputFileName) {
        Set<String> connectors = new HashSet<>();
        Map<String, Integer> dfsnum = new HashMap<>();
        Map<String, Integer> back = new HashMap<>();
        for (String vert : adjacencyMap.keySet()) {
            if (!dfsnum.containsKey(vert)) {
                dfs(adjacencyMap, vert, null, dfsnum, back, connectors, 1);
            }
        }
        System.out.println(connectors.size());
        try {
            FileWriter writer = null;
            if (outputFileName != null) {
                try {
                    writer = new FileWriter(outputFileName);
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                for (String connector : connectors) {
                    System.out.println(connector);
                    writer.write(connector + "\n");
                }
                writer.close();
            } 
            else {
                for (String connector : connectors) {
                    System.out.println(connector);
                }
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        final File folder = new File("/Users/karljosefsson/Desktop/CS245/mywork/Projects/Project3/scandal/maildir");
        fileGraph(folder);
        printAdjacencyMap(mailGraph);
        String connectors = new String("connectors.txt");
        findConnectors(mailGraph, connectors);
        Scanner scanner = new Scanner(System.in);
        String email;
        while (true) {
            System.out.print("Email address of the individual (or EXIT to quit): ");
            email = scanner.nextLine();
            if (email.equalsIgnoreCase("EXIT")) {
                break;
            }
            Set<String> sentTo = mailGraph.getOrDefault(email, new HashSet<>());
            Set<String> receivedFrom = new HashSet<>();
            Set<String> team = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : mailGraph.entrySet()) {
                String sender = entry.getKey();
                Set<String> recipients = entry.getValue();
                if (recipients.contains(email)) {
                    receivedFrom.add(sender);
                    team.addAll(recipients);
                }
            }
            int uniqueSentTo = sentTo.size();
            int uniqueReceivedFrom = receivedFrom.size();
            int teamSize = team.size();
            if (uniqueSentTo == 0 && uniqueReceivedFrom == 0 && teamSize == 0) {
                System.out.println("Email address (" + email + ") not found in the dataset.");
            } 
            else {
                System.out.println("* " + email + " has sent messages to " + uniqueSentTo + " others");
                System.out.println("* " + email + " has received messages from " + uniqueReceivedFrom + " others");
                System.out.println("* " + email + " is in a team with " + teamSize + " individuals");
            }
        }
        scanner.close();
    }
}

