import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HuffmanDecoder {
    private static final String OUTPUT_FILE_NAME = "output.dat";
    private static final String DECODER_TABLE_FILE_NAME = "dec_tab.txt";
    private static final String DECOMPRESSED_FILE_NAME = "decompress.txt";

    public static void main(String[] args) {
        //get the filePath for the decoder table
        String decoderTableFilePath = getDownloadFilePath(DECODER_TABLE_FILE_NAME);
        Map<Integer, String> decoderTable = readDecoderTableFromFile(decoderTableFilePath);

        //get the filePath for the "output.dat" file and the byte array saved in it
        String filePath = getDownloadFilePath(OUTPUT_FILE_NAME);
        byte[] byteArray = readByteArrayFromFile(filePath);

        //convert the array to a bitstring
        String bitString = convertToBitString(byteArray);

        //omit the redundancy
        String trimmedBitString = cutRedundancy(bitString);

        //decode the bitstring
        String decodedText = decodeBitString(trimmedBitString, decoderTable);

        //save the decoded message to the file "decompress.txt" in the download folder
        String decompressedFilePath = getDownloadFilePath(DECOMPRESSED_FILE_NAME);
        decompressedTextToFile(decodedText, decompressedFilePath);
    }

    //method to get the file path
    private static String getDownloadFilePath(String fileName) {
        String userHome = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        return userHome + separator + "Downloads" + separator + fileName;
    }

    //method to read the decoder table from the file
    private static Map<Integer, String> readDecoderTableFromFile(String filePath) {
        Map<Integer, String> decoderTable = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            String[] entries = line.split("-");
            for (String entry : entries) {
                String[] tokens = entry.split(":");
                if (tokens.length == 2) {
                    int asciiCode = Integer.parseInt(tokens[0].trim());
                    String code = tokens[1].trim();
                    decoderTable.put(asciiCode, code);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decoderTable;
    }

    //method to read the byte array from the file
    private static byte[] readByteArrayFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    //method to convert the byte array to bitstring
    private static String convertToBitString(byte[] byteArray) {
        StringBuilder bitStringBuilder = new StringBuilder();
        for (byte b : byteArray) {
            String byteString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            bitStringBuilder.append(byteString);
        }
        return bitStringBuilder.toString();
    }

    //method to cut the redundant 1 and its following 0s from the bitstring
    private static String cutRedundancy(String bitString) {
        int lastIndex = bitString.lastIndexOf('1');
        return bitString.substring(0, lastIndex);
    }

    //method to decode the bitstring
    private static String decodeBitString(String bitString, Map<Integer, String> decoderTable) {
        StringBuilder decodedText = new StringBuilder();
        StringBuilder currentCode = new StringBuilder();

        for (char c : bitString.toCharArray()) {
            currentCode.append(c);
            for (Map.Entry<Integer, String> entry : decoderTable.entrySet()) {
                int asciiCode = entry.getKey();
                String code = entry.getValue();
                if (code.equals(currentCode.toString())) {
                    decodedText.append((char) asciiCode);
                    currentCode.setLength(0); // Reset the current code
                    break;
                }
            }
        }

        return decodedText.toString();
    }

    //method to write the decoded text to a file
    private static void decompressedTextToFile(String text, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
