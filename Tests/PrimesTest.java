import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.truth.Truth;

public class PrimesTest {
	private static final Class<?> PRIMES = Primes.class;
	@Test
	void testFields() {
		Consumer<Class<?>> fieldsAllStatic = 
				c -> Arrays.stream ( c.getDeclaredFields() )
				           .filter ( f->!f.isSynthetic() )
				           .forEach( f->Truth.assertWithMessage( "field '%s.%s' should be static"    .formatted( c.getSimpleName(), f.getName() )).that( Modifier.isStatic ( f.getModifiers() )).isTrue() );
		Consumer<Class<?>> fieldsNonePrivate   = 
				c -> Arrays.stream ( c.getDeclaredFields() )
				           .filter ( f->!f.isSynthetic() )
				           .forEach( f->Truth.assertWithMessage( "field '%s.%s' shouldn't be private".formatted( c.getSimpleName(), f.getName() )).that( Modifier.isPrivate( f.getModifiers() )).isFalse() );
				Consumer<Class<?>> noCollections = 
						c -> Arrays.stream ( c.getDeclaredFields() )
		                           .filter ( f -> !f.isSynthetic() )
		                           .forEach( f -> {
		             			 		var cName  = c.getSimpleName();
		              			 		var fName  = f.getName();
		              			 		var fType  = f.getType();
		              			 		var fClass = fType.getSimpleName();

			            				Truth.assertWithMessage( "field '%s.%s' can't be a Java %s"   .formatted( cName, fName, fClass ))
				            				 .that( Collection.class.isAssignableFrom( fType ))
				            				 .isFalse();
			            				Truth.assertWithMessage( "field '%s.%s' can't be a Java %s"   .formatted( cName, fName, fClass ))
				            				 .that( Dictionary.class.isAssignableFrom( fType ))
				            				 .isFalse();
			            				Truth.assertWithMessage( "field '%s.%s' can't be a Java %s"   .formatted( cName, fName, fClass ))
				            				 .that( Map       .class.isAssignableFrom( fType ))
				            				 .isFalse();
			            				Truth.assertWithMessage( "field '%s.%s' can't be a Java array".formatted( cName, fName ))
			            					 .that( fType.isArray() )
			            					 .isFalse();
		                          });
		fieldsAllStatic  .accept( PRIMES );
		fieldsNonePrivate.accept( PRIMES );
		noCollections    .accept( PRIMES );
	}

	@ParameterizedTest
	@MethodSource("dataRange")
	void testRange(int input, List<String> expected) {
		IntFunction<IntStream> function = Primes.RANGE;
		assertThat           ( function ).isNotNull();
		
		var         stream = function.apply( input );
		assertThat( stream ).isNotNull();
		var         actual = stream.boxed().collect( Collectors.toList() );
		assertThat( actual ).containsExactlyElementsIn( expected ).inOrder();
	}
	static Stream<Arguments> dataRange() {
	    return 
	    Stream.of(
	    		Arguments.of( 
	    				-42, 
	    				List.of() ),
	    		Arguments.of( 
	    				2, 
	    				List.of() ),
	    		Arguments.of( 
	    				3, 
	    				List.of( 2 ) ),
	    		Arguments.of( 
	    				10, 
	    				List.of( 2,3,4,5,6,7,8,9 ) ),
	    		Arguments.of( 
	    				20,
	    				List.of( 2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19 ) )
	    		);
	}
	@ParameterizedTest
	@MethodSource("dataDivisors")
	void testDivisors(int input, List<String> expected) {
		IntFunction<IntStream> function = Primes.DIVISORS;
		assertThat           ( function ).isNotNull();
		
		var         stream = function.apply( input );
		assertThat( stream ).isNotNull();
		var         actual = stream.boxed().collect( Collectors.toList() );
		assertThat( actual ).containsExactlyElementsIn( expected ).inOrder();
	}
	static Stream<Arguments> dataDivisors() {
	    return 
	    Stream.of(
	    		Arguments.of( 
	    				-1, 
	    				List.of() ),
	    		Arguments.of( 
	    				3, 
	    				List.of() ),
	    		Arguments.of( 
	    				4, 
	    				List.of( 2 ) ),
	    		Arguments.of( 
	    				10, 
	    				List.of( 2,5 ) ),
	    		Arguments.of( 
	    				21, 
	    				List.of( 3,7 ) ),
	    		Arguments.of( 
	    				56, 
	    				List.of( 2,4,7,8,14,28 ) ),
	    		Arguments.of( 
	    				121,
	    				List.of( 11 ) )
	    		);
	}
	@ParameterizedTest
	@MethodSource("dataIsPrime")
	void testIsPrime(int input, boolean expected) {
		IntPredicate function = Primes.IS_PRIME;
		assertThat ( function ).isNotNull();

		var         actual = function.test( input );
		assertThat( actual ).isEqualTo( expected );
	}
	static Stream<Arguments> dataIsPrime() {
	    return 
	    Stream.of(
	    		Arguments.of( 
	    				-3, 
	    				false ),
	    		Arguments.of( 
	    				3, 
	    				true ),
	    		Arguments.of( 
	    				4, 
	    				false ),
	    		Arguments.of( 
	    				11, 
	    				true ),
	    		Arguments.of( 
	    				42, 
	    				false ),
	    		Arguments.of( 
	    				83, 
	    				true ),
	    		Arguments.of( 
	    				121,
	    				false )
	    		);
	}
	@ParameterizedTest
	@MethodSource("dataPrimes")
	void testPrimes(int skip, OptionalInt expected) {
		Supplier<IntStream> function = Primes.PRIME_STREAM;
		assertThat        ( function ).isNotNull();
		
		var         actual = function.get().skip( skip ).findFirst();
		assertThat( actual ).isEqualTo( expected );
	}
	static Stream<Arguments> dataPrimes() {
	    return 
	    Stream.of(
	    		Arguments.of( 
	    				0, 
	    				OptionalInt.of( 2 ) ),
	    		Arguments.of( 
	    				1, 
	    				OptionalInt.of( 3 ) ),
	    		Arguments.of( 
	    				2, 
	    				OptionalInt.of( 5 ) ),
	    		Arguments.of( 
	    				10, 
	    				OptionalInt.of( 31 ) ),
	    		Arguments.of( 
	    				20, 
	    				OptionalInt.of( 73 ) ),
	    		Arguments.of( 
	    				100, 
	    				OptionalInt.of( 547 ) )
	    		);
	}
}