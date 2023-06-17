package project;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

abstract class VotingSystem {
    protected Map<String, Integer> candidates;
    protected Connection connection;
    protected Scanner scanner;

    public VotingSystem() {
        candidates = new HashMap<>();
        scanner = new Scanner(System.in);
    }

    public abstract void run();

    protected void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/voting_system", "username", "password");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            System.exit(1);
        }
    }

    protected void registerVoter() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO voters (username) VALUES (?)");
            statement.setString(1, username);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Registration successful!");
            } else {
                System.out.println("Failed to register. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to register. Please try again.");
        }
    }

    protected void loginVoter() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM voters WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean hasVoted = resultSet.getBoolean("has_voted");

                if (hasVoted) {
                    System.out.println("You have already voted!");
                } else {
                    System.out.println("Login successful!");
                }
            } else {
                System.out.println("Username not found!");
            }
        } catch (SQLException e) {
            System.out.println("Failed to login. Please try again.");
        }
    }

    protected void castVote() {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM voters WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean hasVoted = resultSet.getBoolean("has_voted");

                if (hasVoted) {
                    System.out.println("You have already voted!");
                } else {
                    System.out.println("Candidates:");
                    int candidateIndex = 1;
                    for (String candidate : candidates.keySet()) {
                        System.out.println(candidateIndex + ". " + candidate);
                        candidateIndex++;
                    }

                    System.out.print("Enter the candidate number: ");
                    try {
                        int candidateNumber = Integer.parseInt(scanner.nextLine());

                        if (candidateNumber >= 1 && candidateNumber <= candidates.size()) {
                            String candidate = (String) candidates.keySet().toArray()[candidateNumber - 1];
                            candidates.put(candidate, candidates.get(candidate) + 1);
                            updateVoteStatus(username);
                            System.out.println("Vote cast successfully!");
                        } else {
                            System.out.println("Invalid candidate number!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid candidate number! Please enter a number.");
                    }
                }
            } else {
                System.out.println("Username not found!");
            }
        } catch (SQLException e) {
            System.out.println("Failed to cast vote. Please try again.");
        }
    }

    protected void updateVoteStatus(String username) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE voters SET has_voted = true WHERE username = ?");
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to update vote status.");
        }
    }

    protected void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Failed to close the database connection.");
        }
    }
}

class OnlineVotingSystem extends VotingSystem {
    public OnlineVotingSystem() {
        super();
        candidates.put("Candidate 1", 0);
        candidates.put("Candidate 2", 0);
        candidates.put("Candidate 3", 0);
    }

    @Override
    public void run() {
        connectToDatabase();
        boolean votingOpen = true;

        while (votingOpen) {
            System.out.println("1. Register\n2. Login\n3. Vote\n4. Exit");
            System.out.print("Enter your choice: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        registerVoter();
                        break;
                    case 2:
                        loginVoter();
                        break;
                    case 3:
                        castVote();
                        break;
                    case 4:
                        votingOpen = false;
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice! Please enter a number.");
            }
        }

        closeConnection();
    }
}

 class Main {
    public static void main(String[] args) {
        VotingSystem votingSystem = new OnlineVotingSystem();
        votingSystem.run();
    }
}