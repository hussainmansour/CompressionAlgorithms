import java.io.File;
import java.io.IOException;

public class FileNameManipulator {

    String compressedFilePath(String originalFilePath,int chunkSize) throws IOException {
        File originalFile = new File(originalFilePath);
        if(!originalFile.exists()){
            System.out.println("file does not exist");
            System.exit(1);
        }
        String originalFileName = originalFile.getName();
        return originalFile.getParent()+ File.separator+originalFileName+".huffmancompressed";
    }

    String extractedFilePath(String compressedFilePath){
        File compressedFile = new File(compressedFilePath);
        if(!compressedFile.exists()){
            System.out.println("file does not exist");
            System.exit(1);
        }
        if(!compressedFile.getName().endsWith(".huffmancompressed")){
            System.out.println("not the right extension");
            System.exit(1);
        }
        return compressedFile.getParent()+File.separator+"extracted."+compressedFile.getName().replaceFirst(".huffmancompressed","");
    }

//    public static void main(String[] args) throws IOException {
//        FileNameManipulator fm = new FileNameManipulator();
//        String s = fm.compressedFilePath("C:\\Users\\Administrator\\IdeaProjects\\HuffmanCode\\src\\Alice.txt",2);
//        String ds = fm.extractedFilePath("C:\\Users\\Administrator\\IdeaProjects\\HuffmanCode\\src\\20011116.2.Alice.txt.hc");
//        System.out.println("ds = " + ds);
//        System.out.println("s = " + s);
//    }
}
