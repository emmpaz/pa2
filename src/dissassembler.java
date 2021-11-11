import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class dissassembler {
    public static void main(String[] args) throws IOException {
            String inputFile = args[0];
        try (
                InputStream inputStream = new FileInputStream(inputFile);
        ) {

            int byteRead;
            int newInstruct = 0;
            StringBuilder instructionSet = new StringBuilder();
            while ((byteRead = inputStream.read()) != -1) {
                if(newInstruct == 4){
                    System.out.println(instructionSet);
                    MainHandler.CheckInstruction(instructionSet);
                    instructionSet = new StringBuilder();
                    newInstruct = 0;
                }
                instructionSet.append(String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0'));
                newInstruct++;
            }
            System.out.println();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

/**
 * this class handles the main part of decoding
 */
class MainHandler{
    public static void CheckInstruction(StringBuilder i){
        if(i.substring(0, 11).equals("10001011000")){
            //ADD
            System.out.println("ADD");
        }
        else if(i.substring(0,10).equals("1001000100")){
            //ADDI
            System.out.println("ADDI");
        }
        else if(i.substring(0,11).equals("10001010000")){
            //AND
            System.out.println("AND");
        }
        else if(i.substring(0,10).equals("1001001000")){
            //ANDI
            System.out.println("ANDI");
        }
        else if(i.substring(0,6).equals("000101")){
            //B
            System.out.println("B");
        }
        else if(i.substring(0,8).equals("01010100")){
            //B.cond
            System.out.println("B.cond");
        }
        else if(i.substring(0,6).equals("100101")){
            //BL
            System.out.println("BL");
        }
        else if(i.substring(0, 11).equals("11010110000")){
            //BR
            System.out.println("BR");
        }
        else if(i.substring(0, 8).equals("10110101")){
            //CBNZ
            System.out.println("CBNZ");
        }
        else if(i.substring(0, 8).equals("10110100")){
            //CBZ
            System.out.println("CBZ");
        }
        else if(i.substring(0, 11).equals("11001010000")){
            //EOR
            System.out.println("EOR");
        }
        else if(i.substring(0, 10).equals("1101001000")){
            //EORI
            System.out.println("EORI");
        }
        else if(i.substring(0, 11).equals("11111000010")){
            //LDUR
            System.out.println("LDUR");
        }
        else if(i.substring(0, 11).equals("11010011011")){
            //LSL
            System.out.println("LSL");
        }
        else if(i.substring(0, 11).equals("11010011010")){
            //LSR
            System.out.println("LSR");
        }
        else if(i.substring(0, 11).equals("10101010000")){
            //ORR
            System.out.println("ORR");
        }
        else if(i.substring(0, 10).equals("1011001000")){
            //ORRI
            System.out.println("ORRI");
        }
        else if(i.substring(0, 11).equals("11111000000")){
            //STUR
            System.out.println("STUR");
        }
        else if(i.substring(0, 11).equals("11001011000")){
            //SUB
            System.out.println("SUB");
        }
        else if(i.substring(0, 10).equals("1101000100")){
            //SUBI
            System.out.println("SUBI");
        }
        else if(i.substring(0, 10).equals("1111000100")){
            //SUBIS
            System.out.println("SUBIS");
        }
        else if(i.substring(0, 11).equals("11101011000")){
            //SUBS
            System.out.println("SUBS");
        }
        else if(i.substring(0, 11).equals("10011011000")){
            //MUL
            System.out.println("MUL");
        }
        else if(i.substring(0, 11).equals("11111111101")){
            //PRNT
            System.out.println("PRNT");
        }
        else if(i.substring(0, 11).equals("11111111100")){
            //PRNL
            System.out.println("PRNL");
        }
        else if(i.substring(0, 11).equals("11111111110")){
            //DUMP
            System.out.println("DUMP");
        }
        else if(i.substring(0, 11).equals("11111111111")){
            //HALT
            System.out.println("HALT");
        }
        else{
            System.out.println("NOT INSTRUCTION");
        }
    }

    public static int StringToUnsigned(String str){
        int unsigned = 0;
        int power = 0;
        for(int i= str.length()-1; i >= 0; i--, power++){
            if(str.charAt(i) == '1')
                unsigned += Math.pow(2, power);
        }
        return unsigned;
    }

    public static int StringtoSigned(String str){
        int signed = 0;
        if(str.charAt(0) == '1'){
            signed = (int) (-1*Math.pow(2, str.length()-1));
            for(int i = str.length()-1; i > 0; i--){
                if(str.charAt(i) == '1')
                    signed += Math.pow(2, str.length()-i-1);
            }
        }
        else{
            for(int i = str.length()-1; i > 0; i--){
                if(str.charAt(i) == '1')
                    signed += Math.pow(2, str.length()-i-1);
            }
        }
        return signed;
    }
}
