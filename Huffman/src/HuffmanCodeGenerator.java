import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class HuffmanCodeGenerator {


    private HashMap<Byte, Data> allMap;

    public HuffmanCodeGenerator(HashMap<Byte, Data> allMap) {
        this.allMap = allMap;
    }

    //prepare the queue
    private PriorityQueue<Node> initialize(){
        PriorityQueue<Node> queue = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return (int)(o1.frequency- o2.frequency);
            }
        });
        for (Byte chunk :allMap.keySet()) {
            Node leaf = new Node(allMap.get(chunk).freq, chunk, null, null, true);
            queue.add(leaf);
        }
        return queue;
    }

    private Node codeTree(PriorityQueue<Node> characters){
        while(characters.size()>1){
            Node left = characters.poll();
            Node right = characters.poll();
            long newValue = left.frequency + right.frequency;
            Node internalNode = new Node(newValue,left,right,false);
            characters.add(internalNode);
        }
        return characters.poll();
    }

    private void getCode(Node root, long code,int bitLen){
//        System.out.println(root);
        if(root.isLeaf){
            Data p = allMap.get(root.chunk);
            p.code = code;
            p.len = bitLen;
//            this.dictionary.put(root.chunk,new Pair(code,bitLen));
            return;
        }
        getCode(root.left,(code<<1),bitLen+1);
        getCode(root.right,((code<<1)|1),bitLen+1);
    }

    HashMap<Byte, Data> getDictionary(){
        PriorityQueue<Node> queue = initialize();
        Node root = codeTree(queue);
        getCode(root,0,0);
        return this.allMap;
    }

   public static void main(String[] args) throws IOException {
       FrequencyCounter fc = new FrequencyCounter("huffman compression/src/kimokono.txt");
       HashMap<Byte,Data> freqs = fc.countFrequency();
       HuffmanCodeGenerator gem = new HuffmanCodeGenerator(freqs);
       gem.getDictionary();
       System.out.println(gem.allMap);
   }



}
