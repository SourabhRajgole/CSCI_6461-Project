
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

public class MachineSimulator {

    private static final int MEMORY_SIZE = 2048;
    private int[] memory = new int[MEMORY_SIZE];
    private static final int CACHE_SIZE = 16;
    // Points to the next cache line to replace
    private LinkedHashMap<Integer, Integer> cache = new LinkedHashMap<Integer, Integer>(CACHE_SIZE, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
            return size() > CACHE_SIZE;  // Keep the cache size limited to CACHE_SIZE
        }
    };
    private Set<Integer> seenInstructions = new HashSet<>();
    private int[] GPR = new int[4];  // General Purpose Registers R0-R3
    private int[] IXR = new int[3];  // Index Registers
    private int PC, MAR, MBR, IR, CC, MFR; // Other Registers
    private int userInputAddress = 0006;
    private static final int SENTENCE_START_ADDR = 0100;  // Starting address for sentences
    private static final int SENTENCE_COUNT = 6;          // Number of sentences expected
    private static final int USER_INPUT_ADDR = 0200;      // Address for storing user input word
    private List<String> sentences = new ArrayList<>();   // Store sentences for later use
    private JTextField[] gprFields = new JTextField[4];
    private JTextField[] ixrFields = new JTextField[3];
    private JTextField pcField, marField, mbrField, irField, ccField, mfrField, octalInputField, binaryField, consoleInputField;

    private static final int BOOT_START_ADDR = 010;  // Octal 10
    private static final int PROGRAM_START_ADDR = 020;  // Octal 10 + 10 = 020

    private JTextArea consoleOutput, cacheContent, printerArea;
    private JButton runButton, stepButton, haltButton, iplButton, loadButton, storeButton, loadPlusButton, storePlusButton;

    // Declare the frame variable
    private JFrame frame;

    public MachineSimulator() {
        initializeMemory();
        createUI();
    }

    // Initialize memory to zeros
    private void initializeMemory() {
        Arrays.fill(memory, 0);
    }

    // Create UI and buttons
    private void createUI() {
        // Initialize the frame
        frame = new JFrame("CSCI 6461 Machine Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        frame.getContentPane().setBackground(new Color(192, 192, 192));

        // Create the top panel to hold the title and program file panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Create and set up the title label
        JLabel titleLabel = new JLabel("CSCI Machine Simulator");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // Program file panel remains at the top but now is placed below the title label
        JTextField programFileField = new JTextField(20);
        JPanel programFilePanel = new JPanel(new FlowLayout());
        programFilePanel.setOpaque(false);
        programFilePanel.add(new JLabel("Program File"));
        programFilePanel.add(programFileField);
        topPanel.add(programFilePanel, BorderLayout.SOUTH);

        // Create the center panel for registers and other controls
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel gprPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        gprPanel.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            gprFields[i] = new JTextField("0", 5);
            gprPanel.add(new JLabel("GPR " + i));
            gprPanel.add(gprFields[i]);
        }

        JPanel ixrPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        ixrPanel.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            ixrFields[i] = new JTextField("0", 5);
            ixrPanel.add(new JLabel("IXR " + i));
            ixrPanel.add(ixrFields[i]);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        centerPanel.add(gprPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(ixrPanel, gbc);

        JPanel registerPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        registerPanel.setOpaque(false);
        pcField = new JTextField("0", 5);
        marField = new JTextField("0", 5);
        mbrField = new JTextField("0", 5);
        irField = new JTextField("0", 5);
        ccField = new JTextField("0", 5);
        mfrField = new JTextField("0", 5);

        registerPanel.add(new JLabel("PC"));
        registerPanel.add(pcField);
        registerPanel.add(new JLabel("MAR"));
        registerPanel.add(marField);
        registerPanel.add(new JLabel("MBR"));
        registerPanel.add(mbrField);
        registerPanel.add(new JLabel("IR"));
        registerPanel.add(irField);
        registerPanel.add(new JLabel("CC"));
        registerPanel.add(ccField);
        registerPanel.add(new JLabel("MFR"));
        registerPanel.add(mfrField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        centerPanel.add(registerPanel, gbc);

        JPanel cachePrinterPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        cachePrinterPanel.setOpaque(false);
        cacheContent = new JTextArea(4, 10);
        cacheContent.setMargin(new Insets(5, 5, 10, 5));
        
        cacheContent.setEditable(false);

        printerArea = new JTextArea(7, 20);
        printerArea.setMargin(new Insets(5, 5, 10, 5));
        printerArea.setEditable(false);

        cachePrinterPanel.add(new JLabel("Cache Content"));
        cachePrinterPanel.add(new JScrollPane(cacheContent));
        cachePrinterPanel.add(new JLabel("Printer"));
        cachePrinterPanel.add(new JScrollPane(printerArea));

        binaryField = new JTextField(16);
        binaryField.setEditable(false);
        binaryField.setBackground(Color.WHITE);

        octalInputField = new JTextField("0", 5);

        JPanel binaryOctalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        binaryOctalPanel.setOpaque(false);
        binaryOctalPanel.add(new JLabel("BINARY"));
        binaryOctalPanel.add(binaryField);
        binaryOctalPanel.add(new JLabel("OCTAL INPUT"));
        binaryOctalPanel.add(octalInputField);

        consoleInputField = new JTextField(20);
        JPanel consoleInputPanel = new JPanel(new FlowLayout());
        consoleInputPanel.setOpaque(false);
        consoleInputPanel.add(new JLabel("Console Input"));
        consoleInputPanel.add(consoleInputField);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        centerPanel.add(binaryOctalPanel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerPanel.add(cachePrinterPanel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        centerPanel.add(consoleInputPanel, gbc);

        consoleOutput = new JTextArea(10, 40);
        consoleOutput.setEditable(false);
        JScrollPane consoleScrollPane = new JScrollPane(consoleOutput);
        frame.add(consoleScrollPane, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 5, 2));
        buttonPanel.setOpaque(false);

        iplButton = new JButton("IPL");
        iplButton.addActionListener(new IPLActionListener());

        loadButton = new JButton("Load");
        loadButton.addActionListener(new LoadActionListener());

        storeButton = new JButton("Store");
        storeButton.addActionListener(new StoreActionListener());

        loadPlusButton = new JButton("Load+");
        storePlusButton = new JButton("Store+");

        runButton = new JButton("Run");
        runButton.addActionListener(new RunActionListener());
        stepButton = new JButton("Step");
        stepButton.addActionListener(new StepActionListener());

        haltButton = new JButton("Halt");
        haltButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printerArea.append("Program halted manually by clicking halt button");
                stopProgram();
            }
        });

        Color buttonColor = new Color(70, 130, 180);
        Font buttonFont = new Font("Arial", Font.BOLD, 12);
        JButton[] buttons = {iplButton, loadButton, storeButton, loadPlusButton, storePlusButton, runButton, stepButton, haltButton};

        for (JButton button : buttons) {
            if (button == iplButton) {
                button.setBackground(new Color(255, 0, 0));
                buttonPanel.add(storePlusButton);
                continue;
            }
            button.setBackground(buttonColor);
            button.setForeground(Color.WHITE);
            button.setFont(buttonFont);
        }

        buttonPanel.add(loadButton);
        buttonPanel.add(storeButton);
        buttonPanel.add(loadPlusButton);
        buttonPanel.add(storePlusButton);
        buttonPanel.add(runButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(haltButton);
        buttonPanel.add(iplButton);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        // Instead of adding programFilePanel directly, we add the topPanel containing the title and program file panel.
        frame.add(topPanel, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    // IPL button action listener for loading the boot program
    private class IPLActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                java.io.File romFile = fileChooser.getSelectedFile();
                try {
                    loadROMFile(romFile);  // Load the ROM file into memory
                    printerArea.append("ROM file loaded into memory successfully.\n");

                    // Automatically set PC to the address of the first instruction
                    setPCToFirstInstruction();
                    printerArea.append("PC set to the address of the first instruction.\n");

                    displayLoadedROMContents();  // Display the file contents in the printer area
                } catch (IOException ex) {
                    printerArea.append("Error loading ROM file: " + ex.getMessage() + "\n");
                }
            } else {
                printerArea.append("ROM file loading cancelled.\n");
            }
        }
    }

    // Load the selected ROM file into memory and execute the first instruction
    private void loadROMFile(java.io.File romFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(romFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");  // Split into address and instruction parts
            if (parts.length == 2) {
                try {
                    // Parse both the address and instruction as octal values
                    int address = Integer.parseInt(parts[0], 8);      // Address in octal
                    int instruction = Integer.parseInt(parts[1], 8);  // Instruction in octal

                    // Store the instruction in memory at the specified address
                    if (address >= 0 && address < MEMORY_SIZE) {
                        memory[address] = instruction;
                    }
                } catch (NumberFormatException ex) {
                    printerArea.append("Invalid data in ROM file: " + line + "\n");
                }
            } else {
                printerArea.append("Malformed line in ROM file: " + line + "\n");
            }
        }
        reader.close();
        printerArea.append("ROM file loaded into memory successfully.\n");
    }

    // Automatically set the PC to the address of the first instruction
    private void setPCToFirstInstruction() {
        // Assuming the first instruction is located at the first non-zero memory location
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if (memory[i] != 0) {
                PC = i;  // Set PC to the first instruction's address
                pcField.setText(String.valueOf(PC));  // Display the new PC value in the UI
                break;
            }
        }
    }

    // Display the loaded ROM contents in the printer output area
    private void displayLoadedROMContents() {
        printerArea.append("Memory Contents after ROM loading (addresses in decimal, instructions in binary):\n");
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if (memory[i] != 0) {  // Display non-empty memory locations
                int decimalAddress = i;  // Address is already in decimal
                String instructionInBinary = Integer.toBinaryString(memory[i]);  // Instruction in binary
                printerArea.append("Address (Decimal): " + decimalAddress + ", Instruction (Binary): " + instructionInBinary + "\n");
            }
        }
    }

    // Step button action listener
    private class StepActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                PC = Integer.parseInt(pcField.getText());
            } catch (NumberFormatException ex) {
                printerArea.append("Invalid PC value entered.\n");
                return;
            }
            if (pcField.getText().equals("HALT") && !pcField.isEditable()) {
                printerArea.append("Cannot step further. Program has halted.\n");
                return;
            }
            if (PC < MEMORY_SIZE) {
                int instruction = memory[PC];
                executeInstruction(instruction);
                if (!pcField.getText().equals("HALT")) {
                    PC++;
                    pcField.setText(String.valueOf(PC));
                }
            }
        }
    }

    // Run button action listener
    private class RunActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            while (PC < MEMORY_SIZE) {
                int instruction = memory[PC];
                executeInstruction(instruction);
                if (pcField.getText().equals("HALT")) {
                    printerArea.append("Run stopped: Program halted at PC " + PC + ".\n");
                    break;
                }
                PC++;
                pcField.setText(String.valueOf(PC));
            }
        }
    }
    
    private int storeAddress;
    private int inputCounter = 0;
    
    // Execute a single instruction
    private void executeInstruction(int instruction) {
        String binaryInstruction = String.format("%16s", Integer.toBinaryString(instruction)).replace(' ', '0');
        if (binaryInstruction.equals("0000000000000000")) {
            printerArea.append("HLT: Program has been halted at PC " + PC + ".\n");
            stopProgram();
            return;
        }
        if (cache.containsValue(instruction)) {
            printerArea.append("Cache hit: Instruction already present in cache and seen at PC " + PC + "\n");
        } else {
            if (cache.size() >= CACHE_SIZE) {
                int oldestKey = cache.keySet().iterator().next();
                cache.remove(oldestKey);
            }
            cache.put(PC, instruction);
            printerArea.append("Cache miss: Adding new instruction to cache.\n");
        }
        String opcode = binaryInstruction.substring(0, 6);
        int gprIndex = Integer.parseInt(binaryInstruction.substring(6, 8), 2);
        int ixrIndex = Integer.parseInt(binaryInstruction.substring(8, 10), 2);
        int iBit = Integer.parseInt(binaryInstruction.substring(10, 11));
        int address = Integer.parseInt(binaryInstruction.substring(11, 16), 2);
        int effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
        String octalInstruction = Integer.toOctalString(instruction);
        String instructionStr = String.format("%06o", instruction);
        switch (instructionStr) {
            case "110000":
                loadSentencesFile();
                break;
            case "120000":
                getUserInputWord();
                break;
            case "130000":
                searchUserWordInParagraph();
                break;
            case "140000":
                printSearchResults();
                break;
        }
        switch (octalInstruction) {
            case "111110":
                try {
                    String input = JOptionPane.showInputDialog(frame, "Enter the number " + (inputCounter+1) + " of 20 :",  "Input Required", JOptionPane.PLAIN_MESSAGE);
                    if (input != null) {
                        int number = Integer.parseInt(input.trim());
                        GPR[0] = number;
                        printerArea.append("Read number: " + number + "\n");
                        inputCounter++;
                    } else {
                        printerArea.append("Input operation canceled by the user.\n");
                    }
                } catch (NumberFormatException ex) {
                    printerArea.append("Invalid number format entered in dialog box.\n");
                }
                break;
            case "175400":
                if (storeAddress >= 0 && storeAddress < MEMORY_SIZE) {
                    memory[storeAddress] = GPR[0];
                    printerArea.append("Stored value " + GPR[0] + " at memory address " + storeAddress + "\n");
                    storeAddress++;
                } else {
                    printerArea.append("Invalid memory address for storage.\n");
                }
                break;
            case "110001":
                printerArea.append("Stored numbers:\n");
                for (int i = 0; i < 20; i++) {
                    if (memory[i] != 0) {
                        cacheContent.append("Memory[" + i + "]: " + memory[i] + "\n");
                    }
                }
                break;
            case "110010":
                int target = GPR[0];
                int closestNumber = memory[0];
                int minDifference = Math.abs(memory[0] - target);
                for (int i = 1; i < 20; i++) {
                    int currentNumber = memory[i];
                    int difference = Math.abs(currentNumber - target);
                    if (difference < minDifference) {
                        minDifference = difference;
                        closestNumber = currentNumber;
                    }
                }
                printerArea.append("Target number: " + target + ", Closest number found: " + closestNumber + "\n");
                String msg ="Target number: " + target + ", Closest number found: " + closestNumber ;
                JOptionPane.showMessageDialog(frame, msg, "Output", JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                printerArea.append("Unknown instruction: " + octalInstruction + "\n");
                MFR = 1;
               	mfrField.setText(String.valueOf(MFR));
                break;
        }
        // The remainder of the instruction execution switch-case follows...
        // (Opcode based instruction execution code omitted for brevity)
        updateCacheDisplay();
    }

    private void loadSentencesFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            java.io.File paragraphFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(paragraphFile))) {
                String line;
                int address = SENTENCE_START_ADDR;
                int lineIndex = 0;
                while ((line = reader.readLine()) != null && lineIndex < SENTENCE_COUNT) {
                    String[] words = line.toLowerCase().split("\\s+");
                    sentences.add(String.join(" ", words));
                    memory[SENTENCE_START_ADDR + lineIndex] = lineIndex;
                    lineIndex++;
                    printerArea.append("Loaded Sentence :");
                }
            } catch (IOException ex) {
                printerArea.append("Error loading paragraph file: " + ex.getMessage() + "\n");
            }
        }
    }

    private void loadSentenceIntoMemory(int address) {
        if (address >= 0011 && address <= 0016) {
            int sentenceIndex = address - 0010;
            if (sentenceIndex < sentences.size()) {
                memory[address] = sentences.get(sentenceIndex).hashCode();
                printerArea.append("Loaded sentence into memory location " + Integer.toOctalString(address) + ": " + sentences.get(sentenceIndex) + "\n");
            } else {
                printerArea.append("No sentence available for loading into address " + Integer.toOctalString(address) + "\n");
            }
        } else {
            printerArea.append("Invalid address for loading sentence: " + Integer.toOctalString(address) + "\n");
        }
    }

    private void printSentenceFromMemory(int address) {
        if (address >= 0011 && address <= 0016 && memory[address] != 0) {
            printerArea.append("Sentence from memory[" + Integer.toOctalString(address) + "]: " + memory[address] + "\n");
        }
    }

    private void getUserInputWord() {
        String inputWord = JOptionPane.showInputDialog(frame, "Enter a word to search:").toLowerCase();
        if (inputWord != null) {
            memory[USER_INPUT_ADDR] = inputWord.hashCode();
            printerArea.append("User input word stored at memory location " + Integer.toOctalString(userInputAddress) + "\n");
        }
    }

    private void searchUserWordInParagraph() {
        int userWordHash = memory[USER_INPUT_ADDR];
        boolean found = false;
        for (int sentenceIndex = 0; sentenceIndex < sentences.size(); sentenceIndex++) {
            String sentence = sentences.get(sentenceIndex).trim();
            String[] words = sentence.split("\\s+");
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                String cleanWord = words[wordIndex].replaceAll("[^a-zA-Z0-9]", "").trim();
                if (cleanWord.hashCode() == userWordHash) {
                    found = true;
                    printerArea.append("Word found in sentence " + (sentenceIndex + 1) + ", word position " + (wordIndex + 1) + ".\n");
                    JOptionPane.showMessageDialog(frame, "Word found in sentence " + (sentenceIndex + 1) + ", word position " + (wordIndex + 1), "Search Result", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }
        if (!found) {
            printerArea.append("Word not found in any sentence.\n");
            JOptionPane.showMessageDialog(frame, "Word not found in any sentence.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void printSearchResults() {
        printerArea.append("Search operation completed.\n");
    }

    private void stopProgram() {
        pcField.setText("HALT");
        pcField.setEditable(true);
    }

    private int calculateEffectiveAddress(int ixrIndex, int iBit, int address) {
        int effectiveAddress = address;
        if (ixrIndex > 0) {
            effectiveAddress += IXR[ixrIndex - 1];
        }
        if (iBit == 1) {
            effectiveAddress = memory[effectiveAddress];
        }
        return effectiveAddress;
    }

    private class LoadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int address = Integer.parseInt(marField.getText());
                int gprIndex = Integer.parseInt(octalInputField.getText());
                if (address >= 0 && address < MEMORY_SIZE && gprIndex >= 0 && gprIndex < 4) {
                    GPR[gprIndex] = memory[address];
                    gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
                    printerArea.append("LDR: Loaded value " + memory[address] + " from memory address " + address + " into GPR " + gprIndex + ".\n");
                } else {
                    printerArea.append("Invalid memory address or GPR index.\n");
                }
            } catch (NumberFormatException ex) {
                printerArea.append("Invalid input for memory address or GPR index.\n");
            }
        }
    }

    private class StoreActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int address = Integer.parseInt(marField.getText());
                int value = Integer.parseInt(mbrField.getText());
                if (address >= 0 && address < MEMORY_SIZE) {
                    memory[address] = value;
                    printerArea.append("STR: Stored value " + value + " into memory address " + address + ".\n");
                } else {
                    printerArea.append("Invalid memory address.\n");
                }
            } catch (NumberFormatException ex) {
                printerArea.append("Invalid input for memory address or value.\n");
            }
        }
    }

    private void updateCacheDisplay() {
        for (Map.Entry<Integer, Integer> entry : cache.entrySet()) {
            int pcAddress = entry.getKey();
            int instr = entry.getValue();
            cacheContent.append("PC " + pcAddress + ": " + Integer.toBinaryString(instr) + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MachineSimulator::new);
    }
}
