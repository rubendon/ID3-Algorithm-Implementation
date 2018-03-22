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
    private String[][] testData;

    
     /**
     * Recursive method for setting decisionTree values and children
     */
    public void split(TreeNode n, String[][] set, String[][] stringSet) {
        if (decisionTree == null) {
            decisionTree = new TreeNode(null, -1);
            n = decisionTree;
        }
        //no more attributes to split at
        if (stringSet.length == 1) {
            System.out.println("getting mode attribute");
            n.value = getModeClass(set, stringSet);
            System.out.println("mode attribute is - " + strings[strings.length-1][n.value]);
        } else {
            int setClassIndex = set[0].length - 1;
            int stringClassIndex = strings.length - 1;
            int numOfClasses = strings[stringClassIndex].length;

            int bestAttribute = getBestAttribute(set, stringSet);
            System.out.println("best attribute is - " + bestAttribute);
            int attributeIndex = getAttributeIndex(set[0][bestAttribute]);
            System.out.println("best attribute index - " + attributeIndex);
            n.value = attributeIndex;

            int childCount = 0;
            for (int child = 0; child < stringSet[bestAttribute].length; child++) {
                if (stringSet[bestAttribute][child] != null) {
                    childCount++;
                }
            }
            n.children = new TreeNode[childCount];
            System.out.println(n.children.length);

            for (int child = 0; child < childCount; child++) {
                ArrayList<String[]> childSet = new ArrayList<>();
                HashSet childClassSet = new HashSet();

                //adds examples to childSet, and adds classes to childClassSet. 
                childSet.add(set[0]);
                for (int example = 1; example < set.length; example++) {
                    if (set[example][bestAttribute].equals(stringSet[bestAttribute][child])) {
                        childClassSet.add(set[example][setClassIndex]);
                        childSet.add(set[example]);
                    }
                }
                //empty child
                if (childClassSet.isEmpty()) {
                    System.out.println("empty child");
                    n.children[child] = new TreeNode(null, getModeClass(set, stringSet));
                } //pure child
                else if (childClassSet.size() == 1) {
                    int classNum = -1;
                    for (int c = 0; c < numOfClasses; c++) {
                        if (childClassSet.contains(strings[stringClassIndex][c])) {
                            classNum = c;
                            break;
                        }
                    }
                    
                    n.children[child] = new TreeNode(null, classNum);
                    System.out.println("pure - " + classNum + " " + child + " " + strings[attributeIndex][child]);
                } //impure child
                else {
                    //removes best attribute from subset
                    String[][] subSet = new String[childSet.size()][];
                    for (int j = 0; j < subSet.length; j++) { //for each example we want to remove the A'th attribute
                        String[] thisSet = childSet.get(j);
                        String[] tempSet = new String[thisSet.length - 1];
                        int tCount = 0;
                        for (int k = 0; k < stringSet.length; k++) {
                            if (k != bestAttribute) {
                                tempSet[tCount] = thisSet[k];
                                tCount++;
                            }
                        }
                        subSet[j] = tempSet;
                    }

                    //removes best attribute from string set
                    String[][] stringSubSet = new String[stringSet.length - 1][];
                    int count = 0;
                    for (int j = 0; j < stringSet.length; j++) {
                        if (j != bestAttribute) {
                            stringSubSet[count] = stringSet[j];
                            count++;
                        }
                    }
                    n.children[child] = new TreeNode(null, -1);
                    System.out.println("splitting at - " + strings[attributeIndex][child]);
                    split(n.children[child], subSet, stringSubSet);
                }
            }
        }
    }

    public boolean DEBUG = false;

     /**
     * Returns the final (class) column of a set
     */
    public String[] getClassValues(String[][] set) {
        String[] c = new String[set.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = set[i][set[0].length - 1];
        }
        return c;
    }

     /**
     * Converts attribute index of subset to index in original set
     */
    public int getAttributeIndex(String type) {
        for (int i = 0; i < data[0].length; i++) {
            if (data[0][i].equals(type)) {
                return i;
            }
        }
        return 0;
    }
    
     /**
     * Returns index of best attribute in a set or subset
     */
    public int getBestAttribute(String[][] set, String[][] stringSet) {
        double startingEntropy = entropy(getClassValues(set)); //starting entropy
        int bestIndex = 0;
        double bestGain = -1;

        for (int attribute = 0; attribute < stringSet.length - 1; attribute++) {
            double startSize = set.length;
            double gain = startingEntropy;
            for (int s = 0; s < stringSet[attribute].length; s++) { // e.g sunny, cloudy
                ArrayList<Integer> examplesIndex = new ArrayList<>();
                ArrayList<String> classList = new ArrayList();
                //gets the index of children in this subset
                for (int j = 0; j < set.length; j++) { //for each example
                    if (set[j][attribute].equals(stringSet[attribute][s])) { //check which strings it belongs to
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
            }
            if (gain > bestGain) {
                bestGain = gain;
                bestIndex = attribute;
            }
        }
        return bestIndex;
    }

     /**
     * Gets entropy of set
     */
    public double entropy(String[] setClassList) {
        double entropy = 0;
        if (setClassList.length != 0) {
            String[] classes = strings[attributes - 1];
            int[] classCounts = new int[classes.length];
            
            for (int example = 0; example < setClassList.length; example++) {
                for (int classNum = 0; classNum < classCounts.length; classNum++) {
                    if(setClassList[example].equals(classes[classNum]))
                        classCounts[classNum]++;
                }
            }

            double numOfAttributes = setClassList.length;
            for (int classNum = 0; classNum < classCounts.length; classNum++) {
                double xlog = xlogx(classCounts[classNum] / numOfAttributes);
                entropy = entropy - xlog;
            }
        }
        return entropy;
    }

     /**
     * Gets the most common class of an impure leaf node*
     */
    public int getModeClass(String[][] set, String[][] stringSet) {
        int setClassIndex = set[0].length - 1;
        int stringClassIndex = strings.length - 1;
        int numOfClasses = strings[stringClassIndex].length;
        int[] classesCount = new int[numOfClasses];

        for (int example = 1; example < set.length; example++) {
            for (int classNum = 0; classNum < classesCount.length; classNum++) {
                if (set[example][setClassIndex].equals(strings[stringClassIndex][classNum])) {
                    classesCount[classNum]++;
                    System.out.println("found a " + set[example][setClassIndex]);
                    break;
                }
            }
        }
        int bestIndex = -1;
        int most = -1;
        for (int i = 0; i < classesCount.length; i++) {
            if (classesCount[i] > most) {
                most = classesCount[i];
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public void postPrune(TreeNode dTree, String[][] dataset, String[][] strings) {
        
    }

    public void train(String[][] trainingData) {
        indexStrings(trainingData);
        split(decisionTree, trainingData, strings);
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
    public void classify(String[][] test) {
        testData = test;
        for (int example = 1; example < testData.length; example++) {
            TreeNode temp = new TreeNode(null, 0);
            temp = decisionTree;
            
            while (temp.children != null) {
                //get which child to follow
                int val = temp.value;
                for(int i=0;i<temp.children.length;i++){
                    String str = testData[example][val];
                    String str1 = strings[val][i];
                    System.out.println(" " + str1 + "-" + str);
                    if(str.equalsIgnoreCase(str1)){
                        System.out.println("match");
                        temp = temp.children[i];
                    }
                }
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

        String[][] trainingData = parseCSV("./src/aiid3/oneAttributeTrain.csv");
        String[][] testData = parseCSV("./src/aiid3/oneAttributeTest.csv");
        ID3 classifier = new ID3();
        classifier.train(trainingData);
        classifier.printTree();
//        for(int i=0;i<testData.length;i++){
//            for(int j=0;j<testData[0].length;j++){
//                System.out.println(testData[i][j]);
//            }
//        }
//        System.out.println(" ");
//                for(int i=0;i<testData.length;i++){
//            for(int j=0;j<testData[0].length;j++){
//                System.out.println(testData[i][j]);
//            }
//        }
        
        classifier.classify(testData);
//        if (args.length != 2) {
//            error("Expected 2 arguments: file names of training and test data");
//        }
//        String[][] trainingData = parseCSV(args[0]);
//        String[][] testData = parseCSV(args[1]);
//        ID3 classifier = new ID3();
//        classifier.train(trainingData);
//        classifier.printTree();
//        classifier.classify(testData);

    } // main()

} // class ID3
