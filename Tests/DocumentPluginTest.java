import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.truth.Truth;

class DocumentPluginTest {
	private static final Class<?> INTERFACE    = DocumentPlugin   .class;
	private static final Class<?> COUNT_WORDS  = PluginCountWords .class;
	private static final Class<?> REPLACE_TEXT = PluginReplaceText.class;

	private static final String EMPTY               = "";
	private static final String ONE                 = "R2-D2";
	private static final String TWO                 = "Star Wars";
	private static final String LOREM_IPSUM         = "Lorem ipsum dolor sit amet";
	private static final String ALICE_IN_WONDERLAND = "Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, \"and what is the use of a book,\" thought Alice \"without pictures or conversations?\"";
	private static final String WIZARD_OF_OZ        = "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a farmer, and Aunt Em, who was the farmerâ€™s wife. Their house was small, for the lumber to build it had to be carried by wagon many miles. There were four walls, a floor and a roof, which made one room; and this room contained a rusty looking cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in another corner. There was no garret at all, and no cellar -â€”except a small hole dug in the ground, called a cyclone cellar, where the family could go in case one of those great whirlwinds arose, mighty enough to crush any building in its path. It was reached by a trap door in the middle of the floor, from which a ladder led down into the small, dark hole.";
	private static final String DON_QUIXOTE         = "In a village of La Mancha, the name of which I have no desire to call to mind, there lived not long since one of those gentlemen that keep a lance in the lance-rack, an old buckler, a lean hack, and a greyhound for coursing. An olla of rather more beef than mutton, a salad on most nights, scraps on Saturdays, lentils on Fridays, and a pigeon or so extra on Sundays, made away with three-quarters of his income. The rest of it went in a doublet of fine cloth and velvet breeches and shoes to match for holidays, while on week-days he made a brave figure in his best homespun. He had in his house a housekeeper past forty, a niece under twenty, and a lad for the field and market-place, who used to saddle the hack as well as handle the bill-hook. The age of this gentleman of ours was bordering on fifty; he was of a hardy habit, spare, gaunt-featured, a very early riser and a great sportsman. They will have it his surname was Quixada or Quesada (for here there is some difference of opinion among the authors who write on the subject), although from reasonable conjectures it seems plain that he was called Quexana. This, however, is of but little importance to our tale; it will be enough not to stray a hairâ€™s breadth from the truth in the telling of it.";

	@Test
	void testFields() {
		Consumer<Class<?>> hasNoFields = c -> Arrays.stream ( c.getDeclaredFields() )
                .filter ( f->!f.isSynthetic() )
                .forEach( f->fail( String.format("no field should exist; found '%s'", f.getName() )));
		Consumer<Class<?>> allFieldsPrivate = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' must be private", name ))
					.that( Modifier.isPrivate( mod ))
					.isTrue();
				});
		Consumer<Class<?>> noFieldsStatic  = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' cannot be static",  name ))
					.that( Modifier.isStatic( mod ))
					.isFalse();
				});
		hasNoFields     .accept( COUNT_WORDS  );
		allFieldsPrivate.accept( REPLACE_TEXT );
		noFieldsStatic  .accept( REPLACE_TEXT );
	}
	@Test
	void testTypeAndSuperclass() {
		Truth.assertThat( COUNT_WORDS .getSuperclass()).isEqualTo ( Object.class );
		Truth.assertThat( REPLACE_TEXT.getSuperclass()).isEqualTo ( Object.class );
		
		Truth.assertThat( COUNT_WORDS  ).isAssignableTo( INTERFACE );
		Truth.assertThat( REPLACE_TEXT ).isAssignableTo( INTERFACE );
	}
	@ParameterizedTest
	@MethodSource("dataPlugin")
	void testPlugin(Class<?> clazz, DocumentPlugin plugin, String name) {
		Truth.assertThat( plugin.getName() ).isEqualTo( name );
		
		var s = plugin.getSupplier();
		Truth.assertThat( s ).isNotNull();
		var b = s.get();
		Truth.assertThat( b ).isNotNull();
		Truth.assertThat( b ).isNotSameInstanceAs( plugin );
		Truth.assertThat( b ).isInstanceOf( clazz );
		Truth.assertThat( b.getName() ).isEqualTo( name );
	}
	static Stream<Arguments> dataPlugin() {
		return Stream.of(
				Arguments.of( COUNT_WORDS,  new PluginCountWords(),  "count_words"   ),
				Arguments.of( REPLACE_TEXT, new PluginReplaceText(), "replace_text"  )
		);
	}

	@Test
	void testNullDocumentThrowsException() {
		for (var plugin : List.of( new PluginCountWords(), new PluginReplaceText() )) {
			var e      = assertThrows( 
					IllegalArgumentException.class,
					() -> plugin.apply( null ));
			Truth.assertThat( e.getMessage() )
			     .isEqualTo ( "document cannot be null" );
		}
	}
	@ParameterizedTest
	@MethodSource("dataCountWords")
	void testCountWords(Document document, int expected) {
		var plugin = new PluginCountWords();
		var actual = plugin.apply( document );
		Truth.assertThat( actual ).isNotNull();
		Truth.assertThat( actual ).isInstanceOf( Integer.class );
		Truth.assertThat( actual ).isEqualTo( expected );
	}
	static Stream<Arguments> dataCountWords() {
		return Stream.of(
				Arguments.of( new Document( EMPTY ), 0 ),
				Arguments.of( new Document( ONE ), 1 ),
				Arguments.of( new Document( TWO ), 2 ),
				Arguments.of( new Document( LOREM_IPSUM ), 5 ),
				Arguments.of( new Document( ALICE_IN_WONDERLAND ), 57 ),
				Arguments.of( new Document( WIZARD_OF_OZ ), 165 ),
				Arguments.of( new Document( DON_QUIXOTE ), 237 )
		);
	}

	@Test
	void testReplaceTextWithNoValuesThrowsException() {
		var plugin = new PluginReplaceText();
		var doc    = new Document( LOREM_IPSUM );
		var e      = assertThrows( 
				IllegalArgumentException.class,
				() -> plugin.apply( doc  ));
		Truth.assertThat( e.getMessage() )
		     .isEqualTo ( "find/replace values not set" );
	}
	@Test
	void testReplaceTextWithNullValuesThrowsException() {
		var plugin = new PluginReplaceText();
		var e      = assertThrows( 
				IllegalArgumentException.class,
				() -> plugin.setReplace( null, "the" ));
		Truth.assertThat( e.getMessage() )
		     .isEqualTo ( "find cannot be null" );
		var f      = assertThrows( 
				IllegalArgumentException.class,
				() -> plugin.setReplace( "the", null ));
		Truth.assertThat( f.getMessage() )
		     .isEqualTo ( "replace cannot be null" );
	}
	@ParameterizedTest
	@MethodSource("dataReplaceText")
	void testReplaceText(Document document, String find, String replace, String expected) {
		var plugin = new PluginReplaceText();
		plugin.setReplace( find, replace );
		var actual = plugin.apply( document );
		Truth.assertThat( actual ).isNotNull();
		Truth.assertThat( actual ).isInstanceOf( String.class );
		Truth.assertThat( actual ).isEqualTo( expected );
	}
	static Stream<Arguments> dataReplaceText() {
		return Stream.of(
				// no change
				Arguments.of( new Document( EMPTY ),               "the", "THE", EMPTY ),
				Arguments.of( new Document( ONE ),                 "2-D", "2-D", ONE ),
				Arguments.of( new Document( TWO ),                 "r W", "r W", TWO ),
				Arguments.of( new Document( LOREM_IPSUM ),         "ips", "ips", LOREM_IPSUM ),
				Arguments.of( new Document( ALICE_IN_WONDERLAND ), "hOW", "How", ALICE_IN_WONDERLAND ),
				Arguments.of( new Document( WIZARD_OF_OZ ),        "the", "the", WIZARD_OF_OZ ),
				Arguments.of( new Document( DON_QUIXOTE ),         "zzz", "???", DON_QUIXOTE ),
				// changes
				Arguments.of( new Document( ONE ),                 "2",     "8",    "R8-D8" ),
				Arguments.of( new Document( TWO ),                 "ar",    "art",  "Start Warts" ),
				Arguments.of( new Document( LOREM_IPSUM ),         "t ",    "tx",   "Lorem ipsum dolor sitxamet" ),
				Arguments.of( new Document( LOREM_IPSUM ),         "m ",    "m_",   "Lorem_ipsum_dolor sit amet" ),
				Arguments.of( new Document( LOREM_IPSUM ),         "o",     "000",  "L000rem ipsum d000l000r sit amet" ),
				Arguments.of( new Document( ALICE_IN_WONDERLAND ), "\"",    "|",    "Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, |and what is the use of a book,| thought Alice |without pictures or conversations?|" ),
				Arguments.of( new Document( ALICE_IN_WONDERLAND ), "book",  "ipad", "Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the ipad her sister was reading, but it had no pictures or conversations in it, \"and what is the use of a ipad,\" thought Alice \"without pictures or conversations?\"" ),
				Arguments.of( new Document( WIZARD_OF_OZ ),        "Henry", "X",    "Dorothy lived in the midst of the great Kansas prairies, with Uncle X, who was a farmer, and Aunt Em, who was the farmerâ€™s wife. Their house was small, for the lumber to build it had to be carried by wagon many miles. There were four walls, a floor and a roof, which made one room; and this room contained a rusty looking cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. Uncle X and Aunt Em had a big bed in one corner, and Dorothy a little bed in another corner. There was no garret at all, and no cellar -â€”except a small hole dug in the ground, called a cyclone cellar, where the family could go in case one of those great whirlwinds arose, mighty enough to crush any building in its path. It was reached by a trap door in the middle of the floor, from which a ladder led down into the small, dark hole." ),
				Arguments.of( new Document( WIZARD_OF_OZ ),        "a",     "",     "Dorothy lived in the midst of the gret Knss priries, with Uncle Henry, who ws  frmer, nd Aunt Em, who ws the frmerâ€™s wife. Their house ws smll, for the lumber to build it hd to be crried by wgon mny miles. There were four wlls,  floor nd  roof, which mde one room; nd this room contined  rusty looking cookstove,  cupbord for the dishes,  tble, three or four chirs, nd the beds. Uncle Henry nd Aunt Em hd  big bed in one corner, nd Dorothy  little bed in nother corner. There ws no grret t ll, nd no cellr -â€”except  smll hole dug in the ground, clled  cyclone cellr, where the fmily could go in cse one of those gret whirlwinds rose, mighty enough to crush ny building in its pth. It ws reched by  trp door in the middle of the floor, from which  ldder led down into the smll, drk hole." ),
				Arguments.of( new Document( DON_QUIXOTE ),         "it",    "foo",  "In a village of La Mancha, the name of which I have no desire to call to mind, there lived not long since one of those gentlemen that keep a lance in the lance-rack, an old buckler, a lean hack, and a greyhound for coursing. An olla of rather more beef than mutton, a salad on most nights, scraps on Saturdays, lentils on Fridays, and a pigeon or so extra on Sundays, made away wfooh three-quarters of his income. The rest of foo went in a doublet of fine cloth and velvet breeches and shoes to match for holidays, while on week-days he made a brave figure in his best homespun. He had in his house a housekeeper past forty, a niece under twenty, and a lad for the field and market-place, who used to saddle the hack as well as handle the bill-hook. The age of this gentleman of ours was bordering on fifty; he was of a hardy habfoo, spare, gaunt-featured, a very early riser and a great sportsman. They will have foo his surname was Quixada or Quesada (for here there is some difference of opinion among the authors who wrfooe on the subject), although from reasonable conjectures foo seems plain that he was called Quexana. This, however, is of but lfootle importance to our tale; foo will be enough not to stray a hairâ€™s breadth from the truth in the telling of foo." )
		);
	}
}