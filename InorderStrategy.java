import java.util.ArrayList;
import java.util.List;

public class InorderStrategy<E> implements TraversalStrategy<E> {
	@Override
	public List<E> getTraversal(BinaryNode<E> tree) {
		List <E> TR = new ArrayList();
		if (tree == null) {
			throw new IllegalArgumentException();
		}
		if (tree != BinaryNode.empty()) {
			TR.addAll(getTraversal(tree.left()));
			TR.add(tree.value());
			TR.addAll(getTraversal(tree.right()));
		}
		return TR;
	}

}
