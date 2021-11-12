import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

public class dissassembler {
    public static void main(String[] args) throws IOException {
            String inputFile = args[0];
        try (
                InputStream inputStream = new FileInputStream(inputFile);
        ) {
            ArrayList<StringBuilder> entireProgram = new ArrayList<>();
            Hashtable<Integer, Integer> branches = new Hashtable<>();
            entireProgram.add(new StringBuilder("Main: "));
            int byteRead;
            int newInstruct = 0;
            int indexOfProgram = 1;
            StringBuilder instructionSet = new StringBuilder();
            while ((byteRead = inputStream.read()) != -1) {
                instructionSet.append(String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0'));
                if(++newInstruct == 4){
                    entireProgram.add(MainHandler.CheckInstruction(instructionSet, branches, indexOfProgram));
                    instructionSet = new StringBuilder();
                    newInstruct = 0;
                    indexOfProgram++;
                }
            }
            for(Integer i: branches.keySet()){
                System.out.println(entireProgram.get(i) + " " + i + " " + branches.get(i) + " " + entireProgram.get((i<branches.get(i)) ? branches.get(i) : branches.get(i)-1));
                if(entireProgram.get((i<branches.get(i)) ? branches.get(i) : branches.get(i)-1).toString().contains(":")){
                                entireProgram.get((i<branches.get(i)) ? branches.get(i) : branches.get(i)-1).toString().lastIndexOf(" ");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

/**
 * this class handles the main part of decoding
 */
class MainHandler{
    public static StringBuilder CheckInstruction(StringBuilder i, Hashtable<Integer, Integer> branches, int indexOfProgram){
        if(i.substring(0, 11).equals("10001011000")){
            //ADD
            return RType(i, "   ADD", false, false);
        }
        else if(i.substring(0,10).equals("1001000100")){
            //ADDI
            return IType(i, "   ADDI");
        }
        else if(i.substring(0,11).equals("10001010000")){
            //AND
            return RType(i, "   AND", false, false);
        }
        else if(i.substring(0,10).equals("1001001000")){
            //ANDI
            return IType(i, "   ANDI");
        }
        else if(i.substring(0,6).equals("000101")){
            //B
            return BandCBType(i, "   B", false, false, branches, indexOfProgram);
        }
        else if(i.substring(0,8).equals("01010100")){
            //B.cond
            return BandCBType(i, "   B", true, false, branches, indexOfProgram);
        }
        else if(i.substring(0,6).equals("100101")){
            //BL
            return BandCBType(i, "   BL", false, false, branches, indexOfProgram);
        }
        else if(i.substring(0, 11).equals("11010110000")){
            //BR
            return RType(i, "   BR", false, true);
        }
        else if(i.substring(0, 8).equals("10110101")){
            //CBNZ
            return BandCBType(i, "   CBNZ", false, true, branches, indexOfProgram);
        }
        else if(i.substring(0, 8).equals("10110100")){
            //CBZ
            return BandCBType(i, "   CBZ", false, true, branches, indexOfProgram);
        }
        else if(i.substring(0, 11).equals("11001010000")){
            //EOR
            return RType(i, "   EOR", false, false);
        }
        else if(i.substring(0, 10).equals("1101001000")){
            //EORI
            return IType(i, "   EORI");
        }
        else if(i.substring(0, 11).equals("11111000010")){
            //LDUR
            return DType(i, "   LDUR");
        }
        else if(i.substring(0, 11).equals("11010011011")){
            //LSL
            return RType(i, "   LSL", true, false);
        }
        else if(i.substring(0, 11).equals("11010011010")){
            //LSR
            return RType(i, "   LSR", true, false);
        }
        else if(i.substring(0, 11).equals("10101010000")){
            //ORR
            return RType(i, "   ORR", false, false);
        }
        else if(i.substring(0, 10).equals("1011001000")){
            //ORRI
            return IType(i, "   OORI");
        }
        else if(i.substring(0, 11).equals("11111000000")){
            //STUR
            return DType(i, "   STUR");
        }
        else if(i.substring(0, 11).equals("11001011000")){
            //SUB
            return RType(i, "   SUB", false, false);
        }
        else if(i.substring(0, 10).equals("1101000100")){
            //SUBI
            return IType(i, "   SUBI");
        }
        else if(i.substring(0, 10).equals("1111000100")){
            //SUBIS
            return IType(i, "   SUBIS");
        }
        else if(i.substring(0, 11).equals("11101011000")){
            //SUBS
            return RType(i, "   SUBS", false, false);
        }
        else if(i.substring(0, 11).equals("10011011000")){
            //MUL
            return RType(i, "   MUL", false, false);
        }
        else if(i.substring(0, 11).equals("11111111101")){
            //PRNT
            StringBuilder fullString = new StringBuilder("   PRNT X");
            fullString.append(StringToUnsigned(i.substring(27, 32)));
            return fullString;
        }
        else if(i.substring(0, 11).equals("11111111100")){
            //PRNL
            return new StringBuilder("   PRNL");
        }
        else if(i.substring(0, 11).equals("11111111110")){
            //DUMP
            return new StringBuilder("   DUMP");
        }
        else if(i.substring(0, 11).equals("11111111111")){
            //HALT
            return new StringBuilder("   HALT");
        }
        else{
            return new StringBuilder("NOT INSTRUCTION: ").append(i.substring(0, 11));
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

    public static StringBuilder RType(StringBuilder str, String instruction, Boolean logicalShift, Boolean branchRegister){
        StringBuilder fullString = new StringBuilder();
        fullString.append(instruction);
        if(branchRegister){
            if(StringToUnsigned(str.substring(22, 27)) == 30)
                fullString.append(" LR");
            else
                fullString.append(" X").append(StringToUnsigned(str.substring(22, 27)));
        }
        else {
            fullString.append(" X").append(StringToUnsigned(str.substring(27, 32)));
            fullString.append(", X").append(StringToUnsigned(str.substring(22, 27)));
            if (logicalShift)
                fullString.append(", #").append(StringToUnsigned(str.substring(16, 22)));
            else
                fullString.append(", X").append(StringToUnsigned(str.substring(11, 16)));
        }
        return fullString;
    }
    public static StringBuilder IType(StringBuilder str, String instruction){
        StringBuilder fullString = new StringBuilder();
        fullString.append(instruction);
        fullString.append(" X").append(StringToUnsigned(str.substring(27, 32)));
        fullString.append(", X").append(StringToUnsigned(str.substring(22, 27)));
        fullString.append(", #").append(StringtoSigned(str.substring(10, 22)));
        return fullString;
    }
    public static StringBuilder DType(StringBuilder str, String instruction){
        StringBuilder fullString = new StringBuilder();
        fullString.append(instruction);
        fullString.append(" X").append(StringToUnsigned(str.substring(27, 32)));
        fullString.append(", [X").append(StringToUnsigned(str.substring(22, 27))).append(", #").append(StringToUnsigned(str.substring(11, 20))).append("]");
        return fullString;
    }
    public static StringBuilder BandCBType(StringBuilder str, String instruction, Boolean compareFlag, Boolean compareBranch, Hashtable<Integer, Integer> branches, int indexOfProgram){
        StringBuilder fullString = new StringBuilder();
        fullString.append(instruction);
        if(compareFlag){
            if(str.substring(27, 32).equals("00000")){
                fullString.append(".EQ");
            }
            else if(str.substring(27, 32).equals("00001")){
                fullString.append(".NE");
            }
            else if(str.substring(27, 32).equals("00010")){
                fullString.append(".HS");
            }
            else if(str.substring(27, 32).equals("00011")){
                fullString.append(".LO");
            }
            else if(str.substring(27, 32).equals("00100")){
                fullString.append(".MI");
            }
            else if(str.substring(27, 32).equals("00101")){
                fullString.append(".PL");
            }
            else if(str.substring(27, 32).equals("00110")){
                fullString.append(".VS");
            }
            else if(str.substring(27, 32).equals("00111")){
                fullString.append(".VC");
            }
            else if(str.substring(27, 32).equals("01000")){
                fullString.append(".HI");
            }
            else if(str.substring(27, 32).equals("01001")){
                fullString.append(".LS");
            }
            else if(str.substring(27, 32).equals("01010")){
                fullString.append(".GE");
            }
            else if(str.substring(27, 32).equals("01011")){
                fullString.append(".LT");
            }
            else if(str.substring(27, 32).equals("01100")){
                fullString.append(".GT");
            }
            else if(str.substring(27, 32).equals("01101")){
                fullString.append(".LE");
            }
            fullString.append(" ").append(StringtoSigned(str.substring(8, 27)));
            branches.put(indexOfProgram, StringtoSigned(str.substring(8, 27)) + indexOfProgram);
        }
        else if(compareBranch){
            fullString.append(" X").append(StringToUnsigned(str.substring(27, 32))).append(", ").append(StringtoSigned(str.substring(8, 27)));
            branches.put(indexOfProgram, StringtoSigned(str.substring(8, 27)) + indexOfProgram);
        }
        else{
            fullString.append(" ").append(StringtoSigned(str.substring(6, 32)));
            branches.put(indexOfProgram, StringtoSigned(str.substring(6, 32)) + indexOfProgram);
        }
        return fullString;
    }
    public static boolean contains(StringBuilder str, char toCheck){
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == toCheck)
                return true;
        }
        return false;
    }
}
