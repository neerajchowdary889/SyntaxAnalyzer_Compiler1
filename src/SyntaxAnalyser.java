
import java.io.File;
import java.util.ArrayList;


public class SyntaxAnalyser {

    public static ArrayList<File> getJackFiles(File dir){

        File[] files = dir.listFiles();

        ArrayList<File> result = new ArrayList<File>();

        if (files == null) return result;

        for (File f:files){

            if (f.getName().endsWith(".jack")){
                result.add(f);
            }
        }

        return result;

    }

    public static void main(String[] args) {

            File fileIn = new File("C:\\Users\\naidu\\Desktop\\Vipparla Neeraj Chowdary\\AM.EN.U4AIE21067\\nand2tetris\\nand2tetris\\projects\\10\\ArrayTest\\Main.jack");

            String fileOutPath = "", tokenFileOutPath = "";

            File fileOut,tokenFileOut;

            ArrayList<File> jackFiles = new ArrayList<File>();

            if (fileIn.isFile()) {

                String path = fileIn.getAbsolutePath();

                if (!path.endsWith(".jack")) {

                    throw new IllegalArgumentException(".jack file is required!");

                }

                jackFiles.add(fileIn);

            } else if (fileIn.isDirectory()) {

                jackFiles = getJackFiles(fileIn);

                if (jackFiles.size() == 0) {

                    throw new IllegalArgumentException("No jack file in this directory");

                }

            }

            for (File f: jackFiles) {

                fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + "2.xml";
                tokenFileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + "T2.xml";
                fileOut = new File(fileOutPath);
                tokenFileOut = new File(tokenFileOutPath);

                CompilationEngine compilationEngine = new CompilationEngine(f,fileOut,tokenFileOut);

                compilationEngine.compileClass();

                System.out.println("File created : " + fileOutPath);
                System.out.println("File created : " + tokenFileOutPath);
            }

        }

    }
