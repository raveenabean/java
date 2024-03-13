import java.io.*;
import java.util.*;

public class Jumper {
    private List<Building> buildings;
    private int exitPortalBuildingIndex;
    private Random random;
    private Player player;
    private int numberOfTurnsPlayed;
    private int numberOfFuelCellsFound;

    // Default constructor
    public Jumper() {
        buildings = new ArrayList<>();
        random = new Random();
        exitPortalBuildingIndex = -1;
        numberOfTurnsPlayed = 0;
        numberOfFuelCellsFound = 0;
    }

    // Non-default constructor
    public Jumper(List<Building> buildings, int exitPortalBuildingIndex, Player player, int numberOfTurnsPlayed, int numberOfFuelCellsFound) {
        this.buildings = buildings;
        this.exitPortalBuildingIndex = exitPortalBuildingIndex;
        this.player = player;
        this.numberOfTurnsPlayed = numberOfTurnsPlayed;
        this.numberOfFuelCellsFound = numberOfFuelCellsFound;
        random = new Random();
    }

    public void startGame() {
        Scanner scanner = new Scanner(System.in);
        
        // Print initial game message
        System.out.println("=========================================================================================");
        System.out.println("|                         WELCOME TO NOWHERE WHERE NO ONE ESCAPES                       |");
        System.out.println("| You are quested to try and escape using on the jumper device available in Nowhere     |");
        System.out.println("| The cost of this device isn't free! But let's discuss payment if you manage to escape |");
        System.out.println("| Remember the following if you wish to survive:                                        |");
        System.out.println("| - the device will only allow you to jump short distances                              |");
        System.out.println("| - the jump distance is based on the height difference of the buildings jumped         |");
        System.out.println("| - the building heights change frequently over time                                    |");
        System.out.println("| - fuel cells found on the rooftops can refuel the device for a short while            |");
        System.out.println("| - stay far away from the frozen buildings                                             |");
        System.out.println("| - look out for the Nowehre police webs                                                |");
        System.out.println("| Lastly the Underground Guild takes no responsibility and provides no guarantees       |");
        System.out.println("| Should you survive, we will come to collet! Good Luck!                                |");
        System.out.println(); // Empty line

        // Initialize the game & read building from buidlngs.txt
        readBuildingsFromFile();
        initializePlayer(scanner);

        // Gets the exit portal building from 'buildings' using the exitPortalBuildingIndex
        Building exitPortalBuilding = buildings.get(exitPortalBuildingIndex);
        
        // Start the game loop
        //while (!(player.isGameOver(exitPortalBuilding, buildings)))
        while (!player.getHasLost() && !player.getHasWon()) {
            // Display buildings and game interface
            displayGameInterface(exitPortalBuilding);

            // Calculate jump range jump direction and cost of the jump
            int maxJumpRange = buildings.get(player.getPosition()).getHeight();
            String jumpDirection = promptForJumpDirection(maxJumpRange);
            performJump(jumpDirection, maxJumpRange);

            // Update game conditions
            updateGameConditions();

            // Increment the turn counter
            numberOfTurnsPlayed++;
        }
        // Display buildings and game interface
        displayGameInterface(exitPortalBuilding);
        // Display game outcome and stats
        displayOutcome();

        // Write final stats to outcome.txt
        writeOutcomeStats();
    }

    private void readBuildingsFromFile() {
        
        try {
            // Reads each line of the file until it reaches the end (when readLine() returns null)
            BufferedReader reader = new BufferedReader(new FileReader("buildings.txt"));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] buildingDetails = line.split(",");
                int height = Integer.parseInt(buildingDetails[0]);
                boolean hasExitPortal = Boolean.parseBoolean(buildingDetails[1]);
                boolean hasFuelCell = Boolean.parseBoolean(buildingDetails[2]);
                boolean hasWeb = Boolean.parseBoolean(buildingDetails[3]);
                boolean isFrozen = Boolean.parseBoolean(buildingDetails[4]);

                Building building = new Building(height, hasExitPortal, hasFuelCell, hasWeb, isFrozen);
                buildings.add(building);

                if (hasExitPortal) {
                    exitPortalBuildingIndex = buildings.size() - 1;
                }
            }
        } catch (IOException e) {
            // Handle the exception, maybe terminate the game or use a default building configuration.
            System.out.println("Error reading buildings from 'buildings.txt': " + e.getMessage());
        }
    }

    private void initializePlayer(Scanner scanner) {
        System.out.print("Enter your name (between 3 and 12 characters): ");
        String playerName = scanner.nextLine().trim();
        while (playerName.length() < 3 || playerName.length() > 12) {
            System.out.println("Invalid name length. Please enter a valid name.");
            System.out.print("Enter your name (between 3 and 12 characters): ");
            playerName = scanner.nextLine().trim();
        }
        player = new Player(playerName);
    }

    private String promptForJumpDirection(int maxJumpRange) {
        Scanner scanner = new Scanner(System.in);
        String jumpDirection = "";
    
        while (!jumpDirection.equals("left") && !jumpDirection.equals("right") && !jumpDirection.equals("stay")) {
            System.out.println("Choose your jump direction: left, right, or stay");
            jumpDirection = scanner.nextLine().toLowerCase();
        
            if (jumpDirection.equals("left") && player.getPosition() - maxJumpRange < 0) {
                System.out.println("Can't jump that far left. Choose again.");
                jumpDirection = "";
            } else if (jumpDirection.equals("right") && player.getPosition() + maxJumpRange >= buildings.size()) {
                System.out.println("Can't jump that far right. Choose again.");
                jumpDirection = "";
            }
        }
        return jumpDirection;
    }

    private void performJump(String jumpDirection, int maxJumpRange) {
        int currentPosition = player.getPosition();
        int newPosition = calculateNewPosition(jumpDirection, maxJumpRange);
        
        Building currentBuilding = buildings.get(currentPosition);
        Building newBuilding = buildings.get(newPosition);

        int jumpCost = Math.abs(currentBuilding.getHeight() - newBuilding.getHeight()) + 1;
        //System.out.println("Current buidling =  " + currentBuilding.getHeight() );
        //System.out.println("New buidling = " + newBuilding.getHeight() + "Position" + newPosition);

        // Check battery charge
        if (player.getCharge() >= jumpCost) {
            player.consumeCharge(jumpCost);
            player.setPosition(newPosition);
        } else if (player.getCharge() == 0 || player.getCharge() < jumpCost) {
            System.out.println();
            System.out.println("Not enough charge to make the jump.");
            player.setHasLost(true);
        }
    }

    private void updateGameConditions() {
        Building currentBuilding = buildings.get(player.getPosition());
    
        // Update game conditions: Web, Freeze, Exit Portal, Fuel Cell
        if (currentBuilding.hasWeb() == true) {
            System.out.println();
            System.out.println("Oops! You landed on a web and got caught by the Nowhere Police.");
            player.consumeCharge(5); // Deduct charge for being caught in the web
            currentBuilding.setHasWeb(false); // Remove the web
        }
        if (currentBuilding.isFrozen()) {
            System.out.println();
            System.out.println("The building is frozen. Skipping a turn...");
            player.consumeCharge(1); // Deduct charge for skipping a turn
        }
        if (currentBuilding.hasExitPortal() && !currentBuilding.isFrozen()) {
            player.setHasWon(true); // Set game over flag
        }
        if (currentBuilding.hasFuelCell()) {
            System.out.println();
            System.out.println("You found a fuel cell on the roof. Your jumper device is recharged by 5 points.");
            numberOfFuelCellsFound++; // Increment the number of fuel cells found
            player.rechargeDevice(5); // Recharge the player's device
            currentBuilding.setHasFuelCell(false); // Remove the fuel cell
        }

        // Check battery for zero charge
        if (player.getCharge() == 0) {
            System.out.println();
            System.out.println("You have zero charge");
            player.setHasLost(true);
        }

        // Check if fuel cells need to be placed
        if (numberOfTurnsPlayed >0 && numberOfTurnsPlayed % 3 == 0) {
            removeFuelCells();
            placeFuelCellsRandomly();
        } 

        // Update building height randomly
        for (Building building : buildings) {
            changeHeightRandomly(building, buildings);
        }
    
        // Randomly change the location of the web booby-trap
        changeWebLocationRandomly(buildings);
    
        // Randomly freeze a building
        freezeRandomBuilding(buildings);
    
    }

    
    private void displayGameInterface(Building exitPortalBuilding) {
        System.out.println("================== Jumper Game ==================");
        System.out.println("Player: " + player.getName());
        System.out.println("Charge: " + player.getCharge());
        System.out.println("Turn: " + numberOfTurnsPlayed);
        System.out.println("Current Building: " + (player.getPosition() + 1));

        // Maximum building height
        int maxBuildingHeight = getMaxBuildingHeight(buildings);

        // Display the buildings vertically
        for (int floor = maxBuildingHeight; floor >= 0; floor--) {
            for (Building building : buildings) {
                if (building.getHeight() > floor) {
                    System.out.print("[ ]"); // Building block
                }else if (building.getHeight() == floor){
                    if (building.getPosition(buildings) == player.getPosition()) {
                        System.out.print("*P*"); // Print player's position as "P"
                    } else if (building.hasExitPortal()) {
                        System.out.print("(E)"); // Print exit portal as "E"
                    } else if (building.hasWeb()) {
                        System.out.print("(W)"); // Print web trap as "W"
                    } else if (building.isFrozen()) {
                        System.out.print("FRZ"); // Print freeze as "FRZ" */
                    }else if (building.hasFuelCell()) {
                        System.out.print("(F)"); // Print fuel cell as "F"
                    }else{
                        System.out.print("   "); // Building block
                    }
                } else {
                    System.out.print("   "); // Empty space
                }
            }
            System.out.println();
        }
    
        System.out.println("=============================================");
    }


    public int calculateNewPosition(String jumpDirection, int jumpRange) {
        int newPosition = player.getPosition();

        if (jumpDirection.equals("left")) {
            newPosition -= jumpRange;
        } else if (jumpDirection.equals("right")) {
            newPosition += jumpRange;
        }

        // Validate new position to ensure it stays within bounds
        if (newPosition < 0) {
            newPosition = 0; // Player can't go before the first building
        } else if (newPosition >= buildings.size()) {
            newPosition = buildings.size() - 1; // Player can't go beyond the last building
        }
        return newPosition;
    }

    private void placeFuelCellsRandomly() {
        int numFuelCells = random.nextInt(4) + 1; // Random number between 1 and 4
        List<Integer> availableBuildingIndices = new ArrayList<>();

        // Find available building indices to place fuel cells
        for (int i = 0; i < buildings.size(); i++) {
            if (!buildings.get(i).hasFuelCell()) {
                availableBuildingIndices.add(i);
            }
        }

        // Place fuel cells on available buildings
        for (int i = 0; i < numFuelCells && !availableBuildingIndices.isEmpty(); i++) {
            int randomIndex = random.nextInt(availableBuildingIndices.size());
            int randomBuildingIndex = availableBuildingIndices.get(randomIndex);
            buildings.get(randomBuildingIndex).setHasFuelCell(true);
            availableBuildingIndices.remove(randomIndex);
        }
    }

    private void removeFuelCells() {
        for (Building building : buildings) {
            building.setHasFuelCell(false);
        }
    }

    public void displayOutcome() {
        System.out.println();
        System.out.println("Game Over!");

        if (player.getHasWon() == true) {
            System.out.println("Congratulations, " + player.getName() + "! You reached the exit portal and won the game!");
        } else if (player.getHasWon() == false){
            System.out.println("Sorry, " + player.getName() + ". You lost the game.");
        } else {
            System.out.println("Whoops ");
        }

        System.out.println("Final statistics:");
        System.out.println("Player: " + player.getName());
        System.out.println("Final Charge Level: " + player.getCharge());
    }

    private void writeOutcomeStats() {
        try {
            FileWriter writer = new FileWriter("outcome.txt",true);
            writer.write("Player Name: " + player.getName() + "\n");
            writer.write("Number of turns played: " + numberOfTurnsPlayed + "\n");
            writer.write("Charge level: " + player.getCharge() + "\n");
            writer.write("Number of fuel cells found: " + numberOfFuelCellsFound + "\n");
            writer.write("Win status: " + (player.getHasWon() ? "Won" : "Lost") + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to outcome.txt");
        }
    }

     // Setter for the buildings list
    public void setBuildings(List<Building> buildingsList) {
        buildings = buildingsList;
    }

    public int getMaxBuildingHeight(List<Building> buildings) {
        int maxHeight = 0;
        for (Building building : buildings) {
            if (building.getHeight() > maxHeight) {
            maxHeight = building.getHeight();
            }
        }
        return maxHeight;
    }

    public void changeHeightRandomly(Building building, List<Building> buildings) {
        // Change the building's height randomly
        Random random = new Random();

        // Save the current height as the previousHeight
        building.setPreviousHeight(building.getHeight());

        // Generate a random height between 1 and a maximum value
        int maxHeight = getMaxBuildingHeight(buildings); // Use the maximum height value you have
        int newHeight = random.nextInt(maxHeight) + 1;
    
        // Sets the new height as the current building height 
        building.setHeight(newHeight);
    }

    public void freezeRandomBuilding(List<Building> buildings) {
        
        // Unfreeze the currently frozen building (if any)
        for (Building building : buildings) {
            if (building.isFrozen()) {
                building.setFrozen(false);
                break;
            }
        }
        Random random = new Random();

        // Generate a random index for the new locatiion of the frozen building
        int randomBuildingIndex = random.nextInt(buildings.size());
        
        // Set the new building as frozen
        buildings.get(randomBuildingIndex).setFrozen(true);
    }

    public void changeWebLocationRandomly(List<Building> buildings) {
        // Remove the web booby-trap from the current building
        for (Building building : buildings) {
            if (building.hasWeb()) {
                building.setHasWeb(false);
                break;
            }
        }
         Random random = new Random();

        // Generate a random index for the new location of the web booby-trap
        int newWebIndex = random.nextInt(buildings.size());

        // Set the web booby-trap on the new building
        buildings.get(newWebIndex).setHasWeb(true);
    }

    // Other methods and fields...

    public static void main(String[] args) {
        Jumper game = new Jumper();
        game.startGame();
    }
}

class Building {
    private int height;
    private boolean hasExitPortal;
    private boolean hasFuelCell;
    private boolean hasWeb;
    private boolean isFrozen;
    private int previousHeight;

    public Building() {
        this.height = 0;
        this.hasExitPortal = false;
        this.hasFuelCell = false;
        this.hasWeb = false;
        this.isFrozen = false;
    }

    public Building(int height, boolean hasExitPortal, boolean hasFuelCell, boolean hasWeb, boolean isFrozen) {
        this.height = height;
        this.hasExitPortal = hasExitPortal;
        this.hasFuelCell = hasFuelCell;
        this.hasWeb = hasWeb;
        this.isFrozen = isFrozen;
        this.previousHeight = height; 
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean hasExitPortal() {
        return hasExitPortal;
    }

    public void setHasExitPortal(boolean hasExitPortal) {
        this.hasExitPortal = hasExitPortal;
    }

    public boolean hasFuelCell() {
        return hasFuelCell;
    }

    public void setHasFuelCell(boolean hasFuelCell) {
        this.hasFuelCell = hasFuelCell;
    }

    public boolean hasWeb() {
        return hasWeb;
    }

    public void setHasWeb(boolean hasWeb) {
        this.hasWeb = hasWeb;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }

    public int getPosition(List<Building> buildings) {
        // Return the index of this building in the buildings list
        return buildings.indexOf(this);
    }

    public int getPreviousHeight() {
        return previousHeight;
    }

    public void setPreviousHeight(int previousHeight) {
        this.previousHeight = previousHeight;
    }

}

class Player {
    private String name;
    private int position;
    private int charge;
    private boolean hasWon;
    private boolean hasLost;

// Default constructor
    public Player() {
        this.name = "";
        this.position = 0; // Start on the first building
        this.charge = 10; // Initial charge is 50%
        this.hasWon = false; // Initialize hasWon as false
        this.hasLost = false; // Initialize hasLost as false
    }

    // Non-default constructor
    public Player(String name) {
        this.name = name;
        this.position = 0; 
        this.charge = 10;
        this.hasWon = false; 
        this.hasLost = false; 
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public void consumeCharge(int amount) {
        charge -= amount;
        if (charge < 0) {
            charge = 0; // Ensure charge doesn't go below 0
        }
    }

    public void rechargeDevice(int amount) {
        charge += amount;
        if (charge > 20) {
            charge = 20; // Ensure charge doesn't exceed 20
        }
    }

    public boolean getHasWon() {
        return hasWon;
    }

    public void setHasWon(boolean hasWon) {
        this.hasWon = hasWon;
    }

    public boolean getHasLost() {
        return hasLost;
    }

    public void setHasLost(boolean hasLost) {
        this.hasLost = hasLost;
    }

}

