import java.util.ArrayList;
import java.util.Map;

public class Assembler {

    private final Map<String, String> OpCodeMapping;// Mapping of OpCodes to their respective binary representations
    private final ArrayList<String[]> InstructionSet; // List to store parsed assembly instructions
    private final ArrayList<String> listingFileOutput;// Output list for the listing file
    private final ArrayList<String> LoadFileOutput; // Output list for the load file

    // Constructor initializes opcode map and reads input file
    public Assembler(String sourceFile) {
        this.OpCodeMapping = Opcode.getOpCodes();
        this.InstructionSet = FileManager.readInput(sourceFile);
        this.listingFileOutput = new ArrayList<>();
        this.LoadFileOutput = new ArrayList<>();
    }

    // Main method: Entry point of the assembler
    public static void main(String[] args) {
        Assembler assembler = new Assembler("input.txt");
        assembler.joiningprocess();
    }

    // Pads the input string with the given character until it reaches the specified length
    private String leftPad(String String, int length, char PadCharacter) {
        StringBuilder sb = new StringBuilder();
        for (int i = String.length(); i < length; i++) {
            sb.append(PadCharacter);
        }
        sb.append(String);
        return sb.toString();
    }

    // Converts assembly instruction into its corresponding binary representation
    private String ProcessInstruction(String[] instruction) {
        String GPR = "00", index = "00", Indirectaddressing = "0";
        String address = "00000";
        String Opcode = OpCodeMapping.get(instruction[0]);

     // Validate opcode
        if (Opcode == null) {
            System.out.println("Error: Provided OpCode is Invalid!! " + instruction[0]);
            return "Fail";
        }

     // Convert opcode to binary
        String binaryOpcode = leftPad(Integer.toBinaryString(Integer.parseInt(Opcode, 8)), 6, '0');

        try {
            String[] params = instruction[1].split(",");
            
         // Validate parameters based on the instruction type
            switch (instruction[0]) {
                case "LDX":
                case "STX":
                    if (params.length > 3) {
                        System.out.println("Error: For Opcode " + instruction[0] + " only 3 Arguments are Allowed!!");
                        return "Fail";
                    }
                    int firstParamLDX_STX = Integer.parseInt(params[0]);
                    if (firstParamLDX_STX < 1 || firstParamLDX_STX > 3) {
                        System.out.println("Error: " + instruction[0] + " Index Register value must be between 1-3.");
                        return "Fail";
                    }
                    break;
                case "LDR":
                case "LDA":
                case "STR":
                    if (params.length > 4) {  // Max 4 parameters allowed
                        System.out.println("Error: For Opcode " + instruction[0] + " only 4 Arguments are Allowed!!");
                        return "Fail";
                    }
                    int firstParamLDR_LDA_STR = Integer.parseInt(params[0]);
                    if (firstParamLDR_LDA_STR < 0 || firstParamLDR_LDA_STR > 3) {
                        System.out.println("Error: " + instruction[0] + " GPR Value must be between 0-3.");
                        return "Fail";
                    }
                    break;
            }

            // Process instructions based on Opcode
            switch (instruction[0]) {
                
                case "SIR": case "IN": case "OUT": case "CHK":
                    GPR = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 5, '0');
                    break;
                case "NOT":
                    GPR = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    break;
                case "RFS":
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 5, '0');
                    break;
                case "SRC": case "RRC":
                    GPR = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    index = params[2] + params[3];
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 5, '0');
                    break;
                case "LDR": case "LDA": case "STR": case "JCC": 
                case "AMR": case "SMR":
                    GPR = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    index = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 2, '0');
                    if(Integer.parseInt(params[2])>31) {
                    	System.out.println("Address Argument is Invalid : Beyond Limit!!");
                    	return "fail";
                    }
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[2])), 5, '0');
                    Indirectaddressing = (params.length == 4 && params[3].equals("1")) ? "1" : "0";
                    break;
                case "LDX": case "STX": case "DVD": case "MLT": case "ORR": case "JMA": case "AND": case "JSR": case "TRR":
                    GPR = "00";
                    index = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                    if(Integer.parseInt(params[1])>31) {
                    	System.out.println("Address Argument is Invalid : Beyond Limit!!");
                    	return "fail";
                    }
                    address = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 5, '0');
                    if(params.length==3 && params[2].equals("1")) {
                    	Indirectaddressing ="1";
                    }
                    else if(params.length==3 && params[2].equals("0")) {
                    	Indirectaddressing = "0";
                    }
                    else if(params	.length==3){
                    	System.out.println("Error Indirectaddressing cannot be more than 1 bit");
                    	return "Error";
                    }
//                    Indirectaddressing = (params.length == 3 && params[2].equals("1")) ? "1" : "0";
                    break;
                case "JZ":case "JNE": case "JGE":case "SOB":
                	 GPR = leftPad(Integer.toBinaryString(Integer.parseInt(params[0])), 2, '0');
                	 index = leftPad(Integer.toBinaryString(Integer.parseInt(params[1])), 2, '0');
                	  if(Integer.parseInt(params[1])>31) {
                      	System.out.println("Address Argument is Invalid : Beyond Limit!!");
                      	return "fail";
                      }
                	 address = leftPad(Integer.toBinaryString(Integer.parseInt(params[2])),5,'0');
                	 if(params.length==4 && params[3].equals("1")) {
                     	Indirectaddressing ="1";
                     }
                     else if(params.length==4 && params[3].equals("0")) {
                     	Indirectaddressing = "0";
                     }
                     else if(params.length==4){
                     	System.out.println("Error Indirectaddressing cannot be more than 1 bit");
                     	return "Error";
                     }
                	break;
                default:
                    return "Fail";
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Error!!: parsing instruction:" + String.join(" ", instruction));
            return "Fail";
        }

        //        	System.out.println(Indirectaddressing);
        return binaryOpcode + GPR + index + Indirectaddressing + address;
    }


    // Adds a line to the loader file output
    private void appendToLoadFileOutput(String line) {
        LoadFileOutput.add(line);
    }

    // Adds a line to the listing file output
    private void appendTolistingFileOutput(String columns, String[] input) {
        String formattedInstruction = String.join(" ", input);
        listingFileOutput.add(columns + "\t" + formattedInstruction);
    }

    // Processes and assembles the code into machine-readable format
    public void joiningprocess() {
        int programCounter = 0;
        for (String[] input : InstructionSet) {
            String binaryCode;
            switch (input[0]) {
                case "LOC":
                    programCounter = Integer.parseInt(input[1]);
                    appendTolistingFileOutput("      " + "\t" + "      ", input);
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
                    binaryCode = ProcessInstruction(input);
                    break;
            }

            // Convert PC and instruction to octal
            String pcOctal = leftPad(Integer.toOctalString(programCounter), 6, '0');
            String instructionOctal = leftPad(Integer.toOctalString(Integer.parseInt(binaryCode, 2)), 6, '0');

            // Write to files
            appendToLoadFileOutput(pcOctal + " " + instructionOctal);
            appendTolistingFileOutput(pcOctal + " " + instructionOctal, input);

            // Increment PC
            programCounter++;
        }

       // Save final output to files
        FileManager.writeToFile("Load_Output.txt", LoadFileOutput);
        FileManager.writeToFile("Listing_Output.txt", listingFileOutput);
        System.out.println("Assembely Language Translation Completed Succesfully, See The Output in Listing and Load Files.");
    }
}
