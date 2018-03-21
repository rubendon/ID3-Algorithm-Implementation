// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Simon Dixon

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

class ID3 {

    /**
     * Each node of the tree contains either the attribute number (for non-leaf nodes) or class number (for leaf nodes) in <b>value</b>, and an array of tree nodes in <b>children</b> containing each of the children of the node (for non-leaf nodes). The attribute number corresponds to the column number in the training and test files. The children are ordered in the same order as the Strings in strings[][]. E.g., if value == 3, then the array of children correspond to the branches for attribute 3 (named data[0][3]): children[0] is the branch for attribute 3 == strings[3][0] children[1] is the branch for attribute 3 == strings[3][1] children[2] is the branch for attribute 3 == strings[3][2] etc. The class number (leaf nodes) also corresponds to the order of classes in strings[][]. For example, a leaf with value == 3 corresponds to the class label strings[attributes-1][3].
     */
    class TreeNode {

        TreeNode[] children;
        int value;

        public TreeNode(TreeNode[] ch, int val) {
            value = val;
            children = ch;
        } // constructor

        public String toString() {
            return toString("");
        } // toString()

        String toString(String indent) {
            if (children != null) {
                String s = "";
                for (int i = 0; i < children.length; i++) {
                    s += indent + data[0][value] + "="
                            + strings[value][i] + "\n"
                            + children[i].toString(indent + '\t');
                }
                return s;
            } else {
                return indent + "Class: " + strings[attributes - 1][value] + "\n";
            }
        } // toString(String)

    } // inner class TreeNode

    private int attributes; 	// Number of attributes (including the class)
    private int examples;		// Number of training examples
    private TreeNode decisionTree;	// Tree learnt in training, used for classifying
    private String[][] data;	// Training data indexed by example, attribute
    private String[][] strings; // Unique strings for each attribute
    private int[] stringCount;  // Number of unique strings for each attribute

    public void split(TreeNode n, String[][] set, String[][] stringSet) {
        //in case of impure set
        if (stringSet.length == 0) {
            n.value = getBestImpure(set, stringSet);
            return;
        } else if (set.length != 0) {

            //gets the best attribute for this set to split on
            int A = getBestAttribute(set, stringSet);
            int index = getAttributeIndex(set[0][A]);
            if (DEBUG) {
                System.out.println("<--------- " + set[0][A]);
            }
            if (n == null) {
                decisionTree = new TreeNode(null, index);
                n = decisionTree;
            }

            n.value = index;

            int childCount = 0;
            if (stringSet[A] == null) {
                return;
            }
            for (int i = 0; i < stringSet[A].length; i++) {
                if (stringSet[A][i] != null) {
                    childCount++;
                }
            }
            //createse A.n child nodes
            n.children = new TreeNode[childCount];

            //loop through each attribute
            for (int i = 0; i < childCount; i++) {
                if (DEBUG) {
                    System.out.println("Looping through child nodes");
                }
                n.children[i] = new TreeNode(null, index);

                ArrayList<String[]> newSet = new ArrayList<>();
                HashSet hs = new HashSet();

                //gets the index of children in this subset
                newSet.add(set[0]);
                for (int j = 1; j < set.length; j++) { //for each example
                    if (set[j][A].equals(stringSet[A][i])) { //check which strings it belongs to
                        hs.add(set[j][set[0].length - 1]); //add the class of this example
                        newSet.add(set[j]);
                    }
                }

                //check subset is pure, set the child node to the class.
                if (hs.size() == 1) {
                    int classNum = 0;
                    for (int j = 0; j < strings[strings.length - 1].length; j++) {
                        if (hs.contains(strings[strings.length - 1][j])) {
                            classNum = j;
                            break;
                        }
                    }
                    if (DEBUG) {
                        System.out.println("pure set - " + strings[A][i] + " - " + set[0][A]);
                    }
                    n.children[i] = new TreeNode(null, classNum);
                } //if subset is not pure, split again
                else {
                    //create subset excluding this 
                    String[][] subSet = new String[newSet.size()][];
                    for (int j = 0; j < subSet.length; j++) { //for each example we want to remove the A'th attribute
                        String[] thisSet = newSet.get(j);
                        String[] tempSet = new String[thisSet.length - 1];
                        int tCount = 0;
                        for (int k = 0; k < stringSet.length; k++) {
                            if (k != A) {
                                tempSet[tCount] = thisSet[k];
                                tCount++;
                            }
                        }
                        subSet[j] = tempSet;
                    }

                    //creates new subset of strings not containing the attribute we split at
                    String[][] stringSubSet = new String[stringSet.length - 1][];
                    int count = 0;
                    for (int j = 0; j < stringSet.length; j++) {
                        if (j != A) {
                            stringSubSet[count] = stringSet[j];
                            count++;
                        }
                    }

                    if (DEBUG) {
                        System.out.println("splitting at - " + stringSet[A][i] + ", number of examples - " + subSet.length + " string set length - " + stringSubSet.length);
                    }
                    split(n.children[i], subSet, stringSubSet);
                }
            }
        }
    }

    public boolean DEBUG = false;

    public String[] getClassValues(String[][] set) {
        String[] c = new String[set.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = set[i][set[0].length - 1];
        }
        return c;
    }

    public int getAttributeIndex(String type) {
        for (int i = 0; i < data[0].length; i++) {
            if (data[0][i].equals(type)) {
                return i;
            }
        }
        return 0;
    }

    public int getBestAttribute(String[][] set, String[][] stringSet) {
        int best = 100;

        double H = entropy(getClassValues(set)); //starting entropy
        if (DEBUG) {
            System.out.println("starting entropy - " + H);

            System.out.println("");
            System.out.println("examples:");
            for (int i = 0; i < set.length; i++) {
                for (int j = 0; j < set[i].length; j++) {
                    if (set[i][j] != null) {
                        System.out.print(set[i][j] + " ");
                    }
                }
                System.out.println("--");
            }
            System.out.println("");
            System.out.println("strings:");
            for (int i = 0; i < stringSet.length; i++) {
                for (int j = 0; j < stringSet[i].length; j++) {
                    if (stringSet[i][j] != null) {
                        System.out.print(stringSet[i][j] + " ");
                    }
                }
                System.out.println("");
            }
            System.out.println("");

        }
        int bestIndex = 0;
        double bestGain = -1;

        double[] gains = new double[stringSet.length - 1];
        for (int i = 0; i < stringSet.length - 1; i++) {
            double startSize = set.length;
            double gain = H;
            for (int s = 0; s < stringSet[i].length; s++) { // e.g sunny, cloudy

                ArrayList<Integer> examplesIndex = new ArrayList<>();
                ArrayList<String> classList = new ArrayList();
                //gets the index of children in this subset
                for (int j = 0; j < set.length; j++) { //for each example
                    if (set[j][i].equals(stringSet[i][s])) { //check which strings it belongs to
                        examplesIndex.add(j);
                        classList.add(set[j][set[0].length - 1]); //adds the result of this example
                    }
                }

                //create subset using these indexes
                String[][] subset = new String[examplesIndex.size()][];
                for (int j = 0; j < subset.length; j++) {
                    subset[j] = set[examplesIndex.get(j)]; //sets subset values
                }
                double endSize = subset.length;

                String[] classValues = new String[classList.size()];
                //  System.out.println("class list size - " + classList.size() + " for " + stringSet[i][s]);
                for (int j = 0; j < classList.size(); j++) {
                    classValues[j] = classList.get(j);
                }
                double e = entropy(classValues);

                gain -= (e * (endSize / startSize));
                if (stringSet[i][s] != null) {
                    if (DEBUG) {
                        System.out.println("entropy of " + stringSet[i][s] + " - " + e);
                    }
                }
            }

            if (DEBUG) {
                System.out.println("gain is- " + gain);
            }
            if (gain > bestGain) {
                bestGain = gain;
                bestIndex = i;
            }
        }
        if (DEBUG) {
            System.out.println("best gain is - " + bestGain + " at index " + bestIndex + "\n");
        }
        return bestIndex;
    }

    public double entropy(String[] classValues) {
        double H = 0;
        if (classValues.length != 0) {
            String[] classes = strings[attributes - 1];
            int[] counts = new int[classValues.length];
            for (int i = 0; i < classValues.length; i++) {
                for (int j = 0; j < counts.length; j++) {
                    if (classValues[i].equals(classes[j])) {
                        counts[j]++;
                    }
                }
            }

            double size = classValues.length; //total number of attributes
            for (int i = 0; i < counts.length; i++) {
                double xlog = xlogx(counts[i] / size);
                H = H - xlog;
            }
            return H;
        } else {
            return 0;
        }
    }

    public int getBestImpure(String[][] set, String[][] stringSet) {
        String[] cValues = strings[strings.length - 1];
        int[] classCount = new int[cValues.length];
        for (int j = 0; j < cValues.length; j++) { //classes
            for (int i = 0; i < set.length; i++) { //set
                if (set[i][set[i].length - 1].equals(cValues[j])) {
                    classCount[j]++;
                }
            }
        }
        int bestClass = 0;
        int bestCount = 0;
        for (int i = 0; i < classCount.length; i++) {
            if (classCount[i] > bestCount) {
                bestClass = i;
                bestCount = classCount[i];
            }
        }
        System.out.println("unpure final class");
        return bestClass;
    }

    public void train(String[][] trainingData) {
        indexStrings(trainingData);
        split(null, trainingData, strings);
    }

    public ID3() {
        attributes = 0;
        examples = 0;
        decisionTree = null;
        data = null;
        strings = null;
        stringCount = null;
    } // constructor

    public void printTree() {
        if (decisionTree == null) {
            error("Attempted to print null Tree");
        } else {
            System.out.println(decisionTree);
        }
    } // printTree()

    /**
     * Print error message and exit. *
     */
    static void error(String msg) {
        System.err.println("Error: " + msg);
        System.exit(1);
    } // error()

    static final double LOG2 = Math.log(2.0);

    static double xlogx(double x) {
        return x == 0 ? 0 : x * Math.log(x) / LOG2;
    } // xlogx()

    /**
     * Execute the decision tree on the given examples in testData, and print the resulting class names, one to a line, for each example in testData.
     *
     */
    public void classify(String[][] testData) {
        for (int example = 1; example < testData.length; example++) {
            TreeNode temp = new TreeNode(null, 0);
            temp = decisionTree;
            while (temp.children != null) {
                int match = 0;
                for (int att = 0; att < temp.children.length; att++) {
                    if (testData[example][temp.value].equals(strings[temp.value][att])) {
                        match = att;
                        break;
                    }
                }
                temp = temp.children[match];
            }
            System.out.println(strings[strings.length - 1][temp.value]);
        }
    } // classify()

    /**
     * Given a 2-dimensional array containing the training data, numbers each unique value that each attribute has, and stores these Strings in instance variables; for example, for attribute 2, its first value would be stored in strings[2][0], its second value in strings[2][1], and so on; and the number of different values in stringCount[2].
     *
     */
    void indexStrings(String[][] inputData) {
        data = inputData;
        examples = data.length; //num of examples
        attributes = data[0].length; //num of attributes
        stringCount = new int[attributes];
        strings = new String[attributes][examples];// might not need all columns
        int index = 0;
        for (int attr = 0; attr < attributes; attr++) {
            stringCount[attr] = 0;
            for (int ex = 1; ex < examples; ex++) {
                for (index = 0; index < stringCount[attr]; index++) {
                    if (data[ex][attr].equals(strings[attr][index])) {
                        break;	// we've seen this String before
                    }
                }
                if (index == stringCount[attr]) // if new String found
                {
                    strings[attr][stringCount[attr]++] = data[ex][attr];
                }
            } // for each example
        } // for each attribute
    } // indexStrings()

    /**
     * For debugging: prints the list of attribute values for each attribute and their index values.
     *
     */
    void printStrings() {
        for (int attr = 0; attr < attributes; attr++) {
            for (int index = 0; index < stringCount[attr]; index++) {
                System.out.println(data[0][attr] + " value " + index
                        + " = " + strings[attr][index]);
            }
        }
    } // printStrings()

    /**
     * Reads a text file containing a fixed number of comma-separated values on each line, and returns a two dimensional array of these values, indexed by line number and position in line.
     *
     */
    static String[][] parseCSV(String fileName)
            throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String s = br.readLine();
        int fields = 1;
        int index = 0;
        while ((index = s.indexOf(',', index) + 1) > 0) {
            fields++;
        }
        int lines = 1;
        while (br.readLine() != null) {
            lines++;
        }
        br.close();
        String[][] data = new String[lines][fields];
        Scanner sc = new Scanner(new File(fileName));
        sc.useDelimiter("[,\n]");
        for (int l = 0; l < lines; l++) {
            for (int f = 0; f < fields; f++) {
                if (sc.hasNext()) {
                    data[l][f] = sc.next();
                } else {
                    error("Scan error in " + fileName + " at " + l + ":" + f);
                }
            }
        }
        sc.close();
        return data;
    } // parseCSV()

    public static void main(String[] args) throws FileNotFoundException, IOException {

//        String[][] testData = parseCSV("./src/aiid3/realEstateTest.csv");
//        String[][] trainingData = parseCSV("./src/aiid3/realEstateTrain.csv");
//        ID3 classifier = new ID3();
//        classifier.train(trainingData);
//        classifier.printTree();
//        classifier.classify(testData);

        if (args.length != 2) {
            error("Expected 2 arguments: file names of training and test data");
        }
        String[][] trainingData = parseCSV(args[0]);
        String[][] testData = parseCSV(args[1]);
        ID3 classifier = new ID3();
        classifier.train(trainingData);
        classifier.printTree();
        classifier.classify(testData);

    } // main()

} // class ID3
