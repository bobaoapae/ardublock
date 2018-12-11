/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package com.ardublock.translator.block;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.exception.BlockException;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

/**

 @author jvbor
 */
public class PinSaida extends TranslatorBlock {

    public PinSaida(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {
        int port = Integer.parseInt(label.split(" ")[1]);
        String porta = "";
        switch (port) {
            case 1:
                porta = "D7";
                break;
            case 2:
                porta = "D6";
                break;
            case 3:
                porta = "D4";
                break;
            case 4:
                porta = "D3";
                break;
        }
        if (!translator.containsSetupCommand("pinMode(" + porta + ",OUTPUT);")) {
            translator.addSetupCommand("pinMode(" + porta + ",OUTPUT);");
        }
        return porta;
    }

}
