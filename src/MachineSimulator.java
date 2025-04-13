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
	// this Points to the next cache line to replace
	private LinkedHashMap<Integer, Integer> cache = new LinkedHashMap<Integer, Integer>(CACHE_SIZE, 0.75f, true) {
		protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
			return size() > CACHE_SIZE;  //this will Keep the cache size limited to CACHE_SIZE
		}
	};
	private Set<Integer> seenInstructions = new HashSet<>();
	private int[] GPR = new int[4];  // these are General Purpose Registers R0-R3
	private int[] IXR = new int[3];  // Index Registers
	private int PC, MAR, MBR, IR, CC, MFR;// Other Registers
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

	// Create UI and buttons(this is everything about UI design)
	private void createUI() {
		// Initialize the frame
		frame = new JFrame("CSCI 6461 Machine Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 700);
		frame.setLayout(new BorderLayout());

		frame.getContentPane().setBackground(new Color(173, 216, 230));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setOpaque(false);

		JPanel centerPanel = new JPanel(new GridBagLayout());
		centerPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		JPanel gprPanel = new JPanel(new GridLayout(5, 2, 5, 5));
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
		gbc.gridwidth = 2;
		centerPanel.add(gprPanel, gbc);

		gbc.gridx = 2;
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
		gbc.gridwidth = 4;
		centerPanel.add(registerPanel, gbc);

		JPanel cachePrinterPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		cachePrinterPanel.setOpaque(false);
		cacheContent = new JTextArea(5, 20);
		cacheContent.setText("Cache Contents");
		cacheContent.setEditable(false);

		printerArea = new JTextArea(5, 20);
		printerArea.setText("Printer Output");
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
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		centerPanel.add(binaryOctalPanel, gbc);

		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		centerPanel.add(cachePrinterPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		centerPanel.add(consoleInputPanel, gbc);

		consoleOutput = new JTextArea(10, 40);
		consoleOutput.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(consoleOutput);
		frame.add(consoleScrollPane, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 8, 5, 5));
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
			button.setBackground(buttonColor);
			button.setForeground(Color.WHITE);
			button.setFont(buttonFont);
		}

		buttonPanel.add(loadButton);
		buttonPanel.add(storeButton);
		buttonPanel.add(iplButton);
		buttonPanel.add(loadPlusButton);
		buttonPanel.add(runButton);
		buttonPanel.add(stepButton);
		buttonPanel.add(haltButton);
		buttonPanel.add(storePlusButton);

		JTextField programFileField = new JTextField(20);
		JPanel programFilePanel = new JPanel(new FlowLayout());
		programFilePanel.setOpaque(false);
		programFilePanel.add(new JLabel("Program File"));
		programFilePanel.add(programFileField);

		frame.add(centerPanel, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);
		frame.add(programFilePanel, BorderLayout.NORTH);

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
			// Do not allow stepping if the program is halted
			try {
				PC = Integer.parseInt(pcField.getText());  // Ensure PC is updated from user input
			} catch (NumberFormatException ex) {
				printerArea.append("Invalid PC value entered.\n");
				return;
			}
			if (pcField.getText().equals("HALT") && !pcField.isEditable()) {
				printerArea.append("Cannot step further. Program has halted.\n");
				return;
			}

			// Fetch and execute the instruction at the current PC
			if (PC < MEMORY_SIZE) {
				int instruction = memory[PC];  // Fetch instruction from memory
				executeInstruction(instruction);  // Decode and execute the instruction

				// Increment the PC only if it is not a HALT instruction
				if (!pcField.getText().equals("HALT")) {
					PC++;  // Increment PC after execution if it's not HALT
					pcField.setText(String.valueOf(PC));  // Update the PC field
				}
			}
		}
	}

	// Run button action listener
	private class RunActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Start running the program from the current PC
			while (PC < MEMORY_SIZE) {
				int instruction = memory[PC];  // Fetch the instruction from memory
				executeInstruction(instruction);  // Execute the instruction

				// If HALT is encountered, stop execution
				if (pcField.getText().equals("HALT")) {
					printerArea.append("Run stopped: Program halted at PC " + PC + ".\n");
					break;
				}

				// Increment PC for the next instruction
				PC++;
				pcField.setText(String.valueOf(PC));  // Update the PC field
			}
		}
	}
	private int storeAddress;
	private int inputCounter=0;
	// Execute a single instruction
	private void executeInstruction(int instruction) {
		// Ensure binary instruction is 16 bits
		String binaryInstruction = String.format("%16s", Integer.toBinaryString(instruction)).replace(' ', '0');
		if (binaryInstruction.equals("0000000000000000")) {
			// Full binary 0 indicates a HALT, so we stop execution
			printerArea.append("HLT: Program has been halted at PC " + PC + ".\n");
			stopProgram();  // Stop further execution, as HALT should end the program
			return;  // Exit early to avoid further execution
		}
		// Check if instruction at PC is in cache with the same value
		if (cache.containsValue(instruction)) {
			printerArea.append("Cache hit: Instruction already present in cache and seen at PC " + PC + "\n");
		} else {
			// Cache miss or new instruction, add to cache and track in seenInstructions
			// If the cache reached capacity and removed an entry, update seenInstructions
			if (cache.size() >= CACHE_SIZE) {
				int oldestKey = cache.keySet().iterator().next();
				cache.remove(oldestKey);
			}
			cache.put(PC, instruction);  // Add the new instruction
			printerArea.append("Cache miss: Adding new instruction to cache.\n");
		}
		// Extract opcode (6 bits), GPR (2 bits), IXR (2 bits), I (1 bit), Address (5 bits)
		String opcode = binaryInstruction.substring(0, 6);  // First 6 bits as opcode
		int gprIndex = Integer.parseInt(binaryInstruction.substring(6, 8), 2);  // GPR index (2 bits)
		int ixrIndex = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // IXR index (2 bits)
		int iBit = Integer.parseInt(binaryInstruction.substring(10, 11));  // Indirect bit (1 bit)
		int address = Integer.parseInt(binaryInstruction.substring(11, 16), 2);  // Address (5 bits)

		int effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
		String octalInstruction = Integer.toOctalString(instruction);
		String instructionStr = String.format("%06o", instruction);  // Format as a 6-digit octal string
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
			case "111110":  // Read number from user and store temporarily in GPR[0]
				try {
					String input = JOptionPane.showInputDialog(frame, "Enter the number " + (inputCounter+1) + " of 20 :",  "Input Required", JOptionPane.PLAIN_MESSAGE);
					if (input != null) {  // Check if the user didn't click "Cancel"
						int number = Integer.parseInt(input.trim());
						GPR[0] = number;  // Store temporarily in GPR[0]
						printerArea.append("Read number: " + number + "\n");
						inputCounter++;
					} else {
						printerArea.append("Input operation canceled by the user.\n");
					}
				} catch (NumberFormatException ex) {
					printerArea.append("Invalid number format entered in dialog box.\n");
				}
				break;

			case "175400":  // Store the value in GPR[0] to specified memory location in storeAddress
				if (storeAddress >= 0 && storeAddress < MEMORY_SIZE) {
					memory[storeAddress] = GPR[0];  // Store the value from GPR[0] into memory at storeAddress
					printerArea.append("Stored value " + GPR[0] + " at memory address " + storeAddress + "\n");
					storeAddress++;  // Move to the next memory address for sequential storage
				} else {
					printerArea.append("Invalid memory address for storage.\n");
				}
				break;

			case "110001":  // Print the stored 20 numbers
				printerArea.append("Stored numbers:\n");
				for (int i = 0; i < 20; i++) {
					if (memory[i] != 0) {  // Only print non-zero entries
						cacheContent.append("Memory[" + i + "]: " + memory[i] + "\n");
					}
				}
				break;

			case "110010":  // Find the closest number to the target in GPR[0]
				int target = GPR[0];  // Target number to find closest to
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
				MFR = 1;  // Set machine fault register
				mfrField.setText(String.valueOf(MFR));
				break;
		}

		// Handle opcode cases (including newly added instructions like JZ, JNE, JCC, etc.)
		switch (opcode) {
			case "001000":  // JZ - Jump if Zero
				if (GPR[gprIndex] == 0) {
					PC = effectiveAddress;
					printerArea.append("JZ: Jumped to address " + effectiveAddress + " because GPR " + gprIndex + " is zero.\n");
				} else {
					PC++;
				}
				break;
			case "001001":  // JNE - Jump if Not Equal
				if (GPR[gprIndex] != 0) {
					PC = effectiveAddress;
					printerArea.append("JNE: Jumped to address " + effectiveAddress + " because GPR " + gprIndex + " is not zero.\n");
				} else {
					PC++;
				}
				break;
			case "001010":  // JCC - Jump if Condition Code
				if (CC == gprIndex) {  // Assuming CC is stored as the Condition Code register
					PC = effectiveAddress;
					printerArea.append("JCC: Jumped to address " + effectiveAddress + " because Condition Code " + gprIndex + " is true.\n");
				} else {
					PC++;
				}
				break;
			case "001011":  // JMA - Unconditional Jump
				PC = effectiveAddress;
				printerArea.append("JMA: Jumped unconditionally to address " + effectiveAddress + ".\n");
				break;
			case "001100":  // JSR - Jump and Save Return Address
				GPR[3] = PC + 1;  // Save return address into GPR3 (as per description)
				PC = effectiveAddress;  // Jump to the effective address
				printerArea.append("JSR: Saved return address in GPR3 and jumped to address " + effectiveAddress + ".\n");
				break;
			case "001101":  // RFS - Return from Subroutine
				GPR[0] = address;  // Load immediate into GPR0
				PC = GPR[3];  // Set PC to the value in GPR3 (return address)
				printerArea.append("RFS: Returned to address stored in GPR3 and loaded immediate value " + address + " into GPR0.\n");
				break;
			case "001110":  // SOB - Subtract One and Branch
				GPR[gprIndex]--;
				if (GPR[gprIndex] > 0) {
					PC = effectiveAddress;
					printerArea.append("SOB: Decremented GPR " + gprIndex + " and jumped to address " + effectiveAddress + " because value > 0.\n");
				} else {
					PC++;
				}
				break;
			case "001111":  // JGE - Jump if Greater Than or Equal to
				if (GPR[gprIndex] >= 0) {
					PC = effectiveAddress;
					printerArea.append("JGE: Jumped to address " + effectiveAddress + " because GPR " + gprIndex + " is greater than or equal to zero.\n");
				} else {
					PC++;
				}
				break;
			case "000001":  // LDR - Load Register
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				GPR[gprIndex] = memory[effectiveAddress];  // Load value from memory into GPR
				gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
				printerArea.append("LDR: Loaded value from memory address " + effectiveAddress + " into GPR " + gprIndex + "\n");
				break;
			case "000010":  // STR - Store Register
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				memory[effectiveAddress] = GPR[gprIndex];  // Store GPR value into memory
				printerArea.append("STR: Stored value from GPR " + gprIndex + " into memory address " + effectiveAddress + "\n");
				break;
			case "000011":  // LDA - Load Register with Address
				GPR[gprIndex] = address;  // Load the address directly into the GPR
				gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
				printerArea.append("LDA: Loaded address " + address + " into GPR " + gprIndex + "\n");
				break;
			case "100001":  // LDX - Load Index Register from Memory
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				IXR[ixrIndex - 1] = memory[effectiveAddress];  // Load value from memory into IXR
				ixrFields[ixrIndex - 1].setText(String.valueOf(IXR[ixrIndex - 1]));
				printerArea.append("LDX: Loaded value from memory address " + effectiveAddress + " into IXR " + ixrIndex + "\n");
				break;
			case "100010":  // STX - Store Index Register to Memory
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				memory[effectiveAddress] = IXR[ixrIndex - 1];  // Store IXR value into memory
				printerArea.append("STX: Stored value from IXR " + ixrIndex + " into memory address " + effectiveAddress + "\n");
				break;
			case "000000":
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				printerArea.append("Executed instruction at PC" + PC + "\n");
				break;
			case "111000":  // MLT - Multiply Register by Register
				int ry = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // Get Ry
				if (gprIndex >= 0 && gprIndex < 4 && ry >= 0 && ry < 4) {  // Ensure both Rx and Ry are valid
					long product = (long) GPR[gprIndex] * GPR[ry];
					GPR[gprIndex] = (int) (product >> 16);  // High-order bits
					GPR[gprIndex + 1] = (int) (product & 0xFFFF);  // Low-order bits
					if (product > Integer.MAX_VALUE || product < Integer.MIN_VALUE) {
						CC = 1;  // Set OVERFLOW flag
					}
					printerArea.append("MLT: Multiplied GPR " + gprIndex + " with GPR " + ry + ".\n");
				} else {
					printerArea.append("MLT: Invalid register index for Rx or Ry.\n");
				}
				break;

			case "111001":  // DVD - Divide Register by Register
				ry = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // Get Ry
				if (gprIndex >= 0 && gprIndex < 4 && ry >= 0 && ry < 4) {  // Ensure both Rx and Ry are valid
					if (GPR[ry] == 0) {
						CC = 8;  // Set DIVZERO flag
						printerArea.append("DVD: Division by zero error.\n");
					} else {
						int quotient = GPR[gprIndex] / GPR[ry];
						int remainder = GPR[gprIndex] % GPR[ry];
						GPR[gprIndex] = quotient;
						GPR[gprIndex + 1] = remainder;
						printerArea.append("DVD: Divided GPR " + gprIndex + " by GPR " + ry + ". Quotient: " + quotient + ", Remainder: " + remainder + ".\n");
					}
				} else {
					printerArea.append("DVD: Invalid register index for Rx or Ry.\n");
				}
				break;

			case "111010":  // TRR - Test the Equality of Register and Register
				ry = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // Get Ry
				if (gprIndex >= 0 && gprIndex < 4 && ry >= 0 && ry < 4) {  // Ensure both Rx and Ry are valid
					if (GPR[gprIndex] == GPR[ry]) {
						CC = 1;  // Set CC(4) <- 1
						printerArea.append("TRR: GPR " + gprIndex + " equals GPR " + ry + ".\n");
					} else {
						CC = 0;  // Set CC(4) <- 0
						printerArea.append("TRR: GPR " + gprIndex + " does not equal GPR " + ry + ".\n");
					}
				} else {
					printerArea.append("TRR: Invalid register index for Rx or Ry.\n");
				}
				break;

			case "111011":  // AND - Logical And of Register and Register
				ry = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // Get Ry
				if (gprIndex >= 0 && gprIndex < 4 && ry >= 0 && ry < 4) {  // Ensure both Rx and Ry are valid
					GPR[gprIndex] = GPR[gprIndex] & GPR[ry];
					printerArea.append("AND: GPR " + gprIndex + " AND GPR " + ry + " result stored in GPR " + gprIndex + ".\n");
				} else {
					printerArea.append("AND: Invalid register index for Rx or Ry.\n");
				}
				break;
			case "000100":  // AMR - Add Memory to Register
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				GPR[gprIndex] += memory[effectiveAddress];
				printerArea.append("AMR: Added value from memory address " + effectiveAddress + " to GPR " + gprIndex + "\n");
				break;

			case "000101":  // SMR - Subtract Memory from Register
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				GPR[gprIndex] -= memory[effectiveAddress];
				printerArea.append("SMR: Subtracted value from memory address " + effectiveAddress + " from GPR " + gprIndex + "\n");
				break;

			case "000110":  // AIR - Add Immediate to Register
				if (address == 0) {
					// If Immed = 0, do nothing
					printerArea.append("AIR: Immediate value is 0, no operation performed.\n");
				} else if (GPR[gprIndex] == 0) {
					// If c(r) = 0, load r with Immed
					GPR[gprIndex] = address;
					printerArea.append("AIR: GPR " + gprIndex + " was zero, loaded immediate value " + address + "\n");
				} else {
					GPR[gprIndex] += address;
					printerArea.append("AIR: Added immediate value " + address + " to GPR " + gprIndex + "\n");
				}
				break;

			case "000111":  // SIR - Subtract Immediate from Register
				if (address == 0) {
					// If Immed = 0, do nothing
					printerArea.append("SIR: Immediate value is 0, no operation performed.\n");
				} else if (GPR[gprIndex] == 0) {
					// If c(r) = 0, load r with -Immed
					GPR[gprIndex] = -address;
					printerArea.append("SIR: GPR " + gprIndex + " was zero, loaded -Immediate value " + address + "\n");
				} else {
					GPR[gprIndex] -= address;
					printerArea.append("SIR: Subtracted immediate value " + address + " from GPR " + gprIndex + "\n");
				}
				break;

			case "111100":  // ORR - Logical Or of Register and Register
				ry = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // Get Ry
				if (gprIndex >= 0 && gprIndex < 4 && ry >= 0 && ry < 4) {  // Ensure both Rx and Ry are valid
					GPR[gprIndex] = GPR[gprIndex] | GPR[ry];
					printerArea.append("ORR: GPR " + gprIndex + " OR GPR " + ry + " result stored in GPR " + gprIndex + ".\n");
				} else {
					printerArea.append("ORR: Invalid register index for Rx or Ry.\n");
				}
				break;

			case "111101":  // NOT - Logical Not of Register
				if (gprIndex >= 0 && gprIndex < 4) {  // Ensure Rx is valid
					GPR[gprIndex] = ~GPR[gprIndex];
					printerArea.append("NOT: Logical NOT applied to GPR " + gprIndex + ".\n");
				} else {
					printerArea.append("NOT: Invalid register index for Rx.\n");
				}
				break;


			default:
				printerArea.append("Unknown instruction.\n");
				MFR = 1;  // Set machine fault register
				mfrField.setText(String.valueOf(MFR));
				break;
		}
		updateCacheDisplay();
	}
	private void loadSentencesFile() {
		JFileChooser fileChooser = new JFileChooser();
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			java.io.File paragraphFile = fileChooser.getSelectedFile();
			try (BufferedReader reader = new BufferedReader(new FileReader(paragraphFile))) {
				String line;
				int address = SENTENCE_START_ADDR;// Start storing lines at SENTENCE_START_ADDR
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
			int sentenceIndex = address - 0010;  // Calculate index in sentences list based on address

			// Assuming sentences have been preloaded in a list called `sentences`
			if (sentenceIndex < sentences.size()) {
				memory[address] = sentences.get(sentenceIndex).hashCode();  // Store hash of the sentence
				printerArea.append("Loaded sentence into memory location " + Integer.toOctalString(address) + ": " + sentences.get(sentenceIndex) + "\n");
			} else {
				printerArea.append("No sentence available for loading into address " + Integer.toOctalString(address) + "\n");
			}
		} else {
			printerArea.append("Invalid address for loading sentence: " + Integer.toOctalString(address) + "\n");
		}
	}
	// Print sentence from memory to console
	private void printSentenceFromMemory(int address) {
		if (address >= 0011 && address <= 0016 && memory[address] != 0) {
			printerArea.append("Sentence from memory[" + Integer.toOctalString(address) + "]: " + memory[address] + "\n");
		}
	}

	// Get user word and store it in memory at `userInputAddress`
	private void getUserInputWord() {
		String inputWord = JOptionPane.showInputDialog(frame, "Enter a word to search:").toLowerCase();
		if (inputWord != null) {
			memory[USER_INPUT_ADDR] = inputWord.hashCode();  // Store unique representation of the word
			printerArea.append("User input word stored at memory location " + Integer.toOctalString(userInputAddress) + "\n");
		}
	}

	private void searchUserWordInParagraph() {
		int userWordHash = memory[USER_INPUT_ADDR];
		boolean found = false;
		for (int sentenceIndex = 0; sentenceIndex < sentences.size(); sentenceIndex++) {
			String sentence = sentences.get(sentenceIndex).trim();
			String[] words = sentence.split("\\s+");  // Split sentence into individual words

			// Check each word in the sentence
			for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
				String cleanWord = words[wordIndex].replaceAll("[^a-zA-Z0-9]", "").trim();

				// Compare the hash of the cleaned word to the user input's hash
				if (cleanWord.hashCode() == userWordHash) {
					found = true;
					printerArea.append("Word found in sentence " + (sentenceIndex + 1) + ", word position " + (wordIndex + 1) + ".\n");
					JOptionPane.showMessageDialog(frame, "Word found in sentence " + (sentenceIndex + 1) + ", word position " + (wordIndex + 1), "Search Result", JOptionPane.INFORMATION_MESSAGE);
					return;  // Stop after finding the first match
				}
			}
		}
		if (!found) {
			printerArea.append("Word not found in any sentence.\n");
			JOptionPane.showMessageDialog(frame, "Word not found in any sentence.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// Print the final search results
	private void printSearchResults() {
		printerArea.append("Search operation completed.\n");
	}


	private void stopProgram() {
		// Logic to stop the machine execution, as the HALT instruction has been encountered
		pcField.setText("HALT");
		pcField.setEditable(true);
	}

	// Example helper function to calculate the effective address
	private int calculateEffectiveAddress(int ixrIndex, int iBit, int address) {
		int effectiveAddress = address;

		// If IXR is not 0, add the index register's value to the base address
		if (ixrIndex > 0) {
			effectiveAddress += IXR[ixrIndex - 1];  // IXR index 0-2 corresponds to IXR 1-3
		}

		// If indirect addressing is used, fetch the value at the effective address
		if (iBit == 1) {
			effectiveAddress = memory[effectiveAddress];
		}

		return effectiveAddress;
	}

	// Load button action listener (mimicking LDR)
	private class LoadActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int address = Integer.parseInt(marField.getText());  // Get the memory address from MAR
				int gprIndex = Integer.parseInt(octalInputField.getText());  // Get the GPR index from octalInputField

				// Load the value from memory into the specified GPR
				if (address >= 0 && address < MEMORY_SIZE && gprIndex >= 0 && gprIndex < 4) {
					GPR[gprIndex] = memory[address];  // Load value from memory into GPR
					gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));  // Update GPR field in the UI
					printerArea.append("LDR: Loaded value " + memory[address] + " from memory address " + address + " into GPR " + gprIndex + ".\n");
				} else {
					printerArea.append("Invalid memory address or GPR index.\n");
				}
			} catch (NumberFormatException ex) {
				printerArea.append("Invalid input for memory address or GPR index.\n");
			}
		}
	}

	// Store button action listener (mimicking STR)
	private class StoreActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int address = Integer.parseInt(marField.getText());  // Get the memory address from MAR
				//int gprIndex = Integer.parseInt(octalInputField.getText());  // Get the GPR index from octalInputField
				int value = Integer.parseInt(mbrField.getText());
				// Store the value from the specified GPR into the given memory address
				if (address >= 0 && address < MEMORY_SIZE ) {
					memory[address] = value;  // Store value from GPR into memory
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
		cacheContent.setText("Cache Contents:\n");
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


