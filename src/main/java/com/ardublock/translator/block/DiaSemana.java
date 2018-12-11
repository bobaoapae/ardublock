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
public class DiaSemana extends TranslatorBlock {

    public DiaSemana(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {
        String dia = label;
        String diaNumerico = "";
        switch (dia) {
            case "Domingo":
                diaNumerico = "0";
                break;
            case "Segunda":
                diaNumerico = "1";
                break;
            case "Terca":
                diaNumerico = "2";
                break;
            case "Quarta":
                diaNumerico = "3";
                break;
            case "Quinta":
                diaNumerico = "4";
                break;
            case "Sexta":
                diaNumerico = "5";
                break;
            case "Sabado":
                diaNumerico = "6";
                break;
        }
        if (!translator.containsSetupCommand("weekday(0,0,0)")) {
            translator.addDefinitionCommand("int weekday()\n"
                    + "/* Calculate day of week in proleptic Gregorian calendar. Sunday == 0. */\n"
                    + "{\n"
                    + "RtcDateTime now = Rtc.GetDateTime();\n"
                    + "return now.DayOfWeek();}");
        }
        return "weekday() == " + diaNumerico;
    }

}
