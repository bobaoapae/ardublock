/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ardublock.translator.block;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.exception.BlockException;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

/**
 *
 * @author jvbor
 */
public class Lm35 extends TranslatorBlock {

    public Lm35(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {

        String rt = "";
        if (!translator.containsSetupCommand("pinMode(A0,INPUT);")) {
            translator.addSetupCommand("pinMode(A0,INPUT);");
            translator.addDefinitionCommand("float lerTemperaturaLm35(){ return ((float)(analogRead(A0)))*3.3/1023.0/0.01;}");
        }
        rt += "lerTemperaturaLm35()";
        return rt;
    }

}
