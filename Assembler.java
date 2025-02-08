import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Assembler{

    // HashMap that stores details about each operation like argument and argument type
    private static final Map<String, OperationMetadata> operationmap = new HashMap<>();

    enum ArgumentType {
        MANDATORY,    
        OPTIONAL,    
        UNUSED      
    }
    
    // Initialize  HashMap to store operation metadata
    static {

        // Miscellaneous Operations
        operationmap.put("HLT", new OperationMetadata(Arrays.asList(new ArgumentDefinition(10, ArgumentType.UNUSED))));
        operationmap.put("TRAP", new OperationMetadata(Arrays.asList(new ArgumentDefinition(5, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Load/Store Operations
        operationmap.put("LDR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("STR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("LDA", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("LDX", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("STX", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Jump Operations
        operationmap.put("SETCCE", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(8, ArgumentType.UNUSED))));
        operationmap.put("JZ", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("JNE", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.OPTIONAL),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("JCC", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("JMA", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("JSR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("RFS", new OperationMetadata(Arrays.asList(new ArgumentDefinition(5, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("SOB", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("JGE", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Arithmetic/Logical Operations
        operationmap.put("AMR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("SMR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("AIR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(3, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("SIR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(3, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Arithmetic/Logical Operations
        operationmap.put("MLT", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(6, ArgumentType.UNUSED))));
        operationmap.put("DVD", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(6, ArgumentType.UNUSED))));
        operationmap.put("TRR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(6, ArgumentType.UNUSED))));
        operationmap.put("AND", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(6, ArgumentType.UNUSED))));
        operationmap.put("ORR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(6, ArgumentType.UNUSED))));
        operationmap.put("NOT", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(8, ArgumentType.UNUSED))));

        // Shift/Rotate Operations
        operationmap.put("SRC", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(4, ArgumentType.MANDATORY))));
        operationmap.put("RRC", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.UNUSED),new ArgumentDefinition(4, ArgumentType.MANDATORY))));

        // I/O Operations
        operationmap.put("IN", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(3, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("OUT", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(3, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("CHK", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(3, ArgumentType.UNUSED),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Floating Point/Vector Operations
        operationmap.put("FADD", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("FSUB", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Floating Point/Vector Operations
        operationmap.put("VADD", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("VSUB", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));

        // Floating Point/Vector Operations
        operationmap.put("CNVRT", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("LDFR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
        operationmap.put("STFR", new OperationMetadata(Arrays.asList(new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(2, ArgumentType.MANDATORY),new ArgumentDefinition(1, ArgumentType.OPTIONAL),new ArgumentDefinition(5, ArgumentType.MANDATORY))));
    }

    // HashMap to store the OpCode-To-Octal
    private static final Map<String, String> opcodeToOctalMap = new HashMap<>();
    static {

        // Placeholder Operations
        opcodeToOctalMap.put("LOC", "77");
        opcodeToOctalMap.put("DATA", "77");

        // Miscellaneous Operations
        opcodeToOctalMap.put("HLT", "00");
        opcodeToOctalMap.put("TRAP", "30");

        // Load/Store Operations
        opcodeToOctalMap.put("LDR", "01");
        opcodeToOctalMap.put("STR", "02");
        opcodeToOctalMap.put("LDA", "03");
        opcodeToOctalMap.put("LDX", "41");
        opcodeToOctalMap.put("STX", "42");

        // Transfer Operations
        opcodeToOctalMap.put("SETCCE", "44");
        opcodeToOctalMap.put("JZ", "10");
        opcodeToOctalMap.put("JNE", "11");
        opcodeToOctalMap.put("JCC", "12");
        opcodeToOctalMap.put("JMA", "13");
        opcodeToOctalMap.put("JSR", "14");
        opcodeToOctalMap.put("RFS", "15");
        opcodeToOctalMap.put("SOB", "16");
        opcodeToOctalMap.put("JGE", "17");

        // Arithmetic/Logical Operations
        opcodeToOctalMap.put("AMR", "04");
        opcodeToOctalMap.put("SMR", "05");
        opcodeToOctalMap.put("AIR", "06");
        opcodeToOctalMap.put("SIR", "07");

        // Arithmetic/Logical Operations
        opcodeToOctalMap.put("MLT", "70");
        opcodeToOctalMap.put("DVD", "71");
        opcodeToOctalMap.put("TRR", "72");
        opcodeToOctalMap.put("AND", "73");
        opcodeToOctalMap.put("ORR", "74");
        opcodeToOctalMap.put("NOT", "75");

        // Shift/Rotate Operations
        opcodeToOctalMap.put("SRC", "30");
        opcodeToOctalMap.put("RRC", "31");

        // I/O Operations
        opcodeToOctalMap.put("IN", "61");
        opcodeToOctalMap.put("OUT", "62");
        opcodeToOctalMap.put("CHK", "63");

        // Floating Point/Vector Operations
        opcodeToOctalMap.put("FADD", "33");
        opcodeToOctalMap.put("FSUB", "34");

        // Floating Point/Vector Operations
        opcodeToOctalMap.put("VADD", "35");
        opcodeToOctalMap.put("VSUB", "36");

        // Floating Point/Vector Operations
        opcodeToOctalMap.put("CNVRT", "37");
        opcodeToOctalMap.put("LDFR", "50");
        opcodeToOctalMap.put("STFR", "51");
    }


    // Function that converts octal numbers to binary
    private static String decimalToOctal(String dec) {
        int decimal = Integer.parseInt(dec); 
        return Integer.toOctalString(decimal); 
    }

    private static String processInstruction(String opcode, String[] args, OperationMetadata metadata) {
        int instruction = 0;
        
        // Get opcode and shift to most significant 6 bits
        instruction = Integer.parseInt(opcodeToOctalMap.get(opcode), 8) << 10;
        // System.out.println(instruction + " ----");
        // for(String s : args){
        //    System.out.println(s);
        // }
        
        if (opcode.equals("SRC") || opcode.equals("RRC")) {
            // Special handling for SRC/RRC instructions
            instruction |= (Integer.parseInt(args[1]) & 0x3) << 8;  // R
            instruction |= (Integer.parseInt(args[2]) & 0x1) << 7;  // L/R
            instruction |= (Integer.parseInt(args[3]) & 0x1) << 6;  // A/L
            instruction |= Integer.parseInt(args[4]) & 0xF;         // Count
        } else if (opcode.equals("LDX") || opcode.equals("STX")) {
            // Handle index register operations
            // System.out.println("lax");
            
                instruction |= ((Integer.parseInt(args[1]) & 0x3) << 6);  // IXR
                instruction |= Integer.parseInt(args[2]) & 0x1F;   // Address
            
            if (args.length > 3) {
                instruction |= (Integer.parseInt(args[3]) & 0x1) << 5;      //Imm  
            }
        } else if (opcode.equals("LDR") || opcode.equals("STR") || opcode.equals("LDA")) {
            // Handle memory reference instructions
            if (args.length > 1) {
                instruction |= (Integer.parseInt(args[1]) & 0x3) << 8;  // R
            }
            if (args.length > 2) {
                instruction |= (Integer.parseInt(args[2]) & 0x3) << 6;  // IX
            }
            if (args.length > 4) {
                // For format: LDR R,IX,Address,I
                instruction |= (Integer.parseInt(args[3]) & 0x1F);        // Address
                instruction |= (Integer.parseInt(args[4]) & 0x1) << 5;    // I
            } else if (args.length > 3) {
                // For format: LDR R,IX,Address
                instruction |= Integer.parseInt(args[3]) & 0x1F;          // Address
            }
        } else if (opcode.equals("JZ") || opcode.equals("JNE") || opcode.equals("JCC") || 
                  opcode.equals("JMA") || opcode.equals("JSR")) {
            // Handle jump instructions
            if (args.length > 1) {
                instruction |= (Integer.parseInt(args[1]) & 0x3) << 8;  // R
            }
            if (args.length > 2) {
                instruction |= (Integer.parseInt(args[2]) & 0x3) << 6;  // IX
            }
            if (args.length > 3) {
                instruction |= Integer.parseInt(args[3]) & 0x1F;        // Address
            }
        } else {
            // Handle other instructions
            int argIndex = 1;
            for (ArgumentDefinition argDef : metadata.getArgumentDefinitions()) {
                if (argDef.type != ArgumentType.UNUSED && argIndex < args.length) {
                    String arg = args[argIndex++].replace(",", "");
                    int value = Integer.parseInt(arg);
                    int mask = (1 << argDef.bits) - 1;
                    instruction |= (value & mask) << (10 - argIndex * 2);
                }
            }
        }
        
        return String.format("%06o", instruction);
    }   
    static class OperationMetadata {
        private final List<ArgumentDefinition> argumentDefinitions;

        public OperationMetadata(List<ArgumentDefinition> argumentDefinitions) {
            this.argumentDefinitions = argumentDefinitions;
        }

        public List<ArgumentDefinition> getArgumentDefinitions() {
            return argumentDefinitions;
        }
    }

    static class ArgumentDefinition {
        int bits;
        ArgumentType type;

        public ArgumentDefinition(int bits, ArgumentType type) {
            this.bits = bits;
            this.type = type;
        }
    }

    // Function allows us to increment the current location of memory
    private static void incrementCurrentLocation() {
        int decimalLocation = Integer.parseInt(currentLocation, 8); 
        // Convert octal to decimal
        decimalLocation++; // Increment the location
        currentLocation = Integer.toOctalString(decimalLocation); 
        // Convert back to octal
        currentLocation = String.format("%6s", currentLocation).replace(' ', '0'); 
        // Pad with zeros if necessary
    }

    public static String currentLocation = ""; 
    
    public static void main(String[] args) {
        // Variables that hold our input and output file locations
        String inputFilename = "input.txt";
        String outputFilename = "load_output.txt";
        String listingFilename = "listing_output.txt"; // Added listing file
        List<String> outputLines = new ArrayList<>();  
        List<String> listingLines = new ArrayList<>(); // Added for listing
    
        try (Scanner scanner = new Scanner(new File(inputFilename));
             PrintWriter writer = new PrintWriter(new File(outputFilename));
             PrintWriter listingWriter = new PrintWriter(new File(listingFilename))) { // Added listing writer
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String originalLine = line; 
                
                String noComment = line.split(";")[0].trim();
                if (noComment.isEmpty()) {
                    listingLines.add(originalLine);
                    continue;
                }

                // Split the line into words and handle commas
                String[] words = noComment.split("\\s+");
                
                
                // System.out.println(originalLine);
                List<String> finalWords = new ArrayList<>();
                for (String word : words) {
                    if (word.contains(",")) {
                        finalWords.addAll(Arrays.asList(word.split(",")));
                    } else {
                        finalWords.add(word);
                    }
                }
                String[] finalArray = finalWords.toArray(new String[0]);
                
                if (finalArray.length > 0) {
                    String opcode = finalArray[0].toUpperCase();
                    String outputLine = "";
                  
                    if (opcode.equals("LOC")) {
                        currentLocation = String.format("%6s", decimalToOctal(finalArray[1])).replace(' ', '0');
                        listingLines.add(String.format("%-6s          %s", "", originalLine));
                    } else {
                        if (opcode.equals("DATA")) {
                            String dataValue = (finalArray[1].toUpperCase().equals("END")) ? "1024" : finalArray[1];
                            dataValue = String.format("%6s", decimalToOctal(dataValue)).replace(' ', '0');
                            outputLine = String.format("%s  %s", currentLocation, dataValue);
                            outputLines.add(outputLine);  // Add to output lines
                            System.out.println(outputLine);  // Print to terminal
                            listingLines.add(outputLine + "  " + originalLine);
                        } else if (opcodeToOctalMap.containsKey(opcode)) {
                            String instructionOctal = processInstruction(opcode, finalArray, operationmap.get(opcode));
                            outputLine = String.format("%s  %s", currentLocation, instructionOctal);
                            outputLines.add(outputLine);  // Add to output lines
                            System.out.println(outputLine);  // Print to terminal
                            listingLines.add(outputLine + "  " + originalLine);
                        } else {
                            System.err.println("Unknown opcode: " + opcode);
                            listingLines.add(currentLocation + "  ERROR: Unknown opcode  " + originalLine);
                        }
                        incrementCurrentLocation();
                    }
                } else {
                    listingLines.add(originalLine);
                }
            }
            
            // Write output file
            for (String line : outputLines) {
                writer.println(line);
            }
            
            // Write listing file
            for (int i = 0; i < listingLines.size(); i++) {
              listingWriter.println(listingLines.get(i));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
