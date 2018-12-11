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
public class ManterPortaLigadaPor extends TranslatorBlock {

    public ManterPortaLigadaPor(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {
        int tempo = Integer.parseInt(this.getRequiredTranslatorBlockAtSocket(0).toCode());
        int multipliadorTempo = Integer.parseInt(this.getRequiredTranslatorBlockAtSocket(1).toCode());
        tempo *= multipliadorTempo;
        String porta = this.getRequiredTranslatorBlockAtSocket(2).toCode();
        String ret = "";
        translator.addHeaderFile("RTCTimer.h");
        translator.requireLibrary("RTCTimer");
        if (!translator.containsSetupCommand("timer.setNowCallback(getNow);")) {
            translator.addSetupCommand("timer.setNowCallback(getNow);");
            translator.addDefinitionCommand("RTCTimer timer;");
            translator.addDefinitionCommand("uint32_t getNow()\n"
                    + "{\n"
                    + "  RtcDateTime now = Rtc.GetDateTime();\n"
                    + "  return now.Epoch32Time();\n"
                    + "}");
        }
        ret = "if(digitalRead(" + porta + ")==0){digitalWrite(" + porta + ",1); timer.every(" + tempo + ", [](uint32_t v){ digitalWrite(" + porta + ",0); },1);}";
        translator.addLoopCall("timer.update();");
        return ret;
    }

}
