import java.io.*;
import java.util.Objects;

import static java.lang.System.exit;

public class Main {
    //“I acknowledge that I am aware of the academic integrity guidelines of this course, and that I worked on this assignment independently without any unauthorized help”
    public static void main(String[] args) {
        if(args.length<2){
            System.out.println("invalid args count");
            exit(1);
        }
        String mode = args[0];
        switch (mode){
            case "c" :
                if(args.length<2 || Objects.equals(args[1], "")){
                    System.out.println("missing args");
                    exit(1);
                }
                HuffmanCompressor compressor = new HuffmanCompressor(args[1]);
                try {
                    compressor.compress();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "d" :
                if(Objects.equals(args[1], "")){
                    System.out.println("missing args");
                    exit(1);
                }
                HuffmanDecompressor decompressor = new HuffmanDecompressor(args[1]);
                try {
                    decompressor.decompress();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                System.out.println("invalid argument : mode "+mode+" does not exist");
        }
    }
}