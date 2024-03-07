import java.util.Objects;

public class BinaryNode<T> {
	private static final BinaryNode<?> EMPTY = new BinaryNode<>() {
		@Override
		public Object value() {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}
		@Override
		public int hashCode() {
			return Objects.hash( this );
		}
	};
	private final T             value;
	private final BinaryNode<T> left;
	private final BinaryNode<T> right;

	@SuppressWarnings("unchecked")
	public static <T> BinaryNode<T> empty() {
		return (BinaryNode<T>)EMPTY;
	}
	public static <T> BinaryNode<T> of(T value) {
		return BinaryNode.of( value, empty(), empty() );
	}
	public static <T> BinaryNode<T> of(T value, BinaryNode<T> left, BinaryNode<T> right) {
		return new BinaryNode<>( value, left, right );
	}
	@SuppressWarnings("unchecked")
	private BinaryNode() {
		this.value = (T)new Object();
		this.left  = this;
		this.right = this;
	}
	private BinaryNode(T value, BinaryNode<T> left, BinaryNode<T> right) {
		this.value = Objects.requireNonNull( value );
		this.left  = Objects.requireNonNull( left  );
		this.right = Objects.requireNonNull( right );
	}
	
	public T value() {
		return value;
	}
	public BinaryNode<T> left() {
		return left;
	}
	public BinaryNode<T> right() {
		return right;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			if (obj == this) {
				return true;
			}
			if (obj.getClass() == getClass()) {
				var other = (BinaryNode<?>) obj;
				if (value.getClass() == other.value.getClass() &&
						value.equals( other.value ) &&
						left .equals( other.left  ) && 
						right.equals( other.right )) {
					return true;
				}
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		return Objects.hash( value );
	}
}