class Node{
    Long frequency;
    Byte chunk;
    Node left;
    Node right;
    Boolean isLeaf;

    public Node(Long frequency, Byte chunk, Node left, Node right, Boolean isLeaf) {
        this.frequency = frequency;
        this.chunk = chunk;
        this.left = left;
        this.right = right;
        this.isLeaf = isLeaf;
    }

    public Node(Long frequency, Node left, Node right, Boolean isLeaf) {
        this.frequency = frequency;
        this.left = left;
        this.right = right;
        this.isLeaf = isLeaf;
    }


    @Override
    public String toString() {
        return "Node{" +
                "frequency=" + frequency +
                ", chunk=" + chunk +
                ", isLeaf=" + isLeaf +
                '}';
    }
}