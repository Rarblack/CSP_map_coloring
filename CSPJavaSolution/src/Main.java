import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Main {

    // ----------------------------------------------  GLOBAL VALUES
    // map to store inputted variables
    static Map<Integer, Variable> VARIABLES;

    // queue for AC3 to store variables/arcs
    static Queue<int[]> ARCS;

    // excluded colors during AC3
    static Map<Integer, ArrayList<Integer>> REMOVEDCOLORS;

    // indicates the amount of the unique colors
    static int NUMBER_OF_UNIQUE_COLORS = -1;

    // -----------------------------------------------  GLOBAL VALUES END


    // parsing the input file
    public static void parse(String filename) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;

        while ((line = br.readLine()) != null) {

            // skip the comments
            if(line.charAt(0) == '#') continue;
            if(NUMBER_OF_UNIQUE_COLORS == -1) NUMBER_OF_UNIQUE_COLORS = Integer.parseInt(line.split("=")[1].strip());
            else
            {
                int Xi = Integer.parseInt(line.split(",")[0].strip());
                int Xj = Integer.parseInt(line.split(",")[1].strip());

                // Add vertices to vertex collection
                if (!VARIABLES.containsKey(Xi))   VARIABLES.put(Xi, new Variable(NUMBER_OF_UNIQUE_COLORS));
                if (!VARIABLES.containsKey(Xj))   VARIABLES.put(Xj, new Variable(NUMBER_OF_UNIQUE_COLORS));

                // Add undirected edges
                VARIABLES.get(Xi).addArc(Xj);
                VARIABLES.get(Xj).addArc(Xi);
            }
        }

        // reading and storing arcs
        Set<Integer> keys = VARIABLES.keySet();
        for (int key : keys){
            Set<Integer> edges = VARIABLES.get(key).getArcs();
            for (int edge : edges){
                ARCS.add(new int[]{key, edge});
            }
        }
    }


    // checks if value is consistent with assignment then
    public static boolean satisfied(Integer color, Set<Integer> adjacentVertices){

        for (Integer adjVariableId : adjacentVertices){
            if (VARIABLES.get(adjVariableId).getColor() == color)
                return false; // conditions are not satisfied
        }

        return true;
    }

    // if the conditions did not satisfy, map is required to get back to previous state
    public static void restorePreviousState(Variable variable, Set<Integer> neighbors, int color){

        variable.setColor(-1); // removes the assigned color

        // removed colors from neighbor's domains are returned
        for (Integer adjVariableId : neighbors) {
            VARIABLES.get(adjVariableId).addDomainValue(color);
        }

        // restore removed colors from AC-3 checking
        if (REMOVEDCOLORS != null && REMOVEDCOLORS.size() > 0) {
            Set<Integer> keys = REMOVEDCOLORS.keySet();
            for (int key : keys) {
                ArrayList<Integer> values = REMOVEDCOLORS.get(key);
                for (int value: values) {
                    VARIABLES.get(key).addDomainValue(value);
                }
            }
        }
    }


    //  the heuristic is trying to leave the maximum flexibility for subsequent variable assignments.

    public static ArrayList<Integer> ORDERDOMAINVALUES(Set<Integer> availableColors, Set<Integer> adjacentVertices)
    {
        Map<Integer, Integer> colorCountMap = new HashMap<>();
        ArrayList<Integer> sortedColors  = new ArrayList<>();

        for (Integer color : availableColors){
            colorCountMap.put(color, 0); // initializing colorCountMap with 0 values
        }

        // count and update occurrences (inversely) of colors of assigned adjacent vertices
        for (Integer adjVariableId : adjacentVertices)
        {
            int adjacentColor = VARIABLES.get(adjVariableId).getColor();
            if (adjacentColor != -1)
            {
                if (colorCountMap.containsKey(adjacentColor))
                {
                    colorCountMap.put(adjacentColor, colorCountMap.get(adjacentColor) - 1); // decrease the count
                }
            }
        }

        // sort by occurrence values in ascending order
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(colorCountMap.entrySet());
        list.sort(Map.Entry.comparingByValue());

        for (Map.Entry<Integer, Integer> entry : list) {
            sortedColors.add(entry.getKey());
        }
        return sortedColors;
    }


    //  function REVISE(csp, Xi, Xj ) returns true iff we revise the domain of Xi
    public static boolean REVISE(int Xi, int Xj){

        // revised ← false
        boolean revised = false;

        Set<Integer> Di = VARIABLES.get(Xi).getDomain();
        Set<Integer> Dj = VARIABLES.get(Xj).getDomain();

        int y = Dj.iterator().next();

        //      for each x in Di do
        //          if no value y in Dj allows (x ,y) to satisfy the constraint between Xi and Xj then
        if (Dj.size() == 1 && Di.contains(y)) {
            //              delete x from Di
            VARIABLES.get(Xi).removeDomainValue(y);
            if (!REMOVEDCOLORS.containsKey(Xi)) REMOVEDCOLORS.put(Xi, new ArrayList<>());
            REMOVEDCOLORS.get(Xi).add(y);
            //              revised ← true
            revised = true;
        }

        //      return revised
        return revised;
    }


    public static boolean AC3() {
        // local variables: queue, a queue of arcs, initially all the arcs in csp
        Queue<int[]> arcsCopy = new LinkedList<>(ARCS);
        REMOVEDCOLORS = new HashMap<>();

        // checking whether the arc is suitable for binary constraint or not
        while (arcsCopy.size() != 0){
            int[] xy = arcsCopy.remove();
            int Xi = xy[0];
            int Xj = xy[1];

            if(REVISE(Xi,Xj))
                if(VARIABLES.get(Xi).getDomain().size() == 0) return false;
        }

        return true;
    }


    public static boolean BACKTRACK(Integer XiID){
        // if assignment is complete then return assignment part from the book
        if (XiID == -1) return true;

        // var ← SELECT-UNASSIGNED-VARIABLE(csp)
        Variable Xi = VARIABLES.get(XiID);

        Set<Integer> neighbors = Xi.getArcs(); // get adjacent vertices

        ArrayList<Integer> colors = ORDERDOMAINVALUES(Xi.getDomain(), neighbors);
        boolean failure = false;    // flag for failures of FC and AC3 checks within CSP

        // for each value in ORDER-DOMAIN-VALUES(var , assignment, csp) do
        for (Integer color : colors){
            // if value is consistent with assignment then
            if(satisfied(color, neighbors)){
                // add {var = value} to assignment
                Xi.setColor(color);

                int nextVariableId = -1;
                int minRemainingValue = Integer.MAX_VALUE;

                for (Integer neighborID : neighbors){
                    Variable adjVariable = VARIABLES.get(neighborID);
                    adjVariable.removeDomainValue(color);

                    // inferences ← INFERENCE(csp, var , value) - forward checking
                    if (adjVariable.getDomain().size() == 0) {failure = true; break;}

                    // finding next unassigned variable by minimum remaining value
                    if (adjVariable.getColor() == -1 && adjVariable.getDomain().size() < minRemainingValue){
                        minRemainingValue = adjVariable.getDomain().size();
                        nextVariableId = neighborID;
                    }
                }

                // if inferences == failure
                if (!AC3()) failure = true;

                // if result return true
                // recursive call starts
                if (!failure && BACKTRACK(nextVariableId)) return true;

                // remove {var = value} and inferences from assignment
                restorePreviousState(Xi, neighbors, color);
            }
        }

        // no color has been assigned to the Xi
        return false;
    }


    // function BACKTRACKING-SEARCH() returns a solution, or failure
    public static void BACKTRACKSEARCH(){
        boolean solution = false;

        //all variables hashmap IDs
        Set<Integer> keys = VARIABLES.keySet();

        // start coloring uncolored VARIABLES
        for (Integer key : keys) {
            if(VARIABLES.get(key).getColor() == -1) {
                solution = BACKTRACK(key);
                // when some variable is not in the assignments, then solution does not exist
                if (!solution) break;
            }
        }

        // printing the result
        if (!solution) System.out.println("No solution");

        else{
            System.out.println("Solution is found:");
            for (Integer key : keys) {
                int color = VARIABLES.get(key).getColor();

                System.out.printf("Area Number: %d, Assigned Color Number: %d\n", key, color);
            }
        }
    }



    public static void main(String args[]) throws IOException {
        Scanner sc=new Scanner(System.in);
        String filename = "";

        while(filename.equals(""))
        {
            System.out.print("Filename: ");
            filename = sc.nextLine();
        }

        VARIABLES = new HashMap<Integer, Variable>();

        ARCS = new LinkedList<>();

        parse(filename);

        BACKTRACKSEARCH();
    }
}



