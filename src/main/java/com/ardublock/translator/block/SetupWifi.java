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
import java.time.ZonedDateTime;

/**

 @author jvbor
 */
public class SetupWifi extends TranslatorBlock {

    public SetupWifi(Long blockId, Translator translator, String codePrefix, String codeSuffix, String label) {
        super(blockId, translator, codePrefix, codeSuffix, label);
    }

    @Override
    public String toCode() throws SocketNullException, SubroutineNotDeclaredException, BlockException {
        translator.addHeaderFile("ESP8266WiFi.h");
        String ret = "";
        TranslatorBlock tb = this.getRequiredTranslatorBlockAtSocket(0);
        String nomeWifi = tb.toCode();
        tb = this.getRequiredTranslatorBlockAtSocket(1);
        String senhaWifi = tb.toCode();
        ret += "WiFi.mode(WIFI_STA);\r\n";
        ret += "WiFi.begin(" + nomeWifi + "," + senhaWifi + ");\r\n";
        ret += "while (WiFi.status() == WL_DISCONNECTED) {\n"
                + "    delay(500);\n"
                + "  }"
                + "dnsC.begin({8,8,8,8});"
                + "syncTime();";
        if (!translator.containsSetupCommand("syncTimeAdd();")) {
            translator.addHeaderFile("EthernetUdp.h");
            translator.addHeaderFile("Dns.h");
            translator.addHeaderFile("RtcDS1307.h");
            translator.addHeaderFile("Wire.h");
            translator.requireLibrary("Rtc by Makuna");
            translator.addDefinitionCommand("RtcDS1307<TwoWire> Rtc(Wire);");
            translator.addDefinitionCommand("const long timeZoneOffset = "+String.valueOf(ZonedDateTime.now().getOffset().getTotalSeconds())+"L;"
                    + "const long ntpSyncTime = 360000;"
                    + "unsigned int localPort = 8888;"
                    + "const int NTP_PACKET_SIZE = 48;"
                    + "byte packetBuffer[NTP_PACKET_SIZE];"
                    + "EthernetUDP Udp;"
                    + "unsigned long ntpLastUpdate = 0;"
                    + "DNSClient dnsC;");
            translator.addDefinitionCommand("void syncTime() {\n"
                    + "  if (millis() - ntpLastUpdate > ntpSyncTime) {\n"
                    + "    int trys = 0;\n"
                    + "    while (!getTimeAndDate() && trys < 10) {\n"
                    + "      trys++;\n"
                    + "    }\n"
                    + "    if (trys < 10) {\n"
                    + "      Serial.println(\"ntp server update success\");\n"
                    + "    }\n"
                    + "    else {\n"
                    + "      Serial.println(\"ntp server update failed\");\n"
                    + "    }\n"
                    + "  }\n"
                    + "}");
            translator.addDefinitionCommand("int getTimeAndDate() {\n"
                    + "  int flag = 0;\n"
                    + "  Udp.begin(localPort);\n"
                    + "  IPAddress timeServer;\n"
                    + "  dnsC.getHostByName(\"pool.ntp.org\", timeServer);\n"
                    + "  sendNTPpacket(timeServer);\n"
                    + "  delay(1000);\n"
                    + "  if (Udp.parsePacket()) {\n"
                    + "    Udp.read(packetBuffer, NTP_PACKET_SIZE); // read the packet into the buffer\n"
                    + "    unsigned long highWord, lowWord, epoch;\n"
                    + "    highWord = word(packetBuffer[40], packetBuffer[41]);\n"
                    + "    lowWord = word(packetBuffer[42], packetBuffer[43]);\n"
                    + "    epoch = highWord << 16 | lowWord;\n"
                    + "    epoch = epoch - 2208988800UL + 946684800UL + timeZoneOffset;\n"
                    + "    flag = 1;\n"
                    + "    ntpLastUpdate = millis();\n"
                    + "    RtcDateTime compiled = RtcDateTime(epoch);\n"
                    + "    Rtc.SetDateTime(compiled);\n"
                    + "  }\n"
                    + "  return flag;\n"
                    + "}");
            translator.addDefinitionCommand("unsigned long sendNTPpacket(IPAddress& address)\n"
                    + "{\n"
                    + "  memset(packetBuffer, 0, NTP_PACKET_SIZE);\n"
                    + "  packetBuffer[0] = 0b11100011;\n"
                    + "  packetBuffer[1] = 0;\n"
                    + "  packetBuffer[2] = 6;\n"
                    + "  packetBuffer[3] = 0xEC;\n"
                    + "  packetBuffer[12]  = 49;\n"
                    + "  packetBuffer[13]  = 0x4E;\n"
                    + "  packetBuffer[14]  = 49;\n"
                    + "  packetBuffer[15]  = 52;\n"
                    + "  Udp.beginPacket(address, 123);\n"
                    + "  Udp.write(packetBuffer, NTP_PACKET_SIZE);\n"
                    + "  Udp.endPacket();\n"
                    + "}");
            translator.addDefinitionCommand("void syncTimeAdd(){}");
            translator.addSetupCommand("syncTimeAdd();");
            translator.addSetupCommand("Rtc.Begin();");
            translator.addSetupCommand("Rtc.SetSquareWavePin(DS1307SquareWaveOut_Low);");
            translator.addLoopCall("syncTime();");
        }
        return ret;
    }

}
