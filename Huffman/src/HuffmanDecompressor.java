import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class HuffmanDecompressor {
    private String filePath;

    public HuffmanDecompressor(String filePath) {
        this.filePath = filePath;
    }

    // read compressed file
    // get dictionary
    // read bitcount
    // if bitcount is 0 stop reading
    // read data bit by bit and check for equivalence in extracted dictionary
    // if found write in decompressed file
    // else wait till accumulation of a write code

    void decompress() throws IOException {
        long then = System.currentTimeMillis();
        DataInputStream in = new DataInputStream(new FileInputStream(filePath));
        FileNameManipulator fm = new FileNameManipulator();
        String decompressedFilePath = fm.extractedFilePath(filePath);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(decompressedFilePath));
        DictionaryEmbedder embedder = new DictionaryEmbedder();
        HashMap<Data, Byte> dictionary = embedder.extractDictionary(in);
        long fileBytesCount = embedder.fileBytes;
        long bitCount = in.readLong();
        Data sequence = new Data(0, 0);
        int z=-1;// for debugging;
        byte[] outBuffer = new byte[10000000];
        byte[] buffer = new byte[100*1024*1024];
        int indx = 0;
        while ((bitCount > 0) && (z = in.read(buffer)) !=-1) {
            for (int k=0;k < z; k++) {
                byte b = buffer[k];
                for (int i = 7; i >= 0; i--) {
                    if (bitCount == 0)
                        break;
                    sequence.code = sequence.code << 1;
                    int x = b & (1 << i);
                    if (x != 0) {
                        sequence.code |= 1;
                    }
                    bitCount--;
                    sequence.len++;
                    if (dictionary.containsKey(sequence)) {
                        Byte chunk = dictionary.get(sequence);
                        if (indx == (outBuffer.length)) {
                            out.write(outBuffer, 0, outBuffer.length);
                            indx = 0;
                        }
                        outBuffer[indx++] = chunk;
                        fileBytesCount -= 1;
                        sequence.code = 0;
                        sequence.len = 0;
                    }
                }
            }
        }
        if (indx != 0) {
            out.write(outBuffer, 0, indx);
        }
        if (fileBytesCount != 0) {
            System.err.println("error in decompression");
            System.err.println("fileBytesCount = " + fileBytesCount);
            System.err.println("indx = " + indx);
            System.err.println("outBuffer.length = " + outBuffer.length);
            System.err.println("z = " + z);
            System.err.println("bitCount = " + bitCount);
        }
        long now = System.currentTimeMillis();
        in.close();
        out.close();
        System.out.println("decompressing time : " + (now - then) + " ms");
        
        // Verify file integrity with SHA-256 checksum
        verifyFileIntegrity(decompressedFilePath);
    }
    
    /**
     * Verifies the integrity of the decompressed file by comparing its SHA-256 hash
     * with the hash of the original file (if it exists)
     */
    private void verifyFileIntegrity(String decompressedFilePath) {
        try {
            // Get original file path (assuming it's in parent of parent directory)
            File decompressedFile = new File(decompressedFilePath);
            String fileName = decompressedFile.getName();
            File parentParentDir = decompressedFile.getParentFile();
            File originalFile = new File(parentParentDir, fileName);
            
            // Check if original file exists
            if (!originalFile.exists()) {
                System.out.println("Original file not found for integrity check: " + originalFile.getAbsolutePath());
                return;
            }
            
            // Calculate SHA-256 checksums
            String originalChecksum = calculateSHA256(originalFile.getPath());
            String decompressedChecksum = calculateSHA256(decompressedFilePath);
            
            // Always print the checksums
            System.out.println("SHA-256 Checksums:");
            System.out.println("  Original file:   " + originalChecksum + " (" + originalFile.getAbsolutePath() + ")");
            System.out.println("  Decompressed:    " + decompressedChecksum + " (" + decompressedFilePath + ")");
            
            // Compare checksums
            if (originalChecksum.equals(decompressedChecksum)) {
                System.out.println("✓ Integrity check passed: Decompressed file matches original file");
            } else {
                System.out.println("✗ Integrity check failed: Checksums don't match");
                System.out.println("  Original file: " + originalChecksum);
                System.out.println("  Decompressed: " + decompressedChecksum);
            }
        } catch (Exception e) {
            System.err.println("Error during integrity verification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Calculates SHA-256 checksum of a file
     */
    private String calculateSHA256(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        Path path = Paths.get(filePath);
        byte[] fileBytes = Files.readAllBytes(path);
        byte[] hashBytes = digest.digest(fileBytes);
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hashBytes) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        
        return hexString.toString();
    }

    public static void main(String[] args) {
        HuffmanDecompressor decomp = new HuffmanDecompressor("./kimokono.txt.huffmancompressed");
        try {
            decomp.decompress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
