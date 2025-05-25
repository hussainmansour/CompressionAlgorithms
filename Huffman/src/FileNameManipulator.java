import java.io.File;
import java.io.IOException;

public class FileNameManipulator {

    String compressedFilePath(String originalFilePath) throws IOException {
        // Get the directory where the JAR is running
        String jarDirectory = new File(System.getProperty("user.dir")).getAbsolutePath();
        
        // Create a 'compressed' directory if it doesn't exist
        File compressedDir = new File(jarDirectory + File.separator + "Huffman");
        if (!compressedDir.exists()) {
            boolean created = compressedDir.mkdir();
            if (!created) {
                throw new IOException("Failed to create 'Huffman' directory");
            }
        }
        
        // Get original file information
        File originalFile = new File(originalFilePath);
        if (!originalFile.exists()) {
            System.out.println("File does not exist");
            System.exit(1);
        }
        
        String originalFileName = originalFile.getName();
        
        // Return the path in the compressed directory
        return compressedDir.getAbsolutePath() + File.separator + originalFileName + ".huffmancompressed";
    }

    String extractedFilePath(String compressedFilePath) {
        File compressedFile = new File(compressedFilePath);
        if (!compressedFile.exists()) {
            System.out.println("File does not exist");
            System.exit(1);
        }
        if (!compressedFile.getName().endsWith(".huffmancompressed")) {
            System.out.println("Not the right extension");
            System.exit(1);
        }
        return compressedFile.getParent()+File.separator+compressedFile.getName().replaceFirst(".huffmancompressed","");
    }

//    public static void main(String[] args) throws IOException {
//        FileNameManipulator fm = new FileNameManipulator();
//        String s = fm.compressedFilePath("/home/karim/CompressionAlgorithms/Huffman/fsdhkbkasdc.txt");
//        String ds = fm.extractedFilePath("/home/karim/CompressionAlgorithms/Huffman/Huffman/fsdhkbkasdc.txt.huffmancompressed");
//        System.out.println("ds = " + ds);
//        System.out.println("s = " + s);
//    }
}
