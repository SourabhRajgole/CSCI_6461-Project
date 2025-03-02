import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;

public class MachineSimulator {

    private static final int MEMORY_SIZE = 2048;
    private int[] memory = new int[MEMORY_SIZE];

    private int[] GPR = new int[4];  // General Purpose Registers R0-R3
    private int[] IXR = new int[3];  // Index Registers
    private int PC, MAR, MBR, IR, CC, MFR;  // Other Registers

    // UI fields for registers
    private JTextField[] gprFields = new JTextField[4];
    private JTextField[] ixrFields = new JTextField[3];
    private JTextField pcField, marField, mbrField, irField, ccField, mfrField;

    // Extra UI fields
    private JTextField octalInputField, binaryField, consoleInputField, programFileField;

    // Output areas
    private JTextArea consoleOutput, printerArea, cacheContent;

    // Buttons
    private JButton runButton, stepButton, haltButton, iplButton;
    private JButton loadButton, storeButton, loadPlusButton, storePlusButton;

    private JFrame frame;

    // Boot/program start addresses (unchanged)
    private static final int BOOT_START_ADDR = 010;  // Octal 10
    private static final int PROGRAM_START_ADDR = 020;  // Octal 20

    public MachineSimulator() {
        initializeMemory();
        createUI();
    }

    // Initialize memory to zeros
    private void initializeMemory() {
        Arrays.fill(memory, 0);
    }

    // Create the UI in a layout similar to the provided screenshot
    private void createUI() {
        frame = new JFrame("CSCI 6461 Machine Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 500);

        // Main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(192, 192, 192)); // Light gray background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // ========== Row 0: Title Label ==========
        JLabel titleLabel = new JLabel("CSCI 6461 Machine Simulator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(350, 10, 300, 30);
    	mainPanel.add(titleLabel);

        // Reset for next rows
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // ========== Row 1: GPR Panel, IXR Panel, Other Registers, (Cache moved to x=4) ==========

        // -- GPR Panel --
        JPanel gprPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        gprPanel.setOpaque(false);
        gprPanel.add(new JLabel("GPR"));
        gprPanel.add(new JLabel("")); // Empty placeholder to align
        for (int i = 0; i < 4; i++) {
            gprPanel.add(new JLabel("R" + i + ":"));
            gprFields[i] = new JTextField("0", 5);
            gprPanel.add(gprFields[i]);
        }
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(gprPanel, gbc);

        // -- IXR Panel --
        JPanel ixrPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        ixrPanel.setOpaque(false);
        ixrPanel.add(new JLabel("IXR"));
        ixrPanel.add(new JLabel("")); // Empty placeholder
        for (int i = 0; i < 3; i++) {
            ixrPanel.add(new JLabel("IX" + (i + 1) + ":"));
            ixrFields[i] = new JTextField("0", 5);
            ixrPanel.add(ixrFields[i]);
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(ixrPanel, gbc);

        // -- Other Registers (PC, MAR, MBR, IR, CC, MFR) --
        JPanel otherRegPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        otherRegPanel.setOpaque(false);

        pcField = new JTextField("0", 5);
        marField = new JTextField("0", 5);
        mbrField = new JTextField("0", 5);
        irField = new JTextField("0", 5);
        ccField = new JTextField("0", 5);
        mfrField = new JTextField("0", 5);

        otherRegPanel.add(new JLabel("PC:"));
        otherRegPanel.add(pcField);
        otherRegPanel.add(new JLabel("MAR:"));
        otherRegPanel.add(marField);
        otherRegPanel.add(new JLabel("MBR:"));
        otherRegPanel.add(mbrField);
        otherRegPanel.add(new JLabel("IR:"));
        otherRegPanel.add(irField);
        otherRegPanel.add(new JLabel("CC:"));
        otherRegPanel.add(ccField);
        otherRegPanel.add(new JLabel("MFR:"));
        otherRegPanel.add(mfrField);

        gbc.gridx = 2;
        gbc.gridy = 1;
        mainPanel.add(otherRegPanel, gbc);

        // -- Cache Content Area (Moved to x=4, anchored top-left, with a negative top inset) --
        JPanel cachePanel = new JPanel(new BorderLayout());
        cachePanel.setOpaque(false);
        JLabel cacheLabel = new JLabel("Cache Content");
        cacheContent = new JTextArea(9, 27);
        cacheContent.setEditable(false);
        JScrollPane cacheScroll = new JScrollPane(cacheContent);
        cachePanel.add(cacheLabel, BorderLayout.NORTH);
        cachePanel.add(cacheScroll, BorderLayout.CENTER);

        gbc.gridx = 3;                   // Move further to the right
        gbc.gridy = 1;                   // Same row as GPR/IXR, but more to the right
        gbc.gridheight = 2;             // Span downward if needed
        gbc.anchor = GridBagConstraints.NORTHWEST;
        // Move it up by 5 (negative top inset) and to the right by 5
        gbc.insets = new Insets(-5, 5, 0, 0);
        mainPanel.add(cachePanel, gbc);

        // Reset gridheight and insets for subsequent components
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // ========== Row 2: BINARY, OCTAL INPUT ==========

        JLabel binaryLabel = new JLabel("BINARY");
        binaryField = new JTextField(15);
        binaryField.setEditable(false);
        binaryField.setBackground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(binaryLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(binaryField, gbc);

        JLabel octalLabel = new JLabel("OCTAL INPUT");
        octalInputField = new JTextField("0", 15);

        gbc.gridx = 2;
        gbc.gridy = 2;
        mainPanel.add(octalLabel, gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        mainPanel.add(octalInputField, gbc);

        // ========== Row 3: Program File Field ==========

        JLabel programFileLabel = new JLabel("Program File");
        programFileField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(programFileLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;  // Let the text field span two columns
        mainPanel.add(programFileField, gbc);

        // Reset
        gbc.gridwidth = 1;

        // ========== Row 4: Buttons ==========

        // Create button panel with single row of 8 buttons, 5px spacing
JPanel buttonPanel = new JPanel(new GridLayout(1, 8, 4, 4));
buttonPanel.setOpaque(false);

// Create and configure all buttons
iplButton = new JButton("IPL");
iplButton.setBackground(Color.RED);  // Red background only for IPL
iplButton.setForeground(Color.WHITE);
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
haltButton.addActionListener(e -> {
    printerArea.append("Program halted manually by clicking halt button\n");
    stopProgram();
});

// Style all non-IPL buttons with blue color
Color buttonColor = new Color(0, 128, 255);
Font buttonFont = new Font("IBM Plex Mono", Font.BOLD, 12);

// Add all buttons to the buttonPanel (IPL is included here, but its color is already set)
JButton[] buttons = {loadButton, storeButton, loadPlusButton, storePlusButton, 
                    runButton, stepButton, haltButton, iplButton};

// Apply styling to all buttons except IPL (which already has its style)
for (JButton btn : buttons) {
    if (btn != iplButton) {  // Skip IPL button when applying the blue color
        btn.setBackground(buttonColor);
    }
    btn.setForeground(Color.WHITE);
    btn.setFont(buttonFont);
    buttonPanel.add(btn);
}

// Add button panel to main panel
gbc.gridx = 0;
gbc.gridy = 4;
gbc.gridwidth = 0;  // Let it span multiple columns
mainPanel.add(buttonPanel, gbc);

        // ========== Row 5: Printer & Console Input ==========

        // Printer
        JPanel printerPanel = new JPanel(new BorderLayout());
        printerPanel.setOpaque(false);
        JLabel printerLabel = new JLabel("Printer");
        printerArea = new JTextArea(8, 27);
        printerArea.setEditable(false);
        JScrollPane printerScroll = new JScrollPane(printerArea);
        printerPanel.add(printerLabel, BorderLayout.NORTH);
        printerPanel.add(printerScroll, BorderLayout.CENTER);

        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(printerPanel, gbc);

        // Console Panel
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setOpaque(false);
        JLabel consoleLabel = new JLabel("Console Input");
        consoleInputField = new JTextField(20);
        consolePanel.add(consoleLabel, BorderLayout.NORTH);
        consolePanel.add(consoleInputField, BorderLayout.CENTER);


        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(consolePanel, gbc);

        // Finally, add the main panel to the frame
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // ===========================
    //       CORE LOGIC BELOW
    // ===========================

    // Load memory to zeros
    private void loadROMFile(java.io.File romFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(romFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 2) {
                try {
                    int address = Integer.parseInt(parts[0], 8);  // Octal
                    int instruction = Integer.parseInt(parts[1], 8);  // Octal
                    String binaryInstruction = Integer.toBinaryString(instruction);
                    if (address >= 0 && address < MEMORY_SIZE) {
                        memory[address] = Integer.parseInt(binaryInstruction, 2);
                    }
                } catch (NumberFormatException ex) {
                    printerArea.append("Invalid data in ROM file: " + line + "\n");
                }
            } else {
                printerArea.append("Malformed line in ROM file: " + line + "\n");
            }
        }
        reader.close();
    }

    private void setPCToFirstInstruction() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if (memory[i] != 0) {
                PC = i;
                pcField.setText(String.valueOf(PC));
                break;
            }
        }
    }

    private void displayLoadedROMContents() {
        printerArea.append("Memory Contents after ROM loading (addresses in decimal, instructions in binary):\n");
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if (memory[i] != 0) {
                int decimalAddress = i;
                String instructionInBinary = Integer.toBinaryString(memory[i]);
                printerArea.append("Address (Decimal): " + decimalAddress +
                                   ", Instruction (Binary): " + instructionInBinary + "\n");
            }
        }
    }

    private class IPLActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File romFile = fileChooser.getSelectedFile();
                try {
                    loadROMFile(romFile);
                    printerArea.append("ROM file loaded into memory successfully.\n");
                    setPCToFirstInstruction();
                    printerArea.append("PC set to the address of the first instruction.\n");
                    displayLoadedROMContents();
                } catch (IOException ex) {
                    printerArea.append("Error loading ROM file: " + ex.getMessage() + "\n");
                }
            } else {
                printerArea.append("ROM file loading cancelled.\n");
            }
        }
    }

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

    private void executeInstruction(int instruction) {
        String binaryInstruction = String.format("%16s", Integer.toBinaryString(instruction)).replace(' ', '0');
        System.out.println();
        if (binaryInstruction.equals("0000000000000000")) {
            printerArea.append("HLT: Program halted at PC " + PC + ".\n");
            stopProgram();
            return;
        }
        String opcode = binaryInstruction.substring(0, 6);
        int gprIndex = Integer.parseInt(binaryInstruction.substring(6, 8), 2);
        int ixrIndex = Integer.parseInt(binaryInstruction.substring(8, 10), 2);
        int iBit = Integer.parseInt(binaryInstruction.substring(10, 11));
        int address = Integer.parseInt(binaryInstruction.substring(11, 16), 2);

        int effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);

        switch (opcode) {
            case "001000":  // JZ
                if (GPR[gprIndex] == 0) {
                    PC = effectiveAddress;
                    printerArea.append("JZ: Jumped to " + effectiveAddress + " (GPR " + gprIndex + " == 0).\n");
                } else {
                    PC++;
                }
                break;
            case "001001":  // JNE
                if (GPR[gprIndex] != 0) {
                    PC = effectiveAddress;
                    printerArea.append("JNE: Jumped to " + effectiveAddress + " (GPR " + gprIndex + " != 0).\n");
                } else {
                    PC++;
                }
                break;
            case "001010":  // JCC
                if (CC == gprIndex) {
                    PC = effectiveAddress;
                    printerArea.append("JCC: Jumped to " + effectiveAddress + " (CC == " + gprIndex + ").\n");
                } else {
                    PC++;
                }
                break;
            case "001011":  // JMA
                PC = effectiveAddress;
                printerArea.append("JMA: Jumped unconditionally to " + effectiveAddress + ".\n");
                break;
            case "001100":  // JSR
                GPR[3] = PC + 1;
                PC = effectiveAddress;
                printerArea.append("JSR: Saved return addr in GPR3, jumped to " + effectiveAddress + ".\n");
                break;
            case "001101":  // RFS
                GPR[0] = address;
                PC = GPR[3];
                printerArea.append("RFS: Returned to GPR3, loaded " + address + " into GPR0.\n");
                break;
            case "001110":  // SOB
                GPR[gprIndex]--;
                if (GPR[gprIndex] > 0) {
                    PC = effectiveAddress;
                    printerArea.append("SOB: Decremented GPR " + gprIndex + ", jumped to " + effectiveAddress + ".\n");
                } else {
                    PC++;
                }
                break;
            case "001111":  // JGE
                if (GPR[gprIndex] >= 0) {
                    PC = effectiveAddress;
                    printerArea.append("JGE: Jumped to " + effectiveAddress + " (GPR " + gprIndex + " >= 0).\n");
                } else {
                    PC++;
                }
                break;
            case "000001":  // LDR
                GPR[gprIndex] = memory[effectiveAddress];
                gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
                printerArea.append("LDR: Loaded from " + effectiveAddress + " into GPR " + gprIndex + ".\n");
                break;
            case "000010":  // STR
                memory[effectiveAddress] = GPR[gprIndex];
                printerArea.append("STR: Stored GPR " + gprIndex + " into " + effectiveAddress + ".\n");
                break;
            case "000011":  // LDA
                GPR[gprIndex] = address;
                gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
                printerArea.append("LDA: Loaded address " + address + " into GPR " + gprIndex + ".\n");
                break;
            case "100001":  // LDX
                IXR[ixrIndex - 1] = memory[effectiveAddress];
                ixrFields[ixrIndex - 1].setText(String.valueOf(IXR[ixrIndex - 1]));
                printerArea.append("LDX: Loaded from " + effectiveAddress + " into IXR " + ixrIndex + ".\n");
                break;
            case "100010":  // STX
                memory[effectiveAddress] = IXR[ixrIndex - 1];
                printerArea.append("STX: Stored IXR " + ixrIndex + " into " + effectiveAddress + ".\n");
                break;
            case "000000":
                printerArea.append("Stored value at PC " + PC + ".\n");
                break;
            default:
                printerArea.append("Unknown instruction at PC " + PC + ".\n");
                MFR = 1;  // Set machine fault register
                mfrField.setText(String.valueOf(MFR));
                break;
        }
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

    // "Load" button action (mimics LDR)
    private class LoadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int address = Integer.parseInt(marField.getText());
                int gprIndex = Integer.parseInt(octalInputField.getText());
                if (address >= 0 && address < MEMORY_SIZE && gprIndex >= 0 && gprIndex < 4) {
                    GPR[gprIndex] = memory[address];
                    gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
                    printerArea.append("LDR: Loaded value " + memory[address] +
                            " from address " + address + " into GPR " + gprIndex + ".\n");
                } else {
                    printerArea.append("Invalid memory address or GPR index.\n");
                }
            } catch (NumberFormatException ex) {
                printerArea.append("Invalid input for memory address or GPR index.\n");
            }
        }
    }

    // "Store" button action (mimics STR)
    private class StoreActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int address = Integer.parseInt(marField.getText());
                int value = Integer.parseInt(mbrField.getText());
                if (address >= 0 && address < MEMORY_SIZE) {
                    memory[address] = value;
                    printerArea.append("STR: Stored value " + value + " into address " + address + ".\n");
                } else {
                    printerArea.append("Invalid memory address.\n");
                }
            } catch (NumberFormatException ex) {
                printerArea.append("Invalid input for memory address or value.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MachineSimulator::new);
    }
}
