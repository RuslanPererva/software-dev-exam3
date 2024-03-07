import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

interface Primes {
	static final IntFunction<IntStream> RANGE        = n -> IntStream.range(2, n).filter(x->x>0);
	static final IntFunction<IntStream> DIVISORS     = n -> RANGE.apply(n).filter(x-> n%x ==0);
	static final IntPredicate           IS_PRIME     = n -> DIVISORS.apply(n).count()==0&&n>0;
	//https://www.rapid7.com/blog/post/2015/10/16/exploring-lambdas-and-streams-in-java-8/
	// got the use of '&&' from here
	static final Supplier<IntStream>    PRIME_STREAM = () -> RANGE.apply(Integer.MAX_VALUE).filter(IS_PRIME);
}