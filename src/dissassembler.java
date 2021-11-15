import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class dissassembler {
    public static void main(String[] args){
            String inputFile = args[0];
        try (
                InputStream inputStream = new FileInputStream(inputFile);
        ) {
            ArrayList<Instruction> entireProgram = new ArrayList<>();
            ArrayList<BranchInstruction> branches = new ArrayList<>();
            int byteRead;
            int newInstruct = 0;
            int indexOfProgram = 0;
            MainHandler handler = new MainHandler();
            StringBuilder instructionSet = new StringBuilder();
            while ((byteRead = inputStream.read()) != -1) {
                instructionSet.append(String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0'));
                if(++newInstruct == 4){
                    StringBuilder temp = handler.CheckInstruction(instructionSet, branches, indexOfProgram);
                    entireProgram.add(new Instruction((indexOfProgram==0) ? "Main": "Label"+indexOfProgram, temp));
                    instructionSet = new StringBuilder();
                    newInstruct = 0;
                    indexOfProgram++;
                }
            }

            for(BranchInstruction i : branches){
                entireProgram.get(i.branchIndex).useLabel = true;
                entireProgram.get(i.index).string.append(" ").append(entireProgram.get(i.branchIndex).label);
            }
            for(Instruction i : entireProgram){
                if(i.useLabel){
                    i.string.insert(0, i.label + ":\n");
                }
            }
            for(Instruction i : entireProgram)
                System.out.println(i.string);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class Instruction{
    public boolean useLabel;
    public StringBuilder string;
    public String label;
    public Instruction(String label, StringBuilder string){
        useLabel = false;
        this.label = label;
        this.string = new StringBuilder(string);
    }
}

class BranchInstruction{
    public int index;
    public int branchIndex;
    public BranchInstruction(int index, int branchIndex){
        this.index = index;
        this.branchIndex = branchIndex;
    }
}

/**
 * this class handles the main part of decoding
 */
class MainHandler{
    public StringBuilder CheckInstruction(StringBuilder i, ArrayList<BranchInstruction> branches, int indexOfProgram){
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

    public int StringToUnsigned(String str){
        int unsigned = 0;
        int power = 0;
        for(int i= str.length()-1; i >= 0; i--, power++){
            if(str.charAt(i) == '1')
                unsigned += Math.pow(2, power);
        }
        return unsigned;
    }

    public int StringtoSigned(String str){
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

    public StringBuilder RType(StringBuilder str, String instruction, Boolean logicalShift, Boolean branchRegister){
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
    public StringBuilder IType(StringBuilder str, String instruction){
        StringBuilder fullString = new StringBuilder();
        fullString.append(instruction);
        fullString.append(" X").append(StringToUnsigned(str.substring(27, 32)));
        fullString.append(", X").append(StringToUnsigned(str.substring(22, 27)));
        fullString.append(", #").append(StringtoSigned(str.substring(10, 22)));
        return fullString;
    }
    public StringBuilder DType(StringBuilder str, String instruction){
        StringBuilder fullString = new StringBuilder();
        fullString.append(instruction);
        fullString.append(" X").append(StringToUnsigned(str.substring(27, 32)));
        fullString.append(", [X").append(StringToUnsigned(str.substring(22, 27))).append(", #").append(StringToUnsigned(str.substring(11, 20))).append("]");
        return fullString;
    }
    public StringBuilder BandCBType(StringBuilder str, String instruction, Boolean compareFlag, Boolean compareBranch, ArrayList<BranchInstruction> branches, int indexOfProgram){
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
            fullString.append(" ");
            branches.add(new BranchInstruction(indexOfProgram, StringtoSigned(str.substring(8, 27)) + indexOfProgram));
        }
        else if(compareBranch){
            fullString.append(" X").append(StringToUnsigned(str.substring(27, 32))).append(", ");
            branches.add(new BranchInstruction(indexOfProgram, StringtoSigned(str.substring(8, 27)) + indexOfProgram));
        }
        else{
            fullString.append(" ");
            branches.add(new BranchInstruction(indexOfProgram, StringtoSigned(str.substring(6, 32)) + indexOfProgram));
        }
        return fullString;
    }
}
