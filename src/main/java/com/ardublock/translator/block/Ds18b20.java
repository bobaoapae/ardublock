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
public class Ds18b20 extends TranslatorBlock {

    public Ds18b20(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {

        String rt = "";
        String porta = "D0";
        translator.addHeaderFile("OneWire.h");
        translator.addHeaderFile("DallasTemperature.h");
        translator.requireLibrary("OneWire");
        translator.requireLibrary("DallasTemperature");
        if (!translator.containsSetupCommand("sensors" + porta + ".begin();")) {
            translator.addDefinitionCommand("OneWire oneWire" + porta + " (" + porta + ");");
            translator.addDefinitionCommand("DallasTemperature sensors" + porta + "(&oneWire" + porta + ");");
            translator.addSetupCommand("sensors" + porta + ".begin();");
            translator.addDefinitionCommand("float lerTemperaturaDS18B20" + porta + "(){ sensors" + porta + ".requestTemperatures(); return sensors" + porta + ".getTempCByIndex(0);}");
        }
        rt += "lerTemperaturaDS18B20" + porta + "()";
        return rt;
    }

}
