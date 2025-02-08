import java.util.ArrayList;
import java.util.Map;

public class Assembler {

    private final Map<String, String> opcodeMap;
    private final ArrayList<String[]> instructionList;
    private final ArrayList<String> listingFile;
    private final ArrayList<String> loaderFile;

    // Constructor initializes opcode map and reads input file
    public Assembler(String sourceFile) {
        this.opcodeMap = OpCode.getOpCodes();
        this.instructionList = FileHandling.readInput(sourceFile);
        this.listingFile = new ArrayList<>();
        this.loaderFile = new ArrayList<>();
    }

    // Main entry point
    public static void main(String[] args) {
        Assembler assembler = new Assembler("input.txt");
        assembler.processAssembly();
    }

    // Helper method to pad strings with leading zeros
    private String leftPad(String str, int length, char padChar) {
        StringBuilder sb = new StringBuilder();
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    // Converts instruction to octal
    private String convertInstruction(String[] instruction) {
        String register = "00", index = "00", indirectFlag = "0";
        String address = "00000";
        String opcode = opcodeMap.get(instruction[0]);

        if (opcode == null) {
            System.out.println("Error: Invalid opcode " + instruction[0]);
            return "Fail";
        }

        String binaryOpcode = leftPad(Integer.toBinaryString(Integer.parseInt(opcode, 8)), 6, '0');

        try {
            String[] params = instruction[1].split(",");
            
            // Enforce limits on parameter counts and validate first parameter range
            switch (instruction[0]) {
                case "LDX":
                case "STX":
                    if (params.length > 3) {
                        System.out.println("Error: " + instruction[0] + " allows at most 3 parameters.");
                        return "Fail";
                    }
                    int firstParamLDX_STX = Integer.parseInt(params[0]);
                    if (firstParamLDX_STX < 1 || firstParamLDX_STX > 3) {
                        System.out.println("Error: " + instruction[0] + " first parameter must be between 1-3.");
                        return "Fail";
                    }
                    break;
                case "LDR":
                case "LDA":
                case "STR":
                    if (params.length > 4) {  // Max 4 parameters allowed
                        System.out.println("Error: " + instruction[0] + " allows at most 4 parameters.");
                        return "Fail";
                    }
                    int firstParamLDR_LDA_STR = Integer.parseInt(params[0]);
                    if (firstParamLDR_LDA_STR < 0 || firstParamLDR_LDA_STR > 3) {
                        System.out.println("Error: " + instruction[0] + " first parameter must be between 0-3.");
                        return "Fail";
                    }
                    break;
            }

            // Process instructions based on opcode
            switch (instruction[0]) {
                case "LDR": case "LDA": case "STR": case "JCC": case "SOB":
                case "AMR": case "SMR": case "JGE":
                    register = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    index = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 2, '0');
                    if(Integer.parseInt(params[2])>31) {
                    	System.out.println("address out of bound");
                    	return "fail";
                    }
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[2])), 5, '0');
                    indirectFlag = (params.length == 4 && params[3].equals("1")) ? "1" : "0";
                    break;
                case "SIR": case "IN": case "OUT": case "CHK":
                    register = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 5, '0');
                    break;
                case "NOT":
                    register = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    break;
                case "RFS":
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 5, '0');
                    break;
                case "SRC": case "RRC":
                    register = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    index = params[2] + params[3];
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 5, '0');
                    break;
                case "LDX": case "STX": case "DVD": case "MLT": case "JZ":
                case "JNE": case "ORR": case "JMA": case "AND": case "JSR": case "TRR":
                    register = "00";
                    index = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    if(Integer.parseInt(params[1])>31) {
                    	System.out.println("address out of bound");
                    	return "fail";
                    }
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 5, '0');
                    if(params.length==3 && params[2].equals("1")) {
                    	indirectFlag ="1";
                    }
                    else if(params.length==3 && params[2].equals("0")) {
                    	indirectFlag = "0";
                    }
                    else if(params.length==3){
                    	System.out.println("Error indirectFlag cannot be more than 1 bit");
                    	return "Error";
                    }
//                    indirectFlag = (params.length == 3 && params[2].equals("1")) ? "1" : "0";
                    break;
                default:
                    return "Fail";
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error parsing instruction: " + String.join(" ", instruction));
            return "Fail";
        }

        //        	System.out.println(indirectFlag);
        return binaryOpcode + register + index + indirectFlag + address;
    }


    // Adds a line to the loader file
    private void appendToLoaderFile(String line) {
        loaderFile.add(line);
    }

    // Adds a line to the listing file
    private void appendToListingFile(String columns, String[] input) {
        String formattedInstruction = String.join(" ", input);
        listingFile.add(columns + "\t" + formattedInstruction);
    }

    // Processes and assembles the code
    public void processAssembly() {
        int programCounter = 0;
        for (String[] input : instructionList) {
            String binaryCode;
            switch (input[0]) {
                case "LOC":
                    programCounter = Integer.parseInt(input[1]);
                    appendToListingFile("      " + "\t" + "      ", input);
                    continue;
                case "Data":
                    if (input[1].equals("End")) {
                        binaryCode = "10000000000";
                    } else {
                        binaryCode = leftPad(Integer.toBinaryString(Integer.parseInt(input[1])), 16, '0');
                    }
                    break;
                case "End:":
                    binaryCode = "0";
                    break;
                default:
                    binaryCode = convertInstruction(input);
                    break;
            }

            // Convert PC and instruction to octal
            String pcOctal = leftPad(Integer.toOctalString(programCounter), 6, '0');
            String instructionOctal = leftPad(Integer.toOctalString(Integer.parseInt(binaryCode, 2)), 6, '0');

            // Write to files
            appendToLoaderFile(pcOctal + " " + instructionOctal);
            appendToListingFile(pcOctal + " " + instructionOctal, input);

            // Increment PC
            programCounter++;
        }

        // Write final output to files
        FileHandling.writeToFile("Load.txt", loaderFile);
        FileHandling.writeToFile("List.txt", listingFile);
        System.out.println("Listing and Loader Files generated successfully.");
    }
}