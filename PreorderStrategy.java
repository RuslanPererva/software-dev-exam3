import java.util.*;

public class PreorderStrategy<E> implements TraversalStrategy<E> {

	@Override
	public List<E> getTraversal(BinaryNode<E> tree) {
		List <E> TR = new ArrayList();
		if (tree == null) {
			throw new IllegalArgumentException();
		}
		if (tree != BinaryNode.empty()) {
			TR.add(tree.value());
			TR.addAll(getTraversal(tree.left()));
			TR.addAll(getTraversal(tree.right()));
		}
		return TR;
	}

}
