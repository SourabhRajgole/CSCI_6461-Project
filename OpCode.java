import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class OpCode{

    public static final String HLT = "00";
    public static final String LDR = "01";
    public static final String STR = "02";
    public static final String LDA = "03";
    public static final String AMR = "04";
    public static final String SMR = "05";
    public static final String AIR = "06";
    public static final String SIR = "07";
    public static final String JZ = "010";
    public static final String JNE = "011";
    public static final String JCC = "012";
    public static final String JMA = "013";
    public static final String JSR = "014";
    public static final String RFS = "015";
    public static final String SOB = "016";
    public static final String JGE = "017";
    public static final String TRAP = "030";
    public static final String SRC = "031";
    public static final String RRC = "032";
    public static final String FADD = "033";
    public static final String FSUB = "034";
    public static final String VADD = "035";
    public static final String VSUB = "036";
    public static final String CNVRT = "037";
    public static final String LDX = "041";
    public static final String STX = "042";
    public static final String LDFR = "050";
    public static final String STFR = "051";
    public static final String IN = "061";
    public static final String OUT = "062";
    public static final String CHK = "063";
    public static final String MLT = "070";
    public static final String DVD = "071";
    public static final String TRR = "072";
    public static final String AND = "073";
    public static final String ORR = "074";
    public static final String NOT = "075";

    // Precomputed map of opcodes
    private static final Map<String, String> opCodes = new HashMap<>();

    // Static block to initialize the opCodes map
    static {
        for (Field field : OpCode.class.getDeclaredFields()) {
            if (field.getType() == String.class &&
                java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
                try {
                    String name = field.getName();
                    String value = (String) field.get(null);
                    opCodes.put(name, value);
                } catch (IllegalAccessException e) {
                    System.err.println("Error accessing field: " + field.getName());
                }
            }
        }
    }

    public static Map<String, String> getOpCodes() {
        return new HashMap<>(opCodes); // Return a copy to ensure encapsulation
    }
}
