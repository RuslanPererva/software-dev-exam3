import java.util.*;

public class BinaryTreeTraversal<E> {
	private TraversalStrategy<E> strat;
	
	public BinaryTreeTraversal (TraversalStrategy<E> s) {
		if (s == null) {
			throw new IllegalArgumentException();
		}
		this.setStrategy(s);
	}
	public void setStrategy(TraversalStrategy<E> s) {
		if (s == null) {
			throw new IllegalArgumentException();
		}
		else {
			this.strat = s;
		}
	}
	public List<E> getTraversal(BinaryNode <E> root){
		if (root == null) {
			throw new IllegalArgumentException();
		}
		return strat.getTraversal(root);
	}
	
}
