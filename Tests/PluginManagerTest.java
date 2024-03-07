import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class MockedDocumentPlugin implements DocumentPlugin {
	private static boolean init        = false;
	private static boolean constructor = false;
	public MockedDocumentPlugin() {
		init        = false;
		constructor = true;
	}
	@Override
	public
	void init() {
		DocumentPlugin.super.init();
		init        = true;
		constructor = true;
	}
	public static boolean initCalled() {
		return init;
	}
	public static boolean constructorCalled() {
		return constructor;
	}
	@Override
	public Supplier<DocumentPlugin> getSupplier() {
		return MockedDocumentPlugin::new;
	}
	@Override
	public String getName() {
		return "Mocked";
	}
	@Override
	public Document apply(Document d) {
		fail( "This method should not get called" );
		return null;
	}
}

class NotAPlugin {
}

public class PluginManagerTest {
	private static final String CLASS_NAME   = "PluginManager";
	private static final String NOT_A_PLUGIN = "NotAPlugin";
	private static final String NOT_A_CLASS  = "NonExistingClass";
	
	private Class<?> getClazz(String name) {
		Class<?> result = null;
		try {
			Package pkg  = getClass().getPackage();
			String  path = (pkg == null || pkg.getName().isEmpty()) ? "" : pkg.getName()+".";
			result = Class.forName( path + name );
		} catch (ClassNotFoundException e) {
			fail( String.format( "Class %s not found", name ));
		}
		return result;
	}
	private List<Field> getStaticFields() {
		Class<?> cls    = getClazz( CLASS_NAME );
		Field[]  fields = cls.getDeclaredFields();
		return Arrays.stream ( fields )
				     .filter ( f -> !f.isSynthetic() )
				     .filter ( f -> Modifier.isStatic ( f.getModifiers() ))
				     .collect( Collectors.toList() );
	}
	private Constructor<?> getPrivateConstructor() {
		Constructor<?>    result       = null;
		Class<?>          cls          = getClazz( CLASS_NAME );
		Constructor<?>[]  constructors = cls.getDeclaredConstructors();
		for (Constructor<?> c : constructors) {
			int modifier = c.getModifiers();
			if (Modifier.isPrivate( modifier )) {
				if (result == null) {
					result = c;
				} else {
					fail( "More than one private constructor found" );
				}
			} else {
				fail( "non-private constructor found" );
			}
		}
		return result;
	}
	
	private static Object  initial = null;
	private static boolean wasRead = false;
	@BeforeEach
	public void resetStaticFieldInitialValue() {
		try {
			List<Field> fields = getStaticFields();
			if (fields.size() == 1) {
				Field field = fields.get(0);
				field.setAccessible( true );
				if (wasRead) {
					field.set( null, initial );
				} else {
					initial = field.get( null );
					wasRead = true;
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
		}
	}

	@Test
	public void testFieldsArePrivate() {
		Class<?> cls    = getClazz( CLASS_NAME );
		Field[]  fields = cls.getDeclaredFields();
		Arrays.stream( fields )
	          .filter ( f -> !f.isSynthetic() )
	          .forEach( f -> {
					int modifier = f.getModifiers();
					Truth.assertWithMessage( String.format( "field '%s' should be private", f.getName())) 
					     .that( Modifier.isPrivate( modifier ))
					     .isTrue();
	          });
	}
	@Test
	public void testOnePrivateConstructor() {
		Constructor<?> constructor  = getPrivateConstructor();
		Truth.assertWithMessage( "No private constructor found" )
		     .that( constructor )
		     .isNotNull();
	}
	@Test
	public void testOneStaticFieldEager() {
		try {
			List<Field> fields = getStaticFields();
			Truth.assertWithMessage( "Zero or more than one static field found" )
			     .that( fields )
			     .hasSize( 1 );
			Field field = fields.get(0);
			field.setAccessible( true );
			Object value = field.get( null );
			Truth.assertWithMessage( "static field is not null" )
			     .that( value )
			     .isNotNull();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			fail( "error accessing static field " );
		}
	}
	@Test
	public void testGetInstanceSameObject() {
		var a = PluginManager.getInstance();
		Truth.assertWithMessage( "getInstance() returns null" )
		     .that( a )
		     .isNotNull();
		Truth.assertWithMessage( String.format( "getInstance() doesn't return a '%s' object", CLASS_NAME ))
		     .that( getClazz( CLASS_NAME ))
		     .isEqualTo( a.getClass() );
		var b = PluginManager.getInstance();
		Truth.assertWithMessage( "getInstance() returns null" )
		     .that( b )
		     .isNotNull();
		Truth.assertWithMessage( "getInstance() returns different objects" )
		     .that( a )
		     .isSameInstanceAs( b );
	}
	@Test
	public void testLoadPlugin() {
		var a = PluginManager.getInstance();
		Truth.assertWithMessage( "getInstance() returns null" )
		     .that( a )
		     .isNotNull();
		// were constructor & init called?
		Truth.assertThat( MockedDocumentPlugin.constructorCalled() ).isFalse();
		Truth.assertThat( MockedDocumentPlugin.initCalled()        ).isFalse();
		a.load( MockedDocumentPlugin.class.getSimpleName() );
		Truth.assertThat( MockedDocumentPlugin.initCalled()        ).isTrue();
		Truth.assertThat( MockedDocumentPlugin.constructorCalled() ).isTrue();
	}
	@Test
	public void testLoadNonExistingPluginThrowsException() {
		var a = PluginManager.getInstance();
		Truth.assertWithMessage( "getInstance() returns null" )
		     .that( a )
		     .isNotNull();
		var e = assertThrows( IllegalStateException.class,
				()-> a.load( NOT_A_CLASS ));
		Truth.assertThat( e.getMessage() )
		     .isEqualTo( String.format( "error loading class '%s'", NOT_A_CLASS ));
	}
	@Test
	public void testLoadNonPluginThrowsException() {
		var a = PluginManager.getInstance();
		Truth.assertWithMessage( "getInstance() returns null" )
		     .that( a )
		     .isNotNull();
		var e = assertThrows( IllegalStateException.class,
				()-> a.load( NOT_A_PLUGIN ));
		Truth.assertThat( e.getMessage() )
		     .isEqualTo( String.format( "class '%s' is not of type DocumentPlugin", NOT_A_PLUGIN ));
	}	
}