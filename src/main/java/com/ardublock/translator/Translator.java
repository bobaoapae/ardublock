package com.ardublock.translator;

import com.ardublock.translator.adaptor.BlockAdaptor;
import com.ardublock.translator.adaptor.OpenBlocksAdaptor;
import com.ardublock.translator.block.TranslatorBlock;
import com.ardublock.translator.block.TranslatorBlockFactory;
import com.ardublock.translator.block.exception.BlockException;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNameDuplicatedException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;
import edu.mit.blocks.codeblocks.Block;
import edu.mit.blocks.renderable.RenderableBlock;
import edu.mit.blocks.workspace.Workspace;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Translator {

    private List<String> setupCommand;
    private List<String> loopCalls;
    private Set<TranslatorBlock> bodyTranslatreFinishCallbackSet;
    private BlockAdaptor blockAdaptor;
    private Workspace workspace;

    private String rootBlockName;			// block we're constructing ("loop" or "setup")

    private Set<String> headerFileSet;		// header files in the C program
    private Set<String> librarys;		// librarys required to install
    private Set<String> definitionSet;		// definitions in the C program
    private Set<String> inputPinSet;		// input pins in the C program
    private Set<String> outputPinSet;		// output pins in the C program
    private Set<String> variableSet;		// variables in the C program
    private Set<String> functionNameSet;	// functions in the C program

    public Translator(Workspace ws) {
        workspace = ws;
        reset();
    }

    public String generateHeaderCommand() {
        StringBuilder headerCommand = new StringBuilder();

        if (!headerFileSet.isEmpty()) {
            for (String file : headerFileSet) {
                headerCommand.append("#include <" + file + ">\n");
            }
            headerCommand.append("\n");
        }

        if (!definitionSet.isEmpty()) {
            for (String command : definitionSet) {
                headerCommand.append(command + "\n");
            }
            headerCommand.append("\n");
        }

        if (!functionNameSet.isEmpty()) {
            for (String functionName : functionNameSet) {
                headerCommand.append("void " + functionName + "();\n");
            }
            headerCommand.append("\n");
        }

        return headerCommand.toString() + generateSetupFunction();
    }

    /**
     Generate code for the Arduino "setup" function.

     @return a string containing the code for the setup function
     */
    public String generateSetupFunction() {
        // Create a string builder object that we can append code to.
        StringBuilder setupFunction = new StringBuilder();

        // Start by adding the setup function's initialization.
        setupFunction.append("void setup()\n{\n");

        // If there are input pins that need initialization...
        if (!inputPinSet.isEmpty()) {
            // For each input pin...
            for (String pinNumber : inputPinSet) {
                // Add a line of code to initialize the pin.
                setupFunction.append("pinMode(" + pinNumber + ", INPUT);\n");
            }
        }

        // If there are output pins that need initialization...
        if (!outputPinSet.isEmpty()) {
            // If we previously printed input pin code...
            if (!inputPinSet.isEmpty()) {
                // Add an extra newline.
                setupFunction.append("\n");
            }

            // For each output pin...
            for (String pinNumber : outputPinSet) {
                // Add a line of code to initialize the pin.
                setupFunction.append("pinMode(" + pinNumber + ", OUTPUT);\n");
            }
        }

        // If there are setup commands (these are from blocks the user places
        // in the setup block)...
        if (!setupCommand.isEmpty()) {
            // If we previously printed input or output pin code...
            if ((!inputPinSet.isEmpty()) || (!outputPinSet.isEmpty())) {
                // Add an extra newline.
                setupFunction.append("\n");
            }

            // For each setup command...
            for (String command : setupCommand) {
                // Add the command.  Newline has already been included in
                // SetupBlock.java, which generates the setup commands.
                setupFunction.append(command);
            }
        }

        // Add the closing bracket, and two newlines.
        setupFunction.append("}\n\n");

        // Convert the stringbuilder object to a string and return it.
        return setupFunction.toString();
    }

    public String translate(Long blockId) throws SocketNullException, SubroutineNotDeclaredException, BlockException {
        TranslatorBlockFactory translatorBlockFactory = new TranslatorBlockFactory();
        Block block = workspace.getEnv().getBlock(blockId);
        TranslatorBlock rootTranslatorBlock = translatorBlockFactory.buildTranslatorBlock(this, blockId, block.getGenusName(), "", "", block.getBlockLabel());
        return rootTranslatorBlock.toCode();
    }

    public BlockAdaptor getBlockAdaptor() {
        return blockAdaptor;
    }

    public void reset() {
        headerFileSet = new LinkedHashSet<String>();
        librarys = new LinkedHashSet<String>();
        definitionSet = new LinkedHashSet<String>();
        setupCommand = new LinkedList<String>();
        loopCalls = new LinkedList<String>();
        functionNameSet = new HashSet<String>();
        inputPinSet = new HashSet<String>();
        outputPinSet = new HashSet<String>();
        bodyTranslatreFinishCallbackSet = new HashSet<TranslatorBlock>();
        variableSet = new HashSet<String>();
        blockAdaptor = buildOpenBlocksAdaptor();
        rootBlockName = null;
    }

    private BlockAdaptor buildOpenBlocksAdaptor() {
        return new OpenBlocksAdaptor();
    }

    public void addHeaderFile(String headerFile) {
        if (!headerFileSet.contains(headerFile)) {
            headerFileSet.add(headerFile);
        }
    }

    public void requireLibrary(String library) {
        if (!librarys.contains(library)) {
            librarys.add(library);
        }
    }

    public void addSetupCommand(String command) {
        // If the command isn't already in the list of setup commands...
        if (!setupCommand.contains(command)) {
            // Add the command.
            setupCommand.add(command);
        }
    }

    public void addLoopCall(String command) {
        if (!loopCalls.contains(command)) {
            // Add the command.
            loopCalls.add(command);
        }
    }

    public boolean containsSetupCommand(String command) {
        return setupCommand.contains(command);
    }

    public void addSetupCommandForced(String command) {
        setupCommand.add(command);
    }

    /**
     Adds a define statement for a variable.

     @param command - the variable definition to add
     */
    public void addDefinitionCommand(String command) {
        definitionSet.add(command);
    }

    public void addInputPin(String pinNumber) {
        inputPinSet.add(pinNumber);
    }

    public void addOutputPin(String pinNumber) {
        outputPinSet.add(pinNumber);
    }

    /**
     Does the variable already exist in our C program?

     @param name - name of the variable we wish to use in the C program
     @return true if the name has been used in the C program already
     */
    public boolean doesVariableExist(String name) {
        return variableSet.contains(name);
    }

    /**
     Add a variable to the list of variables used in the C program.

     @param name - name of the variable to remember
     */
    public void addVariable(String name) {
        variableSet.add(name);
    }

    /**
     Add a variable, or create a new one if it already exists. Checks if a
     suggested variable name is available. If we've used it already, picks a new
     name for us.

     @param name - a suggested variable name
     @return the name of the variable (could be the same or new)
     */
    public String addOrCreateNumberVariable(String name) {
        // Start with the suggested variable name.
        String newName = name;

        // Create a variable counter, which we'll use if we need to.
        int i = 0;

        // If that variable name has been used before...
        while (variableSet.contains(newName)) {
            // Increment our variable counter.
            i++;

            // Try a new variable name.
            newName = name + i;
        }

        // Remember the new variable name.
        variableSet.add(newName);

        return newName;
    }

    public List<String> getLoopCalls() {
        return loopCalls;
    }

    public void addFunctionName(Long blockId, String functionName) throws SubroutineNameDuplicatedException {
        if (functionName.equals("loop") || functionName.equals("setup") || functionNameSet.contains(functionName)) {
            throw new SubroutineNameDuplicatedException(blockId);
        }

        functionNameSet.add(functionName);
    }

    public boolean containFunctionName(String name) {
        return functionNameSet.contains(name.trim());
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Block getBlock(Long blockId) {
        return workspace.getEnv().getBlock(blockId);
    }

    public void registerBodyTranslateFinishCallback(TranslatorBlock translatorBlock) {
        bodyTranslatreFinishCallbackSet.add(translatorBlock);
    }

    public void beforeGenerateHeader() throws SocketNullException, SubroutineNotDeclaredException {
        for (TranslatorBlock translatorBlock : bodyTranslatreFinishCallbackSet) {
            translatorBlock.onTranslateBodyFinished();
        }
    }

    public String getRootBlockName() {
        return rootBlockName;
    }

    public void setRootBlockName(String rootBlockName) {
        this.rootBlockName = rootBlockName;
    }

    public Set<RenderableBlock> findEntryBlocks() {
        Set<RenderableBlock> loopBlockSet = new HashSet<RenderableBlock>();
        Iterable<RenderableBlock> renderableBlocks = workspace.getRenderableBlocks();

        for (RenderableBlock renderableBlock : renderableBlocks) {
            Block block = renderableBlock.getBlock();

            if (!block.hasPlug() && (Block.NULL.equals(block.getBeforeBlockID()))) {
                if (block.getGenusName().equals("loop")) {
                    loopBlockSet.add(renderableBlock);
                }
                if (block.getGenusName().equals("setup")) {
                    loopBlockSet.add(renderableBlock);
                }
            }
        }

        return loopBlockSet;
    }

    public Set<RenderableBlock> findSubroutineBlocks() throws SubroutineNameDuplicatedException {
        Set<RenderableBlock> subroutineBlockSet = new HashSet<RenderableBlock>();
        Iterable<RenderableBlock> renderableBlocks = workspace.getRenderableBlocks();

        for (RenderableBlock renderableBlock : renderableBlocks) {
            Block block = renderableBlock.getBlock();

            if (!block.hasPlug() && (Block.NULL.equals(block.getBeforeBlockID()))) {
                if (block.getGenusName().equals("subroutine")) {
                    String functionName = block.getBlockLabel().trim();
                    this.addFunctionName(block.getBlockID(), functionName);
                    subroutineBlockSet.add(renderableBlock);
                }
            }
        }

        return subroutineBlockSet;
    }

    public Set<String> getLibrarys() {
        return librarys;
    }

    public String translate(Set<RenderableBlock> loopBlocks, Set<RenderableBlock> subroutineBlocks) throws SocketNullException, SubroutineNotDeclaredException {
        StringBuilder code = new StringBuilder();
        for (RenderableBlock renderableBlock : loopBlocks) {
            Block loopBlock = renderableBlock.getBlock();
            code.append(translate(loopBlock.getBlockID()));
        }

        for (RenderableBlock renderableBlock : subroutineBlocks) {
            Block subroutineBlock = renderableBlock.getBlock();
            code.append(translate(subroutineBlock.getBlockID()));
        }
        beforeGenerateHeader();
        code.insert(0, generateHeaderCommand());

        return code.toString();
    }
}
