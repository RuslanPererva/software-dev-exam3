import java.util.List;

public interface TraversalStrategy<E> {
	List<E> getTraversal(BinaryNode<E> tree);
}