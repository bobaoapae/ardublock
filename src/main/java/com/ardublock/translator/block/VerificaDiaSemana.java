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
public class VerificaDiaSemana extends TranslatorBlock {

    public VerificaDiaSemana(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {

        String rt = "";
        TranslatorBlock translatorBlock = this.getRequiredTranslatorBlockAtSocket(0);
        if (!translator.containsSetupCommand("weekday(0,0,0)")) {
            translator.addDefinitionCommand("int weekday()\n"
                    + "/* Calculate day of week in proleptic Gregorian calendar. Sunday == 0. */\n"
                    + "{\n"
                    + "RtcDateTime now = Rtc.GetDateTime();\n"
                    + "int year = dt.Year();"
                    + "int month = dt.Month();"
                    + "int day = dt.Day();"
                    + "  int adjustment, mm, yy;\n"
                    + "  if (year<2000) year+=2000;\n"
                    + "  adjustment = (14 - month) / 12;\n"
                    + "  mm = month + 12 * adjustment - 2;\n"
                    + "  yy = year - adjustment;\n"
                    + "  return (day + (13 * mm - 1) / 5 +\n"
                    + "    yy + yy / 4 - yy / 100 + yy / 400) % 7;\n"
                    + "}");
        }
        rt += "weekday() == " + translatorBlock.toCode();
        return rt;
    }

}
