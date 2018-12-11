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
public class TipoTempo extends TranslatorBlock {

    public TipoTempo(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {
        String dia = label;
        String multiplicador = "";
        switch (dia) {
            case "Hora":
                multiplicador = "3600000";
                break;
            case "Minuto":
                multiplicador = "60000";
                break;
            case "Segundo":
                multiplicador = "1000";
                break;
        }
        return multiplicador;
    }

}
