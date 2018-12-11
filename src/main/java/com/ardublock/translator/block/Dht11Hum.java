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
public class Dht11Hum extends TranslatorBlock {

    public Dht11Hum(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {

        String rt = "";
        translator.addHeaderFile("DHT.h");
        translator.requireLibrary("DHT");
        if (!translator.containsSetupCommand("dht.begin();")) {
            translator.addDefinitionCommand("DHT dht(D0, DHT11);");
            translator.addSetupCommand("dht.begin();");
        }
        rt += "dht.readHumidity()";
        return rt;
    }

}
