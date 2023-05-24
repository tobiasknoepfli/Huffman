import java.io.*;
import java.util.*;

public class HuffmanEncoder {
    private static final String FILE_NAME = "text.txt";
    private static final String DECODE_TABLE_FILE_NAME = "dec_tab.txt";
    private static final String OUTPUT_FILE_NAME = "output.dat";


    public static void main(String[] args) {
        //get the file path of the file "text.txt" and read the text from the file
        String filePath = getDownloadFilePath(FILE_NAME);
        String text = readFile(filePath);

        //create a map of all the ascii characters in the text and their relative frequency
        Map<Character, Integer> frequencyMap = createFrequencyMap(text);

        //build a huffman tree and the code table from the previously created map
        Node root = buildHuffmanTree(frequencyMap);
        Map<Character, String> huffmanCodes = generateCodes(root);

        //save the decode table to the download folder
        String decodeTableFilePath = getDownloadFilePath(DECODE_TABLE_FILE_NAME);
        saveDecodeTableToFile(huffmanCodes, decodeTableFilePath);

        //convert the text to a bitstring
        String bitString = convertToBitString(text, huffmanCodes);

        //add redundancy to the bitstring
        bitString = addRedundancy(bitString);

        //create a byte array from the bit string
        byte[] byteArray = createByteArray(bitString);

        //save the byte array to the output file "output.dat"
        String outputFilePath = getDownloadFilePath(OUTPUT_FILE_NAME);
        saveByteArrayToFile(byteArray, outputFilePath);
    }

    //method to get the path of the user's download folder
    private static String getDownloadFilePath(String fileName) {
        String userHome = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        return userHome + separator + "Downloads" + separator + fileName;
    }

    //method to read the original text from the file
    private static String readFile(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(new FileReader(filePath))) {
            while (scanner.hasNextLine()) {
                contentBuilder.append(scanner.nextLine()).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    //method to create the frequency map
    private static Map<Character, Integer> createFrequencyMap(String text) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            if (c < 128) {
                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
            }
        }
        return frequencyMap;
    }

    //method to build a huffman tree from the map (algorithm found on the internet)
    private static Node buildHuffmanTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.frequency));

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node('\0', left.frequency + right.frequency, left, right);
            pq.add(parent);
        }

        return pq.poll();
    }

    //method to generate the codes
    private static Map<Character, String> generateCodes(Node root) {
        Map<Character, String> huffmanCodes = new HashMap<>();
        generateHuffmanCodesRecursive(root, "", huffmanCodes);
        return huffmanCodes;
    }

    //recursive method to generate the Codes (with help from the internet)
    private static void generateHuffmanCodesRecursive(Node node, String code, Map<Character, String> huffmanCodes) {
        if (node == null) {
            return;
        }

        if (node.isLeafInTree()) {
            huffmanCodes.put(node.character, code);
        }

        generateHuffmanCodesRecursive(node.left, code + "0", huffmanCodes);
        generateHuffmanCodesRecursive(node.right, code + "1", huffmanCodes);
    }

    //method to write the decode-table to a file
    private static void saveDecodeTableToFile(Map<Character, String> huffmanCodes, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                char character = entry.getKey();
                String code = entry.getValue();
                stringBuilder.append((int) character).append(":").append(code).append("-");
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 1); // Remove the last "-"
            }
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //method to convert text to bitstring
    private static String convertToBitString(String text, Map<Character, String> huffmanCodes) {
        StringBuilder bitStringBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String code = huffmanCodes.get(c);
            if (code != null) {
                bitStringBuilder.append(code);
            }
        }
        return bitStringBuilder.toString();
    }

    //method to add redundancy
    private static String addRedundancy(String bitString) {
        int remainder = bitString.length() % 8;
        int paddingLength = remainder == 0 ? 0 : 8 - remainder;
        StringBuilder paddedBitString = new StringBuilder(bitString);
        paddedBitString.append("1");
        for (int i = 0; i < paddingLength; i++) {
            paddedBitString.append("0");
        }
        return paddedBitString.toString();
    }

    //method to create a byte array
    private static byte[] createByteArray(String bitString) {
        int bitStringLength = bitString.length();
        int byteLength = bitStringLength / 8;
        byte[] byteArray = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            String byteString = bitString.substring(i * 8, (i * 8) + 8);
            byte b = (byte) Integer.parseInt(byteString, 2);
            byteArray[i] = b;
        }
        return byteArray;
    }

    //method to save the byte array to a file
    private static void saveByteArrayToFile(byte[] byteArray, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
