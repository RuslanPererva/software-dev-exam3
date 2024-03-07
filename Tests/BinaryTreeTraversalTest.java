import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.google.common.truth.Truth;

class BinaryTreeTraversalTest {
	@Nested
	class TestingBinaryTreeTraversal {
		@SuppressWarnings("unchecked")
		@Test
		void testInvalidDataThrowsException() {
			var strategy = Mockito.mock( TraversalStrategy.class );
			assertThrows( IllegalArgumentException.class, () -> new BinaryTreeTraversal<Object>( null ));
			assertThrows( IllegalArgumentException.class, () -> new BinaryTreeTraversal<Object>( strategy ).setStrategy( null ));
			assertThrows( IllegalArgumentException.class, () -> new BinaryTreeTraversal<Object>( strategy ).getTraversal( null ));
		}
		@SuppressWarnings("unchecked")
		@Test
		void testSetTraversal() {
			var first     = Mockito.mock( TraversalStrategy.class );
			var second    = Mockito.mock( TraversalStrategy.class );
			var traversal = new BinaryTreeTraversal<Object>( first );

			traversal.getTraversal( BinaryNode.of( "hello" ));
			Mockito.verify( first ).getTraversal( ArgumentMatchers.any() );
			
			traversal.setStrategy( second );
			traversal.getTraversal( BinaryNode.of( "hello" ));
			Mockito.verify( second ).getTraversal( ArgumentMatchers.any() );
			Mockito.verifyNoMoreInteractions( first );
			
		}
		@SuppressWarnings("unchecked")
		@Test
		void testGetTraversalCallsStrategy() {
			var strategy  = Mockito.mock( TraversalStrategy.class );
			Mockito.doReturn( List.of( 42 )).when( strategy ).getTraversal( BinaryNode.of( "hello" ));
			var traversal = new BinaryTreeTraversal<Object>( strategy );

			List<?> actual = traversal.getTraversal( BinaryNode.of( "hello" ));
			Truth.assertThat( actual ).isNotNull();
			Truth.assertThat( actual ).containsExactly( 42 );

			Mockito.verify( strategy ).getTraversal( ArgumentMatchers.any() );
		}
	}
	@Nested
	class TestingTraversals {
		private static final BinaryNode<Double>    one   = 
				BinaryNode.of( 42.0 );
		/*              42
		               x  x
        
		 */
		private static final BinaryNode<Long>      two   = 
				BinaryNode.of( 7L, 
						BinaryNode.of( 5L ), BinaryNode.of( 9L ));
		/*              7 
		             5     9
		            x x   x x
		 */
		private static final BinaryNode<String>    three = 
				BinaryNode.of( "a", 
						BinaryNode.empty(), BinaryNode.of( "b", 
								BinaryNode.empty(), BinaryNode.of( "c" )));
		/*              "a" 
                       x   "b"
                          x   "c"
                             x   x
		*/
		private static final BinaryNode<Character> four  = 
				BinaryNode.of( 'a', 
						BinaryNode.of( 'b', 
								BinaryNode.of( 'c' ), BinaryNode.empty()), BinaryNode.empty());
		/*              "a" 
		             "b"   x
		          "c"   x
		         x   x
		*/
		private static final BinaryNode<Integer>  five  = 
				BinaryNode.of( 42,
						BinaryNode.of( 21,
								BinaryNode.of( 11 ), BinaryNode.of( 32,
										BinaryNode.of( 26 ), BinaryNode.empty())), BinaryNode.of( 63,
												BinaryNode.of( 52, BinaryNode.empty(), BinaryNode.of( 58 )), BinaryNode.of( 74 )));
		/*               42 
		         21             63
		     11      32     52      74
		    x  x   26  x   x  58   x  x
		          x  x       x  x
		*/
		private static final BinaryNode<Integer>  six   = 
				BinaryNode.of( 0,
						BinaryNode.empty(), BinaryNode.of( 14,
								BinaryNode.of( 8, 
									BinaryNode.of(  2, BinaryNode.empty(), BinaryNode.of( 4, BinaryNode.empty(), BinaryNode.of( 6 ))), 
									BinaryNode.of( 10, BinaryNode.empty(), BinaryNode.of( 12 ))), BinaryNode.empty() ));
		/*               0 
		            x        14
		                  8      x
		             2        10
		           x   4     x  12
		             x   6     x  x
		                x x
		*/
		private static final BinaryNode<Long>  seven = 
				BinaryNode.of( 0L,
						BinaryNode.of( -1L,
								BinaryNode.empty(),BinaryNode.of( -4L, 
										BinaryNode.of( -3L, BinaryNode.of( -2L ), BinaryNode.empty()), 
										BinaryNode.of( -7L, BinaryNode.of( -6L, BinaryNode.of( -5L ), BinaryNode.empty() ), BinaryNode.empty() ))), BinaryNode.empty() );
		/*               0 
		            x        14
		                  8      x
		             2        10
		           x   4     x  12
		             x   6     x  x
		                x x
		*/
		@ParameterizedTest
		@MethodSource("dataGetTraversal")
		<T> void testGetTraversal(BinaryNode<T> data, TraversalStrategy<T> strategy, List<T> expected) {
			var message   = "%s".formatted( strategy.getClass().getSimpleName() );
			var traversal = new BinaryTreeTraversal<T>( strategy );
			var actual    = traversal.getTraversal( data );
			Truth.assertWithMessage( message ).that( actual ).isNotNull();
			Truth.assertWithMessage( message ).that( actual ).containsExactlyElementsIn( expected );
		}
		static Stream<Arguments> dataGetTraversal() {
			var in    = new InorderStrategy<>();
			var pre   = new PreorderStrategy<>();
			var post  = new PostorderStrategy<>();
			return Stream.of(
					// In-order (LnR)
					Arguments.of( one,   in,    List.of( 42.0 )),
					Arguments.of( two,   in,    List.of( 5L, 7L, 9L )),
					Arguments.of( three, in,    List.of( "a", "b", "c" )),
					Arguments.of( four,  in,    List.of( 'a', 'b', 'c' )),
					Arguments.of( five,  in,    List.of( 11, 21, 26, 32, 42, 52, 58, 63, 74 )),
					Arguments.of( six,   in,    List.of( 0, 2, 4, 6, 8, 10, 12, 14 )),
					Arguments.of( seven, in,    List.of( -1L, -2L, -3L, -4L, -5L, -6L, -7L, 0L )),
					// Pre-order (nLR)
					Arguments.of( one,   pre,   List.of( 42.0 )),
					Arguments.of( two,   pre,   List.of( 7L, 5L, 9L )),
					Arguments.of( three, pre,   List.of( "a", "b", "c" )),
					Arguments.of( four,  pre,   List.of( 'c', 'b', 'a' )),
					Arguments.of( five,  pre,   List.of(  42, 21, 11, 32, 26, 63, 52, 58, 74 )),
					Arguments.of( six,   pre,   List.of(  0, 14, 8, 2, 4, 6, 10, 12 )),
					Arguments.of( seven, pre,   List.of(  0L, -1L, -4L, -3L, -2L, -7L, -6L, -5L )),
					// Post-order (LRn)
					Arguments.of( one,   post,  List.of( 42.0 )),
					Arguments.of( two,   post,  List.of( 5L, 9L, 7L )),
					Arguments.of( three, post,  List.of( "c", "b", "a" )),
					Arguments.of( four,  post,  List.of( 'c', 'b', 'a' )),
					Arguments.of( five,  post,  List.of(  11, 26, 32, 21, 58, 52, 74, 63, 42 )),
					Arguments.of( six,   post,  List.of(  6, 4, 2, 12, 10, 8, 14, 0 )),
					Arguments.of( seven, post,  List.of(  -2L, -3L, -5L, -6L, -7L, -4L, -1L, 0L ))
					);
		}
	}
}