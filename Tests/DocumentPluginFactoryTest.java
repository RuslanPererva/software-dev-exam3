import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;

class DocumentPluginFactoryTest {
	private static final Class<?>                        CLASS       = DocumentPluginFactory.class;
	private static final Supplier<DocumentPluginFactory> NEW_FACTORY = DocumentPluginFactory::new;
	@BeforeEach
	void clearMap() {
		Function<Class<?>,List<Field>> getStaticFields = clazz -> Arrays.stream ( clazz.getDeclaredFields() )
				.filter ( f -> !f.isSynthetic())
				.filter ( f -> Modifier.isStatic ( f.getModifiers() ))
				.collect( Collectors.toList() );
		var fields = getStaticFields.apply( CLASS );
		Truth.assertWithMessage( "only one static field should exist" )
			 .that( fields )
			 .hasSize( 1 );
		var field  = fields.get( 0 );
		Truth.assertWithMessage( "static field should be a map" )
		     .that( field.getType() )
		     .isAssignableTo( Map.class );
		try {
			field.setAccessible( true );
			Map<?,?> map = (Map<?,?>)field.get( null );
			map.clear();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			fail( "couldn't access static field" );
		}
	}
	@Test
	void testHasMethods() {
		BiConsumer<Class<?>,List<String>>
		hasMethods = (a,b) -> {
			var actual = Arrays.stream ( a.getDeclaredMethods() )
	                           .filter ( m->!m.isSynthetic() )
			                   .map    ( Method::getName )
			                   .collect( Collectors.toList() );
			Truth.assertWithMessage( String.format( "class '%s' does not have expected methods", a.getSimpleName() ))
			     .that             ( actual )
			     .containsExactlyElementsIn( b );
		};
		BiConsumer<Class<?>,List<String>>
		hasObjectMethods = (a,b) -> {
			var actual = Arrays.stream ( a.getDeclaredMethods() )
	                           .filter ( m->!m.isSynthetic())
	                           .filter ( m->!Modifier.isStatic( m.getModifiers() ))
			                   .map    ( Method::getName )
			                   .collect( Collectors.toList() );
			Truth.assertWithMessage( String.format( "class '%s' does not have expected object method", a.getSimpleName() ))
			     .that             ( actual )
			     .containsExactlyElementsIn( b );
		};

		hasMethods      .accept( CLASS, List.of( "add", "has", "get" ));
		hasObjectMethods.accept( CLASS, List.of( "add", "has", "get" ));
	}
	@Test
	void testOneStaticPrivateField() {
		ToLongFunction<Class<?>> fieldsCount = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.count();
		Consumer<Class<?>> allFieldsPrivate = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' must be private", name ))
					.that( Modifier.isPrivate( mod ))
					.isTrue();
				});
		Consumer<Class<?>> allFieldsStatic  = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' must be static",  name ))
					.that( Modifier.isStatic( mod ))
					.isTrue();
				});
		Consumer<Class<?>> allFieldsFinal  = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' must be final",  name ))
					.that( Modifier.isFinal( mod ))
					.isTrue();
				});
		var count = fieldsCount.applyAsLong( CLASS );
		Truth.assertWithMessage( "only one field should exist" )
		     .that( count )
		     .isEqualTo( 1 );
		allFieldsPrivate.accept( CLASS );
		allFieldsStatic .accept( CLASS );
		allFieldsFinal  .accept( CLASS );
		
		Function<Class<?>,List<Field>> getStaticFields = clazz -> Arrays.stream ( clazz.getDeclaredFields() )
				.filter ( f -> !f.isSynthetic())
				.filter ( f -> Modifier.isStatic ( f.getModifiers() ))
				.collect( Collectors.toList() );
		var fields = getStaticFields.apply( CLASS );
		Truth.assertWithMessage( "only one static field should exist" )
			 .that( fields )
			 .hasSize( 1 );
		var field  = fields.get( 0 );
		Truth.assertWithMessage( "static field should be a map" )
		     .that( field.getType() )
		     .isAssignableTo( Map.class );
	}
	@Test
	void testAddWithNullBlankThrowsException() {
		var a = NEW_FACTORY.get();
		// empty name
		for (String name : new String[] { null, "", " ", "  ", "     " }) {
			var e = assertThrows( IllegalArgumentException.class, 
					      () -> a.add( name, () -> null ));
			Truth.assertThat( e.getMessage() )
			     .isEqualTo ( "name cannot be null or blank" );
			Truth.assertThat( a.has( name ))
			     .isFalse();
		}
		// empty supplier
		var e = assertThrows( IllegalArgumentException.class, 
			      () -> a.add( "foo", null ));
		Truth.assertThat( e.getMessage() )
	   	     .isEqualTo ( "supplier cannot be null" );
		Truth.assertThat( a.has( "foo" ))
	         .isFalse();
	}
	@Test
	void testGetInstanceWithNullBlankThrowsException() {
		var a = NEW_FACTORY.get();
		// empty name
		for (String name : new String[] { null, "", " ", "  ", "     " }) {
			var e = assertThrows( IllegalArgumentException.class, 
					      () -> a.get( name ));
			Truth.assertThat( e.getMessage() )
			     .isEqualTo ( "name cannot be null or blank" );
		}
		// non-existing name
		for (String name : new String[] { "foo", "bar", "jinx" }) {
			var e = assertThrows( IllegalArgumentException.class, 
				      () -> a.get( name ));
			Truth.assertThat( e.getMessage() )
		   	     .isEqualTo ( "name doesn't exist" );
		}
	}
	@Test
	void testAdd() {
		var a = NEW_FACTORY.get();
		var m = Mockito.mock( DocumentPlugin.class );
		Truth.assertThat( a.has( "foo" )).isFalse();
		a.add( "foo", ()->m );
		Truth.assertThat( a.has( "foo" )).isTrue();
		Truth.assertThat( a.get( "foo" )).isSameInstanceAs( m );
	}
	@Test
	void testAddAndReplace() {
		var a = NEW_FACTORY.get();
		var m = Mockito.mock( DocumentPlugin.class );
		var n = Mockito.mock( DocumentPlugin.class );
		Truth.assertThat( a.has( "foo" )).isFalse();
		// add
		a.add( "foo", ()->m );
		Truth.assertThat( a.has( "foo" )).isTrue();
		Truth.assertThat( a.get( "foo" )).isSameInstanceAs( m );
		// replace
		a.add( "foo", ()->n );
		Truth.assertThat( a.has( "foo" )).isTrue();
		Truth.assertThat( a.get( "foo" )).isSameInstanceAs( n );
	}
	@Test
	void testAddAndGetWithSeveralFactoryInstances() {
		var a = NEW_FACTORY.get();
		var b = NEW_FACTORY.get();
		var c = NEW_FACTORY.get();
		var m = Mockito.mock( DocumentPlugin.class );
		var n = Mockito.mock( DocumentPlugin.class );
		var o = Mockito.mock( DocumentPlugin.class );
		a.add( "mars",    ()->m );
		b.add( "neptune", ()->n );
		c.add( "orion",   ()->o );
		for (var x : List.of( a, b, c )) {
			Truth.assertThat( x.get( "mars"    )).isSameInstanceAs( m );
			Truth.assertThat( x.get( "neptune" )).isSameInstanceAs( n );
			Truth.assertThat( x.get( "orion"   )).isSameInstanceAs( o );
		}
	}
}